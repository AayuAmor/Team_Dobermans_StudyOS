import logging
from pathlib import Path
from typing import Optional

from app.utils.file_utils import safe_delete

logger = logging.getLogger(__name__)


def cleanup_files(*paths: Optional[Path]) -> None:
    """
    Delete all provided temporary files.
    Silently skips None values and files that no longer exist.
    """
    for path in paths:
        if path is not None:
            safe_delete(path)
            logger.debug(f"Cleaned up temp file: {path.name}")
