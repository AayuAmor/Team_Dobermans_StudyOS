package com.teamdobermans.studyos.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.teamdobermans.studyos.model.FocusSessionModel
import com.teamdobermans.studyos.model.Task
import kotlinx.coroutines.tasks.await

class FocusSessionRepoImpl : FocusSessionRepo {

    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun getTasks(userId: String): List<Task> {
        return try {
            val snapshot = firestore
                .collection("users")
                .document(userId)
                .collection("tasks")
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Task::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun saveSession(session: FocusSessionModel): Boolean {
        return try {
            firestore
                .collection("users")
                .document(session.userId)
                .collection("focusSessions")
                .document(session.id)
                .set(session)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getSessions(userId: String): List<FocusSessionModel> {
        return try {
            val snapshot = firestore
                .collection("users")
                .document(userId)
                .collection("focusSessions")
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(FocusSessionModel::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}