package com.teamdobermans.studyos.repo

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class SettingsRepository(context: Context) {

    private val auth  = FirebaseAuth.getInstance()
    private val db    = FirebaseFirestore.getInstance()
    private val prefs = context.getSharedPreferences("StudyOSPrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_REMINDERS      = "reminders_enabled"
        const val KEY_REMINDER_HOUR   = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
    }

    // ── Reminder toggle ───────────────────────────────────────────────────────

    fun getCachedReminderPreference(): Boolean =
        prefs.getBoolean(KEY_REMINDERS, true)

    suspend fun getReminderPreference(): Boolean {
        val local = prefs.getBoolean(KEY_REMINDERS, true)
        val uid   = auth.currentUser?.uid ?: return local

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
                "updatedAt"        to Timestamp.now()
            )
            db.collection("users").document(uid).set(data, SetOptions.merge()).await()
            true
        }.getOrDefault(false)
    }

    // ── Reminder time ─────────────────────────────────────────────────────────

    fun getCachedReminderTime(): Pair<Int, Int> = Pair(
        prefs.getInt(KEY_REMINDER_HOUR,   8),
        prefs.getInt(KEY_REMINDER_MINUTE, 0)
    )

    suspend fun getReminderTime(): Pair<Int, Int> {
        val local = getCachedReminderTime()
        val uid   = auth.currentUser?.uid ?: return local

        return runCatching {
            val doc = db.collection("users").document(uid).get().await()
            val h = if (doc.contains("reminderHour"))   (doc.getLong("reminderHour")?.toInt()   ?: local.first)  else local.first
            val m = if (doc.contains("reminderMinute")) (doc.getLong("reminderMinute")?.toInt() ?: local.second) else local.second
            prefs.edit().putInt(KEY_REMINDER_HOUR, h).putInt(KEY_REMINDER_MINUTE, m).apply()
            Pair(h, m)
        }.getOrDefault(local)
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR,   hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()

        val uid = auth.currentUser?.uid ?: return

        runCatching {
            val data = mapOf(
                "reminderHour"   to hour,
                "reminderMinute" to minute,
                "updatedAt"      to Timestamp.now()
            )
            db.collection("users").document(uid).set(data, SetOptions.merge()).await()
        }
    }
}
