package com.denizcan.gelgid.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.denizcan.gelgid.data.model.User
import com.denizcan.gelgid.data.model.Transaction
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query

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

    suspend fun getCurrentUser(): Result<User?> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val userDoc = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()
                
                val user = userDoc.toObject(User::class.java)
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            // Transaction ID oluştur
            val transactionRef = firestore.collection("users")
                .document(currentUser.uid)
                .collection("transactions")
                .document()

            // ID'yi ekleyerek transaction'ı güncelle
            val transactionWithId = transaction.copy(
                id = transactionRef.id,
                userId = currentUser.uid
            )

            // Firestore'a kaydet
            transactionRef.set(transactionWithId).await()

            Result.success(transactionWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactions(
        startDate: Long? = null,
        endDate: Long? = null
    ): Result<List<Transaction>> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            var query = firestore.collection("users")
                .document(currentUser.uid)
                .collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)

            // Tarih filtresi varsa ekle
            if (startDate != null && endDate != null) {
                query = query.whereGreaterThanOrEqualTo("date", startDate)
                    .whereLessThanOrEqualTo("date", endDate)
            }

            val snapshot = query.get().await()
            val transactions = snapshot.toObjects(Transaction::class.java)

            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            firestore.collection("users")
                .document(currentUser.uid)
                .collection("transactions")
                .document(transactionId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 