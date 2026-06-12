package com.teamdobermans.studyos.repo

import com.teamdobermans.studyos.model.NoteModel
import kotlinx.coroutines.flow.Flow

interface NoteRepo {
    fun getNotes(): Flow<List<NoteModel>>
    suspend fun createNote(title: String, body: String, folder: String): Boolean
    suspend fun updateNote(note: NoteModel): Boolean
    suspend fun deleteNote(noteId: String)
}
