package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FocusUiState(
    val activeSound: String? = null
)

class FocusViewModel : ViewModel() {

    private val _state = MutableStateFlow(FocusUiState())
    val state: StateFlow<FocusUiState> = _state.asStateFlow()

    fun toggleSound(sound: String) {
        val current = _state.value.activeSound
        _state.value = _state.value.copy(
            activeSound = if (current == sound) null else sound
        )
    }
}

