import logging
from pathlib import Path
from typing import Optional

from fastapi import APIRouter, File, HTTPException, UploadFile

from app.config import settings
from app.core.exceptions import VideoServiceException
from app.schemas.video_schema import URLRequest, VideoNotesResponse
from app.services import cleanup_service, notes_service, upload_service, ytdlp_service
from app.services.whisper_service import WhisperService
from app.utils.file_utils import get_file_extension

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/video", tags=["Video"])


@router.post(
    "/url-to-notes",
    response_model=VideoNotesResponse,
    summary="Convert a YouTube / video URL to study notes",
)
async def url_to_notes(request: URLRequest):
    """
    Download audio from the given URL via yt-dlp, transcribe with Whisper,
    and return structured study notes.
    """
    audio_path: Optional[Path] = None
    try:
        audio_path, title = ytdlp_service.download_audio_from_url(
            request.url, settings.storage_audio
        )
        transcript = WhisperService.transcribe(audio_path)
        notes = notes_service.generate_notes(transcript, title)

        return VideoNotesResponse(
            success=True,
            source_type="url",
            title=title,
            transcript=transcript,
            notes=notes,  # Pydantic coerces the dict to NotesOutput
        )

    except VideoServiceException as exc:
        raise HTTPException(status_code=exc.status_code, detail=exc.message)
    except Exception as exc:
        logger.exception("Unhandled error in url_to_notes")
        raise HTTPException(status_code=500, detail=str(exc))
    finally:
        cleanup_service.cleanup_files(audio_path)


@router.post(
    "/upload-to-notes",
    response_model=VideoNotesResponse,
    summary="Convert an uploaded video / audio file to study notes",
)
async def upload_to_notes(file: UploadFile = File(...)):
    """
    Accept a video or audio upload, extract/use audio, transcribe with Whisper,
    and return structured study notes.
    """
    saved_path: Optional[Path] = None
    audio_path: Optional[Path] = None
    try:
        saved_path = await upload_service.save_uploaded_file(file, settings.storage_uploads)
        ext = get_file_extension(file.filename or "")

        if upload_service.is_video_file(ext):
            audio_path = upload_service.extract_audio_from_video(saved_path, settings.storage_audio)
        else:
            # Already an audio file — use it directly
            audio_path = saved_path

        transcript = WhisperService.transcribe(audio_path)

        title = Path(file.filename).stem if file.filename else "Uploaded File"
        notes = notes_service.generate_notes(transcript, title)

        return VideoNotesResponse(
            success=True,
            source_type="upload",
            title=title,
            transcript=transcript,
            notes=notes,
        )

    except VideoServiceException as exc:
        raise HTTPException(status_code=exc.status_code, detail=exc.message)
    except Exception as exc:
        logger.exception("Unhandled error in upload_to_notes")
        raise HTTPException(status_code=500, detail=str(exc))
    finally:
        # Always clean up; skip duplicate delete when audio_path IS saved_path
        cleanup_service.cleanup_files(saved_path)
        if audio_path and audio_path != saved_path:
            cleanup_service.cleanup_files(audio_path)
