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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.denizcan.gelgid.data.model.RecurringTransaction
import java.util.*

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Kullanıcı koleksiyonlarını oluşturmak için yardımcı fonksiyon
    private suspend fun createUserCollections(userId: String) {
        try {
            // transactions koleksiyonunu oluştur
            firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .document()
                .delete()  // Dummy döküman oluştur ve sil (koleksiyonu oluşturmak için)
                .await()

            // recurring_transactions koleksiyonunu oluştur
            firestore.collection("users")
                .document(userId)
                .collection("recurring_transactions")
                .document()
                .delete()
                .await()
        } catch (e: Exception) {
            println("Koleksiyonlar oluşturulurken hata: ${e.message}")
        }
    }

    // signUp fonksiyonunda kullanıcı koleksiyonlarını oluştur
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
                name = name,
                createdAt = System.currentTimeMillis()
            )
            
            println("Saving user to Firestore")
            try {
                firestore.collection("users")
                    .document(user.id)
                    .set(user)
                    .await()
            } catch (e: Exception) {
                println("Firestore error: ${e.message}")
                authResult.user?.delete()?.await()
                throw Exception("Kullanıcı kaydı tamamlanamadı: ${e.message}")
            }

            // Kullanıcı koleksiyonlarını oluştur
            createUserCollections(user.id)

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

    suspend fun addTransaction(
        title: String = "",
        amount: Double,
        type: TransactionType,
        category: String,
        description: String = "",
        date: Long = System.currentTimeMillis(),
        isRecurring: Boolean = false,
        recurringId: String = ""
    ): Result<Transaction> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            val transactionRef = firestore.collection("users")
                .document(currentUser.uid)
                .collection("transactions")
                .document()

            val transaction = Transaction(
                id = transactionRef.id,
                userId = currentUser.uid,
                title = title,
                amount = amount,
                type = type,
                category = category,
                description = description,
                date = date,
                isRecurring = isRecurring,
                recurringId = recurringId
            )

            transactionRef.set(transaction).await()
            Result.success(transaction)
        } catch (e: Exception) {
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

    suspend fun updateUserProfile(name: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            // Auth profilini güncelle
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }
            currentUser.updateProfile(profileUpdates).await()

            // Firestore'daki kullanıcı dokümanını güncelle
            firestore.collection("users")
                .document(currentUser.uid)
                .update("name", name)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            // Önce mevcut şifreyle yeniden kimlik doğrulama yap
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
            currentUser.reauthenticate(credential).await()

            // Şifreyi güncelle
            currentUser.updatePassword(newPassword).await()

            Result.success(Unit)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("INVALID_CREDENTIAL") == true -> "Mevcut şifre yanlış"
                e.message?.contains("WEAK_PASSWORD") == true -> "Yeni şifre çok zayıf"
                else -> e.message ?: "Şifre değiştirilemedi"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun deleteAccount(password: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            // Önce kullanıcıyı yeniden doğrula
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
            currentUser.reauthenticate(credential).await()

            // Firestore'dan kullanıcı verilerini sil
            firestore.collection("users")
                .document(currentUser.uid)
                .delete()
                .await()

            // Firebase Auth'dan hesabı sil
            currentUser.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Şifre yanlış"
                else -> e.message ?: "Hesap silinemedi"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun addRecurringTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        dayOfMonth: Int,
        startDate: Long = System.currentTimeMillis()
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            println("Adding recurring transaction: title=$title, amount=$amount, type=$type, dayOfMonth=$dayOfMonth, startDate=$startDate")

            val recurringRef = firestore.collection("users")
                .document(currentUser.uid)
                .collection("recurring_transactions")
                .document()

            val recurring = RecurringTransaction(
                id = recurringRef.id,
                userId = currentUser.uid,
                title = title,
                amount = amount,
                type = type,
                category = category,
                dayOfMonth = dayOfMonth,
                startDate = startDate
            )

            // Önce sabit işlemi kaydet
            recurringRef.set(recurring).await()
            println("Recurring transaction saved with id: ${recurring.id}")

            // Hemen işlemleri oluştur
            val endCalendar = Calendar.getInstance()
            val processCalendar = Calendar.getInstance().apply {
                timeInMillis = startDate
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            println("Processing transactions from ${processCalendar.time} to ${endCalendar.time}")

            // Eğer seçilen gün, ayın son gününden büyükse, ayın son gününü al
            val maxDay = processCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            if (dayOfMonth > maxDay) {
                processCalendar.set(Calendar.DAY_OF_MONTH, maxDay)
            }

            // Her ay için işlem oluştur
            while (processCalendar.timeInMillis <= endCalendar.timeInMillis) {
                println("Creating transaction for date: ${processCalendar.time}")
                
                // Bu tarihte işlem oluştur
                addTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    description = "Otomatik ${if(type == TransactionType.INCOME) "gelir" else "gider"}",
                    date = processCalendar.timeInMillis,
                    isRecurring = true,
                    recurringId = recurring.id
                ).onSuccess {
                    println("Transaction created successfully for date: ${processCalendar.time}")
                }.onFailure { e ->
                    println("Failed to create transaction: ${e.message}")
                }

                // Bir sonraki aya geç
                processCalendar.add(Calendar.MONTH, 1)
                
                // Ayın son gününü kontrol et
                val nextMaxDay = processCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                if (dayOfMonth > nextMaxDay) {
                    processCalendar.set(Calendar.DAY_OF_MONTH, nextMaxDay)
                } else {
                    processCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
            }

            // Son işlem tarihini güncelle
            recurringRef.update("lastProcessedDate", endCalendar.timeInMillis).await()
            println("Recurring transaction process completed")

            Result.success(Unit)
        } catch (e: Exception) {
            println("Error in addRecurringTransaction: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getRecurringTransactions(): Result<List<RecurringTransaction>> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            val snapshot = firestore.collection("users")
                .document(currentUser.uid)
                .collection("recurring_transactions")
                .get()
                .await()

            Result.success(snapshot.toObjects(RecurringTransaction::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecurringTransaction(id: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            firestore.collection("users")
                .document(currentUser.uid)
                .collection("recurring_transactions")
                .document(id)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processRecurringTransactionsFromDate(startDate: Long): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            val endCalendar = Calendar.getInstance()
            val startCalendar = Calendar.getInstance().apply {
                timeInMillis = startDate
                // Saat, dakika, saniye sıfırla
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            println("Processing recurring transactions from ${startCalendar.time} to ${endCalendar.time}")

            val recurringTransactions = getRecurringTransactions()
                .getOrDefault(emptyList())

            recurringTransactions.forEach { recurring ->
                println("Processing recurring transaction: ${recurring.title}")

                // Her bir sabit işlem için
                val processCalendar = Calendar.getInstance().apply {
                    timeInMillis = maxOf(startDate, recurring.startDate)
                    // Ayın gününü ayarla
                    set(Calendar.DAY_OF_MONTH, recurring.dayOfMonth)
                    // Saat, dakika, saniye sıfırla
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Eğer seçilen gün, ayın son gününden büyükse, ayın son gününü al
                val maxDay = processCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                if (recurring.dayOfMonth > maxDay) {
                    processCalendar.set(Calendar.DAY_OF_MONTH, maxDay)
                }

                // Başlangıç tarihi ayın seçilen gününden sonraysa, bir sonraki aya geç
                if (processCalendar.timeInMillis < startDate) {
                    processCalendar.add(Calendar.MONTH, 1)
                }

                // Her ay için işlem oluştur
                while (processCalendar.timeInMillis <= endCalendar.timeInMillis) {
                    println("Checking date: ${processCalendar.time}")

                    // Önce bu tarihte işlem var mı kontrol et
                    val existingTransactions = firestore.collection("users")
                        .document(currentUser.uid)
                        .collection("transactions")
                        .whereEqualTo("recurringId", recurring.id)
                        .whereGreaterThanOrEqualTo("date", processCalendar.timeInMillis)
                        .whereLessThan("date", processCalendar.timeInMillis + 24 * 60 * 60 * 1000)
                        .get()
                        .await()

                    // Bu tarihte işlem yoksa oluştur
                    if (existingTransactions.isEmpty) {
                        println("Creating transaction for date: ${processCalendar.time}")
                        addTransaction(
                            title = recurring.title,
                            amount = recurring.amount,
                            type = recurring.type,
                            category = recurring.category,
                            description = "Otomatik ${if(recurring.type == TransactionType.INCOME) "gelir" else "gider"}",
                            date = processCalendar.timeInMillis,
                            isRecurring = true,
                            recurringId = recurring.id
                        ).onSuccess {
                            println("Transaction created successfully")
                        }.onFailure { e ->
                            println("Failed to create transaction: ${e.message}")
                        }
                    } else {
                        println("Transaction already exists for this date")
                    }

                    // Bir sonraki aya geç
                    processCalendar.add(Calendar.MONTH, 1)
                    
                    // Ayın son gününü kontrol et
                    val nextMaxDay = processCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    if (recurring.dayOfMonth > nextMaxDay) {
                        processCalendar.set(Calendar.DAY_OF_MONTH, nextMaxDay)
                    } else {
                        processCalendar.set(Calendar.DAY_OF_MONTH, recurring.dayOfMonth)
                    }
                }

                // Son işlem tarihini güncelle
                firestore.collection("users")
                    .document(currentUser.uid)
                    .collection("recurring_transactions")
                    .document(recurring.id)
                    .update("lastProcessedDate", endCalendar.timeInMillis)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            println("Error processing recurring transactions: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateRecurringTransaction(
        id: String,
        title: String,
        amount: Double,
        category: String,
        dayOfMonth: Int
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))

            firestore.collection("users")
                .document(currentUser.uid)
                .collection("recurring_transactions")
                .document(id)
                .update(
                    mapOf(
                        "title" to title,
                        "amount" to amount,
                        "category" to category,
                        "dayOfMonth" to dayOfMonth,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Koleksiyonları kontrol etmek için
    suspend fun checkAndCreateCollections() {
        val currentUser = auth.currentUser ?: return
        createUserCollections(currentUser.uid)
    }
} 