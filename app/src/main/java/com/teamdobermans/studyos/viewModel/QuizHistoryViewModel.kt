package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.QuizAttemptModel
import com.teamdobermans.studyos.repo.QuizRepoImpl
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class QuizHistoryViewModel : ViewModel() {

    private val repo = QuizRepoImpl()

    val attempts: StateFlow<List<QuizAttemptModel>> = repo.observeAttempts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
