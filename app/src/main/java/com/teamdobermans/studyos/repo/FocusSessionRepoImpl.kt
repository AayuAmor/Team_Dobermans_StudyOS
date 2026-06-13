package com.teamdobermans.studyos.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.teamdobermans.studyos.data.local.database.StudyOSDatabase
import com.teamdobermans.studyos.data.local.entities.SessionEntity
import com.teamdobermans.studyos.data.sync.PendingSyncEntity
import com.teamdobermans.studyos.data.sync.SyncStatus
import com.teamdobermans.studyos.model.FocusSessionModel
import com.teamdobermans.studyos.model.Task
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FocusSessionRepoImpl : FocusSessionRepo {

    private val firestore   = FirebaseFirestore.getInstance()
    private val db          = StudyOSDatabase.getInstance()
    private val sessionDao  = db.sessionDao()
    private val syncDao     = db.pendingSyncDao()

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
        sessionDao.insert(SessionEntity.fromModel(session, SyncStatus.PENDING))
        syncDao.insert(
            PendingSyncEntity(
                id         = UUID.randomUUID().toString(),
                entityType = "SESSION",
                entityId   = session.id,
                operation  = "CREATE",
                createdAt  = System.currentTimeMillis()
            )
        )
        return true
    }

    override suspend fun getSessions(userId: String): List<FocusSessionModel> {
        return try {
            val remote = firestore
                .collection("users")
                .document(userId)
                .collection("focusSessions")
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(FocusSessionModel::class.java) }

            remote.forEach { model ->
                val existing = sessionDao.getPending(userId)
                    .none { it.id == model.id }
                if (existing) {
                    sessionDao.insert(SessionEntity.fromModel(model, SyncStatus.SYNCED))
                }
            }

            remote
        } catch (e: Exception) {
            emptyList()
        }
    }
}
