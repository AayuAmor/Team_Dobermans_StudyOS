package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.teamdobermans.studyos.model.BrainGameMode
import com.teamdobermans.studyos.model.BrainGameScoreModel
import kotlinx.coroutines.tasks.await

class BrainGameRepositoryImpl : BrainGameRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun saveScore(score: BrainGameScoreModel): Result<Unit> {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Please sign in to save your score"))
        }

        return try {
            val document = firestore.collection("users")
                .document(uid)
                .collection("brainGameScores")
                .document(score.id.ifBlank { java.util.UUID.randomUUID().toString() })
            document.set(score.copy(userId = uid)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBestScore(mode: BrainGameMode): Result<BrainGameScoreModel?> {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            return Result.success(null)
        }

        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("brainGameScores")
                .whereEqualTo("mode", mode.name)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            val best = snapshot.documents.firstOrNull()?.toObject(BrainGameScoreModel::class.java)
            Result.success(best)
        } catch (e: Exception) {
            Result.success(null)
        }
    }

    override suspend fun getRecentScores(limit: Int): Result<List<BrainGameScoreModel>> {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            return Result.success(emptyList())
        }

        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("brainGameScores")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            val scores = snapshot.documents.mapNotNull { it.toObject(BrainGameScoreModel::class.java) }
            Result.success(scores)
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }
}
