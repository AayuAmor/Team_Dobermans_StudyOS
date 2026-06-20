package com.teamdobermans.studyos.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.teamdobermans.studyos.MainActivity
import com.teamdobermans.studyos.R

class ReviewNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_NOTE_ID    = "note_id"
        const val KEY_NOTE_TITLE = "note_title"
        const val KEY_STAGE      = "stage"

        const val CHANNEL_ID   = "review_reminders"
        const val CHANNEL_NAME = "Review Reminders"

        fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description    = "Spaced repetition review reminders for your study notes"
                enableVibration(true)
                enableLights(true)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override suspend fun doWork(): Result {
        val noteId    = inputData.getString(KEY_NOTE_ID)    ?: return Result.failure()
        val noteTitle = inputData.getString(KEY_NOTE_TITLE) ?: "your note"
        val stage     = inputData.getInt(KEY_STAGE, 1)

        createChannel(context)
        sendNotification(noteId, noteTitle, stage)
        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification(noteId: String, noteTitle: String, stage: Int) {
        val stageLabel = when (stage) {
            1    -> "Day 1"
            2    -> "Day 4"
            3    -> "Day 7"
            else -> "Review"
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_NOTE_ID, noteId)
            putExtra(EXTRA_REVIEW_STAGE, stage)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            noteId.hashCode() + stage,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_assignment_24)
            .setContentTitle("Time to review your notes")
            .setContentText("Review \"$noteTitle\" to improve retention.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Review \"$noteTitle\" to improve retention.\n" +
                                "This is your $stageLabel spaced repetition review."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .addAction(
                R.drawable.baseline_assignment_24,
                "Open Note",
                openPendingIntent
            )
            .setAutoCancel(true)
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context)
                .notify(noteId.hashCode() + stage, notification)
        }
    }
}

const val EXTRA_OPEN_NOTE_ID  = "open_note_id"
const val EXTRA_REVIEW_STAGE  = "review_stage"