package com.teamdobermans.studyos.network

import com.teamdobermans.studyos.model.UrlRequest
import com.teamdobermans.studyos.model.VideoNotesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface VideoNotesApiService {

    @POST("api/video/url-to-notes")
    suspend fun urlToNotes(@Body request: UrlRequest): VideoNotesResponse

    @Multipart
    @POST("api/video/upload-to-notes")
    suspend fun uploadToNotes(
        @Part file: MultipartBody.Part,
        @Part("summary_style") summaryStyle: RequestBody
    ): VideoNotesResponse
}
