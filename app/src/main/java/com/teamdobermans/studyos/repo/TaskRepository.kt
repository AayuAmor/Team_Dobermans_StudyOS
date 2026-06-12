package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamdobermans.studyos.model.Priority
import com.teamdobermans.studyos.model.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class TaskRepository {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId get() = auth.currentUser?.uid ?: ""

    // ── existing local store (keep for backward compat) ───────────────────
    private val remoteTaskStore = mutableListOf<Task>()

    fun getAllTasks(): List<Task> = remoteTaskStore

    fun insertTask(task: Task) {
        remoteTaskStore.add(task)
    }

    fun updateTask(updatedTask: Task) {
        val index = remoteTaskStore.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) remoteTaskStore[index] = updatedTask
    }

    // ── NEW: Firestore real-time fetch for Focus Session picker ───────────
    fun getActiveTasksFlow(): Flow<List<Task>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(userId)
            .collection("tasks")
            .whereEqualTo("done", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot.documents.mapNotNull { doc ->
                    try {
                        Task(
                            id          = doc.id,
                            title       = doc.getString("title")       ?: return@mapNotNull null,
                            description = doc.getString("description") ?: "",
                            startDate   = LocalDate.parse(doc.getString("startDate") ?: return@mapNotNull null),
                            endDate     = doc.getString("endDate")?.let { LocalDate.parse(it) },
                            priority    = Priority.valueOf(doc.getString("priority") ?: "MEDIUM"),
                            subjectId   = doc.getString("subjectId")   ?: "",
                            subjectName = doc.getString("subjectName") ?: "",
                            done        = doc.getBoolean("done")        ?: false,
                            linkedSessionIds = (doc.get("linkedSessionIds") as? List<*>)
                                ?.filterIsInstance<String>() ?: emptyList()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(tasks)
            }

        awaitClose { listener.remove() }
    }

    // ── NEW: link a session id to a task in Firestore ─────────────────────
    suspend fun linkSessionToTask(taskId: String, sessionId: String) {
        if (userId.isEmpty() || taskId.isEmpty()) return
        try {
            db.collection("users").document(userId)
                .collection("tasks").document(taskId)
                .update("linkedSessionIds",
                    com.google.firebase.firestore.FieldValue.arrayUnion(sessionId))
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}