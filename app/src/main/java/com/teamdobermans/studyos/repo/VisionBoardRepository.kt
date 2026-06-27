package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamdobermans.studyos.model.VisionGoalModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class VisionBoardRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    fun observePinnedGoals(): Flow<List<VisionGoalModel>> {
        val uid = currentUserId ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = firestore.collection("users")
                .document(uid)
                .collection("visionBoard")
                .orderBy("createdAt")
                .addSnapshotListener { snapshot, _ ->
                    val goals = snapshot?.documents?.mapNotNull { doc ->
                        VisionGoalModel(
                            id = doc.id,
                            userId = uid,
                            text = doc.getString("text") ?: return@mapNotNull null,
                            emoji = doc.getString("emoji") ?: "🏅",
                            targetValue = doc.getString("targetValue") ?: "",
                            subject = doc.getString("subject") ?: "General"
                        )
                    } ?: emptyList()
                    trySend(goals)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun addGoal(text: String, emoji: String, targetValue: String, subject: String): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(IllegalStateException("Please sign in first"))
        val data = hashMapOf(
            "text" to text,
            "emoji" to emoji,
            "targetValue" to targetValue,
            "subject" to subject,
            "createdAt" to System.currentTimeMillis()
        )
        return runCatching {
            firestore.collection("users").document(uid).collection("visionBoard").add(data).await()
            Unit
        }
    }

    suspend fun updateGoal(goal: VisionGoalModel): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(IllegalStateException("Please sign in first"))
        if (goal.id.isBlank()) return Result.failure(IllegalArgumentException("Goal id is missing"))
        val data = mapOf(
            "text" to goal.text.trim(),
            "emoji" to goal.emoji,
            "targetValue" to goal.targetValue.trim(),
            "subject" to goal.subject
        )
        return runCatching {
            firestore.collection("users").document(uid).collection("visionBoard").document(goal.id).update(data).await()
        }
    }

    suspend fun deleteGoal(goalId: String): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(IllegalStateException("Please sign in first"))
        if (goalId.isBlank()) return Result.failure(IllegalArgumentException("Goal id is missing"))
        return runCatching {
            firestore.collection("users").document(uid).collection("visionBoard").document(goalId).delete().await()
        }
    }
}
