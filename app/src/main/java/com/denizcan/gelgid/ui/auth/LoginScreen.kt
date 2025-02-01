package com.denizcan.gelgid.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginClick: (email: String, password: String) -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "GelGid",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                showError = false 
            },
            label = { Text("E-posta") },
            singleLine = true,
            isError = showError && email.isEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                showError = false 
            },
            label = { Text("Şifre") },
            singleLine = true,
            isError = showError && password.isEmpty(),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        if (authState is AuthState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { 
                when {
                    email.isEmpty() && password.isEmpty() -> {
                        showError = true
                        errorMessage = "E-posta ve şifre alanları boş bırakılamaz"
                    }
                    email.isEmpty() -> {
                        showError = true
                        errorMessage = "E-posta alanı boş bırakılamaz"
                    }
                    password.isEmpty() -> {
                        showError = true
                        errorMessage = "Şifre alanı boş bırakılamaz"
                    }
                    !email.contains("@") -> {
                        showError = true
                        errorMessage = "Geçerli bir e-posta adresi giriniz"
                    }
                    password.length < 6 -> {
                        showError = true
                        errorMessage = "Şifre en az 6 karakter olmalıdır"
                    }
                    else -> onLoginClick(email, password)
                }
            },
            enabled = authState !is AuthState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Giriş Yap")
        }
        
        TextButton(
            onClick = onRegisterClick
        ) {
            Text("Hesabın yok mu? Kayıt ol")
        }

        // Google Sign In butonu
        Button(
            onClick = onGoogleSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google ikonu eklenebilir
                Text("Google ile Giriş Yap")
            }
        }
    }
} 