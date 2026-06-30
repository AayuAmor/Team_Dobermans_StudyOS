import logging
import logging.handlers
import shutil
import socket
from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.api.routes.video_routes import router as video_router
from app.config import settings
from app.core.exceptions import VideoServiceException
from app.middleware.rate_limit import RateLimitMiddleware
from app.middleware.request_id import RequestIDMiddleware
from app.services.whisper_service import WhisperService
from app.utils.file_utils import ensure_directory

_LOG_FORMAT = "%(asctime)s  [%(levelname)-8s]  %(name)s: %(message)s"
_LOG_DATE_FMT = "%Y-%m-%d %H:%M:%S"

                                                                               
                                                         
logging.basicConfig(
    level=getattr(logging, settings.log_level.upper(), logging.INFO),
    format=_LOG_FORMAT,
    datefmt=_LOG_DATE_FMT,
)
logger = logging.getLogger(__name__)

_STORAGE_DIRS = [
    settings.storage_downloads,
    settings.storage_uploads,
    settings.storage_audio,
    settings.storage_transcripts,
]

def _add_file_handler() -> None:
    log_dir: Path = settings.log_dir
    log_file = log_dir / "studyos-video.log"

    root = logging.getLogger()
    if any(isinstance(h, logging.handlers.RotatingFileHandler) for h in root.handlers):
        return                    

    try:
        log_dir.mkdir(parents=True, exist_ok=True)
        fh = logging.handlers.RotatingFileHandler(
            log_file,
            maxBytes=10 * 1024 * 1024,          
            backupCount=5,
            encoding="utf-8",
        )
        fh.setFormatter(logging.Formatter(_LOG_FORMAT, _LOG_DATE_FMT))
        root.addHandler(fh)
        logger.info(f"Log file: {log_file}")
    except OSError as exc:
        logger.warning(f"Could not open log file {log_file}: {exc}")

def _validate_environment() -> None:
    issues: list[str] = []

    ffmpeg = shutil.which("ffmpeg")
    if ffmpeg:
        logger.info(f"  ffmpeg  : {ffmpeg}")
    else:
        try:
            import imageio_ffmpeg                
            bundled = imageio_ffmpeg.get_ffmpeg_exe()
            logger.info(f"  ffmpeg  : {bundled} (bundled via imageio_ffmpeg)")
        except Exception:
            issues.append(
                "ffmpeg is not available. Run: sudo apt-get install ffmpeg  "
                "OR: pip install imageio[ffmpeg]"
            )

    ffprobe = shutil.which("ffprobe")
    if ffprobe:
        logger.info(f"  ffprobe : {ffprobe}")
    else:
        logger.warning(
            "  ffprobe NOT found on PATH. Duration detection will be unavailable. "
            "Install with: sudo apt-get install ffmpeg"
        )

    if issues:
        for issue in issues:
            logger.critical(f"  STARTUP FAILED: {issue}")
        raise RuntimeError(
            f"{len(issues)} missing critical dependency/ies — see logs above."
        )

@asynccontextmanager
async def lifespan(app: FastAPI):
                                                                                       
                                                                    
    _add_file_handler()

    logger.info("=" * 60)
    logger.info("StudyOS Video Service — startup")
    logger.info("=" * 60)

    logger.info("Validating environment...")
    _validate_environment()

    logger.info("Ensuring storage directories...")
    for path in _STORAGE_DIRS:
        ensure_directory(path)
        logger.info(f"  {path}")

    logger.info(f"Loading Whisper model '{settings.whisper_model}'...")
    WhisperService.load_model(settings.whisper_model)

    try:
        lan_ip = socket.gethostbyname(socket.gethostname())
    except OSError:
        lan_ip = "unknown"

    logger.info("=" * 60)
    logger.info("StudyOS Video Service — READY")
    logger.info(f"  Emulator     → http://10.0.2.2:8000")
    logger.info(f"  Real device  → http://{lan_ip}:8000")
    logger.info(f"  API docs     → http://localhost:8000/docs")
    logger.info(f"  Logs         → {settings.log_dir}/studyos-video.log")
    logger.info("=" * 60)
    yield

    logger.info("StudyOS Video Service — shutting down")

app = FastAPI(
    title="StudyOS Video Service",
    description=(
        "Converts YouTube URLs and uploaded video/audio files into "
        "structured study notes using Whisper transcription."
    ),
    version="2.0.0",
    lifespan=lifespan,
)

                                                                                

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
app.add_middleware(RateLimitMiddleware, calls_per_minute=10)
app.add_middleware(RequestIDMiddleware)

                                                                                

@app.exception_handler(VideoServiceException)
async def _video_exc_handler(request: Request, exc: VideoServiceException) -> JSONResponse:
    request_id = getattr(request.state, "request_id", None)
    logger.warning(f"[{request_id}] {exc.status_code} {exc.message}")
    return JSONResponse(
        status_code=exc.status_code,
        content={"success": False, "error": exc.message, "request_id": request_id},
    )

@app.exception_handler(Exception)
async def _generic_exc_handler(request: Request, exc: Exception) -> JSONResponse:
    request_id = getattr(request.state, "request_id", None)
    logger.exception(
        f"[{request_id}] Unhandled {type(exc).__name__} on {request.method} {request.url.path}"
    )
    return JSONResponse(
        status_code=500,
        content={
            "success": False,
            "error": "An internal server error occurred.",
            "request_id": request_id,
        },
    )

                                                                                

@app.get("/live", tags=["Health"], summary="Liveness probe — always 200 while process runs")
async def liveness():
    return {"status": "alive"}

@app.get("/health", tags=["Health"], summary="Basic health check")
async def health_check():
    return {"status": "ok", "service": "studyos-video-service", "version": "2.0.0"}

@app.get("/ready", tags=["Health"], summary="Readiness probe — 200 when fully initialised")
async def readiness():
    checks = {
        "whisper_loaded": WhisperService.is_loaded(),
        "storage_writable": all(p.exists() and p.is_dir() for p in _STORAGE_DIRS),
    }
    ready = all(checks.values())
    return JSONResponse(
        status_code=200 if ready else 503,
        content={"status": "ready" if ready else "not_ready", "checks": checks},
    )

                                                                                

app.include_router(video_router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
