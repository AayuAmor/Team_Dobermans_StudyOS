import logging
import subprocess
from pathlib import Path

import aiofiles
from fastapi import UploadFile

from app.config import settings
from app.core.exceptions import (
    AudioExtractionException,
    FileTooLargeException,
    UnsupportedFileTypeException,
)
from app.utils.ffmpeg_utils import get_ffmpeg_location
from app.utils.file_utils import generate_unique_stem, get_file_extension, safe_delete

logger = logging.getLogger(__name__)

ALLOWED_VIDEO_EXTENSIONS: frozenset = frozenset({"mp4", "mkv", "mov", "webm"})
ALLOWED_AUDIO_EXTENSIONS: frozenset = frozenset({"mp3", "wav", "m4a"})
ALL_ALLOWED: frozenset = ALLOWED_VIDEO_EXTENSIONS | ALLOWED_AUDIO_EXTENSIONS

_CHUNK_SIZE = 1024 * 1024                    

async def save_uploaded_file(file: UploadFile, upload_dir: Path) -> Path:
    ext = get_file_extension(file.filename or "")
    if ext not in ALL_ALLOWED:
        raise UnsupportedFileTypeException(
            f"'.{ext}' is not supported. Allowed: {', '.join(sorted(ALL_ALLOWED))}"
        )

    dest = upload_dir / f"{generate_unique_stem()}.{ext}"
    max_bytes = settings.max_upload_size_mb * 1024 * 1024
    written = 0

    async with aiofiles.open(dest, "wb") as out:
        while True:
            chunk = await file.read(_CHUNK_SIZE)
            if not chunk:
                break
            written += len(chunk)
            if written > max_bytes:
                await out.close()
                safe_delete(dest)
                raise FileTooLargeException(settings.max_upload_size_mb)
            await out.write(chunk)

    size_mb = written / (1024 * 1024)
    logger.info(f"Upload saved: {dest.name} ({size_mb:.1f} MB)")
    return dest

def extract_audio_from_video(video_path: Path, audio_dir: Path) -> Path:
    audio_path = audio_dir / f"{generate_unique_stem()}.mp3"
    ffmpeg_bin = get_ffmpeg_location()

    cmd = [
        ffmpeg_bin,
        "-i", str(video_path),
        "-vn",                                  
        "-acodec", "libmp3lame",
        "-q:a", "2",                                   
        "-y",                                          
        str(audio_path),
    ]

    try:
        proc = subprocess.run(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            timeout=300,
        )
    except subprocess.TimeoutExpired:
        safe_delete(audio_path)
        raise AudioExtractionException("Audio extraction timed out after 5 minutes.")

    if proc.returncode != 0:
        err = proc.stderr.decode(errors="replace")
        logger.error(f"ffmpeg stderr: {err}")
        raise AudioExtractionException(f"ffmpeg error (code {proc.returncode}): {err[:300]}")

    logger.info(f"Audio extracted: {audio_path.name}")
    return audio_path

def is_video_file(extension: str) -> bool:
    return extension.lower() in ALLOWED_VIDEO_EXTENSIONS
