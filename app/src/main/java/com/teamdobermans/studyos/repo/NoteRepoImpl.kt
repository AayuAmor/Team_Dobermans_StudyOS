package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamdobermans.studyos.model.NoteModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class NoteRepoImpl : NoteRepo {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notesCollection = firestore.collection("notes")

    override fun getNotes(): Flow<List<NoteModel>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = notesCollection
                .whereEqualTo("userId", uid)
                .addSnapshotListener { snapshot, _ ->
                    val notes = snapshot?.documents
                        ?.mapNotNull { it.toObject(NoteModel::class.java) }
                        ?: emptyList()
                    trySend(notes.sortedByDescending { it.lastActivityTime() })
                }
            awaitClose { listener.remove() }
        }
    }

    private fun NoteModel.lastActivityTime(): Long = when {
        updatedAt > 0L -> updatedAt
        timestamp > 0L -> timestamp
        createdAt > 0L -> createdAt
        else -> 0L
    }

    override suspend fun createNote(title: String, body: String, folder: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val docRef = notesCollection.document()
        val note = NoteModel(
            id = docRef.id, title = title, body = body, folder = folder,
            timestamp = System.currentTimeMillis(), userId = userId
        )
        return try {
            docRef.set(note).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createNoteAndReturnId(title: String, body: String, folder: String): String? {
        val userId = auth.currentUser?.uid ?: return null
        val docRef = notesCollection.document()
        val note = NoteModel(
            id = docRef.id, title = title, body = body, folder = folder,
            timestamp = System.currentTimeMillis(), userId = userId
        )
        return try {
            docRef.set(note).await()
            docRef.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun autoSaveNote(note: NoteModel): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            notesCollection.document(note.id)
                .set(note.copy(userId = userId, timestamp = System.currentTimeMillis()))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateNote(note: NoteModel): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            notesCollection.document(note.id).set(note.copy(userId = userId)).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteNote(noteId: String) {
        try {
            notesCollection.document(noteId).delete().await()
        } catch (_: Exception) {
        }
    }
}
