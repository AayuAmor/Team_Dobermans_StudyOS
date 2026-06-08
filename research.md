# StudyOS Engineering Research Book
## Internal Wiki · Architecture Handbook · Engineering Notebook · Development Roadmap

**Version:** 2.0  
**Last Updated:** June 2026  
**Package:** `com.teamdobermans.studyos`  
**Min SDK:** 31 (Android 12) · Target SDK: 36  
**Language:** Kotlin · UI: Jetpack Compose  
**Architecture:** MVVM + Repository Pattern + Firebase  
**Team:** Team Dobermans  
**Primary Developer:** Ayush Kumar Raut

---

> **How to use this document.**  
> This is the internal engineering bible for StudyOS. Not documentation for users. Not an API reference. This document lets a developer who has never seen this codebase:  
> (a) understand every architectural decision ever made,  
> (b) explain every feature in a technical interview or viva,  
> (c) rebuild every feature from scratch,  
> (d) plan the next six months of development without guessing.
>
> Read it linearly once. Then use it as a reference.

---

# MASTER FEATURE STATUS REGISTRY

| Feature | Status | Sprint | Owner | Architecture | Firestore |
|---|---|---|---|---|---|
| Onboarding (3 screens) | Implemented | Sprint 1 | Ayush Kumar Raut | Activity + SharedPrefs | No |
| App Branding / Theme | Implemented | Sprint 1 | Ayush Kumar Raut | Material3 + Color.kt | No |
| Email/Password Auth | Implemented | Sprint 3 | Ayush Kumar Raut | MVVM + Firebase Auth | No |
| Google SSO | Implemented | Sprint 3 | Ayush Kumar Raut | MVVM + GoogleSignInHelper | No |
| Dashboard Overview | Implemented | Sprint 3 | Ayush Kumar Raut | MVVM + DashboardViewModel | No (static) |
| Task Management (CRUD) | Implemented | Sprint 3 | Ayush Kumar Raut | MVVM + TaskRepository | In-Memory |
| Focus Sessions (Pomodoro) | Implemented | Sprint 3 | Ayush Kumar Raut | MVVM + PomodoroViewModel | No |
| Break Timer (Short/Long) | Implemented | Sprint 3 | Ayush Kumar Raut | PomodoroViewModel tabs | No |
| Notes CRUD | Implemented | Sprint 4 | Ayush Kumar Raut | MVVM + NoteRepoImpl | Yes |
| Auto-Save Notes | Implemented | Sprint 4 | Ayush Kumar Raut | Coroutine Debounce + Firestore | Yes |
| Note Folders | Implemented | Sprint 4 | Ayush Kumar Raut | Client-side filter | Partial |
| Link Notes to Tasks | Implemented | Sprint 4 | Ayush Kumar Raut | TaskRepository.linkedNoteIds | In-Memory |
| Distraction-Free Mode | Partially Implemented | Sprint 4 | Ayush Kumar Raut | FocusViewModel | No |
| Flashcards (UI) | Partially Implemented | Sprint 4 | Ayush Kumar Raut | Static composable | No |
| Vision Board | Partially Implemented | Sprint 5 | Ayush Kumar Raut | VisionBoardViewModel | In-Memory |
| Calendar View | Implemented | Sprint 5 | Ayush Kumar Raut | Compose Calendar in PlanBody | No |
| Search Notes | Implemented | Sprint 5 | Ayush Kumar Raut | Client-side filter in Note.kt | No |
| Dashboard Widgets | Partially Implemented | Sprint 5 | Ayush Kumar Raut | DashboardUiState | Static |
| Profile Management | Implemented | Sprint 6 | Ayush Kumar Raut | MVVM + ProfileRepository | Yes |
| Sign Out | Implemented | Sprint 6 | Ayush Kumar Raut | SettingsViewModel | No |
| Settings (UI) | Partially Implemented | Sprint 6 | Ayush Kumar Raut | SettingsViewModel | No |
| Notifications Toggle | Partially Implemented | Sprint 6 | Ayush Kumar Raut | SettingsViewModel (no-op) | No |
| Sync Architecture | Partially Implemented | Sprint 6 | Ayush Kumar Raut | Firestore real-time + offline | Yes |
| Analytics (UI + Charts) | Partially Implemented | Sprint 3 | Ayush Kumar Raut | AnalyticsViewModel | No (static) |
| Progress Heatmap | Partially Implemented | Sprint 5 | Ayush Kumar Raut | ProgressViewModel (random data) | No |
| Subject Management | Partially Implemented | Sprint 3 | Ayush Kumar Raut | SubjectViewModel | No |
| Focus Ambient Sounds | Partially Implemented | Sprint 4 | Ayush Kumar Raut | FocusViewModel (state only) | No |
| Dark Mode | Partially Implemented | Sprint 6 | Ayush Kumar Raut | SettingsViewModel (no-op) | No |
| Video to Notes | Planned | Sprint 9 | TBD | FastAPI + Claude API | No |
| AI Quiz Generation | Planned | Sprint 9 | TBD | FastAPI + Claude API | No |
| Mock Test | Partially Implemented | Sprint 9 | TBD | MockTestViewModel (config only) | No |
| Spaced Repetition Flashcards | Planned | Sprint 10 | TBD | SM-2 Algorithm + Firestore | No |
| Real Study Streak | Planned | Sprint 8 | TBD | StudySessionRepository | No |
| Brain Games | Partially Implemented | Sprint 9 | TBD | Static placeholder UI | No |
| Note Sharing | Future Sprint | Sprint 11+ | TBD | Deep links + Firestore | No |

---

# PART 1 — PRODUCT FOUNDATION

## 1.1 What Is StudyOS

StudyOS is a personal study operating system for Android. The word "operating system" is intentional. An OS does not do one thing well — it provides a platform on which everything else runs. StudyOS is the platform on which a student's entire study life runs.

The competitive landscape StudyOS competes against:

| Tool | What it does | What it misses |
|---|---|---|
| Google Keep | Notes | No tasks, no study tools, no structure |
| Notion | Notes + tasks | Too complex, no study-specific tools |
| Anki | Flashcards | No notes, no tasks, no timer |
| Forest | Focus timer | No notes, no study content |
| Todoist | Tasks | No notes, no study tools |
| YouTube | Learning videos | No capture, no notes, no review |

**The StudyOS thesis:** None of these tools know about each other. A student watches a YouTube lecture, takes notes in Notion, creates tasks in Todoist, reviews flashcards in Anki, and times sessions with Forest. Five apps. Zero connection between them.

StudyOS collapses this stack. Notes connect to tasks. Tasks have deadlines on a calendar. Notes become flashcards. Flashcards become quiz questions. Quiz performance feeds analytics. Analytics show streak and progress. Everything flows through notes.

## 1.2 The Core Data Architecture

```
Raw learning input
(Video / Lecture / Reading)
          ↓
    ┌─────────────┐
    │    NOTES    │  ← The single source of truth
    └─────────────┘
       /    |    \
      ↓     ↓     ↓
   QUIZ  FLASH  TASKS ──→ CALENDAR
   GEN   CARDS     ↓
    ↓      ↓    DASHBOARD
  MOCK  SPACED     ↓
  TEST  REPET.  ANALYTICS
    ↓      ↓       ↓
    └──────┴───→ STREAK
                    ↓
               VISION BOARD
```

This flow is the system design principle that must never be violated. Every feature either creates a note, consumes a note, or connects two note-derived things together.

## 1.3 Target Users

**Primary persona: The Exam-Driven Student**
- University or college student, 2nd or 3rd year
- Has 4–6 active subjects simultaneously
- Watches YouTube lectures, takes notes, has upcoming exams
- Tries multiple apps but loses context when switching apps
- Pain point: "I have notes everywhere but I can't actually study from them"

**Secondary persona: The Self-Learner**
- Teaching themselves a skill (programming, a language, design)
- No fixed curriculum — self-directed
- Needs structure: what have I learned? what do I need to review?
- Pain point: "I know I'm learning but I can't measure it"

---

# PART 2 — SPRINT HISTORY AND RATIONALE

## Why Sprint Order Matters

Understanding which sprint introduced which feature explains why the codebase looks the way it does. Features built in Sprint 1 constrained Sprint 3. Sprint 3 decisions constrained Sprint 4. You cannot understand the architecture without understanding the order features were built.

---

## Sprint 1–2: Foundation and UI Shell

**Goal:** Get a working Android app with StudyOS visual identity and basic navigation.

**Why this sprint existed:**  
Before any feature can be built, the app must exist. Sprints 1–2 established the visual language, the entry point, the onboarding experience, and proved the tech stack worked.

**Features Introduced:**

### Onboarding (3 Screens)
The first thing every new user sees. Three `ComponentActivity` screens (not a NavGraph) reflect the early stage of the codebase — Compose Navigation had not been introduced yet. This is architectural debt: it was built before the NavGraph architecture was decided.

**Why 3 separate activities instead of 1 with NavGraph:**  
NavGraph was not established in Sprint 1. The path of least resistance for sequential screens at that stage was explicit Intent-based navigation. Migrating to NavGraph in Sprint 8 requires replacing these with composable destinations.

### App Branding / Theme
`Color.kt`, `Theme.kt`, `Type.kt` were established. The purple-centric palette (`StudyPurple = 0xFF5B4FD4`) signals focus and calm. Purple is cognitively associated with creativity and concentration. All subsequent screens are built on this palette.

The decision to finalize the theme in Sprint 1 is correct engineering practice. Every screen built before the theme is finalized requires rework. By establishing the color constants first, every later composable can reference `StudyPurple` and `TextPrimary` without hardcoding hex values.

**Architecture Introduced:**
- `ComponentActivity` + `setContent { }` for Compose
- `SharedPreferences` for the `onboarding_completed` persistence flag
- `MaterialTheme` with custom color scheme
- `StudyOSTheme` wrapper composable

**Lessons Learned:**
- Building onboarding as separate Activities creates navigation dead ends. Intent-based navigation cannot pass complex state between screens cleanly.
- The theme should be finalized in Sprint 1. Changing a color constant fixes it everywhere; changing a hardcoded hex requires a grep.

**How Later Sprints Depend On This:**
- Sprint 3 inherits the `MainActivity` entry point logic
- All screens inherit `Color.kt` — changing a constant propagates everywhere
- The SharedPreferences `"onboarding_completed"` flag is checked in Sprint 3's `MainActivity` auth gate

---

## Sprint 3: Authentication + Core Study Infrastructure

**Goal:** Users must have accounts. Data must be private. Core study features must be functional.

**Why these features were grouped together:**  
Authentication is a prerequisite for all data persistence. Firestore queries filter by `userId`. Without auth, there is no `userId`. Sprint 3 combined auth with the first wave of study features that would eventually need Firestore persistence in Sprint 4.

**Features Introduced:**

### Authentication (Email/Password + Google SSO)

**Status:** Implemented  
**Why Sprint 3:** Firebase Firestore queries filter by `userId`. Without auth, there is no `userId`. Notes (Sprint 4) need auth to exist first.

**Architecture decision — Firebase Auth over custom backend:**  
A custom JWT-based backend requires: a running server, HTTPS certificate, JWT signing keys, token refresh logic, session invalidation on password change, and secure token storage. Firebase Auth provides all of this as a managed service. The correct choice is to not operate infrastructure you don't need to operate.

**What was built:**
```
AuthRepository     — suspend fun login, signUp, signInWithCredential returning Result<Unit>
AuthViewModel      — AuthState sealed class (Idle / Loading / Success / Error)
LoginBody          — Composable in NavGraph
SignUpBody         — Composable in NavGraph
GoogleSignInHelper — Singleton: builds GoogleSignInClient, provides sign-in intent
```

**Architecture flow:**
```
View (LoginBody)
  → ViewModel (AuthViewModel.login())
    → Repository (AuthRepository.login())
      → Firebase Auth
    ← Result<Unit>
  ← AuthState.Success / AuthState.Error
← navigate to Home / show error message
```

**The AuthState sealed class:**
```kotlin
sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
```

Sealed classes enforce exhaustive handling. The `when(authState)` block in the UI must handle all four cases or the code does not compile. This prevents the common bug of forgetting to show an error.

**Gap: Google Web Client ID placeholder**  
`strings.xml` contains `default_web_client_id = "YOUR_WEB_CLIENT_ID_HERE"`. This must be replaced with the actual Web Client ID from the Firebase Console's OAuth settings before Google SSO works. This is BUG-009, severity Critical.

### Dashboard Overview

**Status:** Implemented (with static data)  
**Why Sprint 3:** A study app needs a home screen. The dashboard answers "what should I do today?" Even with static data, the visual structure validates the app's value proposition.

**What was built:**
- `DashboardUiState` data class — all dashboard metrics
- `DashboardViewModel` — state management + mini timer
- `DashboardBody` composable — the active Home route in NavGraph
- `HomeScreen.kt` — alternate home composable (prototype, not active in NavGraph — dead code)

**Known bug — BUG-001:**  
`DashboardViewModel` contains: `private val taskRepository = TaskRepository()`. This is a Kotlin compile error — `TaskRepository` is an `object` singleton. Constructors cannot be called on `object` declarations. The correct reference is `TaskRepository` (without parentheses). This must be fixed in Sprint 8.

**Static data decision:**  
Streak (15), weekly hours (8.5), and quiz accuracy (78%) are hardcoded. This was an explicit sprint decision: build the UI first, connect real data in Sprint 8. The alternative — waiting until analytics is real — would delay user testing by multiple sprints. The cost: dashboard metrics never update.

### Task Management

**Status:** Implemented (in-memory persistence)  
**Why Sprint 3:** Tasks provide the "what to do today" component of study planning. The dashboard needs real task data to show on its "Today's Tasks" card.

**The `Task` model:**
```kotlin
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val priority: Priority,         // HIGH, MEDIUM, LOW
    val subjectId: String,
    val subjectName: String,
    val done: Boolean = false,
    val linkedNoteIds: List<String> = emptyList()
) {
    fun isOverdue(): Boolean {
        val deadline = endDate ?: startDate
        return deadline.isBefore(LocalDate.now()) && !done
    }
}
```

`UUID.randomUUID()` for IDs: works offline, no server coordination needed, compatible with Firestore (accepts any string as document ID).

**Architecture decision — in-memory `object` singleton for Sprint 3:**  
In-memory was chosen because: (1) Firestore pattern was being established fresh with Notes in Sprint 4; (2) Task data model needed validation through real usage before committing to a database schema; (3) Sprint 3 timeline was tight.

**Cost:** All tasks lost on process death. Largest data integrity gap in the application after static analytics.

### Focus Sessions (Pomodoro Timer)

**Status:** Implemented  
**Why Sprint 3:** Focus sessions are the engine of the analytics system. Sprint 3 builds the mechanism. Sprint 8 wires it to data persistence.

**Architecture decision — `CountDownTimer` vs. coroutine timer:**  
`CountDownTimer.onTick()` is called from the main thread (safe for StateFlow updates without `withContext`). It handles device sleep states. It is a standard Android primitive with no dependencies.

Tradeoff: `CountDownTimer` cannot survive process death. A `WorkManager`-based timer would survive but is significantly more complex. Sprint 7 adds a `ForegroundService` solution.

### Break Timer (Short Break / Long Break)

**Status:** Implemented  
**Why Sprint 3:** The Pomodoro Technique requires breaks. Short break (5 min) and Long break (15 min) are tabs in `PomodoroViewModel`, not separate features. Same `CountDownTimer` mechanism, different duration.

`PomodoroTab` enum drives duration selection:
```kotlin
enum class PomodoroTab { FOCUS, SHORT_BREAK, LONG_BREAK }

fun totalSecondsForTab(): Long = when (_selectedTab.value) {
    FOCUS       → (_focusMinutes.value * 60).toLong()
    SHORT_BREAK → (_shortMinutes.value * 60).toLong()
    LONG_BREAK  → (_longMinutes.value * 60).toLong()
}
```

**Sprint 3 Lessons Learned:**
- The MVVM + Repository chain established here (View → ViewModel → Repository → Firebase) is reused by every subsequent feature. Getting it right in Sprint 3 paid dividends in Sprint 4 and beyond.
- Grouping auth + core features in one sprint worked because auth was a dependency, not a separate concern. Features that share dependencies belong in the same sprint.
- Static analytics data in Sprint 3 is correct practice, but must be scheduled for replacement. Without an explicit Sprint 8 commitment, static data becomes permanent.

---

## Sprint 4: Backend Integration + Notes System

**Goal:** Connect the app to real cloud storage. Notes must persist across sessions and devices.

**Why these features were grouped together:**  
"Backend Integration" is the infrastructure that makes features real. Sprint 4 took the task UX shells from Sprint 3 and connected notes — the central data entity — to Firebase Firestore for the first time.

### Backend Integration (Firebase Firestore)

**Status:** Implemented for Notes; planned for Tasks, Sessions, Folders in Sprint 7  
**Why Firestore over custom backend:** Managed service, zero server operations, real-time sync, offline persistence. The team is one developer; operating a server is not justified at this stage.

**Pattern established in Sprint 4 for all future Firestore features:**

```kotlin
// 1. Pre-generate ID before write
val id = notesCollection.document().id

// 2. Build model with ID embedded
val note = NoteModel(id=id, ..., userId=uid)

// 3. Write with await()
notesCollection.document(id).set(note).await()

// 4. Read as Flow via callbackFlow + AuthStateListener + addSnapshotListener
fun getItems(): Flow<List<Model>> = callbackFlow {
    var listener: ListenerRegistration? = null
    val authListener = FirebaseAuth.AuthStateListener { auth ->
        listener?.remove()
        val uid = auth.currentUser?.uid ?: run { trySend(emptyList()); return@AuthStateListener }
        listener = collection.whereEqualTo("userId", uid).addSnapshotListener { snap, _ ->
            trySend(snap?.documents?.mapNotNull { it.toObject(Model::class.java) } ?: emptyList())
        }
    }
    auth.addAuthStateListener(authListener)
    awaitClose { listener?.remove(); auth.removeAuthStateListener(authListener) }
}
```

This exact pattern is copied verbatim for TaskRepository Firestore migration (Sprint 7), StudySessionRepository (Sprint 8), and VisionBoardRepository (Sprint 7).

### Note Management (CRUD)

**Status:** Implemented  
**Why Sprint 4:** Notes are the central data entity. Every AI feature (quiz, flashcards, video-to-notes) depends on notes existing in Firestore first. Sprint 4 establishes the foundation.

**Architecture — Flat collection vs. nested per-user:**  
`notes/{noteId}` (flat, all users, filtered by `userId` field) was chosen over `users/{uid}/notes/{noteId}` (nested per user) because:
- Simpler queries (no collection group queries needed)
- Easier Firebase Console management
- Slightly less verbose code

Tradeoff: Security Rules must enforce `userId == request.auth.uid`. Without rules, any authenticated user can read all notes. BUG-010, critical severity.

**State management in ViewModel:**
```kotlin
val notes: StateFlow<List<NoteModel>> = repo.getNotes()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

`SharingStarted.WhileSubscribed(5000)`: keeps the upstream Firestore listener alive for 5 seconds after the last subscriber disappears. Prevents the listener from being torn down and re-established during configuration changes (rotation). The 5000ms window covers most rotation scenarios.

### Auto-Save Notes

**Status:** Implemented  
**Why Sprint 4:** Once notes are in Firestore, the primary user anxiety is "will I lose my work?" Auto-save eliminates that anxiety. It is not a separate feature — it is a trust mechanism for the Notes feature.

See Part 4 (Feature Deep Dives) for the complete architectural breakdown.

### Flashcards (UI Shell)

**Status:** Partially Implemented  
**Why introduced in Sprint 4:** Flashcards are the most evidence-backed study tool after active recall. Sprint 4 built the UI structure (card flip, difficulty buttons) as a shell to validate UX before building the backend in Sprint 10.

**What is missing:** `FlashcardModel`, `FlashcardRepository`, SM-2 algorithm, Firestore persistence, review session flow.

### Link Notes to Tasks

**Status:** Implemented (in-memory only — loses data when app is killed)  
**Why Sprint 4:** Once notes existed in Firestore, the note-task connection became meaningful. A task can reference specific notes the student should use to complete it.

**Implementation:**
```
Task.linkedNoteIds: List<String>     — IDs of linked notes stored in Task
TaskRepository.attachNoteToTask()   — adds noteId to task's linkedNoteIds list
TaskRepository.getTasksForNote()    — reverse lookup: find all tasks linking a note
NoteViewModel.loadLinkedTasks()     — calls getTasksForNote, populates _linkedTasks StateFlow
PlanViewModel.attachNote()          — coordinates note picker → attach operation
```

**Gap:** Since `TaskRepository` is in-memory, these links are lost on process death.

### Distraction-Free Mode

**Status:** Partially Implemented  
**What exists:** `FocusViewModel.activeSound: StateFlow<String?>` tracks which ambient sound name is active. `toggleSound(name)` sets or clears the active sound.

**What is missing:** Actual audio playback. `MediaPlayer` or `ExoPlayer` integration not implemented.

**Sprint 4 Lessons Learned:**
- The pattern established for `NoteRepoImpl` (callbackFlow + AuthStateListener + pre-generated IDs) is the canonical Firestore pattern for this codebase. It should be copied verbatim for every new Firestore-backed feature.
- UI shells (Flashcards, Video-to-Notes) have value even without backends: they validate UX, establish visual language, and allow user testing of interaction models before backend investment.
- `currentEditingNoteId` in the ViewModel is the key insight for preventing autosave duplicates. Identity management is a non-obvious problem that requires deliberate design.

---

## Sprint 5: Dashboard Enhancement + Organization Features

**Goal:** Make the dashboard actionable. Give students better ways to organize and find their work.

**Why these features were grouped together:**  
Sprint 5 addresses the student's second week using the app. They have notes and tasks. Now they need to find things quickly (Search), see everything on a timeline (Calendar), track goals visually (Vision Board), and see the dashboard reflect their work (Dashboard Widgets).

### Vision Board

**Status:** Partially Implemented  
**Why Sprint 5:** Vision boards are a motivational layer. They answer "why am I studying?" The psychological research on implementation intentions shows that writing down a concrete goal increases follow-through by ~33%. A student who sees their goals every time they open the app is more likely to study.

**Architecture:**
```
VisionBoardViewModel
  _pinnedGoals: MutableStateFlow<List<VisionGoalModel>>
  pinGoal(goal) / removeGoal(goal)
```

**Gap:** In-memory. All goals lost on process death. Fix: Firestore persistence in Sprint 7 following the NoteRepoImpl pattern.

### Calendar View

**Status:** Implemented  
**Why Sprint 5:** Tasks need a timeline view. A list of tasks sorted by date is harder to parse than a visual calendar where you can see the whole month.

**Architecture decision — custom calendar vs. library:**  
Custom implementation (pure Compose, `java.time.YearMonth`, `java.time.LocalDate`) was chosen to maintain visual control and avoid a third-party dependency for a moderately complex but standard UI component.

Tradeoff: more code to maintain; edge cases around month boundaries (first day of week offset, variable month length) must be handled manually.

### Search Notes

**Status:** Implemented  
**Architecture decision — client-side vs. Firestore full-text search:**  
Client-side search works because `stateIn()` keeps the full notes list in ViewModel memory. For typical student usage (50–500 notes), client-side filtering has negligible performance cost. Firestore does not support native full-text search; alternatives (Algolia, Typesense) require additional services and billing. Client-side search is the correct choice at this scale.

Scaling limit: approximately 1,000–2,000 notes before noticeable lag. At that scale, Algolia integration becomes necessary.

### Dashboard Widgets

**Status:** Partially Implemented  
**What works:** Today's tasks from `TaskRepository` (real), mini Pomodoro timer (functional), quick-action grid (navigation).  
**What is static:** Streak (15), weekly hours (8.5), quiz accuracy (78%), focus sessions (3).

**Sprint 5 Lessons Learned:**
- Calendar implementation in pure Compose is achievable but requires careful `java.time` math. The month grid requires: `YearMonth.of(year, month).lengthOfMonth()` for day count, `LocalDate.of(year, month, 1).dayOfWeek.value` for first-day offset.
- Client-side search with `contains(query, ignoreCase = true)` is simple and effective for this data volume. Do not over-engineer search at the early stage.

---

## Sprint 6: Profile + Sync Architecture

**Goal:** Users manage their profile. Settings are respected. Data syncs reliably.

**Why these features were grouped together:**  
Profile management and sync are infrastructure features. They make the app feel complete. A user who cannot change their name or password perceives the app as unfinished regardless of study feature quality.

### Profile Management

**Status:** Implemented  
**What was built:**
- `ProfileRepository.updateDisplayName()` — writes to both Firebase Auth profile and Firestore `users/{uid}` document
- `ProfileRepository.updatePassword()` — calls `auth.currentUser!!.updatePassword(newPassword).await()`
- `ProfileViewModel` — wraps repository, `saveResult: StateFlow<String?>`
- `ProfileScreenV2` composable — name + password edit form

**Gap:** `updatePassword()` does not handle `FirebaseAuthRecentLoginRequiredException`. Firebase requires recent authentication for password changes. If the user's session is old, this throws. The UI must catch this and prompt re-authentication (sign in again before changing password). Not currently handled — BUG-011.

### Notifications Toggle

**Status:** Partially Implemented  
**What exists:** Toggle in `SettingsViewModel` with `_notificationsEnabled: MutableStateFlow<Boolean>`.  
**What is missing:** Actual notification scheduling with `WorkManager` or `AlarmManager`.

**Sprint 6 completion path:**
```kotlin
fun toggleNotifications() {
    val enabled = !_notificationsEnabled.value
    _notificationsEnabled.value = enabled
    if (enabled) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "study_reminder", ExistingPeriodicWorkPolicy.REPLACE,
            PeriodicWorkRequestBuilder<StudyReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(calculateDelayUntil(hour = 9, minute = 0))
                .build()
        )
    } else {
        WorkManager.getInstance(context).cancelUniqueWork("study_reminder")
    }
}
```

### Sync Architecture

**Status:** Partially Implemented  
**What works:** Firestore offline persistence (default on Android SDK), AuthStateListener-aware note streams, `.await()` on writes ensures completion.

**Critical gap — Firestore Security Rules not configured:**  
Without rules, any authenticated StudyOS user can read any other user's notes. This must be fixed before public release:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /notes/{noteId} {
      allow read, write: if request.auth != null
                         && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null
                    && request.auth.uid == request.resource.data.userId;
    }
  }
}
```

**Sprint 6 Lessons Learned:**
- Display name must be updated in two places: `FirebaseAuth.UserProfileChangeRequest` (for `currentUser.displayName`) and Firestore `users/{uid}` (for querying). Missing either creates inconsistency.
- SharedPreferences for settings persistence is the correct choice — settings are device-specific, not cloud-synced. Using Firestore for settings would be overengineering.
- Security Rules are not optional. They are the boundary between a working app and a security breach.

---

# PART 3 — ARCHITECTURE DEEP DIVES

## 3.1 MVVM Architecture

### What
Model-View-ViewModel separates the app into three layers:
- **Model**: Data classes and business logic
- **View**: Compose UI functions that observe state and emit events
- **ViewModel**: Holds state, processes user events, calls repositories

### Why
Android's original architecture suffered three problems:
1. **Configuration change instability**: Rotation destroys an Activity. Any in-progress network call, any loaded data — gone.
2. **Untestability**: An Activity that calls Firebase directly cannot be unit-tested without a running Firebase instance.
3. **Monolithic size**: Activities grew to 2,000+ lines because they were the only place to put logic.

ViewModel solves (1) — it survives rotation. Repository solving (2) — inject fake in tests. Layer separation solves (3) — logic distributes across ViewModel + Repository.

### How
```
┌───────────────────────────────────────────────┐
│  VIEW (Composable Function)                    │
│  val state by vm.uiState.collectAsState()      │
│  Button(onClick = { vm.doSomething() })        │
└──────────────────┬────────────────────────────┘
                   │ events (function calls)
                   ▼
┌───────────────────────────────────────────────┐
│  VIEWMODEL                                     │
│  private val _state = MutableStateFlow(...)   │
│  val state: StateFlow<UiState> = _state        │
│  fun doSomething() = viewModelScope.launch { } │
└──────────────────┬────────────────────────────┘
                   │ data (suspend calls / Flow)
                   ▼
┌───────────────────────────────────────────────┐
│  REPOSITORY                                    │
│  suspend fun getData(): Result<T>              │
│  fun getStream(): Flow<List<T>>                │
└──────────────────┬────────────────────────────┘
                   │
                   ▼
        Firebase / Room / In-Memory
```

### Tradeoffs

| Tradeoff | Description |
|---|---|
| Pro: Config survival | ViewModel outlives Activity rotation via ViewModelStore |
| Pro: Testability | Inject fake repository; test ViewModel with no Firebase |
| Pro: Compose fit | StateFlow → collectAsState() is the natural Compose pattern |
| Con: Boilerplate | Every screen needs ViewModel + UiState + Repository |
| Con: State complexity | Multiple StateFlows per ViewModel become hard to coordinate |

### Alternatives

**MVC:** Activity acts as controller. Does not survive rotation cleanly. Breaks at scale.  
**MVP:** Presenter holds logic; View is passive. Requires interfaces for every View. Verbose in Kotlin.  
**MVI:** Unidirectional data flow with a single state object and intent events. More predictable. Significantly more boilerplate. Better for complex screens; unnecessary for current StudyOS complexity.

### Production Improvement
Replace scattered `_saveResult: StateFlow<String?>` fields with a universal `UiState<T>`:

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

Apply to every ViewModel. The View renders `Loading` as a spinner, `Success` as content, `Error` as an error card. Consistent error handling across all screens.

### Rebuild Strategy
1. Create `FeatureUiState(...)` data class with all render fields
2. Create `FeatureViewModel : ViewModel()` with `MutableStateFlow<FeatureUiState>`
3. Implement user actions as `fun doX() = viewModelScope.launch { ... }`
4. Create `FeatureScreen` composable: calls `viewModel()`, passes lambdas to content
5. Create `FeatureContent` composable: takes state and callbacks — no ViewModel dependency

---

## 3.2 Repository Pattern

### What
Abstraction layer between ViewModel and data source. ViewModel asks for data. Repository decides where to get it, transforms it, and returns it.

### Why
Without a Repository, the ViewModel depends on Firebase directly. This is untestable, fragile, and couples business logic to infrastructure. With a Repository, swapping Firestore for Room or a REST API requires only changing the Repository.

### How — StudyOS Repositories

| Repository | Data Source | Access Pattern |
|---|---|---|
| `NoteRepoImpl` | Firebase Firestore | `callbackFlow` for read, `suspend` for write |
| `AuthRepository` | Firebase Auth | `suspend` functions returning `Result<Unit>` |
| `TaskRepository` | In-memory `mutableListOf` | Synchronous object singleton |
| `ProfileRepository` | Firestore + Firebase Auth | `suspend` functions returning `Result<Unit>` |

### The Dead Interface Problem
`NoteRepo.kt` defines:
```kotlin
interface NoteRepo {
    suspend fun addNote(note: NoteModel)
}
```

`NoteRepoImpl` does not implement this interface. The interface was created as a foundation for abstraction that was never completed. Effect: `NoteViewModel` directly instantiates `NoteRepoImpl()`, making test injection impossible without changing the ViewModel.

**Fix (Sprint 8):**
```kotlin
// 1. Complete the interface
interface NoteRepo {
    fun getNotes(): Flow<List<NoteModel>>
    suspend fun createNote(title: String, body: String, folder: String): Boolean
    suspend fun updateNote(note: NoteModel): Boolean
    suspend fun deleteNote(noteId: String)
    suspend fun createNoteAndReturnId(title: String, body: String, folder: String): String?
    suspend fun autoSaveNote(note: NoteModel): Boolean
}

// 2. Implement
class NoteRepoImpl : NoteRepo { ... }

// 3. Inject via parameter with default
class NoteViewModel(private val repo: NoteRepo = NoteRepoImpl()) : ViewModel()
```

---

## 3.3 Firebase Firestore

### What
NoSQL document database with real-time push updates. Data is organized as collections of documents with typed fields.

### Why

| Alternative | Pro | Con |
|---|---|---|
| Firebase Firestore ✓ | Real-time, offline, no server | Cost at scale, no full-text search |
| Room (SQLite) | Zero latency, offline-first | No cross-device sync |
| PostgreSQL (FastAPI) | Relational queries, joins | Requires running server |
| Firebase Realtime DB | Simpler JSON structure | No querying; all filtering client-side |

### Data Schema (Current)

```
Firestore
└── notes/
    └── {auto-id}/
        ├── id: String          ← same as document ID
        ├── title: String
        ├── body: String
        ├── folder: String
        ├── timestamp: Long     ← System.currentTimeMillis()
        └── userId: String      ← Firebase Auth UID
```

**Planned future collections (Sprint 7+):**
```
tasks/{taskId}/               ← migrate from in-memory TaskRepository
  id, title, description, startDate, endDate, priority,
  subjectId, subjectName, done, linkedNoteIds, userId

sessions/{sessionId}/         ← new: study session tracking
  id, userId, date, durationMinutes, subjectId

visionGoals/{goalId}/         ← migrate from in-memory VisionBoardViewModel
  id, userId, text, emoji, targetValue, subject, createdAt

users/{uid}/                  ← profile data
  displayName, email, createdAt
```

### Real-Time Read Pattern

```kotlin
fun getNotes(): Flow<List<NoteModel>> = callbackFlow {
    var notesListener: ListenerRegistration? = null
    val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        notesListener?.remove()
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) { trySend(emptyList()); return@AuthStateListener }
        notesListener = notesCollection
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                trySend(
                    snapshot?.documents
                        ?.mapNotNull { it.toObject(NoteModel::class.java) }
                        ?.sortedByDescending { it.timestamp }
                        ?: emptyList()
                )
            }
    }
    auth.addAuthStateListener(authListener)
    awaitClose {
        notesListener?.remove()
        auth.removeAuthStateListener(authListener)
    }
}
```

Three patterns working together:
1. `callbackFlow`: bridges Firestore's callback API to Kotlin Flow
2. `AuthStateListener`: makes the stream auth-aware — reattaches on sign-in, clears on sign-out
3. `awaitClose`: guarantees cleanup; without it the listener leaks and continues firing

### Write Pattern

```kotlin
suspend fun createNote(title: String, body: String, folder: String): Boolean {
    val userId = auth.currentUser?.uid ?: return false   // auth guard — never writes without owner
    val id = notesCollection.document().id               // pre-generate ID locally
    val note = NoteModel(id=id, title=title, body=body,
                         folder=folder, timestamp=System.currentTimeMillis(), userId=userId)
    return try {
        notesCollection.document(id).set(note).await()   // suspend until Firestore confirms
        true
    } catch (e: Exception) { false }
}
```

**Why pre-generate the ID:** enables the model to carry its own identity before the database confirms the write. Enables autosave duplicate prevention. Enables optimistic UI updates. Works offline (Firestore SDK generates the ID locally).

### Security Gap (BUG-010)

No Security Rules are configured. Every authenticated user can read every other user's notes. Required rules before production:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /notes/{noteId} {
      allow read, update, delete: if request.auth != null
                                  && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null
                    && request.auth.uid == request.resource.data.userId;
    }
  }
}
```

---

## 3.4 Compose Navigation

### What
Jetpack Compose Navigation provides a declarative navigation system where each screen is a composable registered as a destination in a `NavHost`. `NavHostController` manages the back stack.

### How — AppRoutes Sealed Class

```kotlin
sealed class AppRoutes(val route: String) {
    object Auth        : AppRoutes("auth")
    object Login       : AppRoutes("login")
    object SignUp      : AppRoutes("signup")
    object Home        : AppRoutes("home")
    object Study       : AppRoutes("study")
    object Focus       : AppRoutes("focus")
    object Plan        : AppRoutes("plan")
    object Profile     : AppRoutes("profile")
    object Notes       : AppRoutes("notes")
    object Analytics   : AppRoutes("analytics")
    object Flashcards  : AppRoutes("flashcards")
    object BrainGame   : AppRoutes("braingame")
    object VideoNotes  : AppRoutes("videonotes")
    object MockTest    : AppRoutes("mocktest")
    object VisionBoard : AppRoutes("visionboard")
    object Pomodoro    : AppRoutes("pomodoro")
    object Settings    : AppRoutes("settings")
}
```

Sealed class prevents typos: `AppRoutes.Notes.route` is type-checked. `"notse"` is not.

### Dual Navigation System

StudyOS has two navigation systems running simultaneously:

**System A (primary):** Compose NavGraph via `StudyOSNavGraph`  
**System B (legacy):** Direct `Intent` calls from `DashboardActivity`

They do not share a back stack. When a user navigates via System B, the NavGraph does not know about it. The bottom nav's selected state becomes incorrect. Sprint 8 must consolidate all navigation to System A.

### Auth Navigation Pattern

After successful login:
```kotlin
navController.navigate(AppRoutes.Home.route) {
    popUpTo(AppRoutes.Auth.route) { inclusive = true }
}
```

`popUpTo(Auth) { inclusive = true }` removes the Auth screen from the back stack. The user cannot press Back from Home to return to the login screen.

### Conditional Bottom Nav

```kotlin
// In StudyOSNavGraph Scaffold:
val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
bottomBar = {
    if (currentRoute in bottomNavRoutes) StudyOSBottomNav(navController)
}
```

`bottomNavRoutes = setOf("home", "study", "focus", "plan", "profile")`. Sub-screens (Notes editor, Settings, Auth screens) hide the bottom nav.

---

## 3.5 Kotlin Coroutines

### What
Kotlin's concurrency model. Allows asynchronous code to be written in sequential style using `suspend` functions, `launch`/`async` builders, and structured concurrency.

### Why
Firebase's Kotlin SDK provides `.await()` extension that converts Firebase's `Task<T>` to a coroutine-based suspension:

```kotlin
// Without coroutines:
firestore.document(id).set(note)
    .addOnSuccessListener { /* success */ }
    .addOnFailureListener { /* failure */ }

// With coroutines:
try {
    firestore.document(id).set(note).await()
    // success
} catch (e: Exception) {
    // failure
}
```

Sequential reads are natural. Error handling is try/catch. No nested callbacks.

### viewModelScope Contract

Every ViewModel coroutine uses `viewModelScope`:
```kotlin
fun createNote(...) = viewModelScope.launch { repo.createNote(...) }
```

`viewModelScope` is automatically cancelled when the ViewModel is cleared (user navigates away). Any pending Firestore write is abandoned (Firestore will complete it offline). Without this, coroutines outlive their screen and cause memory leaks.

### Job Cancellation Debounce

```kotlin
private var autoSaveJob: Job? = null

fun onEditorChanged(...) {
    autoSaveJob?.cancel()              // cancel previous pending save
    autoSaveJob = viewModelScope.launch {
        delay(1200)                    // cancellable suspension point
        autoSaveDraft(...)             // only runs if not cancelled
    }
}
```

`delay()` is cancellable. When `autoSaveJob?.cancel()` is called, the coroutine is interrupted at `delay()`. `CancellationException` is caught internally. The save never happens.

### callbackFlow Pattern

```kotlin
fun stream(): Flow<Data> = callbackFlow {
    val listener = api.listen { data -> trySend(data) }
    awaitClose { api.removeListener(listener) }
}
```

`trySend` (not `send`): non-blocking; fails silently if the channel cannot accept values.  
`awaitClose`: guaranteed cleanup when the collector cancels or its scope is destroyed.

---

# PART 4 — FEATURE DEEP DIVES

## Feature: Authentication

```
Feature:     Authentication (Email/Password + Google SSO)
Status:      Implemented
Sprint:      Sprint 3
Owner:       Ayush Kumar Raut
Architecture: MVVM + AuthRepository + Firebase Auth + GoogleSignInHelper
Reason:      User identity required for data privacy and Firestore user-scoping
```

### What
Authentication proves who the user is. In StudyOS it also scopes all data — notes, tasks, session records — to a specific uid. Without authentication there is no data privacy and no user-scoped Firestore queries.

### Why Firebase Auth
Custom auth requires: a server, HTTPS, JWT generation/validation, token refresh, secure storage, password hashing, email verification, and session management. Firebase Auth provides all of this as a managed service. Zero server operations. Free at this usage level.

### How

**Email/Password flow:**
```
LoginBody.onLoginClick(email, password)
    ↓ AuthViewModel.login(email, password)
      _authState = Loading
      viewModelScope.launch {
          val result = repository.login(email, password)
          _authState = result.fold({ Success }, { Error(it.message) })
      }
    ↓ AuthRepository.login(email, password)
      return runCatching { auth.signInWithEmailAndPassword(email, password).await() }
    ↓ LaunchedEffect(authState) in LoginBody
      if Success → navigate(Home) { popUpTo(Auth) { inclusive=true } }
      if Error   → show errorMessage
```

**Google SSO flow:**
```
User taps Google button
    ↓ launcher.launch(GoogleSignInHelper.getSignInIntent(context))
    ↓ Google account picker dialog
    ↓ launcher callback: GoogleSignIn.getSignedInAccountFromIntent(data)
    ↓ GoogleAuthProvider.getCredential(account.idToken, null)
    ↓ AuthViewModel.signInWithCredential(credential)
    ↓ AuthRepository: auth.signInWithCredential(credential).await()
    ↓ same AuthState flow as email/password
```

### AuthState Machine
```
Idle ──loginCalled──→ Loading ──success──→ Success ──[navigate]──→ idle on next screen
  ↑                      │
  └──────────────────────┘
                     failure → Error(msg) ──user retries──→ Idle
```

### Edge Cases

| Case | Handling | Gap |
|---|---|---|
| Wrong password | AuthState.Error with Firebase message | Firebase error messages are not user-friendly |
| No network | runCatching catches; Error state | No distinction between wrong password and network error |
| Google cancelled | ApiException caught; Toast | Toast not consistent with app's error pattern |
| Google Play Services absent | getSignInIntent throws | Not handled; Huawei devices will crash |
| Email not verified | Firebase allows login | No verification gate implemented |

### Future Improvements (Sprint 8)
1. Email verification: block dashboard until `currentUser.isEmailVerified`
2. "Forgot password": `auth.sendPasswordResetEmail(email).await()`
3. Re-authentication for password change (fixes BUG-011)
4. Biometric login via `BiometricPrompt`

### Dependency Graph
```
Authentication (userId established)
      ↓
NoteRepoImpl.getNotes()      ← needs userId for whereEqualTo filter
      ↓
NoteViewModel.notes          ← StateFlow of user's notes
      ↓
NotesScreen                  ← renders user's notes

Authentication
      ↓
DashboardViewModel           ← reads currentUser.displayName for greeting
      ↓
DashboardBody                ← shows "Good morning, Ayush"
```

### Rebuild From Memory
1. `AuthRepository`: `suspend fun login(e,p): Result<Unit> = runCatching { auth.signInWithEmailAndPassword(e,p).await() }` · `suspend fun signUp(e,p): Result<Unit>` · `suspend fun signInWithCredential(c): Result<Unit>`
2. `sealed class AuthState` — Idle, Loading, Success, Error(msg)
3. `AuthViewModel`: `val authState: StateFlow<AuthState>` · `fun login(e,p) = viewModelScope.launch { _authState=Loading; val r=repo.login(e,p); _authState=r.fold({Success},{Error(it.message)}) }`
4. `LoginBody`: two `OutlinedTextField`, one `Button`, `LaunchedEffect(authState)` navigates on Success
5. Google: `rememberLauncherForActivityResult(StartActivityForResult())` · parse `GoogleSignIn.getSignedInAccountFromIntent()` · build credential · call `vm.signInWithCredential(credential)`
6. `MainActivity`: if `FirebaseAuth.getInstance().currentUser != null` → startDestination = Home else Auth

---

## Feature: Notes CRUD

```
Feature:     Note Management (Create, Read, Update, Delete)
Status:      Implemented
Sprint:      Sprint 4
Owner:       Ayush Kumar Raut
Architecture: MVVM + NoteRepoImpl + Firestore callbackFlow
Reason:      Notes are the central data entity; all study features derive from notes
```

### What
Notes are the core data type. Each note has a title, body, folder, timestamp, and userId. Stored in Firestore, synced in real-time. The UI provides folder-based organization, search, and a full editor.

### Why Notes Are Central
Every feature — quiz generation, flashcard creation, video-to-notes, task linking — uses a note as either the input or the output. Notes are not just a feature. They are the data layer through which all features interact.

### How — Data Model

```kotlin
data class NoteModel(
    val id: String = "",          // Firestore document ID; default "" for toObject() compat
    val title: String = "",
    val body: String = "",
    val folder: String = "Science", // default folder reduces friction on first note
    val timestamp: Long = 0L,     // System.currentTimeMillis() at last save
    val userId: String = ""       // set by repo, never by UI
)
```

`userId` is always set in the repository from `auth.currentUser?.uid`. The UI cannot accidentally write a note without an owner.

### How — Composable Architecture

```
NotesScreen              ← ViewModel holder; passes lambdas; state collection
    ↓
NotesScreenContent       ← stateful: showEditor, selectedNote, searchQuery, activeFolder
    ↓
CreateEditNoteScreen     ← pure composable: receives all state via parameters and lambdas
    ↓
NoteCard                 ← pure: displays one note, emits click events
```

State hoisting principle: state is elevated to the lowest common ancestor that needs it. `showEditor` lives in `NotesScreenContent` because no sibling above it needs it.

### Folder System

Folders are a UX concept, not a Firestore concept. The UI maintains:
```kotlin
val defaultFolders = remember { listOf("Science", "Social", "English") }
var extraFolders by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
var activeFolder by rememberSaveable { mutableStateOf(defaultFolders.first()) }
```

`rememberSaveable` survives rotation. Does not survive process death. Extra folders are lost when app is killed. **Fix (Sprint 7):** persist folder names to Firestore.

### Search

```kotlin
val visibleNotes = if (searchQuery.isNotEmpty()) {
    allNotes.filter { it.title.contains(searchQuery, ignoreCase = true) ||
                      it.body.contains(searchQuery, ignoreCase = true) }
} else {
    allNotes.filter { it.folder == activeFolder }
}
```

When searching, folder filter is bypassed — correct UX.

### Tradeoffs

| Tradeoff | Description |
|---|---|
| All notes in memory | stateIn() keeps all notes in ViewModel; fast but limited at 1000+ notes |
| Client-side sort | sortedByDescending { timestamp } client-side; avoids Firestore composite index requirement |
| No pagination | All notes fetched at once; must add .limit(50) + cursor for 1000+ notes |
| Folder as string | Renaming a folder requires updating every note in it |

### Dependency Graph
```
Notes (NoteModel in Firestore)
      ↓
Auto-Save Notes       — saves NoteModel automatically while editing
Link Notes to Tasks   — Task.linkedNoteIds references NoteModel.id
Video to Notes        — generates NoteModel from video  [Sprint 9]
Quiz Generation       — uses NoteModel.body as source  [Sprint 9]
Flashcard Creation    — uses NoteModel.body as source  [Sprint 10]
```

### Rebuild From Memory
1. `NoteModel(id, title, body, folder, timestamp, userId)` — all defaults for Firestore deserialization
2. `NoteRepoImpl`: Firestore + Auth refs; `getNotes()` as callbackFlow; five write `suspend` funs
3. `NoteViewModel`: `notes: StateFlow` via `stateIn()`; CRUD funs in `viewModelScope.launch`; `_saveResult: MutableStateFlow<String?>`
4. `NotesScreen` → `NotesScreenContent` → `CreateEditNoteScreen` — each with clear state ownership
5. `CreateEditNoteScreen`: two `OutlinedTextField`, folder `FilterChip` row, save `Button` (enabled when `title.isNotBlank()`)

---

## Feature: Auto-Save Notes

```
Feature:     Auto-Save Notes
Status:      Implemented
Sprint:      Sprint 4
Owner:       Ayush Kumar Raut
Architecture: Coroutine Job Cancellation Debounce + NoteRepoImpl + AutoSaveStatus enum
Reason:      Prevent note loss; eliminate cognitive overhead of manual saving
```

### What
Every keystroke starts a 1200ms countdown. If the user pauses for 1.2 seconds, the note is saved to Firestore. A subtle status indicator ("Saving...", "Saved", "Save failed") appears in the editor header.

### Why 1200ms

**Why not every keystroke:** Firestore charges per write. At 5 keystrokes/second for a 30-minute session = 9,000 write operations. Daily free tier: 20,000. One typing session nearly exhausts the daily quota.

**Why not on focus loss only:** If the app crashes while the user is typing, all work since last focus change is lost.

**Why 1200ms:** Human typing has natural pauses after sentences and clauses (500–1000ms). 1200ms catches the pause after a complete thought. Below 800ms saves mid-word. Above 2000ms feels unsafe.

### How

```
KEYSTROKE
    ↓
autoSaveJob?.cancel()     ← discard previous pending save
autoSaveJob = viewModelScope.launch {
    delay(1200)           ← cancelled if next keystroke arrives within 1200ms
    autoSaveDraft(title, body, folder)
}

(1200ms of silence elapses)
    ↓
autoSaveDraft(title, body, folder)
    ↓
if (title.isBlank() && body.isBlank()) return   ← nothing to save
_autoSaveStatus = SAVING
    ↓
if (_currentEditingNoteId != null)
    → repo.autoSaveNote(NoteModel(id=noteId, ...))
else
    → repo.createNoteAndReturnId(...) → store returned id in _currentEditingNoteId
    ↓
_autoSaveStatus = SAVED | FAILED
if SAVED: delay(3000) → _autoSaveStatus = IDLE (if still SAVED)
```

### Identity Management

`_currentEditingNoteId` prevents duplicate Firestore documents:

| Scenario | `_currentEditingNoteId` before save | Action |
|---|---|---|
| New note, 1st autosave | `null` | createNoteAndReturnId() → store returned ID |
| New note, 2nd+ autosave | `"abc123"` | autoSaveNote(NoteModel(id="abc123", ...)) |
| Editing existing note | Set from `existingNote.id` at editor open | autoSaveNote(NoteModel(id=existingId, ...)) |

Setting logic in `onEditorChanged`:
```kotlin
if (noteId != null && _currentEditingNoteId.value == null) {
    _currentEditingNoteId.value = noteId  // set once; never overwritten
}
```

Only set once — prevents a fast second keystroke from overwriting the autosave-created ID with null.

### Anti-Duplicate on Manual Save

When autosave creates a note, the manual Save button must update (not duplicate) it:
```kotlin
// In NotesScreenContent.onSave:
val autoCreatedId = currentEditingNoteId   // from viewModel.currentEditingNoteId.collectAsState()
when {
    selectedNote != null   → onUpdateNote(selectedNote!!.copy(title=title, body=body, folder=folder))
    autoCreatedId != null  → onUpdateNote(NoteModel(id=autoCreatedId, title=title, body=body, folder=folder))
    else                   → onCreateNote(title, body, folder)
}
```

Three cases cover all save scenarios without duplication.

### Status Display

```kotlin
if (autoSaveStatus != AutoSaveStatus.IDLE) {
    Text(
        text = when (autoSaveStatus) {
            SAVING → "Saving..."
            SAVED  → "Saved"
            FAILED → "Save failed"
            IDLE   → ""
        },
        color = when (autoSaveStatus) {
            FAILED → Color(0xFFFFCDD2)
            else   → Color.White.copy(alpha = 0.7f)
        },
        fontSize = 11.sp,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.End
    )
}
```

Right-aligned, 70% white, 11sp. The design principle: visible when the user looks for it; never distracting.

### Tradeoffs

| Tradeoff | Description |
|---|---|
| 1200ms loss window | Last 1.2 seconds of typing unsaved if process dies mid-delay |
| No retry on failure | FAILED status shown; user must manually retry |
| Full document writes | Every autosave writes the full NoteModel even if one character changed |
| Memory for ID | _currentEditingNoteId held in ViewModel; cleared by clearEditingNote() |

### Future Improvements
1. Retry with exponential backoff on FAILED: `delay(2000)`, retry, `delay(4000)`, retry
2. "Last saved at 14:32" instead of just "Saved"
3. Local Room backup as secondary write: Room first (synchronous), Firestore async queue
4. Diff-based saves: hash comparison, skip write if unchanged

### Dependency Graph
```
Auto-Save Notes
      ↓
Notes (NoteModel stored in Firestore)
      ↓
Link Notes to Tasks
      ↓
Dashboard (today's tasks referencing notes)
```

### Rebuild From Memory
1. `enum class AutoSaveStatus { IDLE, SAVING, SAVED, FAILED }`
2. ViewModel: `_currentEditingNoteId: MutableStateFlow<String?> = null`, `_autoSaveStatus: MutableStateFlow = IDLE`, `var autoSaveJob: Job? = null`
3. `onEditorChanged(noteId?, title, body, folder)`: set ID once; cancel+relaunch job with `delay(1200)`
4. `private suspend fun autoSaveDraft(...)`: blank guard; set SAVING; branch new/existing; set SAVED/FAILED; delay 3000 then reset IDLE
5. `clearEditingNote()`: cancel job; reset both StateFlows
6. `NoteRepoImpl`: `createNoteAndReturnId()` returns `String?`; `autoSaveNote()` copies userId+timestamp
7. `NotesScreen`: collect `autoSaveStatus` + `currentEditingNoteId`; pass `onAutoSave` + `onEditorClosed` lambdas
8. `CreateEditNoteScreen`: call `onAutoSave` on every field change; render status text when not IDLE

---

## Feature: Task Management

```
Feature:     Task Management (CRUD + Priority + Calendar + Note Linking)
Status:      Implemented (in-memory — needs Firestore migration Sprint 7)
Sprint:      Sprint 3
Owner:       Ayush Kumar Raut
Architecture: MVVM + in-memory TaskRepository object + Compose Calendar + LocalDate
Reason:      Students need to track study commitments and deadlines
```

### What
Tasks represent study commitments. They have priorities (HIGH/MEDIUM/LOW), subject associations, date ranges, a completion flag, and links to notes.

### How

**Task model:**
```kotlin
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,           // null = single-day event
    val priority: Priority,
    val subjectId: String,
    val subjectName: String,
    val done: Boolean = false,
    val linkedNoteIds: List<String> = emptyList()
) {
    fun isOverdue(): Boolean {
        val deadline = endDate ?: startDate
        return deadline.isBefore(LocalDate.now()) && !done
    }
}
```

**Priority color mapping:**
```
HIGH   → Color(0xFFE53935)  Red
MEDIUM → Color(0xFFC97B00)  Amber
LOW    → Color(0xFF1B7A3E)  Green
```

**Calendar — key implementation calculations:**
```kotlin
val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
val firstDayOffset = LocalDate.of(year, month, 1).dayOfWeek.value % 7  // 0 = Sunday
```

**Note-Task linking:**
```
Attach:         TaskRepository.attachNoteToTask(taskId, noteId)
                → task.copy(linkedNoteIds = task.linkedNoteIds + noteId)

Note → Tasks:   TaskRepository.getTasksForNote(noteId)
                → remoteTaskStore.filter { noteId in it.linkedNoteIds }

Task → Notes:   TaskRepository.getNoteIdsForTask(taskId)
                → task.linkedNoteIds
```

### Bug — BUG-001
`DashboardViewModel` contains `private val taskRepository = TaskRepository()`. `TaskRepository` is a Kotlin `object` singleton. Object singletons cannot be constructed with `()`. This is a compile error. Fix: `private val taskRepository = TaskRepository`.

### Tradeoffs

| Tradeoff | Description |
|---|---|
| In-memory loss | Tasks lost on process death — critical UX gap |
| Object singleton | DI/testing impossible; must refactor to class for Firestore migration |
| No Firestore | Cannot access tasks from a second device |
| Note links in-memory | Links lost with tasks on process death |

### Sprint 7 Migration Path
```kotlin
// 1. Add userId field to Task
data class Task(... val userId: String = "")

// 2. Create TaskRepositoryImpl (class, not object)
class TaskRepositoryImpl {
    private val tasksCollection = db.collection("tasks")
    fun getTasks(): Flow<List<Task>> = callbackFlow { /* same pattern as NoteRepoImpl */ }
    suspend fun insertTask(task: Task): Boolean { ... }
    suspend fun updateTask(task: Task): Boolean { ... }
    suspend fun deleteTask(taskId: String) { ... }
    suspend fun attachNoteToTask(taskId: String, noteId: String): Boolean { ... }
    fun getTasksForNote(noteId: String): Flow<List<Task>> { ... }
}

// 3. Update PlanViewModel constructor
class PlanViewModel(private val repo: TaskRepositoryImpl = TaskRepositoryImpl()) : ViewModel()
```

### Rebuild From Memory
1. `Priority` enum + `Task` data class with all fields; `isOverdue()` method in model
2. `TaskRepository` object: `mutableListOf<Task>()` backing store; full CRUD + attach/detach/query by note
3. `PlanViewModel`: `tasks = mutableStateListOf()`, form fields as `mutableStateOf`, `handleAddOrUpdateTask()`, `syncFromRepository()`
4. `PlanBody`: month calendar grid, task LazyColumn filtered by selected day, priority chips, FAB, BottomSheet form
5. Note picker: `showNotePicker` flag; notes list from `NoteViewModel.notes`; tap to call `planVm.attachNote(noteId)`

---

## Feature: Pomodoro Timer

```
Feature:     Pomodoro Timer (Focus + Short Break + Long Break + Configurable Durations)
Status:      Implemented
Sprint:      Sprint 3
Owner:       Ayush Kumar Raut
Architecture: PomodoroViewModel + Android CountDownTimer + Compose Canvas arc
Reason:      Structured focus sessions improve study effectiveness and feed analytics
```

### What
The Pomodoro Technique alternates 25-minute focus blocks with 5-minute short breaks and 15-minute long breaks after every 4 sessions. StudyOS implements a configurable version: users set custom durations with sliders.

### Why CountDownTimer vs Coroutine Timer

`CountDownTimer` fires `onTick()` on the main thread — safe for StateFlow updates without `withContext(Dispatchers.Main)`. It handles device sleep states correctly. No dependencies.

A coroutine `while(running) { delay(1000); tick() }` is simpler code but accumulates drift over long durations. For a 25-minute timer, drift is perceptible by minute 20.

### How

**State machine:**
```
IDLE (timer stopped, timeRemaining = totalSeconds)
    ↓ toggleTimer() → startTimer()
RUNNING (countDownTimer.start(), isRunning = true)
    ↓ onTick(ms) → _timeRemaining = ms/1000
RUNNING (updated every second)
    ↓ onFinish() or toggleTimer() → pauseTimer()
IDLE (_sessionsToday += 1, timeRemaining reset to totalSeconds)
```

**Resume after pause:** `startTimer()` creates a new `CountDownTimer(_timeRemaining.value * 1000, 1000)`. `_timeRemaining` retained its value during pause. The timer resumes from where it was paused.

**Circular arc:**
```kotlin
val timerProgress by animateFloatAsState(
    targetValue = timeRemaining.toFloat() / totalSeconds.toFloat(),
    animationSpec = tween(durationMillis = 500)
)
Canvas(modifier = Modifier.size(240.dp)) {
    drawArc(color = StudyPurpleLight, startAngle = -90f, sweepAngle = 360f,
            useCenter = false, style = Stroke(width = 12.dp.toPx()))   // background track
    drawArc(color = StudyPurple, startAngle = -90f,
            sweepAngle = timerProgress * 360f, useCenter = false,
            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)) // progress
}
```

`animateFloatAsState` smoothly interpolates between tick values. The arc does not jump every second — it flows.

### Three-Timer Problem

Three separate timer implementations:
1. `PomodoroViewModel` in `FocusScreen` — canonical
2. `DashboardViewModel` mini timer — duplicate
3. `PomodoroActivity.PomodoroBody` — legacy Activity

**Sprint 8 fix:** Remove mini timer from DashboardViewModel; replace with a navigation button to the Focus tab. Remove `PomodoroActivity`; replace usages with NavGraph navigation to `AppRoutes.Focus.route`.

### Tradeoffs

| Tradeoff | Description |
|---|---|
| Process death | Timer state lost if app is killed; no ForegroundService |
| No notifications | Timer completion is silent when app is in background |
| sessionsToday in-memory | Resets on every app restart |
| Duplicate code | Three implementations; Sprint 8 consolidation required |

### Future Improvements (Sprint 7)
1. `ForegroundService` to keep timer alive in background
2. `NotificationCompat` persistent notification showing remaining time
3. Wire `onFinish()` to `StudySessionRepository.recordSession()` for real analytics
4. Auto-switch to long break after 4 sessions (classic Pomodoro cycle)

### Dependency Graph
```
Pomodoro Timer (onFinish)
      ↓ [Sprint 7 wire]
StudySessionRepository.recordSession(date, duration, subject)
      ↓
Firestore: sessions/{uid}/{sessionId}
      ↓ [Sprint 8 aggregation]
AnalyticsRepository.computeStreak()
AnalyticsRepository.computeWeeklyHours()
      ↓
AnalyticsViewModel._uiState updated with real data
      ↓
ProgressActivity / DashboardBody shows real numbers
```

### Rebuild From Memory
1. `PomodoroTab` enum: FOCUS, SHORT_BREAK, LONG_BREAK
2. `PomodoroViewModel`: StateFlow for tab, durations, isRunning, sessionsToday, timeRemaining
3. `startTimer()`: create `CountDownTimer(timeRemaining*1000, 1000)`, `onTick` updates, `onFinish` increments+resets
4. `pauseTimer()`: `countDownTimer?.cancel()`; isRunning=false; timeRemaining retained
5. `selectTab()`: return if running; update tab; reset timer
6. `FocusScreen`: collect all StateFlows; compute timerProgress; animate; draw Canvas arc; play/pause button

---

## Feature: Analytics and Progress Tracking

```
Feature:     Analytics (Streak, Hours, Charts, Heatmap)
Status:      Partially Implemented (UI complete; all data hardcoded)
Sprint:      Sprint 3 (UI) + Sprint 8 (real data)
Owner:       Ayush Kumar Raut
Architecture: AnalyticsViewModel + Canvas charts + planned StudySessionRepository
Reason:      Students must measure progress to stay motivated and identify gaps
```

### What
Analytics shows streak count, monthly study hours, quiz accuracy, focus score, a weekly hours bar chart, subject breakdown, quiz score trend, and a study activity heatmap.

### Current State

Every value in `AnalyticsViewModel` is hardcoded:
```kotlin
val studyStreak: Int = 14       // never changes
val monthlyHours: Float = 36.5f // never changes
val quizAccuracy: Float = 0.78f // never changes
val focusScore: Int = 72        // never changes
```

The charts look real. The data is not. This is the most misleading gap in the application.

### Why Static for Sprint 3

Building analytics UI before real data exists is correct practice: validates the information architecture, establishes visual design, allows early demos. The cost: if replacement is not scheduled, static data becomes permanent.

### How — Custom Canvas Charts (No Third-Party Library)

All charts use Compose `Canvas`. Choice: full visual control, no dependency management, bar charts are straightforward with Canvas math.

**Weekly bar chart example:**
```kotlin
Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
    val maxVal = data.maxOf { it.hours }
    val barW = size.width / (data.size * 2)
    data.forEachIndexed { i, item ->
        val barH = (item.hours / maxVal) * size.height * 0.8f
        val x = (i * 2 + 0.5f) * barW
        drawRoundRect(color = StudyPurple, topLeft = Offset(x, size.height - barH),
                      size = Size(barW, barH), cornerRadius = CornerRadius(4.dp.toPx()))
    }
}
```

### Sprint 8 Real Analytics Path

```
1. Create StudySessionModel(id, userId, date, durationMinutes, subjectId)
2. Create StudySessionRepository — same Firestore pattern as NoteRepoImpl
3. Wire PomodoroViewModel.onFinish() → sessionRepo.recordSession()
4. Create AnalyticsRepository:
   fun computeStreak(sessions): Int        — consecutive days with sessions
   fun computeWeeklyHours(sessions): List<WeeklyBarData>
   fun computeSubjectBreakdown(sessions): List<SubjectBreakdownItem>
5. AnalyticsViewModel injects AnalyticsRepository
   init { analyticsRepo.stream().collect { _uiState.value = it } }
```

**Flexible streak algorithm:**
```kotlin
fun computeFlexibleStreak(sessionDates: Set<LocalDate>): Int {
    var streak = 0
    var graceDayUsed = false
    var current = LocalDate.now()
    val sorted = sessionDates.sortedDescending()
    for (date in sorted) {
        val gap = ChronoUnit.DAYS.between(date, current).toInt()
        when {
            gap == 0 → { streak++; current = date.minusDays(1) }
            gap == 1 && !graceDayUsed → { graceDayUsed = true; current = date.minusDays(1) }
            gap == 1 && graceDayUsed  → { streak++; graceDayUsed = false; current = date.minusDays(1) }
            else → break
        }
    }
    return streak
}
```

Miss 1 day = grace (streak continues). Miss 2 consecutive days = streak resets.

### Heatmap

`ProgressViewModel.heatData: Array<IntArray> = Array(4) { IntArray(36) { Random.nextInt(0, 5) } }` — currently random.

Should be: `sessions.groupBy { it.date }.mapValues { min(it.value.size, 4) }` mapped to a 4×36 grid (≈18 weeks). `HeatL0`–`HeatL4` colors from `Color.kt` map to activity levels 0–4.

---

## Feature: Vision Board

```
Feature:     Vision Board
Status:      Partially Implemented (in-memory; needs Firestore Sprint 7)
Sprint:      Sprint 5
Owner:       Ayush Kumar Raut
Architecture: VisionBoardViewModel + in-memory MutableStateFlow
Reason:      Motivational layer; implementation intentions improve goal follow-through
```

### What
A personal goal collection displayed on the dashboard and dedicated screen. Each goal has a description, emoji, target value, and subject.

### How

```
VisionBoardViewModel
  _pinnedGoals: MutableStateFlow<List<VisionGoalModel>>
  pinGoal(goal: VisionGoalModel)    — adds to list
  removeGoal(goal: VisionGoalModel) — removes from list
```

`VisionGoalModel(text, emoji, targetValue, subject)` — simple data class.

`VisionBoardBody`: emoji input + text fields + subject dropdown + save button; `LazyColumn` of goal cards.

`VisionBoardWidget` in dashboard: shows first 3 goals as compact cards.

### Sprint 7 Fix
```kotlin
class VisionBoardRepository {
    fun getGoals(): Flow<List<VisionGoalModel>> = callbackFlow { /* Firestore pattern */ }
    suspend fun saveGoal(goal: VisionGoalModel): Boolean { ... }
    suspend fun deleteGoal(goalId: String) { ... }
}
```

---

## Feature: Profile Management

```
Feature:     Profile Management (Display Name + Password Change)
Status:      Implemented
Sprint:      Sprint 6
Owner:       Ayush Kumar Raut
Architecture: MVVM + ProfileRepository + Firebase Auth + Firestore users collection
Reason:      Users must own their identity; personalization builds trust
```

### How

`ProfileRepository.updateDisplayName()` writes to both places:
```kotlin
// Firebase Auth profile (provides currentUser.displayName)
val req = userProfileChangeRequest { displayName = newName }
auth.currentUser!!.updateProfile(req).await()

// Firestore (queryable; used in dashboard greeting)
db.collection("users").document(userId).update("displayName", newName).await()
```

Both writes must succeed for display name to be consistent.

**Password change gap (BUG-011):**
`auth.currentUser!!.updatePassword(newPassword).await()` throws `FirebaseAuthRecentLoginRequiredException` if the session is too old. The UI must catch this and re-authenticate the user before proceeding. Not currently handled.

---

## Feature: Settings

```
Feature:     Settings (Notifications, Dark Mode, Sign Out)
Status:      Partially Implemented (UI toggles; not persisted; dark mode no-op)
Sprint:      Sprint 6
Owner:       Ayush Kumar Raut
Architecture: SettingsViewModel + in-memory StateFlow
Reason:      User control over app behavior
```

### Two Gaps

**Gap 1: Settings not persisted**  
Every toggle resets to default on restart. Fix: `AndroidViewModel` with `Application` parameter; read/write `SharedPreferences` on toggle.

**Gap 2: Dark mode toggle has no effect**  
`StudyOSTheme` in `Theme.kt` ignores `SettingsViewModel.darkMode`. Fix:
```kotlin
// StudyOSNavGraph.kt:
val darkMode by settingsVm.darkMode.collectAsState()
StudyOSTheme(darkTheme = darkMode) { NavHost(...) }

// Theme.kt:
@Composable
fun StudyOSTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme, content = content)
}
```

---

# PART 5 — SYSTEM RELATIONSHIPS

## Feature Dependency Graph

```
              ┌────────────────────────────────────────────────┐
              │               AUTHENTICATION                    │
              │   (userId = key for all other features)         │
              └─────────────────────┬──────────────────────────┘
                                    │
              ┌─────────────────────▼──────────────────────────┐
              │                   NOTES                         │
              │       (NoteModel = central data entity)         │
              └──┬──────────────┬──────────────┬───────────────┘
                 │              │              │
          ┌──────▼──┐     ┌─────▼─────┐  ┌────▼──────────────┐
          │  AUTO   │     │   TASKS   │  │   VIDEO TO NOTES  │
          │  SAVE   │     │  linking  │  │   [Sprint 9]      │
          └─────────┘     └─────┬─────┘  └───────────────────┘
                                │
                         ┌──────▼──────┐
                         │  CALENDAR   │
                         │    VIEW     │
                         └──────┬──────┘
                                │
                         ┌──────▼──────┐
                         │  DASHBOARD  │
                         └──────┬──────┘
                                │
              ┌─────────────────▼───────────────────────┐
              │             ANALYTICS                    │
              │  streak · hours · accuracy · heatmap     │
              └──────┬────────────────────┬─────────────┘
                     │                    │
              ┌──────▼──────┐     ┌───────▼──────┐
              │   POMODORO  │     │   VISION     │
              │    TIMER    │     │   BOARD      │
              └─────────────┘     └──────────────┘
```

## Critical Missing Wire

```
Pomodoro.onFinish()
      ↓  ← THIS WIRE IS MISSING
StudySessionRepository.recordSession(date, duration, subject)
      ↓
Firestore: sessions collection
      ↓
AnalyticsRepository: streak, hours, breakdown
      ↓
AnalyticsViewModel: real data
      ↓
Every analytics-displaying screen updates with real numbers
```

This one wire (connecting Pomodoro to a session repository) unlocks real streak, real weekly hours, real focus score, and real heatmap data. It is the highest-leverage single change in the Sprint 8 backlog.

---

# PART 6 — KNOWN BUGS AND TECHNICAL DEBT

| ID | Bug | File | Severity | Fix |
|---|---|---|---|---|
| BUG-001 | `TaskRepository()` called as constructor on object singleton | `DashboardViewModel.kt` | High (compile error) | `TaskRepository` not `TaskRepository()` |
| BUG-002 | `NoteRepo` interface unused — not implemented by `NoteRepoImpl` | `NoteRepo.kt` | Medium | Implement or delete |
| BUG-003 | Dark mode toggle has no UI effect | `SettingsViewModel.kt` / `Theme.kt` | Medium | Wire StateFlow to StudyOSTheme |
| BUG-004 | Duplicate brain game files | `BrainGame.kt` / `BrainGameActivity.kt` | Low | Consolidate |
| BUG-005 | Dashboard Notes button shows "Coming soon" toast | `DashboardActivity.kt` | High | Navigate to AppRoutes.Notes.route |
| BUG-006 | Duplicate bottom nav in DashboardActivity | `DashboardActivity.kt` | Medium | Remove DashboardBottomNav |
| BUG-007 | All analytics data hardcoded | `AnalyticsViewModel.kt` | High | Connect to StudySessionRepository in Sprint 8 |
| BUG-008 | In-memory tasks lost on process death | `TaskRepository.kt` | Critical | Migrate to Firestore in Sprint 7 |
| BUG-009 | `default_web_client_id` placeholder in strings.xml | `strings.xml:61` | Critical | Replace with Firebase Console value |
| BUG-010 | No Firestore Security Rules | Firebase Console | Critical | Configure read/write rules scoped to userId |
| BUG-011 | `updatePassword()` doesn't handle RecentLoginRequired | `ProfileRepository.kt` | Medium | Catch exception; prompt re-auth |
| BUG-012 | Vision Board goals lost on process death | `VisionBoardViewModel.kt` | High | Firestore in Sprint 7 |
| BUG-013 | Settings toggles not persisted across restarts | `SettingsViewModel.kt` | High | SharedPreferences |
| BUG-014 | User-created note folders not persisted | `Note.kt` | High | Firestore in Sprint 7 |
| BUG-015 | `HomeScreen.kt` dead code not in NavGraph | `HomeScreen.kt` | Low | Remove or wire |

---

# PART 7 — FUTURE SPRINT ROADMAP

## Sprint 7 — Data Durability

**Goal:** Every piece of user data survives process death and device change.

**Why now:** After Sprint 6 the app looks complete. A user who starts using it will lose their tasks the first time Android kills the app. This is the trust-destroying moment that drives uninstalls.

**Work items:**
1. Migrate `TaskRepository` → `TaskRepositoryImpl` (Firestore, same pattern as NoteRepoImpl)
2. Add `userId` field to `Task` model
3. Persist user-created folders to Firestore
4. Persist `VisionBoardViewModel` goals to Firestore
5. Persist settings to SharedPreferences
6. Create `StudySessionRepository` (groundwork for Sprint 8 analytics)

**Architecture introduced:** Firestore schemas for tasks, sessions, goals, folders.

## Sprint 8 — Real Analytics + Code Quality

**Goal:** Replace hardcoded analytics. Fix all critical and high bugs.

**Work items:**
1. Wire `PomodoroViewModel.onFinish()` to `StudySessionRepository.recordSession()`
2. Build `AnalyticsRepository` with flexible streak, weekly hours, subject breakdown
3. Update `AnalyticsViewModel` to read from `AnalyticsRepository`
4. Fix BUG-001 (TaskRepository constructor)
5. Fix BUG-003 (dark mode wire)
6. Fix BUG-005 (dashboard Notes navigation)
7. Fix BUG-011 (re-auth before password change)
8. Consolidate three timer implementations into one
9. Remove duplicate bottom nav (BUG-006)
10. Configure Firestore Security Rules (BUG-010)

## Sprint 9 — AI Features

**Goal:** Implement Video-to-Notes and AI Quiz Generation.

**Architecture:** FastAPI backend (Python) + Retrofit/Ktor client in Android.

**Work items:**
1. FastAPI `/video-to-notes` endpoint: yt-dlp transcript → Claude API summary → NoteModel JSON
2. FastAPI `/generate-quiz` endpoint: NoteModel body → Claude API → `List<QuizQuestion>`
3. `VideoToNotesViewModel` + `ApiRepository` + Retrofit client
4. Full quiz flow: question display → answer selection → scoring → result screen
5. Store quiz results in Firestore for analytics

**Why FastAPI:** Async-native (matches concurrent AI API calls), auto-generates OpenAPI docs, excellent Python AI library ecosystem. Deployed on Cloud Run (pay per request, no server operations).

## Sprint 10 — Spaced Repetition Flashcards

**Goal:** Real flashcard system backed by SM-2 algorithm.

**Work items:**
1. `FlashcardModel(id, front, back, noteId?, interval, easeFactor, nextReviewDate)`
2. `FlashcardRepository` — Firestore backed
3. `FlashcardViewModel` with SM-2 update logic
4. Card flip animation: `animateFloatAsState` + `graphicsLayer { rotationY = rotation }`
5. Review session: show cards where `nextReviewDate <= LocalDate.now()`
6. Feed review sessions into Analytics

**SM-2 algorithm:**
```kotlin
fun updateCard(card: Flashcard, quality: Int): Flashcard {
    // quality: 0=again, 1=hard, 2=good, 3=easy
    val newEase = maxOf(1.3f, card.easeFactor + 0.1f - (3f-quality)*(0.08f+(3f-quality)*0.02f))
    val newInterval = when {
        quality < 1        → 1
        card.interval <= 1 → 6
        else               → (card.interval * newEase).toInt()
    }
    return card.copy(easeFactor=newEase, interval=newInterval,
                     nextReviewDate=LocalDate.now().plusDays(newInterval.toLong()))
}
```

## Sprint 11 — Polish and Production Readiness

1. Remove legacy Activities: consolidate all navigation into NavGraph
2. Implement notification system with WorkManager
3. Subject management: persistent subjects in Firestore, used across notes/tasks/sessions
4. Brain games: implement at least one real game (Memory Match)
5. Focus sounds: MediaPlayer integration for ambient audio
6. App performance: pagination for large note collections, lazy loading

---

# PART 8 — ENGINEERING LESSONS

## Lesson 1: The Repository Pattern Is the Seam That Makes "Later" Possible

Every time "we'll migrate this later" appears in architectural decisions, the Repository pattern is what makes "later" achievable without rewriting everything.

`TaskRepository` was built in-memory as an `object` singleton. Sprint 7 migrates it to Firestore. Because `PlanViewModel` calls `TaskRepository.getAllTasks()`, not Firestore directly, the migration requires:
1. Change `object TaskRepository` → `class TaskRepositoryImpl`
2. Update `PlanViewModel` constructor
3. Implement same function signatures with Firestore backing

`PlanBody` does not change. `PlanViewModel` logic does not change. Only the repository implementation changes. This is the Repository pattern working exactly as designed.

## Lesson 2: StateFlow Is the Contract Between ViewModel and View

Every piece of information the UI needs to render is exposed as a `StateFlow`. The View never calls functions to query data. It subscribes to flows and re-renders when flows emit.

```
ViewModel         View
    StateFlow ──────→ collectAsState() ──→ Compose State
                                                ↓
                                          UI re-renders
```

The unidirectional contract: data flows **out** of the ViewModel as `StateFlow`. Actions flow **in** as function calls. The View never holds business state.

**When this is violated:** If a composable holds business-level state in `var notes by remember { mutableListOf() }`, that state is lost on rotation. Business state belongs in ViewModel.

## Lesson 3: Coroutine Cancellation Is the Foundation of Debouncing

The autosave debounce uses the fundamental guarantee that `delay()` is a cancellable suspension point:
```kotlin
job?.cancel()
job = scope.launch { delay(threshold); doWork() }
```

This pattern applies to any "wait for user to stop X before doing Y":
- Autosave (wait for typing pause before writing to database)
- Search (wait for keystroke pause before querying)
- Scroll-to-load (wait for scroll stop before fetching more)
- Window resize (wait for drag stop before reflowing)

No RxJava, no Handler, no Timer. Pure coroutines.

## Lesson 4: Pre-Generating IDs Enables Offline-First Design

`val id = collection.document().id` before the write:
1. The model carries its own identity before the database confirms the write
2. Enables optimistic UI updates (show the item before Firestore confirms)
3. Enables autosave duplicate prevention (store the ID, update the same document on subsequent saves)
4. Works offline (Firestore generates the ID locally, queues the write, syncs on reconnect)

If you wait for the server to generate IDs, you cannot do any of the above. All writes block until the server responds.

## Lesson 5: Default Parameter Values Are the Compose Backwards-Compatibility Contract

When new parameters are added to composables that have `@Preview` functions, all new parameters must have sensible defaults:
```kotlin
fun CreateEditNoteScreen(
    ...
    onAutoSave: (String?, String, String, String) -> Unit = { _, _, _, _ -> },
    autoSaveStatus: AutoSaveStatus = AutoSaveStatus.IDLE,
)
```

Without defaults, existing `@Preview` functions fail to compile. This is the Compose equivalent of a breaking API change. Rule: every new composable parameter added to a previewed function must have a sensible default.

## Lesson 6: In-Memory Singletons Are Prototyping Tools, Not Production Architecture

`TaskRepository` as an `object` singleton was appropriate for Sprint 3 (validate task UX). It became technical debt the moment the app was deployed to users.

"This will do for now" is the most dangerous phrase in software engineering. "For now" becomes "forever" if the migration is not scheduled explicitly. Sprint 7 is the explicit scheduling.

**Rule:** Any data the user creates must be persisted to non-volatile storage within one sprint of its introduction.

## Lesson 7: Firestore Security Rules Are the Last Line of Defense

Without Security Rules, any authenticated StudyOS user can `db.collection("notes").get()` and read every other user's notes. Authentication proves identity. Security Rules enforce authorization.

The three-minute fix that prevents a catastrophic breach:
```javascript
match /notes/{noteId} {
  allow read, write: if request.auth.uid == resource.data.userId;
  allow create: if request.auth.uid == request.resource.data.userId;
}
```

This must be done before any public release.

## Lesson 8: The callbackFlow + awaitClose Pattern Is the Standard Bridge for Legacy APIs

Firestore's `addSnapshotListener` was designed before Kotlin coroutines. `callbackFlow` converts it to structured, lifecycle-aware Flow.

The critical piece is `awaitClose { listener.remove() }`. Without it:
- The Firestore listener continues firing after the screen is gone
- The ViewModel cannot be garbage collected (memory leak)
- `trySend` calls fail silently on a closed channel

Whenever a callback-based API must become a Flow, this is the pattern: callbackFlow → register listener → `trySend` in callback → `awaitClose` to remove listener.

## Lesson 9: The Three-Timer Problem Is a Symptom of Missing Architecture

StudyOS has three timer implementations. This happened because:
1. Sprint 3 built `PomodoroActivity` (legacy Activity)
2. Sprint 3 also added a mini timer to `DashboardViewModel` for the dashboard
3. Sprint 3 then added `PomodoroViewModel` for the NavGraph's Focus screen

Each decision made sense in isolation. The cumulative result is three implementations with different behaviors, different state machines, and different bugs.

**The architectural principle violated:** A piece of functionality should have exactly one canonical implementation. Variation in presentation is allowed (full-screen vs. mini widget). The underlying logic must be one class.

**Fix:** One `PomodoroViewModel` (already the best). Dashboard shows a navigation card ("Start Focus Session →") instead of a duplicate timer. `PomodoroActivity` removed in Sprint 8.

## Lesson 10: Static Analytics Is a Feature Commitment, Not a Shortcut

Building the analytics UI with hardcoded data in Sprint 3 was the right call — it validated the design and let users see the full vision. But it created an implicit commitment: this UI will eventually show real data.

If Sprint 8 is deprioritized, the analytics screen becomes permanently misleading. A user who sees "Streak: 14 days" on day 1 of using the app will lose trust in the app the moment they realize the numbers never change.

**Rule:** Every piece of UI that displays data must have a documented path to real data and a sprint that implements it. "Static for now" must be scheduled for replacement, not assumed.

---

# APPENDIX A — COMPLETE FILE REFERENCE

| File | Type | Sprint | Purpose |
|---|---|---|---|
| `MainActivity.kt` | Activity | S1 | Entry; onboarding check; auth gate; NavGraph host |
| `model/NoteModel.kt` | Data class | S4 | Firestore note schema (id, title, body, folder, timestamp, userId) |
| `model/TaskModel.kt` | Data class + enum | S3 | Task + Priority enum + isOverdue() |
| `model/AnalyticsModels.kt` | Data classes | S3 | WeeklyBarData, SubjectBreakdownItem, InsightItem |
| `model/PomodoroModel.kt` | Enums | S3 | PomodoroTab, EditingSlider |
| `model/ProgressModel.kt` | Data class | S5 | SubjectProgressModel |
| `model/SubjectModel.kt` | Data class | S3 | Subject(id, name) |
| `model/VisionGoalModel.kt` | Data class | S5 | VisionGoalModel(text, emoji, targetValue, subject) |
| `model/MockTestModel.kt` | Enum | S9 | Difficulty (EASY, MEDIUM, HARD) |
| `repo/NoteRepo.kt` | Interface | S4 | Unused interface — dead code (BUG-002) |
| `repo/NoteRepoImpl.kt` | Class | S4 | Firestore CRUD + real-time stream for notes |
| `repo/AuthRepository.kt` | Class | S3 | Firebase Auth: login, signup, credential sign-in |
| `repo/TaskRepository.kt` | Object singleton | S3 | In-memory task store + note linking (BUG-008) |
| `repo/ProfileRepository.kt` | Class | S6 | Firestore display name + Firebase Auth password |
| `viewModel/AuthViewModel.kt` | ViewModel | S3 | AuthState sealed class; login/signup/credential |
| `viewModel/NoteViewModel.kt` | ViewModel | S4 | Notes + auto-save; AutoSaveStatus enum |
| `viewModel/PlanViewModel.kt` | ViewModel | S3 | Task CRUD + note picker + calendar state |
| `viewModel/DashboardViewModel.kt` | ViewModel | S3 | DashboardUiState + mini timer (BUG-001) |
| `viewModel/PomodoroViewModel.kt` | ViewModel | S3 | Full Pomodoro + tabs + configurable durations |
| `viewModel/FocusViewModel.kt` | ViewModel | S4 | Ambient sound state (no audio playback) |
| `viewModel/AnalyticsViewModel.kt` | ViewModel | S3 | Static analytics data (BUG-007) |
| `viewModel/ProgressViewModel.kt` | ViewModel | S5 | Subject progress + random heatmap |
| `viewModel/HomeViewModel.kt` | ViewModel | S1 | Home screen state (dead code with HomeScreen.kt) |
| `viewModel/StudyViewModel.kt` | ViewModel | S3 | Subject list + search for Study tab |
| `viewModel/SubjectViewModel.kt` | ViewModel | S3 | Subject management with addSubject() |
| `viewModel/MockTestViewModel.kt` | ViewModel | S9 | Mock test config state |
| `viewModel/VisionBoardViewModel.kt` | ViewModel | S5 | In-memory pinned goals (BUG-012) |
| `viewModel/ProfileViewModel.kt` | ViewModel | S6 | Profile edit + saveResult + repo wrapper |
| `viewModel/SettingsViewModel.kt` | ViewModel | S6 | Toggles (BUG-003, BUG-013) + signOut |
| `ui/navigation/AppRoutes.kt` | Sealed class | S3 | 17 type-safe NavGraph route strings |
| `ui/navigation/StudyOSNavGraph.kt` | Composable | S3 | NavHost + Scaffold + conditional bottom nav |
| `ui/navigation/StudyOSBottomNav.kt` | Composable | S3 | 5-tab primary navigation |
| `ui/home/DashboardActivity.kt` | Activity | S3 | Legacy home; DashboardBody composable; mini timer |
| `ui/home/HomeScreen.kt` | Composable | S1 | Dead code — alternate home not in NavGraph (BUG-015) |
| `ui/home/VisionBoardActivity.kt` | Activity | S5 | Vision board shell + VisionBoardBody |
| `ui/auth/LoginActivity.kt` | Activity | S3 | Legacy login shell |
| `ui/auth/SignUpActivity.kt` | Activity | S3 | Legacy signup shell |
| `ui/study/Note.kt` | Composables | S4 | Full notes UI: NotesScreen, NotesScreenContent, CreateEditNoteScreen |
| `ui/study/Quiz.kt` | Activity + Composable | S9 | Static 1-question quiz UI — needs full implementation |
| `ui/study/Flashcards.kt` | Activity + Composable | S4 | Static flashcard UI shell |
| `ui/study/VideotoNotes.kt` | Activity + Composable | S9 | Video-to-notes UI shell — backend not connected |
| `ui/study/MockTestActivity.kt` | Activity + Composable | S9 | Mock test config UI |
| `ui/study/StudyScreen.kt` | Composable | S3 | Study tab hub: subject grid + tool cards |
| `ui/focus/FocusScreen.kt` | Composable | S3 | Focus tab: PomodoroViewModel + Canvas arc |
| `ui/focus/PomodoroActivity.kt` | Activity | S3 | Standalone Pomodoro (legacy, pre-NavGraph) |
| `ui/focus/BrainGame.kt` | Activity + Composable | S9 | Brain game placeholder cards |
| `ui/focus/BrainGameActivity.kt` | Activity | S9 | Duplicate brain game activity (BUG-004) |
| `ui/profile/ProgressActivity.kt` | Activity + Composable | S3 | Full analytics screen with Canvas charts |
| `ui/profile/ProfileActivity.kt` | Activity | S6 | Legacy profile shell |
| `ui/profile/ProfileScreen.kt` | Composable | S6 | ProfileScreenV2 — display name + password |
| `ui/profile/SettingActivity.kt` | Activity | S6 | Settings screen |
| `ui/analytics/AnalyticsCharts.kt` | Composables | S3 | WeeklyBarChart, QuizScoreTrend, HeatMap (Canvas) |
| `ui/components/AnalyticsComponents.kt` | Composables | S3 | AnalyticsHeroRow, InsightsCard, SubjectBreakdownCard |
| `ui/components/StudyBottomNav.kt` | Composable | S3 | Alternative bottom nav component |
| `ui/theme/Color.kt` | Constants | S1 | Full StudyOS color palette |
| `ui/theme/Theme.kt` | Composable | S1 | StudyOSTheme — does not read darkMode toggle (BUG-003) |
| `ui/theme/Type.kt` | Constants | S1 | Typography scale |
| `ui/onboarding/OnboardingActivity1_WelcomePage.kt` | Activity | S1 | Onboarding screen 1 |
| `ui/onboarding/OnboardingActivity2_StudyActivityOS.kt` | Activity | S1 | Onboarding screen 2 |
| `ui/onboarding/OnboardingActivity3_worksOffline.kt` | Activity | S1 | Onboarding screen 3 |
| `ui/plan/PlanActivity.kt` | Activity + Composable | S3 | Task planner with Compose month calendar |
| `utils/GoogleSignInHelper.kt` | Object singleton | S3 | Google Sign-In intent builder |

---

# APPENDIX B — DEPENDENCY INVENTORY

```gradle
// Core Android + Compose
androidx.activity.compose                  setContent {} entry for Compose in Activity
androidx.compose.material3                 Material 3 components
androidx.compose.material.icons.extended   Icon variants (Rounded, Outlined, etc.)
androidx.compose.ui                        Core Compose UI toolkit
androidx.compose.ui.tooling                @Preview support in Android Studio
androidx.lifecycle.viewmodel.compose       viewModel() factory in composables
androidx.navigation.compose                NavHost, NavController, composable() routes

// Firebase
firebase.bom                               Version alignment for all Firebase libraries
firebase.auth                              Email/password + credential authentication
firebase.firestore                         NoSQL document DB with offline persistence

// Google Auth
play-services-auth:21.2.0                  Google Sign-In account picker + token

// Kotlin Coroutines
kotlinx.coroutines.android                 Main dispatcher for UI coroutines
kotlinx.coroutines.play-services           .await() extension for Firebase Tasks

// NOT PRESENT (gaps requiring future work):
// androidx.room                           No local database — all persistence is Firestore or in-memory
// retrofit2 / ktor-client                 No HTTP client — Video-to-Notes backend not connected
// coil / glide                            No remote image loading
// charting library                        All charts are custom Canvas implementations
// workmanager                             No background scheduling — notifications not wired
// androidx.biometric                      No biometric authentication
```

---

# APPENDIX C — GLOSSARY

| Term | Definition in StudyOS Context |
|---|---|
| `callbackFlow` | Kotlin coroutines builder converting callback API (Firestore listener) to Flow |
| `awaitClose` | Block inside callbackFlow; runs on collector cancel; used to remove Firestore listener |
| `StateFlow` | Hot flow always holding current value; ViewModel's observable for UI state |
| `MutableStateFlow` | Writable StateFlow; ViewModel holds it private; exposes as read-only StateFlow |
| `collectAsState()` | Compose extension converting Flow/StateFlow to Compose State<T>; triggers recomposition |
| `viewModelScope` | CoroutineScope tied to ViewModel lifecycle; auto-cancelled on ViewModel.onCleared() |
| `stateIn()` | Converts cold Flow to hot StateFlow within a scope |
| `SharingStarted.WhileSubscribed(5000)` | Keeps upstream Flow alive 5s after last subscriber; survives config changes |
| `ListenerRegistration` | Firestore handle to active snapshot listener; must be .remove()'d in awaitClose |
| `rememberSaveable` | Compose state surviving recomposition + config changes; not process death |
| `popUpTo(route) { inclusive = true }` | Clears NavGraph back stack up to and including the specified route |
| `launchSingleTop` | Navigation flag preventing duplicate destinations at top of back stack |
| SM-2 | SuperMemo Algorithm 2 — spaced repetition algorithm used by Anki; planned for Sprint 10 |
| `CountDownTimer` | Android's built-in callback-based timer; fires onTick() on main thread |
| Debounce | Rate-limiting pattern: wait for event stream to pause before acting on latest value |
| Sealed class | Kotlin type with known subclasses at compile time; enables exhaustive when expressions |
| `object` singleton | Kotlin singleton; single process-lifetime instance; cannot be constructed with () |
| `UUID.randomUUID()` | Locally generated unique ID; no server coordination required |
| `LocalDate` | Java 8 date-only type; requires minSdk 26 or core library desugaring |
| `WakeLock` | Android mechanism preventing CPU sleep; needed for background timer accuracy |
| `ForegroundService` | Android service visible to the user via notification; survives process backgrounding |
| `WorkManager` | Android Jetpack library for deferrable background work with scheduling guarantees |

---

# APPENDIX D — VIVA CHEAT SHEET

Quick answers for each feature under exam conditions.

| Question | Answer |
|---|---|
| "Why Firebase Firestore?" | No server, real-time sync, offline persistence, free tier sufficient, SDK handles auth-scoped queries |
| "How does auto-save work?" | Coroutine Job cancelled and relaunched on every keystroke; 1200ms delay; fires Firestore write |
| "How do you prevent duplicate notes?" | `_currentEditingNoteId` stores the first autosave's document ID; subsequent saves update that document |
| "Why MVVM?" | ViewModel survives rotation; View is pure renderer; Repository enables testing |
| "How does Firestore real-time work?" | callbackFlow + addSnapshotListener + trySend; awaitClose removes listener |
| "Why in-memory tasks?" | Sprint 3 decision: validate UX first, migrate to Firestore in Sprint 7 |
| "How does Google SSO work?" | ActivityResultLauncher → Google picker → idToken → GoogleAuthProvider.getCredential → Firebase signInWithCredential |
| "What is AuthState?" | Sealed class: Idle / Loading / Success / Error; used with when() exhaustively in UI |
| "How does Pomodoro timer work?" | CountDownTimer(timeRemaining * 1000, 1000); onTick updates StateFlow; onFinish increments sessions |
| "What is callbackFlow?" | Converts callback-based APIs to Kotlin Flow; awaitClose guarantees cleanup |
| "What is the biggest architectural gap?" | In-memory TaskRepository loses all tasks on process death; fix: Firestore migration Sprint 7 |
| "Why isn't analytics real?" | Static data in Sprint 3 pending connection to StudySessionRepository in Sprint 8 |
| "How does note-task linking work?" | Task.linkedNoteIds: List<String>; TaskRepository.getTasksForNote(noteId) for reverse query |
| "How are Security Rules configured?" | They aren't — BUG-010; must be configured before production release |

---

*End of StudyOS Engineering Research Book — Version 2.0*  
*Package: com.teamdobermans.studyos*  
*Documented: June 2026*  
*Covers: Sprint 1 through Sprint 6 (implemented) + Sprint 7–11 (planned)*
