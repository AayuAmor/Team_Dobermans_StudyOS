package com.teamdobermans.studyos.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teamdobermans.studyos.model.VideoNotesResponse
import com.teamdobermans.studyos.repo.VideoNotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface VideoNotesUiState {
    data object Idle    : VideoNotesUiState
    data object Loading : VideoNotesUiState
    data class  Success(val response: VideoNotesResponse) : VideoNotesUiState
    data class  Error(val message: String)                : VideoNotesUiState
}

sealed interface SaveState {
    data object Idle    : SaveState
    data object Saving  : SaveState
    data object Saved   : SaveState
    data class  Error(val message: String) : SaveState
}

class VideoNotesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = VideoNotesRepository()

    private val _uiState = MutableStateFlow<VideoNotesUiState>(VideoNotesUiState.Idle)
    val uiState: StateFlow<VideoNotesUiState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun generateFromUrl(url: String) {
        if (url.isBlank()) return
        _uiState.value = VideoNotesUiState.Loading
        viewModelScope.launch {
            repo.notesFromUrl(url.trim())
                .onSuccess { response ->
                    _uiState.value = if (response.success) {
                        VideoNotesUiState.Success(response)
                    } else {
                        VideoNotesUiState.Error("Server returned an unsuccessful response.")
                    }
                }
                .onFailure { e ->
                    _uiState.value = VideoNotesUiState.Error(
                        e.message ?: "Failed to reach the video service."
                    )
                }
        }
    }

    fun generateFromUpload(uri: Uri) {
        _uiState.value = VideoNotesUiState.Loading
        viewModelScope.launch {
            val cr = getApplication<Application>().contentResolver
            val mimeType = cr.getType(uri) ?: "video/mp4"
            val fileName = resolveFileName(uri) ?: "upload.mp4"

            val bytes = try {
                cr.openInputStream(uri)?.use { it.readBytes() }
            } catch (e: Exception) {
                _uiState.value = VideoNotesUiState.Error("Could not read the selected file.")
                return@launch
            }

            if (bytes == null) {
                _uiState.value = VideoNotesUiState.Error("Could not open the selected file.")
                return@launch
            }

            repo.notesFromUpload(fileName, bytes, mimeType)
                .onSuccess { response ->
                    _uiState.value = if (response.success) {
                        VideoNotesUiState.Success(response)
                    } else {
                        VideoNotesUiState.Error("Server returned an unsuccessful response.")
                    }
                }
                .onFailure { e ->
                    _uiState.value = VideoNotesUiState.Error(
                        e.message ?: "Upload failed."
                    )
                }
        }
    }

    fun saveToNotes(title: String, body: String) {
        _saveState.value = SaveState.Saving
        viewModelScope.launch {
            val ok = repo.saveToNotes(title, body)
            _saveState.value = if (ok) SaveState.Saved else SaveState.Error("Failed to save note.")
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun reset() {
        _uiState.value  = VideoNotesUiState.Idle
        _saveState.value = SaveState.Idle
    }

    private fun resolveFileName(uri: Uri): String? {
        val cr = getApplication<Application>().contentResolver
        val cursor = cr.query(uri, null, null, null, null) ?: return null
        return cursor.use { c ->
            val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
        }
    }
}
