package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.FlashcardModel
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.repo.FlashcardRepo
import com.teamdobermans.studyos.repo.FlashcardRepoImpl
import com.teamdobermans.studyos.repo.NoteRepo
import com.teamdobermans.studyos.repo.NoteRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FlashcardScreenMode { Dashboard, SelectNote, GenerateOptions, PreviewGenerated, Review }
enum class FlashcardTab { DueToday, AllCards, ByNote }
enum class FlashcardCountOption { Five, Ten, Fifteen, Auto }
enum class FlashcardDifficultyOption { Basic, Medium, ExamStyle }
enum class FlashcardQuestionStyle { Definition, ShortAnswer, ConceptCheck, Mixed }

data class FlashcardStats(
    val totalCards: Int = 0,
    val dueToday: Int = 0,
    val mastered: Int = 0
)

data class FlashcardNoteGroup(
    val noteId: String,
    val noteTitle: String,
    val cards: List<FlashcardModel>,
    val dueCount: Int
)

class FlashcardViewModel(
    private val repo: FlashcardRepo = FlashcardRepoImpl(),
    noteRepo: NoteRepo = NoteRepoImpl()
) : ViewModel() {

    private val _mode = MutableStateFlow(FlashcardScreenMode.Dashboard)
    val mode: StateFlow<FlashcardScreenMode> = _mode.asStateFlow()

    private val _selectedTab = MutableStateFlow(FlashcardTab.DueToday)
    val selectedTab: StateFlow<FlashcardTab> = _selectedTab.asStateFlow()

    private val _allCards = MutableStateFlow<List<FlashcardModel>>(emptyList())
    val allCards: StateFlow<List<FlashcardModel>> = _allCards.asStateFlow()

    private val _dueCards = MutableStateFlow<List<FlashcardModel>>(emptyList())
    val dueCards: StateFlow<List<FlashcardModel>> = _dueCards.asStateFlow()

    private val _currentCard = MutableStateFlow<FlashcardModel?>(null)
    val currentCard: StateFlow<FlashcardModel?> = _currentCard.asStateFlow()

    private val _notesLoading = MutableStateFlow(true)
    val notesLoading: StateFlow<Boolean> = _notesLoading.asStateFlow()

    private val _cardsLoading = MutableStateFlow(false)
    val cardsLoading: StateFlow<Boolean> = _cardsLoading.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedNote = MutableStateFlow<NoteModel?>(null)
    val selectedNote: StateFlow<NoteModel?> = _selectedNote.asStateFlow()

    private val _noteSearchQuery = MutableStateFlow("")
    val noteSearchQuery: StateFlow<String> = _noteSearchQuery.asStateFlow()

    private val _selectedFolder = MutableStateFlow<String?>(null)
    val selectedFolder: StateFlow<String?> = _selectedFolder.asStateFlow()

    private val _countOption = MutableStateFlow(FlashcardCountOption.Auto)
    val countOption: StateFlow<FlashcardCountOption> = _countOption.asStateFlow()

    private val _difficultyOption = MutableStateFlow(FlashcardDifficultyOption.Basic)
    val difficultyOption: StateFlow<FlashcardDifficultyOption> = _difficultyOption.asStateFlow()

    private val _questionStyle = MutableStateFlow(FlashcardQuestionStyle.Mixed)
    val questionStyle: StateFlow<FlashcardQuestionStyle> = _questionStyle.asStateFlow()

    private val _generatedCards = MutableStateFlow<List<FlashcardModel>>(emptyList())
    val generatedCards: StateFlow<List<FlashcardModel>> = _generatedCards.asStateFlow()

    val notes: StateFlow<List<NoteModel>> = noteRepo.getNotes()
        .onEach { _notesLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val folders: StateFlow<List<String>> = notes
        .combine(_noteSearchQuery) { notes, _ ->
            notes.map { it.folder.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .sortedWith(String.CASE_INSENSITIVE_ORDER)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filteredNotes: StateFlow<List<NoteModel>> =
        combine(notes, _noteSearchQuery, _selectedFolder) { notes, query, folder ->
            val q = query.trim()
            notes
                .filter { note -> folder == null || note.folder.equals(folder, ignoreCase = true) }
                .filter { note ->
                    q.isBlank() || note.title.contains(q, true) || note.body.contains(
                        q,
                        true
                    ) || note.folder.contains(q, true)
                }
                .sortedByDescending { it.lastActivityTime() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats: StateFlow<FlashcardStats> = combine(_allCards, _dueCards) { all, due ->
        FlashcardStats(
            totalCards = all.size,
            dueToday = due.size,
            mastered = all.count { it.mastered || it.intervalDays >= 7 || it.reviewCount >= 3 }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FlashcardStats())

    val noteGroups: StateFlow<List<FlashcardNoteGroup>> = combine(_allCards, notes) { cards, notes ->
        val noteTitleById = notes.associate { it.id to it.title.ifBlank { "Untitled Note" } }
        cards.groupBy { card -> card.noteId.ifBlank { "unlinked" } }
            .map { (noteId, groupedCards) ->
                val title = when {
                    noteId != "unlinked" -> noteTitleById[noteId]
                        ?: groupedCards.firstOrNull()?.noteTitle?.ifBlank { null } ?: "Deleted note"

                    else -> groupedCards.firstOrNull()?.noteTitle?.ifBlank { null } ?: "Unlinked cards"
                }
                FlashcardNoteGroup(
                    noteId = noteId,
                    noteTitle = title,
                    cards = groupedCards,
                    dueCount = groupedCards.count { it.isDue() }
                )
            }
            .sortedBy { it.noteTitle.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refreshCards()
    }

    fun refreshCards() {
        viewModelScope.launch {
            _cardsLoading.value = true
            _errorMessage.value = null
            runCatching {
                val all = repo.getAllFlashcards().sortedByDescending { it.createdAt }
                val due = repo.getDueFlashcards().sortedBy { it.nextReviewDate }
                _allCards.value = all
                _dueCards.value = due
                _currentCard.value = due.firstOrNull()
            }.onFailure {
                _errorMessage.value = "Failed to load flashcards. Please try again."
            }
            _cardsLoading.value = false
        }
    }

    fun showDashboard() {
        _mode.value = FlashcardScreenMode.Dashboard
    }

    fun showNotePicker() {
        _mode.value = FlashcardScreenMode.SelectNote
    }

    fun showReview() {
        _mode.value = FlashcardScreenMode.Review
    }

    fun selectTab(tab: FlashcardTab) {
        _selectedTab.value = tab
    }

    fun setNoteSearchQuery(query: String) {
        _noteSearchQuery.value = query
    }

    fun selectFolder(folder: String?) {
        _selectedFolder.value = folder
    }

    fun setCountOption(option: FlashcardCountOption) {
        _countOption.value = option
    }

    fun setDifficultyOption(option: FlashcardDifficultyOption) {
        _difficultyOption.value = option
    }

    fun setQuestionStyle(style: FlashcardQuestionStyle) {
        _questionStyle.value = style
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun selectNote(note: NoteModel) {
        _selectedNote.value = note
        _generatedCards.value = emptyList()
        _errorMessage.value = null
        _mode.value = FlashcardScreenMode.GenerateOptions
    }

    fun generatePreview() {
        val note = _selectedNote.value ?: return
        if (note.body.wordCount() < 12) {
            _errorMessage.value = "Note is too short. Add more content to generate better flashcards."
            return
        }
        _isGenerating.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            val generated = extractFlashcards(note)
            if (generated.isEmpty()) {
                _errorMessage.value =
                    "Failed to generate flashcards from this note. Add more definitions or key points."
                _mode.value = FlashcardScreenMode.GenerateOptions
            } else {
                _generatedCards.value = generated
                _mode.value = FlashcardScreenMode.PreviewGenerated
            }
            _isGenerating.value = false
        }
    }

    fun regeneratePreview() {
        generatePreview()
    }

    fun updateGeneratedCard(index: Int, question: String, answer: String) {
        _generatedCards.value = _generatedCards.value.mapIndexed { i, card ->
            if (i == index) card.copy(question = question.trim(), answer = answer.trim()) else card
        }
    }

    fun removeGeneratedCard(index: Int) {
        _generatedCards.value = _generatedCards.value.filterIndexed { i, _ -> i != index }
    }

    fun saveGeneratedCards(onComplete: (Boolean) -> Unit = {}) {
        val cards = _generatedCards.value.filter { it.question.isNotBlank() && it.answer.isNotBlank() }
        if (cards.isEmpty()) {
            _errorMessage.value = "No flashcards to save."
            onComplete(false)
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            runCatching {
                cards.forEach { repo.addFlashcard(it) }
                refreshCards()
            }.onSuccess {
                _generatedCards.value = emptyList()
                _mode.value = FlashcardScreenMode.Dashboard
                _selectedTab.value = FlashcardTab.AllCards
                onComplete(true)
            }.onFailure {
                _errorMessage.value = "Failed to save flashcards. Please try again."
                onComplete(false)
            }
            _isSaving.value = false
        }
    }

    fun addCard(question: String, answer: String, note: NoteModel? = _selectedNote.value) {
        viewModelScope.launch {
            repo.addFlashcard(
                FlashcardModel(
                    question = question.trim(),
                    answer = answer.trim(),
                    noteId = note?.id.orEmpty(),
                    noteTitle = note?.title.orEmpty(),
                    subjectId = note?.folder.orEmpty(),
                    createdAt = System.currentTimeMillis()
                )
            )
            refreshCards()
        }
    }

    fun submitReview(card: FlashcardModel, quality: Int) {
        viewModelScope.launch {
            val newEase = maxOf(1.3f, card.easeFactor + 0.1f - (5 - quality) * 0.08f)
            val newInterval = if (quality < 3) 1 else (card.intervalDays * newEase).toInt().coerceAtLeast(1)
            val updated = card.copy(
                intervalDays = newInterval,
                easeFactor = newEase,
                reviewCount = card.reviewCount + 1,
                mastered = quality >= 5 && newInterval >= 7,
                nextReviewDate = System.currentTimeMillis() + newInterval * 86_400_000L
            )
            repo.updateFlashcard(updated)
            refreshCards()
        }
    }

    private fun extractFlashcards(note: NoteModel): List<FlashcardModel> {
        val targetCount = when (_countOption.value) {
            FlashcardCountOption.Five -> 5
            FlashcardCountOption.Ten -> 10
            FlashcardCountOption.Fifteen -> 15
            FlashcardCountOption.Auto -> 10
        }
        val noteTitle = note.title.ifBlank { "this note" }
        val cleanedLines = note.body
            .lines()
            .flatMap { it.split(". ") }
            .map { it.trim().trim('-', '•', '*').trim() }
            .filter { it.length >= 12 }
            .distinctBy { it.lowercase() }

        val definitionCards = cleanedLines.mapNotNull { line ->
            val separators = listOf(" is ", " are ", " means ", " refers to ", ":", " - ", " – ")
            val separator = separators.firstOrNull { line.contains(it, ignoreCase = true) } ?: return@mapNotNull null
            val parts = line.split(separator, limit = 2)
            val term = parts.getOrNull(0)?.trim()?.takeIf { it.length in 3..80 } ?: return@mapNotNull null
            val answer = parts.getOrNull(1)?.trim()?.takeIf { it.length >= 8 } ?: return@mapNotNull null
            buildCard(note, questionFor(term, line), answer)
        }

        val conceptCards = cleanedLines
            .filter { line -> definitionCards.none { it.answer.equals(line, ignoreCase = true) } }
            .mapIndexed { index, line ->
                val question = when (_questionStyle.value) {
                    FlashcardQuestionStyle.Definition -> "What does key point ${index + 1} from $noteTitle mean?"
                    FlashcardQuestionStyle.ShortAnswer -> "Summarize this point from $noteTitle."
                    FlashcardQuestionStyle.ConceptCheck -> "Why is this important in $noteTitle?"
                    FlashcardQuestionStyle.Mixed -> if (index % 2 == 0) "What is the key idea here?" else "Explain this concept from $noteTitle."
                }
                buildCard(note, question, line)
            }

        return (definitionCards + conceptCards).take(targetCount)
    }

    private fun questionFor(term: String, sourceLine: String): String {
        return when (_questionStyle.value) {
            FlashcardQuestionStyle.Definition -> "What is $term?"
            FlashcardQuestionStyle.ShortAnswer -> "Define $term in one or two lines."
            FlashcardQuestionStyle.ConceptCheck -> "Why is $term important?"
            FlashcardQuestionStyle.Mixed -> if (sourceLine.length > 90) "Explain $term." else "What is $term?"
        }
    }

    private fun buildCard(note: NoteModel, question: String, answer: String): FlashcardModel = FlashcardModel(
        question = question,
        answer = answer,
        subjectId = note.folder.trim(),
        noteId = note.id,
        noteTitle = note.title.ifBlank { "Untitled Note" },
        createdAt = System.currentTimeMillis(),
        nextReviewDate = System.currentTimeMillis()
    )

    private fun FlashcardModel.isDue(): Boolean = nextReviewDate <= System.currentTimeMillis()

    private fun NoteModel.lastActivityTime(): Long = when {
        updatedAt > 0L -> updatedAt
        timestamp > 0L -> timestamp
        createdAt > 0L -> createdAt
        else -> 0L
    }

    private fun String.wordCount(): Int = split(Regex("\\s+")).count { it.isNotBlank() }
}
