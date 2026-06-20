package com.teamdobermans.studyos.repo

import com.teamdobermans.studyos.model.UrlRequest
import com.teamdobermans.studyos.model.VideoNotesResponse
import com.teamdobermans.studyos.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class VideoNotesRepository {

    private val api = RetrofitClient.videoNotesApiService

    suspend fun notesFromUrl(url: String, summaryStyle: String = "detailed"): Result<VideoNotesResponse> = runCatching {
        api.urlToNotes(UrlRequest(url, summaryStyle))
    }

    suspend fun notesFromUpload(
        fileName: String,
        fileBytes: ByteArray,
        mimeType: String,
        summaryStyle: String = "detailed"
    ): Result<VideoNotesResponse> = runCatching {
        val requestBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
        val styleBody = summaryStyle.toRequestBody("text/plain".toMediaTypeOrNull())
        api.uploadToNotes(part, styleBody)
    }

    suspend fun saveToNotes(
        title: String,
        body: String
    ): Boolean {
        val noteRepo = NoteRepoImpl()
        return noteRepo.createNote(title = title, body = body, folder = "Video Notes")
    }
}
