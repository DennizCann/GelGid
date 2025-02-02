package com.denizcan.gelgid.ui.asset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denizcan.gelgid.data.model.Asset
import com.denizcan.gelgid.data.model.AssetType
import com.denizcan.gelgid.data.model.AssetHistory
import com.denizcan.gelgid.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AssetState {
    object Initial : AssetState()
    object Loading : AssetState()
    data class Success(val asset: Asset) : AssetState()
    data class Error(val message: String) : AssetState()
}

class AssetViewModel(
    private val repository: FirebaseRepository
) : ViewModel() {
    private val _assetState = MutableStateFlow<AssetState>(AssetState.Initial)
    val assetState: StateFlow<AssetState> = _assetState

    private val _assets = MutableStateFlow<List<Asset>>(emptyList())
    val assets: StateFlow<List<Asset>> = _assets

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _assetHistory = MutableStateFlow<List<AssetHistory>>(emptyList())
    val assetHistory: StateFlow<List<AssetHistory>> = _assetHistory

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger

    fun addAsset(
        name: String,
        type: AssetType,
        amount: Double,
        description: String
    ) {
        viewModelScope.launch {
            _assetState.value = AssetState.Loading

            val asset = Asset(
                name = name,
                type = type,
                amount = amount,
                description = description
            )

            repository.addAsset(asset)
                .onSuccess {
                    _assetState.value = AssetState.Success(it)
                    getAssets() // Listeyi güncelle
                    refreshAssets()
                }
                .onFailure { exception ->
                    _assetState.value = AssetState.Error(
                        exception.message ?: "Varlık eklenemedi"
                    )
                }
        }
    }

    suspend fun getAssets(): Result<Unit> {
        return try {
            repository.getAssets()
                .onSuccess { assetsList ->
                    _assets.value = assetsList
                }
                .map { } // Result<List<Asset>>'i Result<Unit>'e dönüştürüyoruz
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateAsset(asset: Asset) {
        viewModelScope.launch {
            _assetState.value = AssetState.Loading

            repository.updateAsset(asset)
                .onSuccess {
                    _assetState.value = AssetState.Success(it)
                    getAssets() // Listeyi güncelle
                    refreshAssets()
                }
                .onFailure { exception ->
                    _assetState.value = AssetState.Error(
                        exception.message ?: "Varlık güncellenemedi"
                    )
                }
        }
    }

    fun deleteAsset(assetId: String) {
        viewModelScope.launch {
            repository.deleteAsset(assetId)
                .onSuccess {
                    getAssets() // Listeyi güncelle
                    refreshAssets()
                }
                .onFailure { exception ->
                    // Hata durumunu handle et
                }
        }
    }

    fun getAssetHistory(assetId: String) {
        viewModelScope.launch {
            repository.getAssetHistory(assetId)
                .onSuccess { history ->
                    _assetHistory.value = history
                }
        }
    }

    fun updateAssetValue(
        assetId: String,
        newAmount: Double,
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.addAssetHistory(assetId, newAmount, note)
                .onSuccess {
                    getAssets()  // Listeyi güncelle
                    getAssetHistory(assetId)  // Geçmişi güncelle
                }
        }
    }

    fun refreshAssets() {
        viewModelScope.launch {
            getAssets()
            _refreshTrigger.value = _refreshTrigger.value + 1
        }
    }
} 