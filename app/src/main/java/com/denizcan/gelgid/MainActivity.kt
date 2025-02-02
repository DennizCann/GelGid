package com.denizcan.gelgid


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.denizcan.gelgid.ui.auth.AuthViewModel
import com.denizcan.gelgid.ui.theme.GelGidTheme
import com.denizcan.gelgid.data.repository.FirebaseRepository
import com.denizcan.gelgid.auth.GoogleAuthUiClient
import kotlinx.coroutines.launch
import com.denizcan.gelgid.ui.transaction.TransactionViewModel
import com.denizcan.gelgid.ui.asset.AssetViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.denizcan.gelgid.navigation.Screen
import com.denizcan.gelgid.ui.auth.LoginScreen
import com.denizcan.gelgid.ui.auth.RegisterScreen
import com.denizcan.gelgid.ui.home.HomeScreen
import com.denizcan.gelgid.ui.auth.AuthState
import com.denizcan.gelgid.ui.profile.ProfileViewModel

class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(applicationContext)
    }

    private val repository by lazy { FirebaseRepository() }
    
    private val authViewModel by lazy { 
        AuthViewModel(repository, googleAuthUiClient)
    }
    
    private val transactionViewModel by lazy {
        TransactionViewModel(repository)
    }

    private val assetViewModel by lazy {
        AssetViewModel(repository)
    }

    private val profileViewModel by lazy {
        ProfileViewModel(repository, authViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Uygulama başladığında mevcut oturumu kontrol et
        lifecycleScope.launch {
            authViewModel.checkAuthState()
        }
        
        var signInIntentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                lifecycleScope.launch {
                    authViewModel.signInWithGoogleIntent(result.data ?: return@launch)
                }
            }
        }

        setContent {
            GelGidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GelGidApp(
                        authViewModel = authViewModel,
                        transactionViewModel = transactionViewModel,
                        assetViewModel = assetViewModel,
                        profileViewModel = profileViewModel,
                        onGoogleSignInClick = {
                            lifecycleScope.launch {
                                try {
                                    val signInIntent = authViewModel.signInWithGoogle()
                                    signInIntentLauncher.launch(signInIntent)
                                } catch (e: Exception) {
                                    println("Google ile giriş hatası: ${e.message}")
                                    // TODO: Kullanıcıya hata mesajı göster
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GelGidApp(
    authViewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel,
    assetViewModel: AssetViewModel,
    profileViewModel: ProfileViewModel,
    onGoogleSignInClick: () -> Unit
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            is AuthState.SignedOut -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginClick = { email, password ->
                    authViewModel.signIn(email, password)
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                },
                onGoogleSignInClick = onGoogleSignInClick
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterClick = { email, password, name ->
                    authViewModel.signUp(email, password, name)
                },
                onBackToLoginClick = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Home.route) {
            val user = (authState as? AuthState.Success)?.user
            if (user != null) {
                HomeScreen(
                    user = user,
                    onSignOut = {
                        authViewModel.signOut()
                    },
                    transactionViewModel = transactionViewModel,
                    assetViewModel = assetViewModel,
                    profileViewModel = profileViewModel,
                    authViewModel = authViewModel,
                    recurringTransactionViewModel = transactionViewModel
                )
            }
        }
    }
}
