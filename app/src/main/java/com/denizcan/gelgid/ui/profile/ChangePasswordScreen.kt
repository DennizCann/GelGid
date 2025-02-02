package com.denizcan.gelgid.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onChangePassword: (String, String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
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
                title = { Text("Şifre Değiştir") },
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
                // Mevcut Şifre
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { 
                        currentPassword = it
                        showError = false
                    },
                    label = { Text("Mevcut Şifre") },
                    visualTransformation = if (showCurrentPassword) 
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                            Icon(
                                imageVector = if (showCurrentPassword) 
                                    Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showCurrentPassword) 
                                    "Şifreyi Gizle" else "Şifreyi Göster"
                            )
                        }
                    },
                    isError = showError,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Yeni Şifre
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        showError = false
                    },
                    label = { Text("Yeni Şifre") },
                    visualTransformation = if (showNewPassword) 
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                imageVector = if (showNewPassword) 
                                    Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showNewPassword) 
                                    "Şifreyi Gizle" else "Şifreyi Göster"
                            )
                        }
                    },
                    isError = showError,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Yeni Şifre Tekrar
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        showError = false
                    },
                    label = { Text("Yeni Şifre Tekrar") },
                    visualTransformation = if (showConfirmPassword) 
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) 
                                    Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showConfirmPassword) 
                                    "Şifreyi Gizle" else "Şifreyi Göster"
                            )
                        }
                    },
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

                Spacer(modifier = Modifier.height(24.dp))

                // Değiştir Butonu
                Button(
                    onClick = {
                        when {
                            currentPassword.isBlank() || newPassword.isBlank() || 
                            confirmPassword.isBlank() -> {
                                showError = true
                                errorMessage = "Tüm alanlar doldurulmalıdır"
                            }
                            newPassword.length < 6 -> {
                                showError = true
                                errorMessage = "Yeni şifre en az 6 karakter olmalıdır"
                            }
                            newPassword != confirmPassword -> {
                                showError = true
                                errorMessage = "Yeni şifreler eşleşmiyor"
                            }
                            else -> {
                                onChangePassword(currentPassword, newPassword)
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
                        Text("Şifreyi Değiştir")
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