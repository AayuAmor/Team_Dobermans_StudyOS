package com.teamdobermans.studyos.repo

import com.teamdobermans.studyos.model.UrlRequest
import com.teamdobermans.studyos.model.VideoNotesResponse
import com.teamdobermans.studyos.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class VideoNotesRepository {

    private val api = RetrofitClient.videoNotesApiService

    suspend fun notesFromUrl(url: String): Result<VideoNotesResponse> = runCatching {
        api.urlToNotes(UrlRequest(url))
    }

    suspend fun notesFromUpload(
        fileName: String,
        fileBytes: ByteArray,
        mimeType: String
    ): Result<VideoNotesResponse> = runCatching {
        val requestBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
        api.uploadToNotes(part)
    }

    suspend fun saveToNotes(
        title: String,
        body: String
    ): Boolean {
        val noteRepo = NoteRepoImpl()
        return noteRepo.createNote(title = title, body = body, folder = "Video Notes")
    }
}
