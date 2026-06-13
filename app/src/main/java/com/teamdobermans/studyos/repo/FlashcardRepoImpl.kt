package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.teamdobermans.studyos.model.FlashcardModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class FlashcardRepoImpl : FlashcardRepo {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun userCardsCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("flashcards")
    }

    override fun observeFlashcards(deckName: String?): Flow<List<FlashcardModel>> {
        val collection = userCardsCollection() ?: return flowOf(emptyList())
        return callbackFlow {
            val query = if (deckName != null) {
                collection.whereEqualTo("deckName", deckName)
            } else {
                collection
            }
            val listener = query.addSnapshotListener { snapshot, _ ->
                val cards = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(FlashcardModel::class.java)?.copy(id = doc.id)
                    }
                    ?.sortedByDescending { it.createdAt }
                    ?: emptyList()
                trySend(cards)
            }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun createFlashcard(front: String, back: String, deckName: String): String? {
        val collection = userCardsCollection() ?: return null
        val uid = auth.currentUser?.uid ?: return null
        val docRef = collection.document()
        val now = System.currentTimeMillis()
        val card = FlashcardModel(
            id = docRef.id,
            userId = uid,
            front = front.trim(),
            back = back.trim(),
            deckName = deckName.trim().ifBlank { "General" },
            createdAt = now,
            updatedAt = now
        )
        return try {
            docRef.set(card).await()
            docRef.id
        } catch (e: Exception) { null }
    }

    override suspend fun updateFlashcard(card: FlashcardModel): Boolean {
        val collection = userCardsCollection() ?: return false
        return try {
            collection.document(card.id)
                .set(card.copy(updatedAt = System.currentTimeMillis()))
                .await()
            true
        } catch (e: Exception) { false }
    }

    override suspend fun deleteFlashcard(cardId: String) {
        val collection = userCardsCollection() ?: return
        try { collection.document(cardId).delete().await() } catch (_: Exception) {}
    }

    override suspend fun recordReview(cardId: String) {
        val collection = userCardsCollection() ?: return
        try {
            collection.document(cardId).update(
                mapOf(
                    "lastReviewedAt" to System.currentTimeMillis(),
                    "reviewCount" to FieldValue.increment(1),
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
        } catch (_: Exception) {}
    }
}