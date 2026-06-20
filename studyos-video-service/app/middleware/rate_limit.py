import time
from collections import defaultdict
from threading import Lock

from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request
from starlette.responses import JSONResponse, Response

_WINDOW_SECONDS = 60

class RateLimitMiddleware(BaseHTTPMiddleware):

    def __init__(self, app, calls_per_minute: int = 10):
        super().__init__(app)
        self._limit = calls_per_minute
        self._counts: dict = defaultdict(list)
        self._lock = Lock()

    async def dispatch(self, request: Request, call_next) -> Response:
        if not request.url.path.startswith("/api/"):
            return await call_next(request)

        ip = request.client.host if request.client else "unknown"
        now = time.time()

        with self._lock:
            window_start = now - _WINDOW_SECONDS
            self._counts[ip] = [t for t in self._counts[ip] if t > window_start]

            if len(self._counts[ip]) >= self._limit:
                request_id = getattr(request.state, "request_id", None)
                return JSONResponse(
                    status_code=429,
                    content={
                        "success": False,
                        "error": f"Rate limit exceeded — maximum {self._limit} requests per minute.",
                        "request_id": request_id,
                    },
                )

            self._counts[ip].append(now)

        return await call_next(request)
