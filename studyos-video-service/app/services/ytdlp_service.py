
import logging
import random
import time
from pathlib import Path
from typing import Tuple

import yt_dlp

from app.core.exceptions import (
    DownloadFailedException,
    GeoBlockedException,
    InvalidURLException,
    RateLimitedException,
    VideoAgeRestrictedException,
    VideoBlockedException,
    VideoServiceException,
    VideoUnavailableException,
)
from app.utils.ffmpeg_utils import get_ffmpeg_location
from app.utils.file_utils import generate_unique_stem

logger = logging.getLogger(__name__)

_MAX_RETRIES = 3
_RETRY_BASE_DELAY = 2.0                                   

_USER_AGENTS = [
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Mozilla/5.0 (X11; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0",
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_4_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15",
    "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Mobile/15E148 Safari/604.1",
]

                                                                                    
_PLAYER_CLIENT_ROTATION = [
    ["android", "web"],
    ["ios", "web"],
    ["android_creator", "web"],
]

def download_audio_from_url(url: str, output_dir: Path) -> Tuple[Path, str]:
    if not url.startswith(("http://", "https://")):
        raise InvalidURLException(f"URL must start with http:// or https://: {url!r}")

    ffmpeg_path = get_ffmpeg_location()
    stem = generate_unique_stem()
    output_template = str(output_dir / stem)

    last_error: VideoServiceException = DownloadFailedException(
        "Download failed after all retries. The video may be restricted or temporarily unavailable."
    )
    title = "Unknown Video"

    for attempt in range(1, _MAX_RETRIES + 1):
        user_agent = random.choice(_USER_AGENTS)
        player_clients = _PLAYER_CLIENT_ROTATION[(attempt - 1) % len(_PLAYER_CLIENT_ROTATION)]
        logger.info(f"  yt-dlp attempt {attempt}/{_MAX_RETRIES}  clients={player_clients}")

        ydl_opts = {
            "format": "bestaudio/best",
            "outtmpl": output_template,
            "postprocessors": [
                {
                    "key": "FFmpegExtractAudio",
                    "preferredcodec": "mp3",
                    "preferredquality": "128",
                }
            ],
            "ffmpeg_location": ffmpeg_path,
            "quiet": True,
            "no_warnings": True,
            "noplaylist": True,
            "http_headers": {"User-Agent": user_agent},
            "extractor_args": {
                "youtube": {"player_client": player_clients}
            },
            "socket_timeout": 30,
            "retries": 2,
            "fragment_retries": 2,
        }

        try:
            title = _run_download(ydl_opts, url)
            break                                  

        except (
            VideoUnavailableException,
            VideoAgeRestrictedException,
            GeoBlockedException,
            InvalidURLException,
        ):
            raise                                       

        except (VideoBlockedException, RateLimitedException) as exc:
            last_error = exc
            if attempt < _MAX_RETRIES:
                delay = _RETRY_BASE_DELAY * (2 ** (attempt - 1))
                logger.warning(
                    f"  {type(exc).__name__} on attempt {attempt} — retrying in {delay:.1f}s with new UA/client"
                )
                time.sleep(delay)

        except DownloadFailedException as exc:
            last_error = exc
            if attempt < _MAX_RETRIES:
                delay = _RETRY_BASE_DELAY * attempt
                logger.warning(f"  Download error on attempt {attempt} — retrying in {delay:.1f}s")
                time.sleep(delay)

    else:
        raise last_error

    audio_path = Path(f"{output_template}.mp3")
    if not audio_path.exists():
        candidates = list(output_dir.glob(f"{stem}.*"))
        if not candidates:
            raise DownloadFailedException(
                "Audio file missing after download — ffmpeg postprocessing may have failed."
            )
        audio_path = candidates[0]

    logger.info(f"Audio saved: {audio_path.name}")
    return audio_path, title

def _run_download(opts: dict, url: str) -> str:
    try:
        with yt_dlp.YoutubeDL(opts) as ydl:
            info = ydl.extract_info(url, download=True)
            title = "Unknown Video"
            if info:
                title = info.get("title") or info.get("webpage_url_basename") or "Unknown Video"
        logger.info(f"  yt-dlp downloaded: '{title}'")
        return title
    except yt_dlp.utils.DownloadError as exc:
        raise _classify_ytdlp_error(str(exc))
    except Exception as exc:
        logger.error(f"  Unexpected yt-dlp error: {type(exc).__name__}: {exc}")
        raise DownloadFailedException("An unexpected error occurred while downloading the video.")

def _classify_ytdlp_error(raw: str) -> VideoServiceException:
    lower = raw.lower()

    if "unsupported url" in lower or "no suitable" in lower:
        return InvalidURLException(
            "The URL is not supported. Please provide a valid YouTube or video URL."
        )
    if "video unavailable" in lower or "this video does not exist" in lower:
        return VideoUnavailableException()
    if "private video" in lower or ("sign in" in lower and "required" in lower):
        return VideoUnavailableException("This video is private or requires sign-in to access.")
    if ("age" in lower and "restrict" in lower) or "confirm your age" in lower or "age-restricted" in lower:
        return VideoAgeRestrictedException()
    if "copyright" in lower or "content removed" in lower or "been removed" in lower:
        return VideoUnavailableException(
            "This video has been removed or is unavailable due to copyright restrictions."
        )
    if "429" in lower or "too many request" in lower:
        return RateLimitedException()
    if "not available in your country" in lower or "unavailable in your region" in lower:
        return GeoBlockedException()
    if "403" in lower or "http error 403" in lower or "forbidden" in lower:
        return VideoBlockedException()

    logger.debug(f"Unclassified yt-dlp error: {raw[:300]}")
    return DownloadFailedException(
        "The video could not be downloaded. It may be restricted or temporarily unavailable."
    )
