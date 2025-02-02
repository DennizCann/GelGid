package com.denizcan.gelgid.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.denizcan.gelgid.data.model.Transaction
import com.denizcan.gelgid.data.model.TransactionType
import com.denizcan.gelgid.ui.transaction.TransactionViewModel
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: TransactionViewModel
) {
    val transactions by viewModel.transactions.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Raporlar") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MonthlyReportCard(transactions)
            TimeBasedAnalysisCard(transactions)
            CategoryDistributionCard(transactions)
        }
    }
}

@Composable
fun MonthlyReportCard(transactions: List<Transaction>) {
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    val monthlyTransactions = transactions.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
        cal.get(Calendar.MONTH) == currentMonth
    }
    
    val totalIncome = monthlyTransactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }
        
    val totalExpense = monthlyTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Bu Ay",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Gelir
                Column {
                    Text(
                        text = "Gelir",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₺$totalIncome",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Green
                    )
                }
                
                // Gider
                Column {
                    Text(
                        text = "Gider",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₺$totalExpense",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Net
                Column {
                    Text(
                        text = "Net",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₺${totalIncome - totalExpense}",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (totalIncome >= totalExpense) Color.Green else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun TimeBasedAnalysisCard(transactions: List<Transaction>) {
    var selectedPeriod by remember { mutableStateOf(TimePeriod.WEEKLY) }
    val dateFormatter = remember { SimpleDateFormat("dd MMMM", Locale("tr")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Zaman Bazlı Analiz",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Dönem seçici
            TabRow(
                selectedTabIndex = selectedPeriod.ordinal,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                TimePeriod.values().forEach { period ->
                    Tab(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        text = { Text(period.title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seçilen döneme göre analiz
            when (selectedPeriod) {
                TimePeriod.WEEKLY -> WeeklyAnalysis(transactions, dateFormatter)
                TimePeriod.MONTHLY -> MonthlyAnalysis(transactions, dateFormatter)
                TimePeriod.YEARLY -> YearlyAnalysis(transactions)
            }
        }
    }
}

enum class TimePeriod(val title: String) {
    WEEKLY("Haftalık"),
    MONTHLY("Aylık"),
    YEARLY("Yıllık")
}

@Composable
fun WeeklyAnalysis(transactions: List<Transaction>, dateFormatter: SimpleDateFormat) {
    val calendar = Calendar.getInstance()
    val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
    
    val weeklyTransactions = transactions.filter {
        calendar.timeInMillis = it.date
        calendar.get(Calendar.WEEK_OF_YEAR) == currentWeek
    }

    val dailyTotals = weeklyTransactions.groupBy {
        calendar.timeInMillis = it.date
        calendar.get(Calendar.DAY_OF_WEEK)
    }.mapValues { entry ->
        entry.value.sumOf { 
            if (it.type == TransactionType.INCOME) it.amount else -it.amount 
        }
    }

    Column {
        calendar.firstDayOfWeek = Calendar.MONDAY
        (Calendar.MONDAY..Calendar.SUNDAY).forEach { day ->
            val amount = dailyTotals[day] ?: 0.0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when(day) {
                        Calendar.MONDAY -> "Pazartesi"
                        Calendar.TUESDAY -> "Salı"
                        Calendar.WEDNESDAY -> "Çarşamba"
                        Calendar.THURSDAY -> "Perşembe"
                        Calendar.FRIDAY -> "Cuma"
                        Calendar.SATURDAY -> "Cumartesi"
                        else -> "Pazar"
                    }
                )
                Text(
                    text = "₺$amount",
                    color = if (amount >= 0) Color.Green else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun MonthlyAnalysis(transactions: List<Transaction>, dateFormatter: SimpleDateFormat) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    
    val monthlyTransactions = transactions.filter {
        calendar.timeInMillis = it.date
        calendar.get(Calendar.MONTH) == currentMonth && 
        calendar.get(Calendar.YEAR) == currentYear
    }

    // Haftalık bazda grupla
    val weeklyTotals = monthlyTransactions.groupBy {
        calendar.timeInMillis = it.date
        calendar.get(Calendar.WEEK_OF_MONTH)
    }.mapValues { entry ->
        entry.value.sumOf { 
            if (it.type == TransactionType.INCOME) it.amount else -it.amount 
        }
    }

    Column {
        (1..5).forEach { week ->
            val amount = weeklyTotals[week] ?: 0.0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$week. Hafta",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "₺$amount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (amount >= 0) Color.Green else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun YearlyAnalysis(transactions: List<Transaction>) {
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    
    val yearlyTransactions = transactions.filter {
        calendar.timeInMillis = it.date
        calendar.get(Calendar.YEAR) == currentYear
    }

    // Aylık bazda grupla
    val monthlyTotals = yearlyTransactions.groupBy {
        calendar.timeInMillis = it.date
        calendar.get(Calendar.MONTH)
    }.mapValues { entry ->
        entry.value.sumOf { 
            if (it.type == TransactionType.INCOME) it.amount else -it.amount 
        }
    }

    Column {
        val monthNames = listOf(
            "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
        )
        
        monthNames.forEachIndexed { index, monthName ->
            val amount = monthlyTotals[index] ?: 0.0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "₺$amount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (amount >= 0) Color.Green else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CategoryDistributionCard(transactions: List<Transaction>) {
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    val monthlyTransactions = transactions.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
        cal.get(Calendar.MONTH) == currentMonth
    }
    
    val categoryExpenses = monthlyTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { transaction -> transaction.amount } }
        .toList()
        .sortedByDescending { it.second }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Kategori Dağılımı",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            categoryExpenses.forEach { (category, amount) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "₺$amount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
} 