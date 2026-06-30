package com.teamdobermans.studyos.repo

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.notification.ReviewScheduler
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class ReviewReminderRepository(context: Context) {

    private val scheduler = ReviewScheduler(context)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth      = FirebaseAuth.getInstance()

    private fun noteDoc(noteId: String) =
        firestore.collection("notes").document(noteId)

    suspend fun enableReminder(note: NoteModel): Boolean {
        val now = System.currentTimeMillis()
        val updates = mapOf(
            "reminderEnabled" to true,
            "reviewStage"     to 0,
            "createdAt"       to if (note.createdAt == 0L) now else note.createdAt,
            "nextReviewAt"    to (now + DAY_MS),
            "lastReviewedAt"  to 0L
        )
        return try {
            noteDoc(note.id).update(updates).await()
            scheduler.scheduleDay1(note.id, note.title)
            true
        } catch (_: Exception) { false }
    }

    suspend fun disableReminder(noteId: String): Boolean {
        return try {
            noteDoc(noteId).update(
                mapOf(
                    "reminderEnabled" to false,
                    "nextReviewAt"    to 0L
                )
            ).await()
            scheduler.cancelAll(noteId)
            true
        } catch (_: Exception) { false }
    }

    suspend fun markReviewed(note: NoteModel): Boolean {
        if (note.reviewStage >= MAX_STAGE) return true

        val now       = System.currentTimeMillis()
        val newStage  = note.reviewStage + 1
        val nextReviewAt = when (newStage) {
            1    -> now + DAY_MS
            2    -> now + 4 * DAY_MS
            3    -> now + 7 * DAY_MS
            else -> 0L
        }

        return try {
            noteDoc(note.id).update(
                mapOf(
                    "reviewStage"    to newStage,
                    "lastReviewedAt" to now,
                    "nextReviewAt"   to nextReviewAt
                )
            ).await()

            when (note.reviewStage) {
                0 -> { scheduler.cancelDay1(note.id); if (newStage < MAX_STAGE) scheduler.scheduleDay4(note.id, note.title) }
                1 -> { scheduler.cancelDay4(note.id); if (newStage < MAX_STAGE) scheduler.scheduleDay7(note.id, note.title) }
                2 -> { scheduler.cancelDay7(note.id) }
            }

            true
        } catch (_: Exception) { false }
    }

    fun observeNote(noteId: String): Flow<NoteModel?> {
        return callbackFlow {
            val listener = noteDoc(noteId).addSnapshotListener { snap, _ ->
                trySend(snap?.toObject(NoteModel::class.java))
            }
            awaitClose { listener.remove() }
        }
    }

    fun getUpcomingReviews(): Flow<List<NoteModel>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = firestore.collection("notes")
                .whereEqualTo("userId", uid)
                .whereEqualTo("reminderEnabled", true)
                .addSnapshotListener { snapshot, _ ->
                    val notes = snapshot?.documents
                        ?.mapNotNull { it.toObject(NoteModel::class.java) }
                        ?.filter { it.nextReviewAt > 0L && it.reviewStage < MAX_STAGE }
                        ?.sortedBy { it.nextReviewAt }
                        ?: emptyList()
                    trySend(notes)
                }
            awaitClose { listener.remove() }
        }
    }

    companion object {
        const val DAY_MS   = 24L * 60 * 60 * 1000
        const val MAX_STAGE = 3

        const val STAGE_1_DELAY_DAYS = 1
        const val STAGE_2_DELAY_DAYS = 4
        const val STAGE_3_DELAY_DAYS = 7
    }
}