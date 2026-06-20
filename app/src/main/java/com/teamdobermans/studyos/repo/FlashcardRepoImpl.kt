package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.teamdobermans.studyos.model.FlashcardModel
import kotlinx.coroutines.tasks.await

class FlashcardRepoImpl : FlashcardRepo {

    private val db = FirebaseFirestore.getInstance()

    private fun getCollection(): CollectionReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) return null
        return db.collection("users").document(uid).collection("flashcards")
    }

    override suspend fun getAllFlashcards(): List<FlashcardModel> {
        val collection = getCollection() ?: return emptyList()
        return try {
            collection.get().await().toObjects(FlashcardModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getDueFlashcards(): List<FlashcardModel> {
        val collection = getCollection() ?: return emptyList()
        val now = System.currentTimeMillis()
        return try {
            collection
                .whereLessThanOrEqualTo("nextReviewDate", now)
                .get()
                .await()
                .toObjects(FlashcardModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateFlashcard(card: FlashcardModel) {
        val collection = getCollection() ?: return
        if (card.id.isBlank()) return
        try {
            collection.document(card.id).set(card).await()
        } catch (e: Exception) {
            // Log or handle error
        }
    }

    override suspend fun addFlashcard(card: FlashcardModel) {
        val collection = getCollection() ?: return
        try {
            val doc = collection.document()
            doc.set(card.copy(id = doc.id)).await()
        } catch (e: Exception) {
            // Log or handle error
        }
    }
}