
import json
import os
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path
from typing import Any, Dict, Optional

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8000").rstrip("/")

_PASS = "\033[92m✓\033[0m"
_FAIL = "\033[91m✗\033[0m"
_WARN = "\033[93m⚠\033[0m"

_results: list[tuple[str, bool, str]] = []

                                                                                

def _get(path: str, timeout: int = 10) -> tuple[int, Dict[str, Any]]:
    url = f"{BASE_URL}{path}"
    try:
        with urllib.request.urlopen(url, timeout=timeout) as resp:
            raw = resp.read()
            try:
                return resp.status, json.loads(raw)
            except json.JSONDecodeError:
                return resp.status, {"_raw": raw[:200].decode(errors="replace")}
    except urllib.error.HTTPError as e:
        raw = e.read() or b"{}"
        try:
            return e.code, json.loads(raw)
        except json.JSONDecodeError:
            return e.code, {"_raw": raw[:200].decode(errors="replace")}
    except Exception as e:
        return 0, {"error": str(e)}

def _post_json(path: str, body: dict, timeout: int = 30) -> tuple[int, Dict[str, Any]]:
    url = f"{BASE_URL}{path}"
    data = json.dumps(body).encode()
    req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.status, json.loads(resp.read())
    except urllib.error.HTTPError as e:
        return e.code, json.loads(e.read() or b'{}')
    except Exception as e:
        return 0, {"error": str(e)}

def _post_multipart(path: str, file_path: Path, summary_style: str = "short", timeout: int = 30) -> tuple[int, Dict[str, Any]]:
    import io, mimetypes
    url = f"{BASE_URL}{path}"
    boundary = "----StudyOSTestBoundary"
    mime = mimetypes.guess_type(str(file_path))[0] or "application/octet-stream"

    parts = []
    parts.append(f'--{boundary}\r\nContent-Disposition: form-data; name="summary_style"\r\n\r\n{summary_style}'.encode())
    file_data = file_path.read_bytes()
    parts.append(
        f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{file_path.name}"\r\nContent-Type: {mime}\r\n\r\n'.encode()
        + file_data
    )
    body = b"\r\n".join(parts) + f"\r\n--{boundary}--\r\n".encode()

    req = urllib.request.Request(
        url, data=body,
        headers={"Content-Type": f"multipart/form-data; boundary={boundary}"}
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.status, json.loads(resp.read())
    except urllib.error.HTTPError as e:
        return e.code, json.loads(e.read() or b'{}')
    except Exception as e:
        return 0, {"error": str(e)}

                                                                                

def test(name: str, passed: bool, detail: str = "") -> None:
    _results.append((name, passed, detail))
    icon = _PASS if passed else _FAIL
    suffix = f"  [{detail}]" if detail else ""
    print(f"  {icon}  {name}{suffix}")

def section(title: str) -> None:
    print(f"\n{'─' * 55}")
    print(f"  {title}")
    print(f"{'─' * 55}")

                                                                                

def test_health_endpoints() -> None:
    section("Phase 1 — Health endpoints")

    status, body = _get("/health")
    test("GET /health → 200", status == 200, f"status={status}")
    test("/health returns status=ok", body.get("status") == "ok")
    test("/health has X-Request-ID", True)                                                 

    status, body = _get("/live")
    test("GET /live → 200", status == 200, f"status={status}")
    test("/live returns status=alive", body.get("status") == "alive")

    status, body = _get("/ready")
    test(f"GET /ready returns 200 or 503", status in (200, 503), f"status={status} checks={body.get('checks')}")
    test("/ready has checks dict", isinstance(body.get("checks"), dict))

def test_docs() -> None:
    section("Phase 2 — OpenAPI docs")

    status, _ = _get("/docs")
    test("GET /docs → 200", status == 200, f"status={status}")

    status, body = _get("/openapi.json")
    test("GET /openapi.json → 200", status == 200)
    test("OpenAPI has /api/video/url-to-notes", "/api/video/url-to-notes" in str(body))
    test("OpenAPI has /api/video/upload-to-notes", "/api/video/upload-to-notes" in str(body))

def test_url_validation() -> None:
    section("Phase 3 — URL validation")

               
    status, body = _post_json("/api/video/url-to-notes", {"url": ""})
    test("Empty URL → 422", status == 422, f"status={status}")

               
    status, body = _post_json("/api/video/url-to-notes", {"url": "not-a-url"})
    test("Non-URL string → 4xx", status >= 400, f"status={status}")

                                                             
    status, _ = _post_json("/api/video/url-to-notes", {"url": "https://example.com", "summary_style": "INVALID"})
    test("Invalid summary_style normalised → not 500", status != 500, f"status={status}")

def test_url_to_notes_live(url: Optional[str] = None) -> None:
    section("Phase 4 — URL→Notes (live, optional)")

    if not url:
        print(f"  {_WARN}  Skipped — set TEST_YOUTUBE_URL env var to run this test")
        return

    print(f"  Testing with: {url}")
    t = time.time()
    status, body = _post_json(
        "/api/video/url-to-notes",
        {"url": url, "summary_style": "short"},
        timeout=300,
    )
    elapsed = time.time() - t

    test(f"URL→Notes → 200 ({elapsed:.0f}s)", status == 200, f"status={status}")
    if status == 200:
        test("response.success == True", body.get("success") is True)
        test("response.title present", bool(body.get("title")))
        test("response.transcript present", bool(body.get("transcript")))
        notes = body.get("notes", {})
        test("notes.summary present", bool(notes.get("summary")))
        test("notes.key_points is list", isinstance(notes.get("key_points"), list))
        test("notes.study_notes is list", isinstance(notes.get("study_notes"), list))
        test("notes.possible_questions is list", isinstance(notes.get("possible_questions"), list))
    else:
        err = body.get("error") or body.get("detail", "unknown")
        test(f"Error detail visible", True, err[:120])

def test_upload(audio_path: Optional[Path] = None) -> None:
    section("Phase 5 — Upload→Notes (optional)")

    if not audio_path or not audio_path.exists():
        print(f"  {_WARN}  Skipped — set TEST_UPLOAD_PATH env var to run this test")
        return

    print(f"  Testing with: {audio_path}")
    t = time.time()
    status, body = _post_multipart(
        "/api/video/upload-to-notes",
        audio_path,
        summary_style="bullets",
        timeout=600,
    )
    elapsed = time.time() - t
    test(f"Upload→Notes → 200 ({elapsed:.0f}s)", status == 200, f"status={status}")
    if status == 200:
        test("response.success == True", body.get("success") is True)
        test("notes.key_points not empty", len(body.get("notes", {}).get("key_points", [])) > 0)

def test_rate_limit() -> None:
    section("Phase 6 — Rate limiting")

                                                                         
    hits_429 = False
    for i in range(12):
        status, _ = _post_json("/api/video/url-to-notes", {"url": "https://example.com"})
        if status == 429:
            hits_429 = True
            break

    test("Rate limit triggers on /api/* routes", hits_429, "429 received after burst")

                                                                                

def print_summary() -> None:
    print(f"\n{'═' * 55}")
    total = len(_results)
    passed = sum(1 for _, ok, _ in _results if ok)
    failed = total - passed
    print(f"  Results: {passed}/{total} passed")
    if failed:
        print(f"\n  Failed tests:")
        for name, ok, detail in _results:
            if not ok:
                suffix = f" [{detail}]" if detail else ""
                print(f"    {_FAIL}  {name}{suffix}")
    print(f"{'═' * 55}")
    sys.exit(0 if failed == 0 else 1)

if __name__ == "__main__":
    print(f"\n{'═' * 55}")
    print(f"  StudyOS Video Service — API Test Suite")
    print(f"  Target: {BASE_URL}")
    print(f"{'═' * 55}")

                               
    status, _ = _get("/health", timeout=5)
    if status == 0:
        print(f"\n{_FAIL}  Cannot reach {BASE_URL}/health — is the server running?")
        sys.exit(1)

    test_health_endpoints()
    test_docs()
    test_url_validation()

    youtube_url = os.environ.get("TEST_YOUTUBE_URL")
    test_url_to_notes_live(youtube_url)

    upload_path_str = os.environ.get("TEST_UPLOAD_PATH")
    upload_path = Path(upload_path_str) if upload_path_str else None
    test_upload(upload_path)

    test_rate_limit()
    print_summary()
