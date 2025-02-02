package com.denizcan.gelgid.ui.asset

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denizcan.gelgid.data.model.Asset
import com.denizcan.gelgid.data.model.AssetType
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    viewModel: AssetViewModel,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onItemClick: (String) -> Unit
) {
    val assets by viewModel.assets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Varlıklar") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                assets.isEmpty() -> {
                    Text(
                        text = "Henüz varlık eklenmemiş",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = assets.sortedByDescending { it.amount },
                            key = { it.id }
                        ) { asset ->
                            AssetItem(
                                asset = asset,
                                onDeleteClick = { 
                                    viewModel.deleteAsset(asset.id)
                                },
                                onEditClick = { 
                                    onEditClick(asset.id)
                                },
                                onClick = { onItemClick(asset.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetItem(
    asset: Asset,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssetTypeIcon(asset.type)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = asset.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = asset.type.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "₺${asset.amount}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (asset.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = asset.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEditClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Düzenle"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Düzenle")
                }

                TextButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sil")
                }
            }
        }
    }
}

@Composable
fun AssetTypeIcon(type: AssetType) {
    val icon = when (type) {
        AssetType.BANK_ACCOUNT -> Icons.Default.AccountBalance
        AssetType.REAL_ESTATE -> Icons.Default.Home
        AssetType.VEHICLE -> Icons.Default.DirectionsCar
        AssetType.GOLD -> Icons.Default.MonetizationOn
        AssetType.STOCK -> Icons.Default.TrendingUp
        AssetType.CRYPTOCURRENCY -> Icons.Default.CurrencyBitcoin
        AssetType.OTHER -> Icons.Default.Category
    }

    Icon(
        imageVector = icon,
        contentDescription = type.title,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp)
    )
} 