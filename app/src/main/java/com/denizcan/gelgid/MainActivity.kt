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
import com.denizcan.gelgid.navigation.NavGraph
import com.denizcan.gelgid.ui.auth.AuthViewModel
import com.denizcan.gelgid.ui.theme.GelGidTheme
import com.denizcan.gelgid.data.repository.FirebaseRepository
import com.denizcan.gelgid.auth.GoogleAuthUiClient
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(applicationContext)
    }

    private val authViewModel by lazy { 
        AuthViewModel(FirebaseRepository(), googleAuthUiClient)
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
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        onGoogleSignInClick = {
                            lifecycleScope.launch {
                                try {
                                    val signInIntent = authViewModel.signInWithGoogle()
                                    signInIntentLauncher.launch(signInIntent)
                                } catch (e: Exception) {
                                    // Hata durumunu handle et
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}