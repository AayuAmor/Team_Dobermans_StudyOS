package com.teamdobermans.studyos.repo

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await()
        Unit
    }

    suspend fun signInWithCredential(credential: AuthCredential): Result<Unit> = runCatching {
        auth.signInWithCredential(credential).await()
        Unit
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null
}

