package com.teamdobermans.studyos.notification

import android.content.Context
import com.teamdobermans.studyos.repo.SettingsRepository

object SessionManager {

    private const val PREFS_NAME = "StudyOSPrefs"
    const val KEY_SESSION_ACTIVE = "session_active"

    fun startSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_SESSION_ACTIVE, true).apply()
        StudyReminderScheduler.cancel(context)
    }

    fun endSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_SESSION_ACTIVE, false).apply()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(SettingsRepository.KEY_REMINDERS, true)) return
        val hour   = prefs.getInt(SettingsRepository.KEY_REMINDER_HOUR,   8)
        val minute = prefs.getInt(SettingsRepository.KEY_REMINDER_MINUTE, 0)
        StudyReminderScheduler.schedule(context, hour, minute)
    }

    fun isSessionActive(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SESSION_ACTIVE, false)
}