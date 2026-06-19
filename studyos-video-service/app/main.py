import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.api.routes.video_routes import router as video_router
from app.config import settings
from app.core.exceptions import VideoServiceException
from app.services.whisper_service import WhisperService
from app.utils.file_utils import ensure_directory

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  [%(levelname)-8s]  %(name)s: %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
logger = logging.getLogger(__name__)

_STORAGE_DIRS = [
    settings.storage_downloads,
    settings.storage_uploads,
    settings.storage_audio,
    settings.storage_transcripts,
]


@asynccontextmanager
async def lifespan(app: FastAPI):
    # ── Startup ──────────────────────────────────────────────────────────────
    logger.info("StudyOS Video Service — startup")

    for path in _STORAGE_DIRS:
        ensure_directory(path)
        logger.info(f"  storage ready: {path}")

    WhisperService.load_model(settings.whisper_model)

    logger.info("StudyOS Video Service — ready to serve requests")
    yield

    # ── Shutdown ─────────────────────────────────────────────────────────────
    logger.info("StudyOS Video Service — shutting down")


app = FastAPI(
    title="StudyOS Video Service",
    description=(
        "Converts YouTube URLs and uploaded video/audio files into "
        "structured study notes using Whisper transcription."
    ),
    version="1.0.0",
    lifespan=lifespan,
)

# ── CORS — allow all origins by default (lock down in production via .env) ───
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ── Global exception handler for service-level errors ────────────────────────
@app.exception_handler(VideoServiceException)
async def _video_exc_handler(request: Request, exc: VideoServiceException) -> JSONResponse:
    return JSONResponse(
        status_code=exc.status_code,
        content={"success": False, "error": exc.message},
    )


# ── Routes ────────────────────────────────────────────────────────────────────
@app.get("/health", tags=["Health"])
async def health_check():
    return {"status": "ok", "service": "studyos-video-service"}


app.include_router(video_router)
