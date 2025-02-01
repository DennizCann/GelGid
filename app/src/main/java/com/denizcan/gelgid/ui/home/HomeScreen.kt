package com.denizcan.gelgid.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.denizcan.gelgid.data.model.User
import com.denizcan.gelgid.navigation.NavigationItem
import com.denizcan.gelgid.ui.transaction.AddTransactionScreen
import com.denizcan.gelgid.ui.transaction.TransactionViewModel
import com.denizcan.gelgid.ui.transaction.TransactionsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    onSignOut: () -> Unit,
    transactionViewModel: TransactionViewModel
) {
    var selectedItem by remember { mutableStateOf(0) }
    val navController = rememberNavController()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GelGid") },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Çıkış Yap"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    NavigationItem.Home,
                    NavigationItem.AddTransaction,
                    NavigationItem.Transactions,
                    NavigationItem.Reports,
                    NavigationItem.Profile
                )
                
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            navController.navigate(item.route)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavigationItem.Home.route) {
                HomeContent(user = user)
            }
            composable(NavigationItem.AddTransaction.route) {
                AddTransactionScreen(viewModel = transactionViewModel)
            }
            composable(NavigationItem.Transactions.route) {
                TransactionsScreen(viewModel = transactionViewModel)
            }
            composable(NavigationItem.Reports.route) {
                ReportsScreen()
            }
            composable(NavigationItem.Profile.route) {
                ProfileScreen(user = user)
            }
        }
    }
}

@Composable
fun HomeContent(user: User) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hoş Geldin, ${user.name}!",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun ReportsScreen() {
    // Raporlar ekranı
    Text(text = "Raporlar")
}

@Composable
fun ProfileScreen(user: User) {
    // Profil ekranı
    Column {
        Text(text = "Profil")
        Text(text = "Ad: ${user.name}")
        Text(text = "E-posta: ${user.email}")
    }
} 