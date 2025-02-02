package com.denizcan.gelgid.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denizcan.gelgid.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User,
    onUpdateProfile: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel
) {
    var name by remember { mutableStateOf(user.name) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val profileState = viewModel.profileState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(profileState) {
        when (profileState) {
            is ProfileState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (profileState as ProfileState.Error).message,
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profili Düzenle") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Ad Soyad
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        showError = false
                    },
                    label = { Text("Ad Soyad") },
                    isError = showError,
                    modifier = Modifier.fillMaxWidth()
                )

                if (showError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // E-posta (değiştirilemez)
                OutlinedTextField(
                    value = user.email,
                    onValueChange = { },
                    label = { Text("E-posta") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Kaydet Butonu
                Button(
                    onClick = {
                        when {
                            name.isBlank() -> {
                                showError = true
                                errorMessage = "Ad Soyad alanı boş bırakılamaz"
                            }
                            name.length < 3 -> {
                                showError = true
                                errorMessage = "Ad Soyad en az 3 karakter olmalıdır"
                            }
                            else -> {
                                onUpdateProfile(name)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = profileState !is ProfileState.Loading
                ) {
                    if (profileState is ProfileState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Kaydet")
                    }
                }
            }

            // Loading göstergesi
            if (profileState is ProfileState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
} 