package com.denizcan.gelgid.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.denizcan.gelgid.data.model.Asset
import com.denizcan.gelgid.data.model.TransactionType
import com.denizcan.gelgid.data.model.User
import com.denizcan.gelgid.navigation.NavGraph
import com.denizcan.gelgid.navigation.NavigationItem
import com.denizcan.gelgid.ui.transaction.TransactionViewModel
import com.denizcan.gelgid.ui.asset.AssetViewModel
import com.denizcan.gelgid.ui.asset.AssetTypeIcon
import com.denizcan.gelgid.ui.profile.ProfileViewModel
import com.denizcan.gelgid.ui.auth.AuthViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import androidx.navigation.NavController
import com.denizcan.gelgid.data.model.RecurringTransaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    onSignOut: () -> Unit,
    transactionViewModel: TransactionViewModel,
    assetViewModel: AssetViewModel,
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    recurringTransactionViewModel: TransactionViewModel
) {
    var selectedItem by remember { mutableStateOf(0) }
    val navController = rememberNavController()
    var showMenu by remember { mutableStateOf(false) }
    
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
                    NavigationItem.Profile,
                    NavigationItem.Assets
                )
                
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { 
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            Column {
                if (showMenu) {
                    // Sabit Gelir Ekle
                    FloatingActionButton(
                        onClick = { 
                            showMenu = false
                            navController.navigate("add_recurring_income")
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.TrendingUp, "Sabit Gelir Ekle")
                            Spacer(Modifier.width(8.dp))
                            Text("Sabit Gelir Ekle")
                        }
                    }

                    // Sabit Gider Ekle
                    FloatingActionButton(
                        onClick = { 
                            showMenu = false
                            navController.navigate("add_recurring_expense")
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.TrendingDown, "Sabit Gider Ekle")
                            Spacer(Modifier.width(8.dp))
                            Text("Sabit Gider Ekle")
                        }
                    }

                    // Varlık Ekle
                    FloatingActionButton(
                        onClick = { 
                            showMenu = false
                            navController.navigate(NavigationItem.AddAsset.route)
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccountBalance, "Varlık Ekle")
                            Spacer(Modifier.width(8.dp))
                            Text("Varlık Ekle")
                        }
                    }
                }

                // Ana FAB
                FloatingActionButton(
                    onClick = { showMenu = !showMenu }
                ) {
                    Icon(
                        if (showMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (showMenu) "Menüyü Kapat" else "Menüyü Aç"
                    )
                }
            }
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            paddingValues = paddingValues,
            user = user,
            transactionViewModel = transactionViewModel,
            assetViewModel = assetViewModel,
            profileViewModel = profileViewModel,
            authViewModel = authViewModel,
            recurringTransactionViewModel = recurringTransactionViewModel
        )
    }
}

@Composable
fun HomeContent(
    user: User,
    viewModel: TransactionViewModel,
    assetViewModel: AssetViewModel,
    navController: NavController
) {
    val transactions by viewModel.transactions.collectAsState()
    val assets by assetViewModel.assets.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(key1 = true) {
        try {
            coroutineScope {
                launch { 
                    viewModel.getTransactions()
                        .onFailure {
                            hasError = true
                        }
                }
                launch { 
                    assetViewModel.getAssets()
                        .onFailure {
                            hasError = true
                        }
                }
                launch {
                    viewModel.getRecurringTransactions()
                }
            }
        } catch (e: Exception) {
            hasError = true
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            hasError -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Veriler yüklenirken bir hata oluştu",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            isLoading = true
                            hasError = false
                            scope.launch { 
                                viewModel.getTransactions()
                                assetViewModel.getAssets()
                            }
                        }
                    ) {
                        Text("Tekrar Dene")
                    }
                }
            }
            else -> {
                Column {
                    // Toplam Varlık Kartı
                    TotalAssetsCard(assets = assets)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Sabit İşlemler Kartı
                    RecurringTransactionsCard(
                        recurringTransactions = viewModel.recurringTransactions.collectAsState().value,
                        onClick = { navController.navigate("recurring_transactions") }
                    )
                }
            }
        }
    }
}

@Composable
fun TotalAssetsCard(assets: List<Asset>) {
    val totalAssets = assets.sumOf { it.amount }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Toplam Varlık",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "₺${totalAssets}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            if (assets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Varlık tiplerine göre dağılım
                assets.groupBy { it.type }
                    .mapValues { it.value.sumOf { asset -> asset.amount } }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(3)
                    .forEach { (type, amount) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssetTypeIcon(type)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = type.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "₺${amount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
            }
        }
    }
}

@Composable
fun RecurringTransactionsCard(
    recurringTransactions: List<RecurringTransaction>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Sabit İşlemler",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            val totalIncome = recurringTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }

            val totalExpense = recurringTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            val balance = totalIncome - totalExpense

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Toplam Gelir",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₺$totalIncome",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Green
                    )
                }

                Column {
                    Text(
                        text = "Toplam Gider",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₺$totalExpense",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Net Bakiye: ₺$balance",
                style = MaterialTheme.typography.titleLarge,
                color = if (balance >= 0) Color.Green else MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
