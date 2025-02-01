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
        icon = Icons.Default.Info
    )
    
    object Profile : NavigationItem(
        route = "profile",
        title = "Profil",
        icon = Icons.Default.Person
    )
} 