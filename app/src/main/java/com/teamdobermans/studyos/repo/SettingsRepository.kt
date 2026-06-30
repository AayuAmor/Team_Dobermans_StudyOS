package com.teamdobermans.studyos.repo

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class SettingsRepository(context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val prefs = context.getSharedPreferences("StudyOSPrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_REMINDERS = "reminders_enabled"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
        const val KEY_DAILY_GOAL_MINUTES = "daily_goal_minutes"
        const val KEY_FOCUS_MINUTES = "focus_duration_minutes"
        const val KEY_BREAK_MINUTES = "break_duration_minutes"
    }

    fun getCachedReminderPreference(): Boolean =
        prefs.getBoolean(KEY_REMINDERS, true)

    suspend fun getReminderPreference(): Boolean {
        val local = prefs.getBoolean(KEY_REMINDERS, true)
        val uid = auth.currentUser?.uid ?: return local

        return runCatching {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.contains("remindersEnabled")) {
                val remote = doc.getBoolean("remindersEnabled") ?: true
                prefs.edit().putBoolean(KEY_REMINDERS, remote).apply()
                remote
            } else {
                local
            }
        }.getOrDefault(local)
    }

    suspend fun setReminderPreference(enabled: Boolean): Boolean {
        prefs.edit().putBoolean(KEY_REMINDERS, enabled).apply()

        val uid = auth.currentUser?.uid ?: return false

        return runCatching {
            val data = mapOf(
                "remindersEnabled" to enabled,
                "updatedAt" to Timestamp.now()
            )
            db.collection("users").document(uid).set(data, SetOptions.merge()).await()
            true
        }.getOrDefault(false)
    }

    fun getCachedReminderTime(): Pair<Int, Int> = Pair(
        prefs.getInt(KEY_REMINDER_HOUR, 8),
        prefs.getInt(KEY_REMINDER_MINUTE, 0)
    )

    suspend fun getReminderTime(): Pair<Int, Int> {
        val local = getCachedReminderTime()
        val uid = auth.currentUser?.uid ?: return local

        return runCatching {
            val doc = db.collection("users").document(uid).get().await()
            val h =
                if (doc.contains("reminderHour")) (doc.getLong("reminderHour")?.toInt() ?: local.first) else local.first
            val m = if (doc.contains("reminderMinute")) (doc.getLong("reminderMinute")?.toInt()
                ?: local.second) else local.second
            prefs.edit().putInt(KEY_REMINDER_HOUR, h).putInt(KEY_REMINDER_MINUTE, m).apply()
            Pair(h, m)
        }.getOrDefault(local)
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()

        val uid = auth.currentUser?.uid ?: return

        runCatching {
            val data = mapOf(
                "reminderHour" to hour,
                "reminderMinute" to minute,
                "updatedAt" to Timestamp.now()
            )
            db.collection("users").document(uid).set(data, SetOptions.merge()).await()
        }
    }

    fun getCachedDailyStudyGoalMinutes(): Int = prefs.getInt(KEY_DAILY_GOAL_MINUTES, 120)

    suspend fun getDailyStudyGoalMinutes(): Int {
        val local = getCachedDailyStudyGoalMinutes()
        val uid = auth.currentUser?.uid ?: return local
        return runCatching {
            val doc = db.collection("users").document(uid).get().await()
            val remote = doc.getLong("dailyStudyGoalMinutes")?.toInt() ?: local
            prefs.edit().putInt(KEY_DAILY_GOAL_MINUTES, remote).apply()
            remote
        }.getOrDefault(local)
    }

    suspend fun setDailyStudyGoalMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_DAILY_GOAL_MINUTES, minutes).apply()
        val uid = auth.currentUser?.uid ?: return
        runCatching {
            db.collection("users").document(uid).set(
                mapOf("dailyStudyGoalMinutes" to minutes, "updatedAt" to Timestamp.now()),
                SetOptions.merge()
            ).await()
        }
    }

    fun getCachedFocusDurationMinutes(): Int = prefs.getInt(KEY_FOCUS_MINUTES, 25)

    suspend fun getFocusDurationMinutes(): Int {
        val local = getCachedFocusDurationMinutes()
        val uid = auth.currentUser?.uid ?: return local
        return runCatching {
            val doc = db.collection("users").document(uid).get().await()
            val remote = doc.getLong("focusDurationMinutes")?.toInt() ?: local
            prefs.edit().putInt(KEY_FOCUS_MINUTES, remote).apply()
            remote
        }.getOrDefault(local)
    }

    suspend fun setFocusDurationMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_FOCUS_MINUTES, minutes).apply()
        val uid = auth.currentUser?.uid ?: return
        runCatching {
            db.collection("users").document(uid).set(
                mapOf("focusDurationMinutes" to minutes, "updatedAt" to Timestamp.now()),
                SetOptions.merge()
            ).await()
        }
    }

    fun getCachedBreakDurationMinutes(): Int = prefs.getInt(KEY_BREAK_MINUTES, 5)

    suspend fun getBreakDurationMinutes(): Int {
        val local = getCachedBreakDurationMinutes()
        val uid = auth.currentUser?.uid ?: return local
        return runCatching {
            val doc = db.collection("users").document(uid).get().await()
            val remote = doc.getLong("breakDurationMinutes")?.toInt() ?: local
            prefs.edit().putInt(KEY_BREAK_MINUTES, remote).apply()
            remote
        }.getOrDefault(local)
    }

    suspend fun setBreakDurationMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_BREAK_MINUTES, minutes).apply()
        val uid = auth.currentUser?.uid ?: return
        runCatching {
            db.collection("users").document(uid).set(
                mapOf("breakDurationMinutes" to minutes, "updatedAt" to Timestamp.now()),
                SetOptions.merge()
            ).await()
        }
    }
}
