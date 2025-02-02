package com.denizcan.gelgid.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.gelgid.data.model.Transaction
import com.denizcan.gelgid.data.model.TransactionType
import com.denizcan.gelgid.data.model.RecurringTransaction
import com.denizcan.gelgid.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

sealed class TransactionState {
    object Initial : TransactionState()
    object Loading : TransactionState()
    data class Success(val transaction: Transaction) : TransactionState()
    data class Error(val message: String) : TransactionState()
}

class TransactionViewModel(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _transactionState = MutableStateFlow<TransactionState>(TransactionState.Initial)
    val transactionState: StateFlow<TransactionState> = _transactionState

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _recurringTransactions = MutableStateFlow<List<RecurringTransaction>>(emptyList())
    val recurringTransactions: StateFlow<List<RecurringTransaction>> = _recurringTransactions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun addTransaction(
        amount: Double,
        description: String,
        type: TransactionType,
        category: String,
        date: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            _transactionState.value = TransactionState.Loading

            repository.addTransaction(
                amount = amount,
                description = description,
                type = type,
                category = category,
                date = date
            ).onSuccess { transaction ->
                println("Transaction added successfully: ${transaction.id}")
                _transactionState.value = TransactionState.Success(transaction)
            }.onFailure { exception ->
                println("Error adding transaction: ${exception.message}")
                _transactionState.value = TransactionState.Error(
                    exception.message ?: "İşlem kaydedilemedi"
                )
            }
        }
    }

    suspend fun getTransactions(): Result<Unit> {
        return try {
            repository.getTransactions()
                .onSuccess { transactionsList ->
                    _transactions.value = transactionsList
                }
                .map { } // Result<List<Transaction>>'ı Result<Unit>'e dönüştürüyoruz
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
                .onSuccess {
                    // İşlem silinince listeyi güncelle
                    getTransactions()
                }
                .onFailure { exception ->
                    // TODO: Hata durumunu handle et
                }
        }
    }

    fun addRecurringTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        dayOfMonth: Int,
        startDate: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.addRecurringTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    dayOfMonth = dayOfMonth,
                    startDate = startDate
                ).onSuccess {
                    println("Recurring transaction added successfully")
                    getRecurringTransactions()
                    getTransactions()  // Normal işlemleri de güncelle
                }.onFailure { e ->
                    println("Error adding recurring transaction: ${e.message}")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getRecurringTransactions() {
        viewModelScope.launch {
            repository.getRecurringTransactions()
                .onSuccess { transactions ->
                    _recurringTransactions.value = transactions
                }
        }
    }

    fun deleteRecurringTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteRecurringTransaction(id)
            getRecurringTransactions()
        }
    }

    fun checkAndProcessRecurringTransactions() {
        viewModelScope.launch {
            // Son 3 ay için işlemleri kontrol et
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -3)  // 3 ay öncesine git
            calendar.set(Calendar.DAY_OF_MONTH, 1)  // Ayın başına git
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            repository.processRecurringTransactionsFromDate(calendar.timeInMillis)
                .onSuccess {
                    println("Recurring transactions processed successfully")
                    getTransactions()
                }
                .onFailure { e ->
                    println("Error processing recurring transactions: ${e.message}")
                }
        }
    }

    fun updateRecurringTransaction(
        id: String,
        title: String,
        amount: Double,
        category: String,
        dayOfMonth: Int
    ) {
        viewModelScope.launch {
            repository.updateRecurringTransaction(
                id = id,
                title = title,
                amount = amount,
                category = category,
                dayOfMonth = dayOfMonth
            )
            getRecurringTransactions()
        }
    }

    init {
        viewModelScope.launch {
            repository.checkAndCreateCollections()
            checkAndProcessRecurringTransactions()
            getRecurringTransactions()

            // Her gün kontrol et
            while (true) {
                try {
                    kotlinx.coroutines.delay(24 * 60 * 60 * 1000) // 24 saat bekle
                    checkAndProcessRecurringTransactions()
                } catch (e: Exception) {
                    println("Daily check error: ${e.message}")
                }
            }
        }
    }
} 