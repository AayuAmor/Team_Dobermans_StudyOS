package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamdobermans.studyos.data.local.database.StudyOSDatabase
import com.teamdobermans.studyos.data.local.entities.NoteEntity
import com.teamdobermans.studyos.data.sync.PendingSyncEntity
import com.teamdobermans.studyos.data.sync.SyncStatus
import com.teamdobermans.studyos.model.NoteModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NoteRepoImpl : NoteRepo {

    private val firestore       = FirebaseFirestore.getInstance()
    private val auth            = FirebaseAuth.getInstance()
    private val notesCollection = firestore.collection("notes")
    private val db              = StudyOSDatabase.getInstance()
    private val noteDao         = db.noteDao()
    private val syncDao         = db.pendingSyncDao()

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        bootstrapFromFirestore()
    }

    /**
     * Listens to Firestore in the background and mirrors any remotely authoritative
     * notes into Room. Notes with PENDING/SYNCING local status are skipped so in-flight
     * local edits are never overwritten.
     */
    private fun bootstrapFromFirestore() {
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid ?: return@addAuthStateListener
            notesCollection
                .whereEqualTo("userId", uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot == null) return@addSnapshotListener
                    val remoteNotes = snapshot.documents
                        .mapNotNull { it.toObject(NoteModel::class.java) }
                    repoScope.launch {
                        remoteNotes.forEach { note ->
                            val existing = noteDao.getById(note.id)
                            if (existing == null || existing.syncStatus == SyncStatus.SYNCED.name) {
                                noteDao.insert(NoteEntity.fromModel(note, SyncStatus.SYNCED))
                            }
                        }
                    }
                }
        }
    }

    override fun getNotes(): Flow<List<NoteModel>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return noteDao.getNotesForUser(uid).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun createNote(title: String, body: String, folder: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val id     = notesCollection.document().id
        val note   = NoteModel(
            id        = id,
            title     = title,
            body      = body,
            folder    = folder,
            timestamp = System.currentTimeMillis(),
            userId    = userId
        )
        noteDao.insert(NoteEntity.fromModel(note, SyncStatus.PENDING))
        queueSync(id, "NOTE", "CREATE")
        return true
    }

    suspend fun createNoteAndReturnId(title: String, body: String, folder: String): String? {
        val userId = auth.currentUser?.uid ?: return null
        val id     = notesCollection.document().id
        val note   = NoteModel(
            id        = id,
            title     = title,
            body      = body,
            folder    = folder,
            timestamp = System.currentTimeMillis(),
            userId    = userId
        )
        noteDao.insert(NoteEntity.fromModel(note, SyncStatus.PENDING))
        queueSync(id, "NOTE", "CREATE")
        return id
    }

    suspend fun autoSaveNote(note: NoteModel): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        noteDao.insert(NoteEntity.fromModel(note.copy(userId = userId), SyncStatus.PENDING))
        queueSync(note.id, "NOTE", "UPDATE")
        return true
    }

    override suspend fun updateNote(note: NoteModel): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        noteDao.insert(NoteEntity.fromModel(note.copy(userId = userId), SyncStatus.PENDING))
        queueSync(note.id, "NOTE", "UPDATE")
        return true
    }

    override suspend fun deleteNote(noteId: String) {
        noteDao.deleteById(noteId)
        syncDao.deleteByEntityId(noteId)
        try {
            notesCollection.document(noteId).delete().await()
        } catch (_: Exception) {
            queueSync(noteId, "NOTE", "DELETE")
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
