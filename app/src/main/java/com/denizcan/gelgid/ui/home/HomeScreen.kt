package com.denizcan.gelgid.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.denizcan.gelgid.data.model.Asset
import com.denizcan.gelgid.data.model.Transaction
import com.denizcan.gelgid.data.model.TransactionType
import com.denizcan.gelgid.data.model.User
import com.denizcan.gelgid.navigation.NavGraph
import com.denizcan.gelgid.navigation.NavigationItem
import com.denizcan.gelgid.ui.transaction.TransactionViewModel
import com.denizcan.gelgid.ui.asset.AssetViewModel
import com.denizcan.gelgid.ui.asset.AssetTypeIcon
import com.denizcan.gelgid.ui.profile.ProfileViewModel
import java.util.Calendar
import java.util.Date
import com.denizcan.gelgid.ui.auth.AuthViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    onSignOut: () -> Unit,
    transactionViewModel: TransactionViewModel,
    assetViewModel: AssetViewModel,
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel
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
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            paddingValues = paddingValues,
            user = user,
            transactionViewModel = transactionViewModel,
            assetViewModel = assetViewModel,
            profileViewModel = profileViewModel,
            authViewModel = authViewModel
        )
    }
}

@Composable
fun HomeContent(
    user: User,
    viewModel: TransactionViewModel,
    assetViewModel: AssetViewModel
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
                    
                    // Gelir/Gider Özeti
                    MonthlyOverview(transactions = transactions)
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
fun MonthlyOverview(transactions: List<Transaction>) {
    val currentMonth = Calendar.getInstance().apply {
        time = Date()
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Tüm zamanlar için toplam
    val totalIncome = transactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }
    
    val totalExpense = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }
    
    val totalBalance = totalIncome - totalExpense

    // Bu ay için
    val monthlyTransactions = transactions.filter { it.date >= currentMonth }
    
    val monthlyIncome = monthlyTransactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }
    
    val monthlyExpense = monthlyTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    // En çok harcama yapılan kategoriler
    val topExpenseCategories = monthlyTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { transaction -> transaction.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(3)

    // En çok gelir kategorileri
    val topIncomeCategories = monthlyTransactions
        .filter { it.type == TransactionType.INCOME }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { transaction -> transaction.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Toplam Bakiye Bölümü
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Toplam Bakiye",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "₺${totalBalance}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    color = if (totalBalance >= 0) Color.Green else Color.Red
                )
            }
            
            Divider(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
            )
            
            // Bu Ay Özeti
            Text(
                text = "Bu Ay",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Gelir/Gider Özeti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Gelir Özeti
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Gelir",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₺${monthlyIncome}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Green
                    )
                }

                // Dikey Çizgi
                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )

                // Gider Özeti
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Gider",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₺${monthlyExpense}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Kategori Özetleri
            if (topIncomeCategories.isNotEmpty() || topExpenseCategories.isNotEmpty()) {
                Divider(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Gelir Kategorileri
                    if (topIncomeCategories.isNotEmpty()) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "En Çok Gelir",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            topIncomeCategories.forEach { (category, amount) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 8.dp, top = 4.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "₺${amount}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Green
                                    )
                                }
                            }
                        }
                    }

                    // Dikey Çizgi (eğer her iki kategori de varsa)
                    if (topIncomeCategories.isNotEmpty() && topExpenseCategories.isNotEmpty()) {
                        Divider(
                            modifier = Modifier
                                .height(100.dp)
                                .width(1.dp)
                                .padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }

                    // Gider Kategorileri
                    if (topExpenseCategories.isNotEmpty()) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "En Çok Gider",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            topExpenseCategories.forEach { (category, amount) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "₺${amount}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportsScreen() {
    // Raporlar ekranı
    Text(text = "Raporlar")
} 