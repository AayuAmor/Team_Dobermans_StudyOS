package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.teamdobermans.studyos.data.local.database.StudyOSDatabase
import com.teamdobermans.studyos.data.local.entities.TaskEntity
import com.teamdobermans.studyos.data.sync.PendingSyncEntity
import com.teamdobermans.studyos.data.sync.SyncStatus
import com.teamdobermans.studyos.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.UUID

class TaskRepository {

    private val auth     = FirebaseAuth.getInstance()
    private val db       = StudyOSDatabase.getInstance()
    private val taskDao  = db.taskDao()
    private val syncDao  = db.pendingSyncDao()

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val uid get() = auth.currentUser?.uid ?: ""

    fun getAllTasks(): List<Task> = runBlocking(Dispatchers.IO) {
        if (uid.isEmpty()) return@runBlocking emptyList()
        taskDao.getAllForUser(uid).map { it.toTask() }
    }

    fun insertTask(task: Task) {
        repoScope.launch {
            if (uid.isEmpty()) return@launch
            taskDao.insert(TaskEntity.fromTask(task, uid, SyncStatus.PENDING))
            queueSync(task.id, "TASK", "CREATE")
        }
    }

    fun updateTask(updatedTask: Task) {
        repoScope.launch {
            if (uid.isEmpty()) return@launch
            val existing = taskDao.getById(updatedTask.id)
            val entity   = TaskEntity.fromTask(updatedTask, uid, SyncStatus.PENDING)
            taskDao.insert(entity.copy(createdAt = existing?.createdAt ?: entity.createdAt))
            queueSync(updatedTask.id, "TASK", "UPDATE")
        }
    }

    fun getTodaysTasks(): List<Task> = runBlocking(Dispatchers.IO) {
        if (uid.isEmpty()) return@runBlocking emptyList()
        val today = LocalDate.now()
        taskDao.getAllForUser(uid)
            .map { it.toTask() }
            .filter { !it.done && !it.startDate.isAfter(today) }
    }

    fun getActiveTasksFlow(): Flow<List<Task>> {
        if (uid.isEmpty()) return kotlinx.coroutines.flow.flowOf(emptyList())
        return taskDao.getTasksForUser(uid).map { entities ->
            entities.map { it.toTask() }
        }
    }

    fun linkSessionToTask(taskId: String, sessionId: String) {
        repoScope.launch {
            val entity = taskDao.getById(taskId) ?: return@launch
            val updated = if (entity.linkedNoteIds.isBlank()) sessionId
                          else "${entity.linkedNoteIds},$sessionId"
            taskDao.updateLinkedNotes(
                taskId       = taskId,
                linkedNoteIds = updated,
                updatedAt    = System.currentTimeMillis(),
                syncStatus   = SyncStatus.PENDING.name
            )
            queueSync(taskId, "TASK", "UPDATE")
        }
    }

    fun getTasksForNote(noteId: String): List<Task> = runBlocking(Dispatchers.IO) {
        if (uid.isEmpty()) return@runBlocking emptyList()
        taskDao.getAllForUser(uid)
            .map { it.toTask() }
            .filter { noteId in it.linkedNoteIds }
    }

    fun attachNoteToTask(taskId: String, noteId: String) {
        repoScope.launch {
            val entity = taskDao.getById(taskId) ?: return@launch
            val ids = entity.linkedNoteIds
                .split(",")
                .filter { it.isNotBlank() }
                .toMutableList()
            if (noteId !in ids) {
                ids.add(noteId)
                taskDao.updateLinkedNotes(
                    taskId        = taskId,
                    linkedNoteIds = ids.joinToString(","),
                    updatedAt     = System.currentTimeMillis(),
                    syncStatus    = SyncStatus.PENDING.name
                )
                queueSync(taskId, "TASK", "UPDATE")
            }
        }
    }

    fun removeNoteFromTask(taskId: String, noteId: String) {
        repoScope.launch {
            val entity = taskDao.getById(taskId) ?: return@launch
            val ids = entity.linkedNoteIds
                .split(",")
                .filter { it.isNotBlank() && it != noteId }
            taskDao.updateLinkedNotes(
                taskId        = taskId,
                linkedNoteIds = ids.joinToString(","),
                updatedAt     = System.currentTimeMillis(),
                syncStatus    = SyncStatus.PENDING.name
            )
            queueSync(taskId, "TASK", "UPDATE")
        }
    }

    private suspend fun queueSync(entityId: String, entityType: String, operation: String) {
        syncDao.insert(
            PendingSyncEntity(
                id         = UUID.randomUUID().toString(),
                entityType = entityType,
                entityId   = entityId,
                operation  = operation,
                createdAt  = System.currentTimeMillis()
            )
        )
    }
}
