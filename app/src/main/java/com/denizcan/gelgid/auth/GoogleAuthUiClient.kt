package com.denizcan.gelgid.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.denizcan.gelgid.data.model.User
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore

class GoogleAuthUiClient(
    private val context: Context
) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("499007679131-vq92uvp633332jn8kkc8j031mjh4b1mv.apps.googleusercontent.com")
            .requestEmail()
            .build()
        
        GoogleSignIn.getClient(context, gso)
    }

    suspend fun signIn(): Intent {
        return try {
            // Her seferinde oturumu temizle
            googleSignInClient.signOut().await()
            // Yeni intent oluştur
            googleSignInClient.signInIntent
        } catch (e: Exception) {
            println("Google Sign In failed: ${e.message}")
            throw e
        }
    }

    suspend fun signInWithIntent(intent: Intent): Result<User> {
        return try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(intent).await()
            val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credentials).await()
            
            val user = User(
                id = authResult.user?.uid ?: "",
                email = authResult.user?.email ?: "",
                name = authResult.user?.displayName ?: ""
            )

            // Kullanıcıyı Firestore'a kaydet
            try {
                firestore.collection("users")
                    .document(user.id)
                    .set(user)
                    .await()
            } catch (e: Exception) {
                println("Firestore error: ${e.message}")
                throw e
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            println("Sign out failed: ${e.message}")
        }
    }
} 