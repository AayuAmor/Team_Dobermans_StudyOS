package com.teamdobermans.studyos.model

import com.google.gson.annotations.SerializedName

data class UrlRequest(
    val url: String,
    @SerializedName("summary_style") val summaryStyle: String = "detailed"
)

data class ImportantTerm(
    val term: String,
    val meaning: String
)

data class StudyNoteSection(
    val heading: String,
    val points: List<String>
)

data class NotesOutput(
    val summary: String = "",
    @SerializedName("key_points")        val keyPoints: List<String>          = emptyList(),
    @SerializedName("important_terms")   val importantTerms: List<ImportantTerm>  = emptyList(),
    @SerializedName("study_notes")       val studyNotes: List<StudyNoteSection>   = emptyList(),
    @SerializedName("possible_questions") val possibleQuestions: List<String>  = emptyList()
)

data class VideoNotesResponse(
    val success: Boolean,
    @SerializedName("source_type") val sourceType: String,
    val title: String,
    val transcript: String,
    val notes: NotesOutput
)
