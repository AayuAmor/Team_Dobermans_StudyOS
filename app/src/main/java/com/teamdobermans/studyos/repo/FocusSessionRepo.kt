package com.teamdobermans.studyos.repo

import com.teamdobermans.studyos.model.FocusSessionModel
import com.teamdobermans.studyos.model.Task

interface FocusSessionRepo {
    suspend fun getTasks(userId: String): List<Task>
    suspend fun saveSession(session: FocusSessionModel): Boolean
}