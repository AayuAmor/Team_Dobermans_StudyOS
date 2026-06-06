# StudyOS Architecture

## Core Philosophy

StudyOS is not a note-taking app.

StudyOS is a Personal Learning Operating System.

Everything revolves around knowledge.

The Note becomes the central object of the entire application.

Every feature either creates knowledge, consumes knowledge, tests knowledge, or visualizes knowledge.

---

# The Knowledge Engine

The Note is the source of truth.

```text
Video
│
├── Notes
│
├── Flashcards
│
├── Quiz
│
├── Mock Test
│
├── Tasks
│
├── Analytics
│
└── Revision Sessions
```

Everything derives from Notes.

Never duplicate knowledge.

Generate it once.

Reuse it everywhere.

---

# Final Feature Architecture

## Dashboard

Purpose:

Display learning status and provide entry points into the learning system.

Responsibilities:

* Today's Tasks
* Study Hours
* Streak
* Focus Session
* Continue Learning
* Quick Actions
* Analytics Preview
* Vision Board Preview

Owner:

```text
DashboardActivity
```

---

## Notes System

Purpose:

The central knowledge repository.

Responsibilities:

* Create Notes
* Edit Notes
* Organize Notes
* Tag Notes
* Search Notes
* Link Notes to Tasks
* Generate Learning Content

Owner:

```text
NoteActivity
NotesViewModel
NoteRepository
```

---

## Task System

Purpose:

Convert learning goals into actions.

Responsibilities:

* Create Task
* Update Task
* Prioritize Task
* Link Notes
* Track Completion

Owner:

```text
PlanActivity
PlanViewModel
TaskRepository
```

Relationship:

```text
Task
↓
Linked Notes
```

---

## Focus System

Purpose:

Convert planned learning into focused execution.

Responsibilities:

* Pomodoro
* Deep Work Sessions
* Focus Statistics

Owner:

```text
PomodoroActivity
```

---

## Analytics System

Purpose:

Measure learning effectiveness.

Single Owner:

```text
ProgressActivity
```

Responsibilities:

* Study Hours
* Weekly Hours
* Streak
* Quiz Accuracy
* Focus Score
* Completion Rate

There should be no separate AnalyticsScreen.


ProgressActivity owns analytics.

---

# Knowledge Generation Layer

This is the intelligence layer.

All generated content comes from Notes.

---

## Quiz Generation

Backend Required:

No

Implementation:

Rule-Based NLP

Files:

```text
QuestionGenerator.kt
QuizViewModel.kt
QuizScreen.kt
```

Input:

```text
Note
```

Output:

```text
MCQs
```

Flow:

```text
Note
↓
QuestionGenerator
↓
Quiz Questions
```

---

## Mock Test Generation

Backend Required:

No

Implementation:

Same engine as Quiz Generation.

Input:

```text
Multiple Notes
```

Output:

```text
Exam-style Question Set
```

Flow:

```text
Selected Notes
↓
Question Generator
↓
Question Bank
↓
Mock Test
```

---

## Flashcard Generation

Backend Required:

No

Implementation:

Rule-Based Extraction

Input:

```text
Note
```

Output:

```text
Front
Back
```

Flow:

```text
Note
↓
Important Concepts
↓
Flashcards
```

---

# Video To Notes System

Backend Required:

Yes

Technology:

```text
FastAPI
faster-whisper
FFmpeg
yt-dlp
```

Purpose:

Transform videos into structured notes.

Input Sources:

1. Local Video Upload
2. Shared Video URL

Both paths enter the same processing pipeline.

---

## Unified Video Pipeline

```text
Android
│
├── Upload Video
│
└── Share URL
        │
        ▼
FastAPI Backend
        │
        ▼
Media Processing Queue
        │
        ▼
Audio Extraction
        │
        ▼
Whisper Transcription
        │
        ▼
Note Structuring
        │
        ▼
Generated Notes
        │
        ▼
Android Notes System
```

---

## Why FastAPI

FastAPI is used because:

* Whisper runs best in Python
* FFmpeg integration is easier
* yt-dlp integration is easier
* Processing can happen asynchronously
* Android remains lightweight

Android should never process videos directly.

Android only uploads.

FastAPI performs computation.

---


# Final Technical Decisions

Dashboard
→ Android MVVM

Tasks
→ Android MVVM

Notes
→ Android MVVM

Quiz Generation
→ Local Rule-Based NLP

Mock Test Generation
→ Local Rule-Based NLP

Flashcards
→ Local Rule-Based NLP

Analytics
→ ProgressActivity only

Video To Notes
→ FastAPI + FFmpeg + faster-whisper + yt-dlp

AI APIs
→ Not required for v1

```

StudyOS v1 focuses on execution, learning, and knowledge management, not expensive AI. AI becomes an enhancement later, not the foundation.
```
