package com.teamdobermans.studyos.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Listens for device-boot-completed broadcasts so that study reminders
 * are re-scheduled after a reboot.
 *
 * AlarmManager alarms do not survive a device restart — without this receiver
 * the user would never receive another notification until reopening the app.
 *
 * Requires RECEIVE_BOOT_COMPLETED permission in the manifest.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                StudyReminderScheduler.rescheduleIfEnabled(context)
            }
        }
    }
}
