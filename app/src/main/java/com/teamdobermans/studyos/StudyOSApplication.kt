package com.teamdobermans.studyos

import android.app.Application
import com.teamdobermans.studyos.notification.ReviewNotificationWorker
import com.teamdobermans.studyos.notification.StudyReminderScheduler

class StudyOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        StudyReminderScheduler.createChannel(this)
        ReviewNotificationWorker.createChannel(this)
    }
}