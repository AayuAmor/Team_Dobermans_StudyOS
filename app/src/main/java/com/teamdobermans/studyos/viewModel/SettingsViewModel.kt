package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamdobermans.studyos.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun prefsDoc() = db
        .collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("preferences")
        .document("settings")

    // ── UI State ───────────────────────────────────────────────────────
    private val _prefs = MutableStateFlow(UserPreferences())
    val prefs: StateFlow<UserPreferences> = _prefs.asStateFlow()

    private val _signedOut = MutableStateFlow(false)
    val signedOut: StateFlow<Boolean> = _signedOut.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // ── Load from Firestore on init ────────────────────────────────────
    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            try {
                val snap = prefsDoc().get().await()
                val loaded = snap.toObject(UserPreferences::class.java)
                if (loaded != null) _prefs.value = loaded
            } catch (e: Exception) {
                // keep defaults if load fails
            }
        }
    }

    // ── Save to Firestore ──────────────────────────────────────────────
    fun savePreferences(updated: UserPreferences) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                prefsDoc().set(updated).await()
                _prefs.value = updated
            } catch (e: Exception) {
                // optionally show error
            } finally {
                _isSaving.value = false
            }
        }
    }

    // ── Sign out ───────────────────────────────────────────────────────
    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        _signedOut.value = true
    }
}