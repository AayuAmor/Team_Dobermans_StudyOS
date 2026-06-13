package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.FlashcardModel
import com.teamdobermans.studyos.repo.FlashcardRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FlashcardsUiState(
    val cards: List<FlashcardModel> = emptyList(),
    val decks: List<String> = listOf("All"),
    val selectedDeck: String = "All",
    val isLoading: Boolean = true,
    val error: String? = null,
    val isReviewMode: Boolean = false,
    val reviewIndex: Int = 0,
    val isCardFlipped: Boolean = false,
    val showCreateDialog: Boolean = false,
    val editingCard: FlashcardModel? = null,
    val frontInput: String = "",
    val backInput: String = "",
    val deckInput: String = "General"
)

@OptIn(ExperimentalCoroutinesApi::class)
class FlashcardViewModel : ViewModel() {

    private val repo = FlashcardRepoImpl()

    private val _selectedDeck = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(FlashcardsUiState())
    val uiState: StateFlow<FlashcardsUiState> = _uiState.asStateFlow()

    private val cards: StateFlow<List<FlashcardModel>> = _selectedDeck
        .flatMapLatest { deck -> repo.observeFlashcards(deck) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            cards.collect { newCards ->
                val deckNames = newCards.map { it.deckName }.distinct().sorted()
                val decks = listOf("All") + deckNames
                _uiState.value = _uiState.value.copy(
                    cards = newCards,
                    decks = decks,
                    isLoading = false
                )
            }
        }
    }

    fun selectDeck(deck: String) {
        _selectedDeck.value = deck.takeIf { it != "All" }
        _uiState.value = _uiState.value.copy(
            selectedDeck = deck,
            isLoading = true,
            reviewIndex = 0,
            isCardFlipped = false
        )
    }

    fun startReview() {
        if (_uiState.value.cards.isEmpty()) return
        _uiState.value = _uiState.value.copy(
            isReviewMode = true,
            reviewIndex = 0,
            isCardFlipped = false
        )
    }

    fun exitReview() {
        _uiState.value = _uiState.value.copy(
            isReviewMode = false,
            reviewIndex = 0,
            isCardFlipped = false
        )
    }

    fun flipCard() {
        _uiState.value = _uiState.value.copy(isCardFlipped = !_uiState.value.isCardFlipped)
    }

    fun nextCard() {
        val state = _uiState.value
        if (state.reviewIndex >= state.cards.size - 1) return
        viewModelScope.launch { repo.recordReview(state.cards[state.reviewIndex].id) }
        _uiState.value = state.copy(reviewIndex = state.reviewIndex + 1, isCardFlipped = false)
    }

    fun previousCard() {
        val state = _uiState.value
        if (state.reviewIndex <= 0) return
        _uiState.value = state.copy(reviewIndex = state.reviewIndex - 1, isCardFlipped = false)
    }

    fun openCreateDialog() {
        val currentDeck = _uiState.value.selectedDeck.takeIf { it != "All" } ?: "General"
        _uiState.value = _uiState.value.copy(
            showCreateDialog = true,
            editingCard = null,
            frontInput = "",
            backInput = "",
            deckInput = currentDeck
        )
    }

    fun openEditDialog(card: FlashcardModel) {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = true,
            editingCard = card,
            frontInput = card.front,
            backInput = card.back,
            deckInput = card.deckName
        )
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false, editingCard = null)
    }

    fun updateFrontInput(text: String) {
        _uiState.value = _uiState.value.copy(frontInput = text)
    }

    fun updateBackInput(text: String) {
        _uiState.value = _uiState.value.copy(backInput = text)
    }

    fun updateDeckInput(text: String) {
        _uiState.value = _uiState.value.copy(deckInput = text)
    }

    fun saveCard() {
        val state = _uiState.value
        if (state.frontInput.isBlank() || state.backInput.isBlank()) return
        val deckName = state.deckInput.trim().ifBlank { "General" }
        viewModelScope.launch {
            val existing = state.editingCard
            if (existing != null) {
                repo.updateFlashcard(
                    existing.copy(
                        front = state.frontInput.trim(),
                        back = state.backInput.trim(),
                        deckName = deckName
                    )
                )
            } else {
                repo.createFlashcard(state.frontInput.trim(), state.backInput.trim(), deckName)
            }
            dismissDialog()
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch { repo.deleteFlashcard(cardId) }
    }
}