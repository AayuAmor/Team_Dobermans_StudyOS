package com.teamdobermans.studyos.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.teamdobermans.studyos.model.VideoNotesResponse
import com.teamdobermans.studyos.repo.VideoNotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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

    private val _uiState  = MutableStateFlow<VideoNotesUiState>(VideoNotesUiState.Idle)
    val uiState: StateFlow<VideoNotesUiState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()


    fun generateFromUrl(url: String, summaryStyle: String = "detailed") {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return

        val urlError = validateUrl(trimmed)
        if (urlError != null) {
            _uiState.value = VideoNotesUiState.Error(urlError)
            return
        }

        _uiState.value = VideoNotesUiState.Loading
        viewModelScope.launch {
            repo.notesFromUrl(trimmed, summaryStyle)
                .onSuccess { response ->
                    _uiState.value = if (response.success) {
                        VideoNotesUiState.Success(response)
                    } else {
                        VideoNotesUiState.Error("The server processed the request but returned an error. Try a different video.")
                    }
                }
                .onFailure { e ->
                    _uiState.value = VideoNotesUiState.Error(friendlyMessage(e))
                }
        }
    }

    fun generateFromUpload(uri: Uri, summaryStyle: String = "detailed") {
        _uiState.value = VideoNotesUiState.Loading
        viewModelScope.launch {
            val cr       = getApplication<Application>().contentResolver
            val mimeType = cr.getType(uri) ?: "video/mp4"
            val fileName = resolveFileName(uri) ?: "upload.mp4"

            val bytes = try {
                cr.openInputStream(uri)?.use { it.readBytes() }
            } catch (e: Exception) {
                _uiState.value = VideoNotesUiState.Error("Could not read the selected file. Try choosing it again.")
                return@launch
            }

            if (bytes == null) {
                _uiState.value = VideoNotesUiState.Error("Could not open the selected file. The file may have been moved or deleted.")
                return@launch
            }

            repo.notesFromUpload(fileName, bytes, mimeType, summaryStyle)
                .onSuccess { response ->
                    _uiState.value = if (response.success) {
                        VideoNotesUiState.Success(response)
                    } else {
                        VideoNotesUiState.Error("The server processed the upload but returned an error.")
                    }
                }
                .onFailure { e ->
                    _uiState.value = VideoNotesUiState.Error(friendlyMessage(e))
                }
        }
    }

    fun saveToNotes(title: String, body: String) {
        _saveState.value = SaveState.Saving
        viewModelScope.launch {
            val ok = repo.saveToNotes(title, body)
            _saveState.value = if (ok) SaveState.Saved else SaveState.Error("Failed to save note. Please try again.")
        }
    }

    fun resetSaveState() { _saveState.value = SaveState.Idle }

    fun reset() {
        _uiState.value  = VideoNotesUiState.Idle
        _saveState.value = SaveState.Idle
    }


    private fun validateUrl(url: String): String? {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "Please enter a valid URL starting with http:// or https://"
        }
        return null
    }

    private fun friendlyMessage(e: Throwable): String = when (e) {
        is ConnectException ->
            "Cannot connect to the backend server.\n" +
            "Make sure the StudyOS video service is running on port 8000 " +
            "and your device is on the same Wi-Fi network."

        is SocketTimeoutException ->
            "The request timed out. The video may be very long, or the server is under load.\n" +
            "Try again with a shorter video."

        is UnknownHostException ->
            "Could not resolve the server address. Check that your device has internet access " +
            "and the backend host is reachable."

        is HttpException -> {
            val code = e.code()
            val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
            val detail = extractDetail(body)
            when (code) {
                400  -> "Bad request (400).\n${detail.ifBlank { "Check that the URL points to a supported video." }}"
                422  -> "Processing failed (422).\n${detail.ifBlank { "The server could not handle this video." }}"
                500  -> "Server error (500).\n${detail.ifBlank { "Something went wrong on the backend." }}"
                503  -> "Service unavailable (503). Please try again in a moment."
                else -> "Server returned HTTP $code.\n${detail.ifBlank { "Unknown error." }}"
            }
        }

        is JsonSyntaxException ->
            "The server returned an unexpected response format. " +
            "Make sure you are running the latest version of the StudyOS backend."

        else -> {
            val msg = e.message ?: e.javaClass.simpleName
            "Unexpected error: $msg"
        }
    }

    private fun extractDetail(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return ""
                val match = Regex("\"detail\"\\s*:\\s*\"([^\"]+)\"").find(errorBody)
        return match?.groupValues?.getOrNull(1) ?: errorBody.take(200)
    }

    private fun resolveFileName(uri: Uri): String? {
        val cr     = getApplication<Application>().contentResolver
        val cursor = cr.query(uri, null, null, null, null) ?: return null
        return cursor.use { c ->
            val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
        }
    }
}
