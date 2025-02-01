package com.denizcan.gelgid.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denizcan.gelgid.data.model.TransactionType
import com.denizcan.gelgid.data.model.getCategories
import java.util.Date
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import com.denizcan.gelgid.data.repository.FirebaseRepository
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TransactionViewModel(FirebaseRepository()) as T
            }
        }
    )
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var isExpanded by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val transactionState by viewModel.transactionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("tr")) }
    
    LaunchedEffect(transactionState) {
        when (transactionState) {
            is TransactionState.Success -> {
                // Form'u temizle
                amount = ""
                description = ""
                selectedCategory = ""
                // Başarı mesajı göster
                snackbarMessage = "İşlem başarıyla kaydedildi"
                showSnackbar = true
                // İşlem listesini güncelle
                viewModel.getTransactions()
            }
            is TransactionState.Error -> {
                // Hata mesajı göster
                snackbarMessage = (transactionState as TransactionState.Error).message
                showSnackbar = true
            }
            else -> {}
        }
    }

    // Snackbar gösterimi için LaunchedEffect
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(
                message = snackbarMessage,
                duration = SnackbarDuration.Short
            )
            showSnackbar = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // İşlem Tipi Seçimi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { 
                        selectedType = TransactionType.EXPENSE
                        selectedCategory = "" // Kategoriyi sıfırla
                    },
                    label = { Text("Gider") }
                )
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { 
                        selectedType = TransactionType.INCOME
                        selectedCategory = "" // Kategoriyi sıfırla
                    },
                    label = { Text("Gelir") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tutar Girişi
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Tutar") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kategori Seçimi
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }
                ) {
                    getCategories(selectedType).forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                isExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Açıklama
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Açıklama") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tarih Seçici
            OutlinedTextField(
                value = dateFormatter.format(selectedDate),
                onValueChange = { },
                readOnly = true,
                label = { Text("Tarih") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Tarih Seç"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kaydet Butonu
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (amountDouble != null && selectedCategory.isNotEmpty()) {
                        viewModel.addTransaction(
                            amount = amountDouble,
                            description = description,
                            type = selectedType,
                            category = selectedCategory,
                            date = selectedDate.time
                        )
                    } else {
                        snackbarMessage = "Lütfen geçerli bir tutar girin ve kategori seçin"
                        showSnackbar = true
                    }
                },
                enabled = amount.isNotEmpty() && selectedCategory.isNotEmpty() &&
                        transactionState !is TransactionState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (transactionState is TransactionState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Kaydet")
                }
            }
        }

        // DatePicker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                    }) {
                        Text("Tamam")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                    }) {
                        Text("İptal")
                    }
                }
            ) {
                DatePicker(
                    state = rememberDatePickerState(
                        initialSelectedDateMillis = selectedDate.time
                    ),
                    showModeToggle = false,
                    title = { Text("Tarih Seç") }
                )
            }
        }
    }
} 