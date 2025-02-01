package com.denizcan.gelgid.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.denizcan.gelgid.data.model.User
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient = Identity.getSignInClient(context)
) {
    private val auth = FirebaseAuth.getInstance()

    suspend fun signIn(): IntentSender? {
        try {
            oneTapClient.signOut().await()
            
            val result = oneTapClient.beginSignIn(
                BeginSignInRequest.Builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId("499007679131-vq92uvp633332jn8kkc8j031mjh4b1mv.apps.googleusercontent.com")
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .setAutoSelectEnabled(false)
                    .build()
            ).await()
            
            return result.pendingIntent.intentSender
        } catch (e: Exception) {
            println("Google Sign In failed: ${e.message}")
            return null
        }
    }

    suspend fun signInWithIntent(intent: Intent): Result<User> {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)

        return try {
            val authResult = auth.signInWithCredential(googleCredentials).await()
            val user = User(
                id = authResult.user?.uid ?: "",
                email = authResult.user?.email ?: "",
                name = authResult.user?.displayName ?: ""
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            println("Sign out failed: ${e.message}")
        }
    }
} 