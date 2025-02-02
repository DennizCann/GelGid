package com.denizcan.gelgid.ui.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denizcan.gelgid.data.model.TransactionType
import com.denizcan.gelgid.data.model.TransactionCategories
import com.denizcan.gelgid.ui.components.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringTransactionScreen(
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit,
    initialType: TransactionType
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var dayOfMonth by remember { mutableStateOf("1") }
    var startDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("tr")) }

    val categories = if (initialType == TransactionType.INCOME) 
        TransactionCategories.incomeCategories else TransactionCategories.expenseCategories

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialType == TransactionType.INCOME) "Sabit Gelir Ekle" else "Sabit Gider Ekle") },
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
                onValueChange = { },
                label = { Text("Kategori") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showCategoryDialog = true }) {
                        Icon(Icons.Default.ArrowDropDown, "Kategori Seç")
                    }
                }
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
            
            // Başlangıç Tarihi
            OutlinedTextField(
                value = dateFormatter.format(Date(startDate)),
                onValueChange = { },
                label = { Text("Başlangıç Tarihi") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, "Tarih Seç")
                    }
                }
            )
            
            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
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
                            errorMessage = "Kategori seçiniz"
                        }
                        else -> {
                            viewModel.addRecurringTransaction(
                                title = title,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                type = initialType,
                                category = category,
                                dayOfMonth = dayOfMonth.toInt(),
                                startDate = startDate
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

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Kategori Seç") },
            text = {
                LazyColumn {
                    items(categories) { categoryItem ->
                        ListItem(
                            headlineContent = { Text(categoryItem) },
                            modifier = Modifier.clickable {
                                category = categoryItem
                                showCategoryDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { 
                startDate = it
                showDatePicker = false 
            },
            initialDate = startDate
        )
    }
}