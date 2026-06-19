import logging
import time
from pathlib import Path
from typing import Optional

from fastapi import APIRouter, File, Form, HTTPException, Request, UploadFile

from app.config import settings
from app.core.exceptions import VideoServiceException
from app.schemas.video_schema import URLRequest, VideoNotesResponse
from app.services import cleanup_service, notes_service, upload_service, ytdlp_service
from app.services.whisper_service import WhisperService
from app.utils.file_utils import get_file_extension

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/video", tags=["Video"])

_VALID_STYLES = {"short", "detailed", "exam", "bullets"}

def _rid(request: Request) -> str:
    return getattr(request.state, "request_id", "-")

                                                                                

@router.post(
    "/url-to-notes",
    response_model=VideoNotesResponse,
    summary="Convert a YouTube / video URL to study notes",
)
async def url_to_notes(body: URLRequest, request: Request):
    audio_path: Optional[Path] = None
    rid = _rid(request)
    t_start = time.perf_counter()

    logger.info(f"[{rid}] url-to-notes  url={body.url!r}  style={body.summary_style!r}")

    try:
        logger.info(f"[{rid}] [1/3] yt-dlp download starting…")
        t0 = time.perf_counter()
        audio_path, title = ytdlp_service.download_audio_from_url(
            body.url, settings.storage_audio
        )
        logger.info(f"[{rid}] [1/3] yt-dlp done in {time.perf_counter() - t0:.1f}s  title={title!r}")

        logger.info(f"[{rid}] [2/3] Whisper transcription starting…")
        t0 = time.perf_counter()
        transcript = WhisperService.transcribe(audio_path)
        logger.info(f"[{rid}] [2/3] Whisper done in {time.perf_counter() - t0:.1f}s  chars={len(transcript)}")

        logger.info(f"[{rid}] [3/3] Notes generation starting…")
        t0 = time.perf_counter()
        notes = notes_service.generate_notes(transcript, title, summary_style=body.summary_style)
        logger.info(f"[{rid}] [3/3] Notes done in {time.perf_counter() - t0:.1f}s")

        logger.info(f"[{rid}] url-to-notes complete in {time.perf_counter() - t_start:.1f}s")

        return VideoNotesResponse(
            success=True,
            source_type="url",
            title=title,
            transcript=transcript,
            notes=notes,
        )

    except VideoServiceException:
        raise                                                              

    except Exception:
        logger.exception(f"[{rid}] Unhandled error in url_to_notes after {time.perf_counter() - t_start:.1f}s")
        raise HTTPException(status_code=500, detail="An internal server error occurred.")

    finally:
        cleanup_service.cleanup_files(audio_path)

                                                                                

@router.post(
    "/upload-to-notes",
    response_model=VideoNotesResponse,
    summary="Convert an uploaded video / audio file to study notes",
)
async def upload_to_notes(
    request: Request,
    file: UploadFile = File(...),
    summary_style: str = Form("detailed"),
):
    saved_path: Optional[Path] = None
    audio_path: Optional[Path] = None
    rid = _rid(request)
    t_start = time.perf_counter()

    style = summary_style.strip().lower() if summary_style.strip().lower() in _VALID_STYLES else "detailed"
    logger.info(f"[{rid}] upload-to-notes  file={file.filename!r}  style={style!r}")

    try:
        logger.info(f"[{rid}] [1/3] Saving uploaded file…")
        t0 = time.perf_counter()
        saved_path = await upload_service.save_uploaded_file(file, settings.storage_uploads)
        ext = get_file_extension(file.filename or "")
        logger.info(
            f"[{rid}] [1/3] Saved in {time.perf_counter() - t0:.1f}s  "
            f"size={saved_path.stat().st_size // 1024}KB  ext={ext!r}"
        )

        if upload_service.is_video_file(ext):
            logger.info(f"[{rid}] [2a] Video detected — extracting audio via ffmpeg…")
            t0 = time.perf_counter()
            audio_path = upload_service.extract_audio_from_video(saved_path, settings.storage_audio)
            logger.info(f"[{rid}] [2a] Audio extracted in {time.perf_counter() - t0:.1f}s")
        else:
            logger.info(f"[{rid}] [2a] Audio file detected — skipping ffmpeg extraction.")
            audio_path = saved_path

        logger.info(f"[{rid}] [2/3] Whisper transcription starting…")
        t0 = time.perf_counter()
        transcript = WhisperService.transcribe(audio_path)
        logger.info(f"[{rid}] [2/3] Whisper done in {time.perf_counter() - t0:.1f}s  chars={len(transcript)}")

        title = Path(file.filename).stem if file.filename else "Uploaded File"
        logger.info(f"[{rid}] [3/3] Notes generation starting…")
        t0 = time.perf_counter()
        notes = notes_service.generate_notes(transcript, title, summary_style=style)
        logger.info(f"[{rid}] [3/3] Notes done in {time.perf_counter() - t0:.1f}s")

        logger.info(f"[{rid}] upload-to-notes complete in {time.perf_counter() - t_start:.1f}s")

        return VideoNotesResponse(
            success=True,
            source_type="upload",
            title=title,
            transcript=transcript,
            notes=notes,
        )

    except VideoServiceException:
        raise

    except Exception:
        logger.exception(f"[{rid}] Unhandled error in upload_to_notes after {time.perf_counter() - t_start:.1f}s")
        raise HTTPException(status_code=500, detail="An internal server error occurred.")

    finally:
        cleanup_service.cleanup_files(saved_path)
        if audio_path and audio_path != saved_path:
            cleanup_service.cleanup_files(audio_path)
