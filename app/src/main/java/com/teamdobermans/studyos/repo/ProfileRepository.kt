package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProfileRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    fun currentUserEmail(): String = auth.currentUser?.email ?: ""

    suspend fun updateDisplayName(name: String): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not logged in")
        db.collection("users").document(uid).update("displayName", name).await()
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        auth.currentUser?.updatePassword(newPassword)?.await() ?: error("Not logged in")
    }
}
