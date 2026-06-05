package com.teamdobermans.studyos

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

object GoogleSignInHelper {

    private var googleSignInClient: GoogleSignInClient? = null

    fun getClient(context: Context): GoogleSignInClient {
        if (googleSignInClient == null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(context, gso)
        }
        return googleSignInClient!!
    }

    fun getSignInIntent(context: Context): Intent = getClient(context).signInIntent
}
