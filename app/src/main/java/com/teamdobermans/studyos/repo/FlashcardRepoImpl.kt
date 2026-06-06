package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.teamdobermans.studyos.model.FlashcardModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FlashcardRepoImpl {
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val col  = db.collection("flashcards")

    fun getFlashcards(): Flow<List<FlashcardModel>> = callbackFlow {
        var listener: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val userId = firebaseAuth.currentUser?.uid
            listener?.remove()

            if (userId == null) {
                trySend(emptyList())
                return@AuthStateListener
            }

            listener = col
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                    val cards = snapshot?.documents
                        ?.mapNotNull { it.toObject(FlashcardModel::class.java) }
                        ?.sortedBy { it.timestamp }
                        ?: emptyList()
                    trySend(cards)
                }
        }

        auth.addAuthStateListener(authListener)
        awaitClose {
            listener?.remove()
            auth.removeAuthStateListener(authListener)
        }
    }

    suspend fun createFlashcard(question: String, answer: String, subject: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val id = col.document().id
        val card = FlashcardModel(
            id        = id,
            question  = question,
            answer    = answer,
            subject   = subject,
            timestamp = System.currentTimeMillis(),
            userId    = userId
        )
        return try { col.document(id).set(card).await(); true } catch (e: Exception) { false }
    }

    suspend fun updateFlashcard(card: FlashcardModel): Boolean {
        return try { col.document(card.id).set(card).await(); true } catch (e: Exception) { false }
    }

    suspend fun deleteFlashcard(cardId: String) {
        try { col.document(cardId).delete().await() } catch (e: Exception) { }
    }
}