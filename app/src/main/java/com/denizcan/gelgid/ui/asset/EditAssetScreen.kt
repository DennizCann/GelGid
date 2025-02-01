package com.denizcan.gelgid.ui.asset

import androidx.compose.foundation.layout.*
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
import com.denizcan.gelgid.data.model.AssetType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAssetScreen(
    asset: Asset,
    viewModel: AssetViewModel,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf(asset.name) }
    var amount by remember { mutableStateOf(asset.amount.toString()) }
    var description by remember { mutableStateOf(asset.description) }
    var selectedType by remember { mutableStateOf(asset.type) }
    var isExpanded by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val assetState by viewModel.assetState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(assetState) {
        when (assetState) {
            is AssetState.Success -> {
                snackbarMessage = "Varlık başarıyla güncellendi"
                showSnackbar = true
                onNavigateBack()
            }
            is AssetState.Error -> {
                snackbarMessage = (assetState as AssetState.Error).message
                showSnackbar = true
            }
            else -> {}
        }
    }

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
        topBar = {
            TopAppBar(
                title = { Text("Varlık Düzenle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Varlık Adı
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Varlık Adı") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Varlık Tipi
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedType.title,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Varlık Tipi") },
                    leadingIcon = {
                        AssetTypeIcon(selectedType)
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }
                ) {
                    AssetType.values().forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AssetTypeIcon(type)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(type.title)
                                }
                            },
                            onClick = {
                                selectedType = type
                                isExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Değer
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Değer") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Açıklama
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Açıklama") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Güncelle Butonu
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (name.isNotEmpty() && amountDouble != null) {
                        viewModel.updateAsset(
                            asset.copy(
                                name = name,
                                type = selectedType,
                                amount = amountDouble,
                                description = description
                            )
                        )
                    } else {
                        snackbarMessage = "Lütfen geçerli bir değer girin"
                        showSnackbar = true
                    }
                },
                enabled = name.isNotEmpty() && amount.isNotEmpty() &&
                        assetState !is AssetState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (assetState is AssetState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Güncelle")
                }
            }
        }
    }
} 