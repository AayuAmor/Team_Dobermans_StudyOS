package com.teamdobermans.studyos.data.analytics

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

class AnalyticsRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getCurrentStreak(): Flow<Int> {
        val uid = auth.currentUser?.uid ?: return flowOf(0)
        return callbackFlow {
            val listener = firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    trySend(snapshot?.getLong("streak")?.toInt() ?: 0)
                }
            awaitClose { listener.remove() }
        }
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