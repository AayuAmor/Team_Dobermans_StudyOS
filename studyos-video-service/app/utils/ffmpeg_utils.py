import logging
import shutil

from app.core.exceptions import AudioExtractionException

logger = logging.getLogger(__name__)

def get_ffmpeg_location() -> str:
    system_ffmpeg = shutil.which("ffmpeg")
    if system_ffmpeg:
        logger.debug(f"Using system ffmpeg: {system_ffmpeg}")
        return system_ffmpeg

    try:
        import imageio_ffmpeg                
        bundled = imageio_ffmpeg.get_ffmpeg_exe()
        logger.debug(f"Using bundled ffmpeg: {bundled}")
        return bundled
    except Exception:
        pass

    raise AudioExtractionException(
        "ffmpeg is not available. Install it with: sudo apt-get install ffmpeg  "
        "or add imageio[ffmpeg] to requirements.txt"
    )
