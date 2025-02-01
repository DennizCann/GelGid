package com.denizcan.gelgid.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.gelgid.data.model.Transaction
import com.denizcan.gelgid.data.model.TransactionType
import com.denizcan.gelgid.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

            val transaction = Transaction(
                amount = amount,
                description = description,
                type = type,
                category = category,
                date = date
            )

            println("Adding transaction: $transaction")

            repository.addTransaction(transaction)
                .onSuccess {
                    println("Transaction added successfully: ${it.id}")
                    _transactionState.value = TransactionState.Success(it)
                }
                .onFailure { exception ->
                    println("Error adding transaction: ${exception.message}")
                    _transactionState.value = TransactionState.Error(
                        exception.message ?: "İşlem kaydedilemedi"
                    )
                }
        }
    }

    fun getTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                println("ViewModel: Getting transactions")
                repository.getTransactions()
                    .onSuccess { transactions ->
                        println("ViewModel: Successfully loaded ${transactions.size} transactions")
                        _transactions.value = transactions
                    }
                    .onFailure { exception ->
                        println("ViewModel: Error loading transactions - ${exception.message}")
                        exception.printStackTrace()
                    }
            } finally {
                _isLoading.value = false
            }
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
} 