package com.denizcan.gelgid.ui.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.gelgid.data.model.User
import com.denizcan.gelgid.data.repository.FirebaseRepository
import com.denizcan.gelgid.auth.GoogleAuthUiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
    data class Success(val user: User) : AuthState()
    object SignedOut : AuthState()
}

class AuthViewModel(
    private val repository: FirebaseRepository,
    private val googleAuthUiClient: GoogleAuthUiClient
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            repository.signIn(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Success(user)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Giriş başarısız")
                }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            println("Starting signup process") // Debug log
            repository.signUp(email, password, name)
                .onSuccess { user ->
                    println("Signup successful, user: $user") // Debug log
                    _authState.value = AuthState.Success(user)
                }
                .onFailure { exception ->
                    println("Signup failed: ${exception.message}") // Debug log
                    _authState.value = AuthState.Error(exception.message ?: "Kayıt başarısız")
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            googleAuthUiClient.signOut()  // Google oturumunu da temizle
            _authState.value = AuthState.SignedOut
        }
    }

    suspend fun signInWithGoogle(): Intent {
        return googleAuthUiClient.signIn()
    }

    fun signInWithGoogleIntent(intent: Intent) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            googleAuthUiClient.signInWithIntent(intent)
                .onSuccess { user ->
                    _authState.value = AuthState.Success(user)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Google ile giriş başarısız")
                }
        }
    }

    fun checkAuthState() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            repository.getCurrentUser()
                .onSuccess { user ->
                    if (user != null) {
                        _authState.value = AuthState.Success(user)
                    } else {
                        _authState.value = AuthState.Initial
                    }
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Oturum kontrolü başarısız")
                }
        }
    }
} 