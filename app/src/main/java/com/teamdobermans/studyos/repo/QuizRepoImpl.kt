package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.teamdobermans.studyos.model.QuizAttemptModel
import com.teamdobermans.studyos.model.QuizModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class QuizRepoImpl : QuizRepo {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun quizzesCol() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("quizzes")
    }

    private fun attemptsCol() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("quizAttempts")
    }

    override suspend fun saveQuiz(quiz: QuizModel): String? {
        val col = quizzesCol() ?: return null
        val uid = auth.currentUser?.uid ?: return null
        val ref = if (quiz.id.isBlank()) col.document() else col.document(quiz.id)
        return try {
            ref.set(quiz.copy(id = ref.id, userId = uid)).await()
            ref.id
        } catch (_: Exception) { null }
    }

    override suspend fun getQuiz(quizId: String): QuizModel? {
        val col = quizzesCol() ?: return null
        return try {
            col.document(quizId).get().await().toObject(QuizModel::class.java)
        } catch (_: Exception) { null }
    }

    override fun observeAttempts(): Flow<List<QuizAttemptModel>> {
        val col = attemptsCol() ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = col
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snap, _ ->
                    val list = snap?.documents
                        ?.mapNotNull { it.toObject(QuizAttemptModel::class.java) }
                        ?: emptyList()
                    trySend(list)
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun saveAttempt(attempt: QuizAttemptModel): Boolean {
        val col = attemptsCol() ?: return false
        val uid = auth.currentUser?.uid ?: return false
        val ref = if (attempt.id.isBlank()) col.document() else col.document(attempt.id)
        return try {
            ref.set(attempt.copy(id = ref.id, userId = uid)).await()
            true
        } catch (_: Exception) { false }
    }
}
