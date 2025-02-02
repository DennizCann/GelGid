package com.denizcan.gelgid.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.denizcan.gelgid.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ChevronRight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onDeleteAccount: (String) -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel,
    navController: NavController
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val profileState = viewModel.profileState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale("tr")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Profil Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profil Fotoğrafı (İlk harf)
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.first().toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Kullanıcı Bilgileri
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Katılma: ${if (user.createdAt > 0) dateFormatter.format(Date(user.createdAt)) else "Bilinmiyor"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ayarlar Listesi
            Text(
                text = "Hesap Ayarları",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Profil Düzenle
            ProfileSettingsItem(
                icon = Icons.Default.Edit,
                title = "Profili Düzenle",
                onClick = onEditProfile
            )

            // Şifre Değiştir
            ProfileSettingsItem(
                icon = Icons.Default.Lock,
                title = "Şifre Değiştir",
                onClick = onChangePassword
            )

            Divider(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
            )

            // Hesap Yönetimi başlığı
            Text(
                text = "Hesap Yönetimi",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sadece Hesabı Sil seçeneği
            ProfileSettingsItem(
                icon = Icons.Default.Delete,
                title = "Hesabı Kalıcı Olarak Sil",
                tint = Color(0xFFE53935),  // Koyu kırmızı renk
                onClick = { showDeleteDialog = true }
            )
        }

        // Hesap Silme Dialog'u
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Hesabı Kaldır") },
                text = {
                    Column {
                        Text("Hesabınızı kaldırmak istediğinizden emin misiniz? Bu işlem geri alınamaz.")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Onaylamak için şifrenizi girin:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                showError = false
                            },
                            label = { Text("Şifre") },
                            visualTransformation = if (showPassword) 
                                VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) 
                                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showPassword) 
                                            "Şifreyi Gizle" else "Şifreyi Göster"
                                    )
                                }
                            },
                            isError = showError,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (showError) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            when {
                                password.isBlank() -> {
                                    showError = true
                                    errorMessage = "Şifre alanı boş bırakılamaz"
                                }
                                else -> {
                                    onDeleteAccount(password)
                                    showDeleteDialog = false
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Hesabı Kaldır")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showDeleteDialog = false
                        password = ""
                        showError = false
                    }) {
                        Text("İptal")
                    }
                }
            )
        }

        // Snackbar gösterimi
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
    }
}

@Composable
fun ProfileSettingsItem(
    icon: ImageVector,
    title: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = tint
            )
        }
    }
} 