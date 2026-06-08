package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.teamdobermans.studyos.model.NoteModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NoteRepoImpl {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notesCollection = db.collection("notes")

    fun getNotes(): Flow<List<NoteModel>> = callbackFlow {
        var notesListener: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val userId = firebaseAuth.currentUser?.uid
            notesListener?.remove()

            if (userId == null) {
                trySend(emptyList())
                return@AuthStateListener
            }

            notesListener = notesCollection
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val notes = snapshot?.documents
                        ?.mapNotNull { it.toObject(NoteModel::class.java) }
                        ?.sortedByDescending { it.timestamp }
                        ?: emptyList()
                    trySend(notes)
                }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            notesListener?.remove()
            auth.removeAuthStateListener(authListener)
        }
    }

    suspend fun createNote(title: String, body: String, folder: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val id = notesCollection.document().id
        val note = NoteModel(
            id = id,
            title = title,
            body = body,
            folder = folder,
            timestamp = System.currentTimeMillis(),
            userId = userId
        )
        return try {
            notesCollection.document(id).set(note).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createNoteAndReturnId(title: String, body: String, folder: String): String? {
        val userId = auth.currentUser?.uid ?: return null
        val id = notesCollection.document().id
        val note = NoteModel(
            id = id,
            title = title,
            body = body,
            folder = folder,
            timestamp = System.currentTimeMillis(),
            userId = userId
        )
        return try {
            notesCollection.document(id).set(note).await()
            id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun autoSaveNote(note: NoteModel): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val updated = note.copy(userId = userId, timestamp = System.currentTimeMillis())
        return try {
            notesCollection.document(updated.id).set(updated).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateNote(note: NoteModel): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            notesCollection.document(note.id).set(
                note.copy(userId = userId, timestamp = System.currentTimeMillis())
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteNote(noteId: String) {
        try {
            notesCollection.document(noteId).delete().await()
        } catch (e: Exception) {
        }
    }
}
