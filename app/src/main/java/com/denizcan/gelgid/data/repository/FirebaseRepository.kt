package com.denizcan.gelgid.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.denizcan.gelgid.data.model.User
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun signUp(email: String, password: String, name: String): Result<User> {
        return try {
            println("Creating auth user")
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            
            println("Updating user profile")
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }
            authResult.user?.updateProfile(profileUpdates)?.await()
            
            val user = User(
                id = authResult.user?.uid ?: "",
                email = email,
                name = name
            )
            
            println("Saving user to Firestore")
            try {
                firestore.collection("users")
                    .document(user.id)
                    .set(user)
                    .await()
            } catch (e: Exception) {
                println("Firestore error: ${e.message}")
                // Firestore hatası durumunda Authentication'dan da kullanıcıyı silelim
                authResult.user?.delete()?.await()
                throw Exception("Kullanıcı kaydı tamamlanamadı: ${e.message}")
            }

            println("Signup completed successfully")
            Result.success(user)
        } catch (e: Exception) {
            println("Signup failed with error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userDoc = firestore.collection("users")
                .document(authResult.user?.uid ?: "")
                .get()
                .await()
            
            val user = userDoc.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Kullanıcı bulunamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
} 