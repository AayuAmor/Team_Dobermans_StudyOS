package com.teamdobermans.studyos.repo

import com.teamdobermans.studyos.model.QuizAttemptModel
import com.teamdobermans.studyos.model.QuizModel
import kotlinx.coroutines.flow.Flow

interface QuizRepo {
    suspend fun saveQuiz(quiz: QuizModel): String?
    suspend fun getQuiz(quizId: String): QuizModel?
    fun observeAttempts(): Flow<List<QuizAttemptModel>>
    suspend fun saveAttempt(attempt: QuizAttemptModel): Boolean
}
