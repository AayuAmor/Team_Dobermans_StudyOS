package com.teamdobermans.studyos.notification

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.teamdobermans.studyos.repo.SettingsRepository

class StudyReminderReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("StudyOSPrefs", Context.MODE_PRIVATE)

        if (!prefs.getBoolean(SettingsRepository.KEY_REMINDERS, true)) return

        StudyReminderScheduler.createChannel(context)

        val notifManager = NotificationManagerCompat.from(context)

        if (notifManager.areNotificationsEnabled()) {
            notifManager.notify(
                StudyReminderScheduler.NOTIFICATION_ID,
                StudyReminderScheduler.buildNotification(context)
            )
        }

        val hour   = prefs.getInt(SettingsRepository.KEY_REMINDER_HOUR,   8)
        val minute = prefs.getInt(SettingsRepository.KEY_REMINDER_MINUTE, 0)
        StudyReminderScheduler.schedule(context, hour, minute)
    }
}
