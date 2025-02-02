package com.denizcan.gelgid.ui.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.denizcan.gelgid.data.model.Transaction
import com.denizcan.gelgid.data.model.TransactionType
import com.denizcan.gelgid.ui.transaction.TransactionViewModel
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: TransactionViewModel,
    onWeeklyReportClick: () -> Unit = {},
    onMonthlyReportClick: () -> Unit = {},
    onYearlyReportClick: () -> Unit = {}
) {
    val transactions by viewModel.transactions.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Raporlar") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { 
                WeeklyReportCard(
                    transactions = transactions,
                    onClick = onWeeklyReportClick
                )
            }
            item { 
                MonthlyReportCard(
                    transactions = transactions,
                    onClick = onMonthlyReportClick
                )
            }
            item { 
                YearlyReportCard(
                    transactions = transactions,
                    onClick = onYearlyReportClick
                )
            }
        }
    }
}

@Composable
fun WeeklyReportCard(
    transactions: List<Transaction>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Haftalık Rapor",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Son 7 günün işlemlerini al
            val calendar = Calendar.getInstance()
            val today = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val weekAgo = calendar.timeInMillis
            
            // Günlük işlemleri grupla
            val dailyTransactions = transactions
                .filter { it.date in weekAgo..today }
                .groupBy {
                    calendar.timeInMillis = it.date
                    calendar.get(Calendar.DAY_OF_YEAR)
                }
            
            // Son 7 günü listele
            calendar.timeInMillis = today
            repeat(7) { daysAgo ->
                calendar.timeInMillis = today
                calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
                val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                val dayTransactions = dailyTransactions[dayOfYear] ?: emptyList()
                
                val dayIncome = dayTransactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                    
                val dayExpense = dayTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                
                val dayName = when(calendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "Pazartesi"
                    Calendar.TUESDAY -> "Salı"
                    Calendar.WEDNESDAY -> "Çarşamba"
                    Calendar.THURSDAY -> "Perşembe"
                    Calendar.FRIDAY -> "Cuma"
                    Calendar.SATURDAY -> "Cumartesi"
                    else -> "Pazar"
                }
                
                DailyTransactionRow(
                    dayName = dayName,
                    income = dayIncome,
                    expense = dayExpense
                )
            }
        }
    }
}

@Composable
fun MonthlyReportCard(
    transactions: List<Transaction>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Aylık Rapor",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            
            val monthlyTransactions = transactions.filter {
                calendar.timeInMillis = it.date
                calendar.get(Calendar.MONTH) == currentMonth &&
                calendar.get(Calendar.YEAR) == currentYear
            }
            
            // Haftalık grupla
            val weeklyTransactions = monthlyTransactions.groupBy {
                calendar.timeInMillis = it.date
                calendar.get(Calendar.WEEK_OF_MONTH)
            }
            
            // Haftaları listele
            (1..5).forEach { week ->
                val weekTransactions = weeklyTransactions[week] ?: emptyList()
                
                val weekIncome = weekTransactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                    
                val weekExpense = weekTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                
                WeeklyTransactionRow(
                    weekName = "$week. Hafta",
                    income = weekIncome,
                    expense = weekExpense
                )
            }
        }
    }
}

@Composable
fun YearlyReportCard(
    transactions: List<Transaction>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Yıllık Rapor",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            
            val yearlyTransactions = transactions.filter {
                calendar.timeInMillis = it.date
                calendar.get(Calendar.YEAR) == currentYear
            }
            
            // Aylık grupla
            val monthlyTransactions = yearlyTransactions.groupBy {
                calendar.timeInMillis = it.date
                calendar.get(Calendar.MONTH)
            }
            
            // Ayları listele
            val monthNames = listOf(
                "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
                "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
            )
            
            monthNames.forEachIndexed { index, monthName ->
                val monthTransactions = monthlyTransactions[index] ?: emptyList()
                
                val monthIncome = monthTransactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                    
                val monthExpense = monthTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                
                MonthlyTransactionRow(
                    monthName = monthName,
                    income = monthIncome,
                    expense = monthExpense
                )
            }
        }
    }
}

@Composable
fun DailyTransactionRow(
    dayName: String,
    income: Double,
    expense: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "+₺$income",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Green
            )
            Text(
                text = "-₺$expense",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun WeeklyTransactionRow(
    weekName: String,
    income: Double,
    expense: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = weekName,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "+₺$income",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Green
            )
            Text(
                text = "-₺$expense",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun MonthlyTransactionRow(
    monthName: String,
    income: Double,
    expense: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = monthName,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "+₺$income",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Green
            )
            Text(
                text = "-₺$expense",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}


