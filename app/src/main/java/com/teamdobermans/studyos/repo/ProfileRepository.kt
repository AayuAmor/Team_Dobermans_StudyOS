package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import com.teamdobermans.studyos.model.UserProfile
import kotlinx.coroutines.tasks.await

class ProfileRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    fun currentUserEmail(): String = auth.currentUser?.email ?: ""

    suspend fun getProfile(): Result<UserProfile> = runCatching {
        val user = auth.currentUser ?: error("Not logged in")
        val uid  = user.uid
        val doc  = db.collection("users").document(uid).get().await()
        UserProfile(
            uid   = uid,
            name  = doc.getString("displayName")
                ?: user.displayName
                ?: user.email?.substringBefore("@")
                ?: "",
            email = user.email ?: ""
        )
    }

    suspend fun updateProfile(name: String, email: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("Not logged in")
        val uid  = user.uid

        val profileChange = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(profileChange).await()

        if (email.isNotBlank() && email != user.email) {
            try {
                @Suppress("DEPRECATION")
                user.updateEmail(email).await()
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                error("Please sign out and sign back in before changing your email address")
            }
        }

        val data = mapOf(
            "displayName" to name,
            "email"       to email,
            "updatedAt"   to Timestamp.now()
        )
        db.collection("users").document(uid).set(data, SetOptions.merge()).await()
    }

    suspend fun updateDisplayName(name: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("Not logged in")
        val uid  = user.uid

        val profileChange = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(profileChange).await()

        db.collection("users").document(uid).update("displayName", name).await()
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        auth.currentUser?.updatePassword(newPassword)?.await() ?: error("Not logged in")
    }
}
