package com.denizcan.gelgid.data.model

enum class TransactionType {
    INCOME, // Gelir
    EXPENSE // Gider
}

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,  // Sabit işlem mi?
    val recurringId: String = ""       // Bağlı olduğu sabit işlemin ID'si
) 