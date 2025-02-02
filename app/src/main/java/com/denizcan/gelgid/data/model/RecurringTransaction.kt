package com.denizcan.gelgid.data.model

data class RecurringTransaction(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val dayOfMonth: Int = 1,
    val startDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastProcessedDate: Long = 0L
) 