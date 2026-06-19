from typing import Any, Dict, Optional

from fastapi.responses import JSONResponse


def error_json(message: str, status_code: int = 500, detail: Optional[str] = None) -> JSONResponse:
    body: Dict[str, Any] = {"success": False, "error": message}
    if detail:
        body["detail"] = detail
    return JSONResponse(content=body, status_code=status_code)
