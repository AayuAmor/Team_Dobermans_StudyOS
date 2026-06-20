import logging
from pathlib import Path
from typing import Optional

import whisper

from app.core.exceptions import TranscriptionException

logger = logging.getLogger(__name__)

class WhisperService:

    _model: Optional[whisper.Whisper] = None
    _model_name: str = "base"

    @classmethod
    def load_model(cls, model_name: str = "base") -> None:
        cls._model_name = model_name
        logger.info(f"Loading Whisper model '{model_name}' — this may take a moment...")
        cls._model = whisper.load_model(model_name)
        logger.info(f"Whisper model '{model_name}' ready.")

    @classmethod
    def transcribe(cls, audio_path: Path) -> str:
        if cls._model is None:
            raise TranscriptionException("Whisper model not loaded. Check server startup logs.")

        logger.info(f"Transcribing: {audio_path.name}")
        try:
                                                                         
            result = cls._model.transcribe(str(audio_path), fp16=False)
        except Exception as exc:
            logger.error(f"Whisper transcription error: {exc}")
            raise TranscriptionException(f"Transcription failed: {exc}")

        transcript = result.get("text", "").strip()
        logger.info(f"Transcription complete ({len(transcript)} characters).")
        return transcript

    @classmethod
    def is_loaded(cls) -> bool:
        return cls._model is not None
