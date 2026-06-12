package com.teamdobermans.studyos.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.teamdobermans.studyos.MainActivity
import com.teamdobermans.studyos.R
import com.teamdobermans.studyos.repo.SettingsRepository
import java.util.Calendar

/**
 * Central coordinator for study reminder alarms.
 *
 * Alarm lifetime:
 *  - schedule()  → one-shot exact alarm for the next occurrence of (hour:minute)
 *  - Receiver    → re-arms the alarm for the following day after each fire
 *  - cancel()    → removes the pending alarm
 *  - rescheduleIfEnabled() → called by BootReceiver to survive reboots
 */
object StudyReminderScheduler {

    const val CHANNEL_ID      = "study_reminders"
    const val NOTIFICATION_ID = 1001
    private const val ALARM_RC = 1001  // PendingIntent request code

    // ── Notification channel ──────────────────────────────────────────────────

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Study Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description  = "Daily study session reminders"
            enableVibration(true)
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    // ── Alarm scheduling ──────────────────────────────────────────────────────

    /**
     * Schedules a one-shot alarm for the next occurrence of [hour]:[minute].
     * If that time has already passed today the alarm fires tomorrow.
     * The receiver re-arms this for the following day after each fire.
     */
    fun schedule(context: Context, hour: Int, minute: Int) {
        createChannel(context)

        val alarmManager  = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = alarmPendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT)

        val triggerAt = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE,      minute)
            set(Calendar.SECOND,      0)
            set(Calendar.MILLISECOND, 0)
            // Already passed today → push to tomorrow
            if (!after(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        // canScheduleExactAlarms() requires SCHEDULE_EXACT_ALARM (declared in manifest).
        // Graceful fallback: setAndAllowWhileIdle delivers within a few minutes of
        // the target time, still reliable enough for a study reminder.
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel(context: Context) {
        val alarmManager  = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = alarmPendingIntent(context, PendingIntent.FLAG_NO_CREATE)
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    /** Called by BootReceiver — re-schedules only if the user has reminders enabled. */
    fun rescheduleIfEnabled(context: Context) {
        val prefs = context.getSharedPreferences("StudyOSPrefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean(SettingsRepository.KEY_REMINDERS, true)) return
        val hour   = prefs.getInt(SettingsRepository.KEY_REMINDER_HOUR,   8)
        val minute = prefs.getInt(SettingsRepository.KEY_REMINDER_MINUTE, 0)
        schedule(context, hour, minute)
    }

    // ── Notification builder ──────────────────────────────────────────────────

    fun buildNotification(context: Context) =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_assignment_24)
            .setContentTitle("Time to Study! 📚")
            .setContentText("Your daily study session is waiting. Let's make progress today!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your daily study session is waiting. Let's make progress today!")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppPendingIntent(context))
            .setAutoCancel(true)
            .build()

    // ── PendingIntent helpers ─────────────────────────────────────────────────

    private fun alarmPendingIntent(context: Context, extraFlags: Int): PendingIntent? =
        PendingIntent.getBroadcast(
            context,
            ALARM_RC,
            Intent(context, StudyReminderReceiver::class.java),
            extraFlags or PendingIntent.FLAG_IMMUTABLE
        )

    private fun openAppPendingIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}
