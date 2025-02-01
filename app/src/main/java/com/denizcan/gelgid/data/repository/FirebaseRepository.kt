package com.denizcan.gelgid.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.denizcan.gelgid.data.model.User
import com.denizcan.gelgid.data.model.Transaction
import com.denizcan.gelgid.data.model.TransactionType
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query
import com.denizcan.gelgid.data.model.Asset
import com.denizcan.gelgid.data.model.AssetHistory

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

            println("Adding transaction for user: ${currentUser.uid}")

            // Koleksiyon yolunu kontrol et
            val transactionRef = firestore
                .collection("users")                 // users koleksiyonu
                .document(currentUser.uid)           // kullanıcı dokümanı
                .collection("transactions")          // transactions alt koleksiyonu
                .document()                         // yeni transaction dokümanı

            val transactionWithId = transaction.copy(
                id = transactionRef.id,
                userId = currentUser.uid
            )

            // Firestore'a kaydedilen veri yapısını kontrol et
            val data = mapOf(
                "id" to transactionWithId.id,
                "userId" to transactionWithId.userId,
                "amount" to transactionWithId.amount,
                "description" to transactionWithId.description,
                "type" to transactionWithId.type.name,  // INCOME veya EXPENSE olarak string
                "category" to transactionWithId.category,
                "date" to transactionWithId.date,
                "createdAt" to transactionWithId.createdAt
            )

            println("Saving transaction with data: $data")
            println("At path: ${transactionRef.path}")

            transactionRef.set(data).await()

            Result.success(transactionWithId)
        } catch (e: Exception) {
            println("Error saving transaction: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getTransactions(): Result<List<Transaction>> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            println("Getting transactions for user: ${currentUser.uid}")

            // Koleksiyon yolunu kontrol et
            val collectionRef = firestore
                .collection("users")                 // users koleksiyonu
                .document(currentUser.uid)           // kullanıcı dokümanı
                .collection("transactions")          // transactions alt koleksiyonu

            println("Querying collection at path: ${collectionRef.path}")

            val snapshot = collectionRef
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            println("Found ${snapshot.size()} documents")
            snapshot.documents.forEach { doc ->
                println("Document ${doc.id} data: ${doc.data}")
            }

            val transactions = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    println("Parsing document ${doc.id} with data: $data")
                    
                    val transaction = Transaction(
                        id = doc.id,
                        userId = data?.get("userId") as? String ?: "",
                        amount = (data?.get("amount") as? Number)?.toDouble() ?: 0.0,
                        description = data?.get("description") as? String ?: "",
                        type = try {
                            TransactionType.valueOf(data?.get("type") as? String ?: "EXPENSE")
                        } catch (e: Exception) {
                            println("Error parsing type: ${e.message}")
                            TransactionType.EXPENSE
                        },
                        category = data?.get("category") as? String ?: "",
                        date = (data?.get("date") as? Number)?.toLong() ?: System.currentTimeMillis(),
                        createdAt = (data?.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                    println("Successfully parsed transaction: $transaction")
                    transaction
                } catch (e: Exception) {
                    println("Error parsing document ${doc.id}: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }

            Result.success(transactions)
        } catch (e: Exception) {
            println("Error getting transactions: ${e.message}")
            e.printStackTrace()
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

    // Varlık ekleme
    suspend fun addAsset(asset: Asset): Result<Asset> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            val assetRef = firestore.collection("users")
                .document(currentUser.uid)
                .collection("assets")
                .document()

            val assetWithId = asset.copy(
                id = assetRef.id,
                userId = currentUser.uid
            )

            assetRef.set(assetWithId).await()
            Result.success(assetWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Varlıkları getirme
    suspend fun getAssets(): Result<List<Asset>> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            val snapshot = firestore.collection("users")
                .document(currentUser.uid)
                .collection("assets")
                .get()
                .await()

            val assets = snapshot.toObjects(Asset::class.java)
            Result.success(assets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Varlık güncelleme
    suspend fun updateAsset(asset: Asset): Result<Asset> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            firestore.collection("users")
                .document(currentUser.uid)
                .collection("assets")
                .document(asset.id)
                .set(asset)
                .await()

            Result.success(asset)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Varlık silme
    suspend fun deleteAsset(assetId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            firestore.collection("users")
                .document(currentUser.uid)
                .collection("assets")
                .document(assetId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addAssetHistory(assetId: String, amount: Double, note: String = ""): Result<AssetHistory> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            val historyRef = firestore.collection("users")
                .document(currentUser.uid)
                .collection("assets")
                .document(assetId)
                .collection("history")
                .document()

            val history = AssetHistory(
                id = historyRef.id,
                assetId = assetId,
                amount = amount,
                note = note
            )

            historyRef.set(history).await()

            // Asset'in amount değerini güncelle
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("assets")
                .document(assetId)
                .update(
                    mapOf(
                        "amount" to amount,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()

            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAssetHistory(assetId: String): Result<List<AssetHistory>> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            val snapshot = firestore.collection("users")
                .document(currentUser.uid)
                .collection("assets")
                .document(assetId)
                .collection("history")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val history = snapshot.toObjects(AssetHistory::class.java)
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 