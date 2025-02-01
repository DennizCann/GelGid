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

            repository.addTransaction(transaction)
                .onSuccess {
                    _transactionState.value = TransactionState.Success(it)
                }
                .onFailure { exception ->
                    _transactionState.value = TransactionState.Error(
                        exception.message ?: "İşlem kaydedilemedi"
                    )
                }
        }
    }
} 