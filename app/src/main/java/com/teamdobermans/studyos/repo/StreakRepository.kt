package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Transaction
import com.teamdobermans.studyos.model.StudyStreakModel
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

interface StreakRepository {
    suspend fun getStreak(): Result<StudyStreakModel>
    suspend fun recordStudyActivity(activityType: String): Result<StudyStreakModel>
    suspend fun resetStreakIfMissed(): Result<StudyStreakModel>
}

class FirestoreStreakRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : StreakRepository {

    private fun streakDocumentRef() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("analytics").document("streak")
    }

    override suspend fun getStreak(): Result<StudyStreakModel> = runCatching {
        val uid = auth.currentUser?.uid ?: error("No authenticated user")
        val doc = firestore.collection("users").document(uid).collection("analytics").document("streak")
            .get().await()
        doc.toObject(StudyStreakModel::class.java) ?: StudyStreakModel()
    }

    override suspend fun recordStudyActivity(activityType: String): Result<StudyStreakModel> = runCatching {
        val ref = streakDocumentRef() ?: error("No authenticated user")
        val today = LocalDate.now().toString()
        val result = firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val existing = snapshot.toObject(StudyStreakModel::class.java) ?: StudyStreakModel()
            val next = StudyStreakModel.calculateNextStreak(
                currentStreak = existing.currentStreak,
                longestStreak = existing.longestStreak,
                totalStudyDays = existing.totalStudyDays,
                lastStudyDate = existing.lastStudyDate,
                today = LocalDate.now()
            )
            transaction.set(ref, next.copy(lastStreakUpdatedAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()), SetOptions.merge())
            next
        }.await()
        result
    }

    override suspend fun resetStreakIfMissed(): Result<StudyStreakModel> = runCatching {
        val ref = streakDocumentRef() ?: error("No authenticated user")
        val result = firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val existing = snapshot.toObject(StudyStreakModel::class.java) ?: StudyStreakModel()
            val reset = StudyStreakModel.resetIfMissed(
                currentStreak = existing.currentStreak,
                longestStreak = existing.longestStreak,
                lastStudyDate = existing.lastStudyDate,
                totalStudyDays = existing.totalStudyDays,
                today = LocalDate.now()
            )
            transaction.set(ref, reset.copy(updatedAt = System.currentTimeMillis()), SetOptions.merge())
            reset
        }.await()
        result
    }
}
