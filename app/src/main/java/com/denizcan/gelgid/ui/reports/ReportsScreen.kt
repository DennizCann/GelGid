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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

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