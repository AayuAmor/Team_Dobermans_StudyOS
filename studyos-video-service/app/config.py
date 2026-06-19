from pathlib import Path
from typing import List

from pydantic_settings import BaseSettings

BASE_DIR = Path(__file__).resolve().parent.parent


class Settings(BaseSettings):
    # Whisper model: tiny | base | small | medium | large
    whisper_model: str = "base"

    # File upload
    max_upload_size_mb: int = 500

    # Storage paths (resolved relative to project root)
    storage_downloads: Path = BASE_DIR / "storage" / "downloads"
    storage_uploads: Path = BASE_DIR / "storage" / "uploads"
    storage_audio: Path = BASE_DIR / "storage" / "audio"
    storage_transcripts: Path = BASE_DIR / "storage" / "transcripts"

    # CORS — JSON array string in .env: CORS_ORIGINS=["http://10.0.2.2:8000"]
    cors_origins: List[str] = ["*"]

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
