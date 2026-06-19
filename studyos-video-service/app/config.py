from pathlib import Path
from typing import List

from pydantic_settings import BaseSettings

BASE_DIR = Path(__file__).resolve().parent.parent

class Settings(BaseSettings):
                                                         
    whisper_model: str = "base"

                 
    max_upload_size_mb: int = 500

                                                       
    storage_downloads: Path = BASE_DIR / "storage" / "downloads"
    storage_uploads: Path = BASE_DIR / "storage" / "uploads"
    storage_audio: Path = BASE_DIR / "storage" / "audio"
    storage_transcripts: Path = BASE_DIR / "storage" / "transcripts"

             
    log_level: str = "INFO"
    log_dir: Path = BASE_DIR / "logs"

                                                                             
    cors_origins: List[str] = ["*"]

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}

settings = Settings()
