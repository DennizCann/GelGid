package com.denizcan.gelgid.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denizcan.gelgid.data.model.RecurringTransaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecurringTransactionScreen(
    transaction: RecurringTransaction,
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf(transaction.title) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var category by remember { mutableStateOf(transaction.category) }
    var dayOfMonth by remember { mutableStateOf(transaction.dayOfMonth.toString()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sabit İşlemi Düzenle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Başlık") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Tutar") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Kategori") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = dayOfMonth,
                onValueChange = { 
                    val day = it.toIntOrNull()
                    if (day != null && day in 1..31) {
                        dayOfMonth = it
                    }
                },
                label = { Text("Ayın Günü (1-31)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    when {
                        title.isBlank() -> {
                            showError = true
                            errorMessage = "Başlık boş bırakılamaz"
                        }
                        amount.isBlank() -> {
                            showError = true
                            errorMessage = "Tutar boş bırakılamaz"
                        }
                        category.isBlank() -> {
                            showError = true
                            errorMessage = "Kategori boş bırakılamaz"
                        }
                        else -> {
                            viewModel.updateRecurringTransaction(
                                id = transaction.id,
                                title = title,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                category = category,
                                dayOfMonth = dayOfMonth.toInt()
                            )
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kaydet")
            }
        }
    }
} 