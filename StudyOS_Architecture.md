# StudyOS Architecture v2.1

## Core Philosophy

StudyOS is not a note-taking application.

StudyOS is a Personal Learning Operating System designed to help students capture knowledge, organize learning, execute study plans, and measure progress.

Knowledge is the foundation of the entire system.

The Note is the central object of StudyOS.

Every feature either creates knowledge, consumes knowledge, tests knowledge, reinforces knowledge, or visualizes knowledge.

The system is designed around a single principle:

Generate knowledge once and reuse it everywhere.

---

# The Knowledge Engine

The Note is the single source of truth.

```text
Video
│
▼
Notes
│
├── Flashcards
├── Quiz
├── Mock Test
├── Tasks
├── Revision Sessions
├── Analytics
└── Progress Tracking
```

Every learning feature derives from Notes.

Knowledge should never be duplicated.

The same note should power:

* Quiz Generation
* Mock Test Generation
* Flashcard Generation
* Revision Sessions
* Linked Tasks
* Learning Analytics

---

# Application Architecture

StudyOS follows:

```text
MVVM + Repository Pattern
```

Architecture Flow:

```text
UI Layer
    ↓
ViewModel Layer
    ↓
Repository Layer
    ↓
Local Storage / Backend Services
```

Responsibilities:

```text
UI
→ Displays state

ViewModel
→ Handles business logic

Repository
→ Handles data access

Backend
→ Handles heavy processing
```

Activities must never contain business logic.

Business logic belongs inside ViewModels and Repositories.

---

# Dashboard System

## Purpose

Provide a complete overview of the user's learning status.

## Owner

```text
DashboardActivity
DashboardViewModel
```

## Responsibilities

* Today's Tasks
* Daily Study Hours
* Weekly Study Hours
* Current Streak
* Longest Streak
* Focus Session Launcher
* Continue Learning
* Vision Board Preview
* Analytics Preview
* Quick Actions

## Dashboard Flow

```text
Task Repository
Analytics Repository
Streak Repository
Focus Repository
        ↓
DashboardViewModel
        ↓
DashboardActivity
```

---

# Notes System

## Purpose

The central knowledge repository of StudyOS.

## Owner

```text
NoteActivity
NotesViewModel
NoteRepository
```

## Responsibilities

* Create Notes
* Edit Notes
* Delete Notes
* Search Notes
* Organize Notes
* Tag Notes
* Link Notes to Tasks
* Generate Learning Materials

## Note Model

Each Note should contain:

```text
id
title
content
subject
tags
createdAt
updatedAt
```

---

# Task System

## Purpose

Convert learning goals into actionable study plans.

## Owner

```text
PlanActivity
PlanViewModel
TaskRepository
```

## Responsibilities

* Create Tasks
* Update Tasks
* Complete Tasks
* Prioritize Tasks
* Link Notes
* Schedule Study Work

## Task Relationship

```text
Task
│
└── Linked Notes
```

A task can contain multiple linked notes.

A note can be linked to multiple tasks.

---

# Streak System

## Purpose

Encourage consistency while remaining realistic for students.

## Owner

```text
StreakRepository
StreakViewModel
```

## StudyOS Rule

StudyOS uses a Flexible Streak System.

Rules:

```text
Miss 1 day
→ Streak continues

Miss 2 days
→ Streak continues

Miss 3 consecutive days
→ Streak resets
```

## Meaningful Study Activity

The streak only updates when the user performs meaningful study actions.

Examples:

* Complete Pomodoro Session
* Complete Task
* Complete Quiz
* Complete Mock Test
* Create Note
* Complete Revision Session

Opening the application does not count.

## Workflow

```text
Study Activity
        ↓
StudyActivityTracker
        ↓
StreakRepository
        ↓
Dashboard Refresh
```

---

# Focus System

## Purpose

Convert plans into focused execution.

## Owner

```text
PomodoroActivity
```

## Responsibilities

* Pomodoro Timer
* Deep Work Sessions
* Session Statistics
* Focus Tracking

## Workflow

```text
Focus Session
        ↓
Focus Repository
        ↓
Analytics
        ↓
Streak System
```

---

# Analytics System

## Purpose

Measure learning effectiveness.

## Owner

```text
ProgressActivity
```

There must not be a separate Analytics screen.

ProgressActivity is the single analytics owner.

## Responsibilities

* Daily Study Hours
* Weekly Study Hours
* Monthly Study Hours
* Current Streak
* Longest Streak
* Focus Score
* Quiz Accuracy
* Task Completion Rate
* Revision Statistics

---

# Knowledge Generation Layer

This layer transforms Notes into learning materials.

Everything originates from Notes.

---

# Quiz Generation

## Backend Required

No

## Technology

Rule-Based NLP

## Files

```text
QuestionGenerator.kt
QuizViewModel.kt
QuizScreen.kt
```

## Workflow

```text
Note
    ↓
QuestionGenerator
    ↓
Generated MCQs
    ↓
QuizScreen
```

---

# Mock Test Generation

## Backend Required

No

## Technology

Rule-Based NLP

## Workflow

```text
Selected Notes
        ↓
QuestionGenerator
        ↓
Question Bank
        ↓
Mock Test
```

## Purpose

Provide exam-style practice from existing notes.

---

# Flashcard Generation

## Backend Required

No

## Technology

Rule-Based Extraction

## Workflow

```text
Note
    ↓
Concept Extraction
    ↓
Flashcards
```

## Purpose

Provide active recall learning.

---

# Video To Notes System

## Backend Required

Yes

## Technology Stack

```text
FastAPI
faster-whisper
FFmpeg
yt-dlp
```

## Purpose

Transform educational videos into structured notes.

## Supported Inputs

1. Local Video Upload
2. Shared Video URL

Both use the same backend pipeline.

---

# Unified Video Pipeline

```text
Android Application
        │
        ├── Upload Video
        │
        └── Share URL
                │
                ▼
FastAPI Backend
                │
                ▼
Media Queue
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
Notes Repository
                │
                ▼
StudyOS Knowledge Engine
```

---

# Why FastAPI

FastAPI is used because:

* Whisper performs best in Python
* FFmpeg integration is straightforward
* yt-dlp integration is straightforward
* Background processing is easier
* Android remains lightweight
* Future AI integrations become easier

Android should never process large media files directly.

Android uploads.

FastAPI processes.

---

# Final Technical Decisions

```text
Dashboard
→ Android MVVM

Notes
→ Android MVVM

Tasks
→ Android MVVM

Streak System
→ Flexible Streak Architecture

Quiz Generation
→ Local Rule-Based NLP

Mock Test Generation
→ Local Rule-Based NLP

Flashcard Generation
→ Local Rule-Based NLP

Analytics
→ ProgressActivity

Video To Notes
→ FastAPI + FFmpeg + faster-whisper + yt-dlp

AI APIs
→ Not Required For V1
```

## StudyOS Mission

StudyOS focuses on:

* Knowledge Management
* Learning Consistency
* Active Recall
* Revision
* Focused Study
* Productivity

Artificial Intelligence is an enhancement layer, not the foundation of the platform.
