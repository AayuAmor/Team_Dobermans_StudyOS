import os
import uuid
from pathlib import Path

def generate_unique_stem() -> str:
    return uuid.uuid4().hex

def get_file_extension(filename: str) -> str:
    return Path(filename).suffix.lower().lstrip(".")

def ensure_directory(path: Path) -> None:
    path.mkdir(parents=True, exist_ok=True)

def safe_delete(path: Path) -> None:
    try:
        if path and path.exists():
            os.remove(path)
    except Exception:
        pass                                               

def file_size_mb(path: Path) -> float:
    return path.stat().st_size / (1024 * 1024)
