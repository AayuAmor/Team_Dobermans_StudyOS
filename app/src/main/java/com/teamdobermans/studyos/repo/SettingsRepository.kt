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
        const val KEY_REMINDERS = "reminders_enabled"
    }

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
        // Write locally first so the preference is instant even if offline
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
}
