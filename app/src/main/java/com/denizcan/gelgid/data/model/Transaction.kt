package com.denizcan.gelgid.data.model

enum class TransactionType {
    INCOME, // Gelir
    EXPENSE // Gider
}

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val date: Long = System.currentTimeMillis(),
    val category: String = ""
) 