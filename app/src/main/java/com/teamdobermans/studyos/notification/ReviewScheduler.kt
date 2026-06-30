package com.teamdobermans.studyos.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class ReviewScheduler(context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun scheduleDay1(noteId: String, noteTitle: String) =
        enqueue(noteId, noteTitle, stage = 1, delayDays = DAY_1_DELAY)

    fun scheduleDay4(noteId: String, noteTitle: String) =
        enqueue(noteId, noteTitle, stage = 2, delayDays = DAY_4_DELAY)

    fun scheduleDay7(noteId: String, noteTitle: String) =
        enqueue(noteId, noteTitle, stage = 3, delayDays = DAY_7_DELAY)

    fun cancelAll(noteId: String) {
        workManager.cancelAllWorkByTag(reviewTag(noteId))
    }

    fun cancelDay1(noteId: String) = workManager.cancelUniqueWork(workName(noteId, 1))

    fun cancelDay4(noteId: String) = workManager.cancelUniqueWork(workName(noteId, 2))

    fun cancelDay7(noteId: String) = workManager.cancelUniqueWork(workName(noteId, 3))

    private fun enqueue(noteId: String, noteTitle: String, stage: Int, delayDays: Long) {
        val request = OneTimeWorkRequestBuilder<ReviewNotificationWorker>()
            .setInitialDelay(delayDays, TimeUnit.DAYS)
            .setInputData(
                workDataOf(
                    ReviewNotificationWorker.KEY_NOTE_ID    to noteId,
                    ReviewNotificationWorker.KEY_NOTE_TITLE to noteTitle,
                    ReviewNotificationWorker.KEY_STAGE      to stage
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .addTag(reviewTag(noteId))
            .build()

        workManager.enqueueUniqueWork(
            workName(noteId, stage),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        private const val DAY_1_DELAY = 1L
        private const val DAY_4_DELAY = 4L
        private const val DAY_7_DELAY = 7L

        fun workName(noteId: String, stage: Int) = "review_${noteId}_stage_$stage"

        fun reviewTag(noteId: String) = "review_$noteId"
    }
}