package com.denizcan.gelgid.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.gelgid.data.repository.FirebaseRepository
import com.denizcan.gelgid.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Initial : ProfileState()
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel(
    private val repository: FirebaseRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState: StateFlow<ProfileState> = _profileState

    fun updateProfile(name: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                repository.updateUserProfile(name)
                    .onSuccess {
                        _profileState.value = ProfileState.Success
                    }
                    .onFailure { exception ->
                        _profileState.value = ProfileState.Error(
                            exception.message ?: "Profil güncellenemedi"
                        )
                    }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Bir hata oluştu")
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                repository.changePassword(currentPassword, newPassword)
                    .onSuccess {
                        _profileState.value = ProfileState.Success
                    }
                    .onFailure { exception ->
                        _profileState.value = ProfileState.Error(
                            exception.message ?: "Şifre değiştirilemedi"
                        )
                    }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Bir hata oluştu")
            }
        }
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                repository.deleteAccount(password)
                    .onSuccess {
                        _profileState.value = ProfileState.Success
                        authViewModel.signOut()
                    }
                    .onFailure { exception ->
                        _profileState.value = ProfileState.Error(
                            exception.message ?: "Hesap silinemedi"
                        )
                    }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Bir hata oluştu")
            }
        }
    }
} 