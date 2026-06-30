# StudyOS Video Service

FastAPI backend that converts YouTube URLs and uploaded video/audio files into structured study notes using local Whisper transcription.

---

## Architecture

```
Request
  │
  ├── POST /api/video/url-to-notes   ──► yt-dlp download
  │                                        │
  └── POST /api/video/upload-to-notes ──► save + ffmpeg extract
                                           │
                                    Whisper transcribe
                                           │
                                    notes_service (pure Python)
                                           │
                                    JSON response
```

---

## Prerequisites

| Tool | Install |
|------|---------|
| Python 3.12 | `sudo apt install python3.12` / [python.org](https://python.org) |
| ffmpeg | `sudo apt install ffmpeg` / `brew install ffmpeg` |
| pip | Ships with Python |

---

## Setup

```bash
# 1. Navigate to the project
cd studyos-video-service

# 2. Create and activate virtual environment
python3.12 -m venv venv
source venv/bin/activate        # Windows: venv\Scripts\activate

# 3. Install dependencies
pip install -r requirements.txt

# 4. Configure environment
cp .env.example .env

# 5. Run
uvicorn app.main:app --reload
```

The server starts at **http://localhost:8000**.  
On first run, Whisper downloads the model weights (~150 MB for `base`).

---

## Docker

```bash
# Build and start
docker-compose up --build

# Run in background
docker-compose up -d --build

# Stop
docker-compose down
```

---

## API Reference

### `GET /health`

```json
{ "status": "ok", "service": "studyos-video-service" }
```

---

### `POST /api/video/url-to-notes`

Convert a YouTube or any yt-dlp-supported URL into study notes.

**Request body (JSON):**
```json
{ "url": "https://www.youtube.com/watch?v=dQw4w9WgXcQ" }
```

**Response:**
```json
{
  "success": true,
  "source_type": "url",
  "title": "Never Gonna Give You Up",
  "transcript": "We're no strangers to love...",
  "notes": {
    "summary": "...",
    "key_points": ["...", "..."],
    "important_terms": [
      { "term": "Machine Learning", "meaning": "Machine Learning is a subset of AI..." }
    ],
    "study_notes": [
      {
        "heading": "Section 1: Core Concepts",
        "points": ["...", "..."]
      }
    ],
    "possible_questions": ["What is...?", "Explain..."]
  }
}
```

---

### `POST /api/video/upload-to-notes`

Upload a local video or audio file.

**Request:** `multipart/form-data`, field name: `file`

Accepted formats: `mp4`, `mkv`, `mov`, `webm`, `mp3`, `wav`, `m4a`

---

## Testing with curl

```bash
# Health check
curl http://localhost:8000/health

# URL to notes
curl -X POST http://localhost:8000/api/video/url-to-notes \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.youtube.com/watch?v=BHACKCNDMW8"}'

# File upload (audio)
curl -X POST http://localhost:8000/api/video/upload-to-notes \
  -F "file=@/path/to/lecture.mp3"

# File upload (video)
curl -X POST http://localhost:8000/api/video/upload-to-notes \
  -F "file=@/path/to/lecture.mp4"
```

Interactive docs: http://localhost:8000/docs

---

## Android Retrofit Integration

### 1. Data classes (Kotlin)

```kotlin
data class URLRequest(val url: String)

data class ImportantTerm(val term: String, val meaning: String)

data class StudyNoteSection(val heading: String, val points: List<String>)

data class NotesOutput(
    val summary: String,
    @SerializedName("key_points") val keyPoints: List<String>,
    @SerializedName("important_terms") val importantTerms: List<ImportantTerm>,
    @SerializedName("study_notes") val studyNotes: List<StudyNoteSection>,
    @SerializedName("possible_questions") val possibleQuestions: List<String>
)

data class VideoNotesResponse(
    val success: Boolean,
    @SerializedName("source_type") val sourceType: String,
    val title: String,
    val transcript: String,
    val notes: NotesOutput
)
```

### 2. Retrofit interface

```kotlin
interface VideoService {

    @POST("api/video/url-to-notes")
    suspend fun urlToNotes(@Body request: URLRequest): VideoNotesResponse

    @Multipart
    @POST("api/video/upload-to-notes")
    suspend fun uploadToNotes(
        @Part file: MultipartBody.Part
    ): VideoNotesResponse
}
```

### 3. Retrofit client

```kotlin
object RetrofitClient {
    // Use 10.0.2.2 for Android emulator → localhost on host machine
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val videoService: VideoService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VideoService::class.java)
    }
}
```

### 4. ViewModel usage

```kotlin
// URL flow
viewModelScope.launch {
    val response = RetrofitClient.videoService.urlToNotes(URLRequest(url))
    // response.notes.summary, response.notes.keyPoints, etc.
}

// Upload flow
val file = File(filePath)
val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

viewModelScope.launch {
    val response = RetrofitClient.videoService.uploadToNotes(body)
}
```

---

## Configuration (`.env`)

| Variable | Default | Description |
|----------|---------|-------------|
| `WHISPER_MODEL` | `base` | `tiny` / `base` / `small` / `medium` / `large` |
| `MAX_UPLOAD_SIZE_MB` | `500` | Max upload size in megabytes |
| `CORS_ORIGINS` | `["*"]` | JSON array of allowed CORS origins |

### Whisper model trade-offs

| Model | VRAM | Speed | Accuracy |
|-------|------|-------|----------|
| tiny  | ~1 GB | Fastest | Low |
| base  | ~1 GB | Fast | Good |
| small | ~2 GB | Medium | Better |
| medium| ~5 GB | Slow | High |
| large | ~10 GB | Slowest | Best |

---

## Extending Notes Generation

The entire notes pipeline lives in `app/services/notes_service.py` and exposes a single function:

```python
def generate_notes(transcript: str, title: str = "") -> dict: ...
```

To replace it with an LLM/RAG backend, implement the same signature and return the same dict shape. No other file needs to change.

---

## Project Structure

```
studyos-video-service/
├── app/
│   ├── main.py              # FastAPI app, lifespan, CORS, global error handler
│   ├── config.py            # Pydantic Settings (loaded from .env)
│   ├── api/routes/
│   │   └── video_routes.py  # Route handlers
│   ├── schemas/
│   │   └── video_schema.py  # Pydantic request / response models
│   ├── services/
│   │   ├── ytdlp_service.py    # yt-dlp audio download
│   │   ├── upload_service.py   # File upload + ffmpeg extraction
│   │   ├── whisper_service.py  # Singleton Whisper model
│   │   ├── notes_service.py    # Pure-Python notes generator
│   │   └── cleanup_service.py  # Temp file deletion
│   ├── utils/
│   │   ├── file_utils.py    # Path helpers
│   │   └── response_utils.py
│   └── core/
│       └── exceptions.py    # Typed domain exceptions
├── storage/                 # Runtime file storage (gitignored)
├── requirements.txt
├── .env.example
├── Dockerfile
└── docker-compose.yml
```
