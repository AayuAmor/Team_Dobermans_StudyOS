package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.teamdobermans.studyos.model.FocusSessionModel
import com.teamdobermans.studyos.repo.FocusSessionRepoImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionHistoryViewModel : ViewModel() {

    private val repo = FocusSessionRepoImpl()
    private val auth = FirebaseAuth.getInstance()

    private val _sessions = MutableStateFlow<List<FocusSessionModel>>(emptyList())
    val sessions: StateFlow<List<FocusSessionModel>> = _sessions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSessions()
    }

    private fun loadSessions() {
        val uid = auth.currentUser?.uid ?: run {
            _isLoading.value = false
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _sessions.value = repo.getSessions(uid)
            _isLoading.value = false
        }
    }

    fun refresh() = loadSessions()
}