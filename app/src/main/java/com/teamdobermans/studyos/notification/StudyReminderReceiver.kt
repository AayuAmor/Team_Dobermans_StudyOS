package com.teamdobermans.studyos.notification

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.teamdobermans.studyos.repo.SettingsRepository

/**
 * Receives the AlarmManager broadcast each day at the user's reminder time.
 *
 * Responsibilities:
 *  1. Verify reminders are still enabled (user may have toggled off after scheduling).
 *  2. Verify notification permission is granted.
 *  3. Post the study reminder notification.
 *  4. Re-arm the alarm for the same time tomorrow (AlarmManager.setExact* is one-shot).
 */
class StudyReminderReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")  // areNotificationsEnabled() is the effective permission check
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("StudyOSPrefs", Context.MODE_PRIVATE)

        // Respect the current toggle state (user may disable after alarm was armed)
        if (!prefs.getBoolean(SettingsRepository.KEY_REMINDERS, true)) return

        // Ensure notification channel exists
        StudyReminderScheduler.createChannel(context)

        val notifManager = NotificationManagerCompat.from(context)

        // areNotificationsEnabled() returns false if the user has blocked notifications
        // for the app (or revoked POST_NOTIFICATIONS on Android 13+).
        if (notifManager.areNotificationsEnabled()) {
            notifManager.notify(
                StudyReminderScheduler.NOTIFICATION_ID,
                StudyReminderScheduler.buildNotification(context)
            )
        }

        // Re-arm for the same time tomorrow.
        val hour   = prefs.getInt(SettingsRepository.KEY_REMINDER_HOUR,   8)
        val minute = prefs.getInt(SettingsRepository.KEY_REMINDER_MINUTE, 0)
        StudyReminderScheduler.schedule(context, hour, minute)
    }
}
