package com.denizcan.gelgid.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.denizcan.gelgid.data.model.RecurringTransaction
import androidx.navigation.NavController
import com.denizcan.gelgid.data.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionsScreen(
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit,
    navController: NavController
) {
    val recurringTransactions by viewModel.recurringTransactions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sabit İşlemler") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recurringTransactions) { transaction ->
                RecurringTransactionItem(
                    transaction = transaction,
                    onEditClick = {
                        navController.navigate("edit_recurring_transaction/${transaction.id}")
                    },
                    onDeleteClick = {
                        viewModel.deleteRecurringTransaction(transaction.id)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionItem(
    transaction: RecurringTransaction,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "₺${transaction.amount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (transaction.type == TransactionType.INCOME) 
                        Color.Green else MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Her ayın ${transaction.dayOfMonth}. günü",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, "Düzenle")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, "Sil")
                }
            }
        }
    }
} 