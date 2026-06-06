package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.teamdobermans.studyos.repo.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        repository.login(email, password)
            .onSuccess { _authState.value = AuthState.Success }
            .onFailure { _authState.value = AuthState.Error(it.message ?: "Login failed") }
    }

    fun signUp(email: String, password: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        repository.signUp(email, password)
            .onSuccess { _authState.value = AuthState.Success }
            .onFailure { _authState.value = AuthState.Error(it.message ?: "Sign up failed") }
    }

    fun signInWithCredential(credential: AuthCredential) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        repository.signInWithCredential(credential)
            .onSuccess { _authState.value = AuthState.Success }
            .onFailure { _authState.value = AuthState.Error(it.message ?: "Google sign-in failed") }
    }

    fun resetState() { _authState.value = AuthState.Idle }
}

