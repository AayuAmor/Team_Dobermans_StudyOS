import logging
from pathlib import Path
from typing import Tuple

import yt_dlp

from app.core.exceptions import DownloadFailedException, InvalidURLException
from app.utils.file_utils import generate_unique_stem

logger = logging.getLogger(__name__)


def download_audio_from_url(url: str, output_dir: Path) -> Tuple[Path, str]:
    """
    Download the best available audio from a video URL via yt-dlp and
    convert it to MP3 using ffmpeg.

    Returns:
        (audio_path, video_title)

    Raises:
        InvalidURLException: URL is malformed or the extractor rejects it.
        DownloadFailedException: Network/format error during download.
    """
    if not url.startswith(("http://", "https://")):
        raise InvalidURLException(f"URL must start with http:// or https://: {url}")

    stem = generate_unique_stem()
    output_template = str(output_dir / stem)

    ydl_opts = {
        "format": "bestaudio/best",
        "outtmpl": output_template,
        "postprocessors": [
            {
                "key": "FFmpegExtractAudio",
                "preferredcodec": "mp3",
                "preferredquality": "192",
            }
        ],
        "quiet": True,
        "no_warnings": True,
        # Prevent playlist downloads — process only the first item
        "noplaylist": True,
    }

    title = "Unknown Video"
    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(url, download=True)
            if info:
                title = info.get("title") or info.get("webpage_url_basename") or "Unknown Video"
        logger.info(f"yt-dlp downloaded: '{title}'")
    except yt_dlp.utils.DownloadError as exc:
        logger.error(f"yt-dlp DownloadError: {exc}")
        raise DownloadFailedException(f"Could not download from URL. {_friendly_ydl_error(str(exc))}")
    except Exception as exc:
        logger.error(f"Unexpected yt-dlp error: {exc}")
        raise DownloadFailedException(f"Unexpected error while downloading: {exc}")

    # yt-dlp appends the codec extension after postprocessing
    audio_path = Path(f"{output_template}.mp3")
    if not audio_path.exists():
        # Fall back to any file with our unique stem (edge case: different extension)
        candidates = list(output_dir.glob(f"{stem}.*"))
        if not candidates:
            raise DownloadFailedException("Audio file not found after download — ffmpeg may not be installed.")
        audio_path = candidates[0]

    logger.info(f"Audio saved: {audio_path}")
    return audio_path, title


def _friendly_ydl_error(raw: str) -> str:
    """Extract the human-readable part from a yt-dlp error string."""
    if "Unsupported URL" in raw:
        return "The URL is not supported by yt-dlp."
    if "Video unavailable" in raw:
        return "The video is unavailable or private."
    if "confirm your age" in raw:
        return "Age-restricted content cannot be downloaded without authentication."
    return raw.split("ERROR:")[-1].strip() if "ERROR:" in raw else raw
