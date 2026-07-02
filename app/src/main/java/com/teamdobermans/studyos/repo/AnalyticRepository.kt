package com.teamdobermans.studyos.data.analytics

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.teamdobermans.studyos.model.StudyStreakModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class AnalyticsRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getCurrentStreak(): Flow<Int> {
        val uid = auth.currentUser?.uid ?: return flowOf(0)
        return callbackFlow {
            val listener = firestore.collection("users").document(uid)
                .collection("analytics").document("streak")
                .addSnapshotListener { snapshot, _ ->
                    val streak = snapshot?.toObject(StudyStreakModel::class.java) ?: StudyStreakModel()
                    trySend(streak.currentStreak)
                }
            awaitClose { listener.remove() }
        }
    }

    fun observeStreak(): Flow<StudyStreakModel> {
        val uid = auth.currentUser?.uid ?: return flowOf(StudyStreakModel())
        return callbackFlow {
            val listener = firestore.collection("users").document(uid)
                .collection("analytics").document("streak")
                .addSnapshotListener { snapshot, _ ->
                    val streak = snapshot?.toObject(StudyStreakModel::class.java) ?: StudyStreakModel()
                    trySend(streak)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun getStreak(): Result<StudyStreakModel> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Please sign in to track your streak")
        val snapshot = firestore.collection("users").document(uid)
            .collection("analytics").document("streak")
            .get().await()
        snapshot.toObject(StudyStreakModel::class.java) ?: StudyStreakModel()
    }

    suspend fun recordStudyActivity(activityType: String): Result<StudyStreakModel> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Please sign in to track your streak")
        val streakRef = firestore.collection("users").document(uid)
            .collection("analytics").document("streak")
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        Tasks.await(firestore.runTransaction { transaction ->
            val snapshot = transaction.get(streakRef)
            val existing = snapshot.toObject(StudyStreakModel::class.java) ?: StudyStreakModel()
            val currentDate = today.toString()
            val previousDate = existing.lastStudyDate
            val next = when {
                previousDate == null -> StudyStreakModel(
                    currentStreak = 1,
                    longestStreak = 1,
                    lastStudyDate = currentDate,
                    totalStudyDays = 1,
                    updatedAt = System.currentTimeMillis()
                )
                previousDate == currentDate -> existing.copy(updatedAt = System.currentTimeMillis())
                previousDate == yesterday.toString() -> existing.copy(
                    currentStreak = existing.currentStreak + 1,
                    longestStreak = maxOf(existing.longestStreak, existing.currentStreak + 1),
                    lastStudyDate = currentDate,
                    totalStudyDays = existing.totalStudyDays + 1,
                    updatedAt = System.currentTimeMillis()
                )
                else -> existing.copy(
                    currentStreak = 1,
                    longestStreak = maxOf(existing.longestStreak, 1),
                    lastStudyDate = currentDate,
                    totalStudyDays = existing.totalStudyDays + 1,
                    updatedAt = System.currentTimeMillis()
                )
            }
            transaction.set(streakRef, next.copy(lastStreakUpdatedAt = System.currentTimeMillis()), SetOptions.merge())
            next
        })
    }

    suspend fun resetStreakIfMissed(): Result<StudyStreakModel> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Please sign in to track your streak")
        val streakRef = firestore.collection("users").document(uid)
            .collection("analytics").document("streak")
        val today = LocalDate.now()
        Tasks.await(firestore.runTransaction { transaction ->
            val snapshot = transaction.get(streakRef)
            val existing = snapshot.toObject(StudyStreakModel::class.java) ?: StudyStreakModel()
            val previousDate = existing.lastStudyDate
            val reset = if (previousDate == null || previousDate == today.toString() || previousDate == today.minusDays(1).toString()) {
                existing.copy(updatedAt = System.currentTimeMillis())
            } else {
                existing.copy(currentStreak = 0, updatedAt = System.currentTimeMillis())
            }
            transaction.set(streakRef, reset.copy(updatedAt = System.currentTimeMillis()), SetOptions.merge())
            reset
        })
    }

    fun getWeeklyStudyHours(): Flow<Float> {
        val uid = auth.currentUser?.uid ?: return flowOf(0f)
        return callbackFlow {
            val listener = firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    trySend(snapshot?.getDouble("weeklyStudyHours")?.toFloat() ?: 0f)
                }
            awaitClose { listener.remove() }
        }
    }

    fun getPendingTaskCount(): Flow<Int> {
        val uid = auth.currentUser?.uid ?: return flowOf(0)
        return callbackFlow {
            val listener = firestore.collection("users").document(uid)
                .collection("tasks")
                .whereEqualTo("done", false)
                .addSnapshotListener { snapshot, _ ->
                    trySend(snapshot?.size() ?: 0)
                }
            awaitClose { listener.remove() }
        }
    }

    fun getDailyTaskProgress(): Flow<Float> {
        val uid = auth.currentUser?.uid ?: return flowOf(0f)
        return callbackFlow {
            val listener = firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    trySend(snapshot?.getDouble("dailyTaskProgress")?.toFloat() ?: 0f)
                }
            awaitClose { listener.remove() }
        }
    }
}