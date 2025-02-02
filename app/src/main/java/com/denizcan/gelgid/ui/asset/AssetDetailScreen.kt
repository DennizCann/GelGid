package com.denizcan.gelgid.ui.asset

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denizcan.gelgid.data.model.Asset
import com.denizcan.gelgid.data.model.AssetHistory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(
    asset: Asset,
    viewModel: AssetViewModel,
    onNavigateBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    val assetHistory by viewModel.assetHistory.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getAssetHistory(asset.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(asset.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
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
            // Varlık Detay Kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showUpdateDialog = true },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Varlık Tipi ve İkonu
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssetTypeIcon(asset.type)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = asset.type.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Güncel Değer
                    Text(
                        text = "Güncel Değer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₺${asset.amount}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (asset.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        // Açıklama
                        Text(
                            text = "Açıklama",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = asset.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Değer Geçmişi
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Değer Geçmişi",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (assetHistory.isEmpty()) {
                        Text(
                            text = "Henüz değer güncellemesi yapılmamış",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn {
                            items(assetHistory.sortedByDescending { it.date }) { history ->
                                AssetHistoryItem(history = history)
                                if (history != assetHistory.last()) {
                                    Divider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Değer Güncelleme Dialog'u
        if (showUpdateDialog) {
            UpdateValueDialog(
                currentValue = asset.amount,
                onDismiss = { showUpdateDialog = false },
                onConfirm = { newAmount, note ->
                    viewModel.updateAssetValue(asset.id, newAmount, note)
                    showUpdateDialog = false
                }
            )
        }

        // Silme Onay Dialog'u
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Varlığı Sil") },
                text = { Text("Bu varlığı silmek istediğinizden emin misiniz?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteAsset(asset.id)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Sil")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("İptal")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateValueDialog(
    currentValue: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf(currentValue.toString()) }
    var note by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Değer Güncelle") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it
                        showError = false
                    },
                    label = { Text("Yeni Değer") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showError,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (showError) {
                    Text(
                        text = "Lütfen geçerli bir değer girin",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Not (Opsiyonel)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (amountDouble != null) {
                        onConfirm(amountDouble, note)
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("Güncelle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun AssetHistoryItem(history: AssetHistory) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "₺${history.amount}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = formatDate(history.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (history.note.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = history.note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Tarih formatlama fonksiyonu
private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr"))
    return dateFormat.format(Date(timestamp))
} 