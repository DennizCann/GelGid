package com.denizcan.gelgid.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : NavigationItem(
        route = "home",
        title = "Ana Sayfa",
        icon = Icons.Default.Home
    )
    
    object AddTransaction : NavigationItem(
        route = "add_transaction",
        title = "İşlem Ekle",
        icon = Icons.Default.Add
    )
    
    object Transactions : NavigationItem(
        route = "transactions",
        title = "İşlemler",
        icon = Icons.Default.List
    )
    
    object Reports : NavigationItem(
        route = "reports",
        title = "Raporlar",
        icon = Icons.Default.Assessment
    )
    
    object Profile : NavigationItem(
        route = "profile",
        title = "Profil",
        icon = Icons.Default.Person
    )
    
    object Assets : NavigationItem(
        route = "assets",
        title = "Varlıklar",
        icon = Icons.Default.AccountBalance
    )
    
    object AddAsset : NavigationItem(
        route = "add_asset",
        title = "Varlık Ekle",
        icon = Icons.Default.Add
    )
    
    object EditAsset : NavigationItem(
        route = "edit_asset/{assetId}",
        title = "Varlık Düzenle",
        icon = Icons.Default.Edit
    ) {
        fun createRoute(assetId: String) = "edit_asset/$assetId"
    }
    
    object AssetDetail : NavigationItem(
        route = "asset_detail/{assetId}",
        title = "Varlık Detayı",
        icon = Icons.Default.AccountBalance
    ) {
        fun createRoute(assetId: String) = "asset_detail/$assetId"
    }
} 