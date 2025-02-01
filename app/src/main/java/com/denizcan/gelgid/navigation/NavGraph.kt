package com.denizcan.gelgid.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.denizcan.gelgid.ui.auth.AuthState
import com.denizcan.gelgid.ui.auth.AuthViewModel
import com.denizcan.gelgid.ui.auth.LoginScreen
import com.denizcan.gelgid.ui.auth.RegisterScreen
import com.denizcan.gelgid.ui.home.HomeScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onGoogleSignInClick: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    // Debug için state değişimlerini logla
    LaunchedEffect(authState) {
        println("AuthState changed to: $authState") // Debug log
        when (authState) {
            is AuthState.Success -> {
                println("Navigating to Home") // Debug log
                navController.navigate(Screen.Home.route) {
                    // Geri tuşuna basınca login ekranına dönmeyi engellemek için
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            is AuthState.SignedOut -> {
                println("Navigating to Login") // Debug log
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {
                println("No navigation needed for state: $authState") // Debug log
            }
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
                    }
                )
            }
        }
    }
} 