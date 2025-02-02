package com.denizcan.gelgid.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.denizcan.gelgid.data.model.User
import com.denizcan.gelgid.ui.transaction.TransactionViewModel
import com.denizcan.gelgid.ui.asset.AssetViewModel
import com.denizcan.gelgid.ui.home.HomeContent
import com.denizcan.gelgid.ui.transaction.AddTransactionScreen
import com.denizcan.gelgid.ui.transaction.TransactionsScreen
import com.denizcan.gelgid.ui.asset.AssetsScreen
import com.denizcan.gelgid.ui.asset.AddAssetScreen
import com.denizcan.gelgid.ui.asset.EditAssetScreen
import com.denizcan.gelgid.ui.asset.AssetDetailScreen
import com.denizcan.gelgid.ui.home.ReportsScreen
import com.denizcan.gelgid.ui.profile.ProfileScreen
import com.denizcan.gelgid.ui.profile.EditProfileScreen
import com.denizcan.gelgid.ui.profile.ProfileViewModel
import com.denizcan.gelgid.ui.profile.ProfileState
import com.denizcan.gelgid.ui.profile.ChangePasswordScreen
import com.denizcan.gelgid.ui.auth.AuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    user: User,
    transactionViewModel: TransactionViewModel,
    assetViewModel: AssetViewModel,
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel
) {
    val profileState = profileViewModel.profileState.collectAsState().value

    // LaunchedEffect ile profil güncelleme durumunu izleyelim
    LaunchedEffect(profileState) {
        when (profileState) {
            is ProfileState.Success -> {
                // Profil güncellendiğinde geri dön
                navController.popBackStack()
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavigationItem.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(NavigationItem.Home.route) {
            HomeContent(
                user = user,
                viewModel = transactionViewModel,
                assetViewModel = assetViewModel
            )
        }

        // Ana ekranlar
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
            ProfileScreen(
                user = user,
                onEditProfile = {
                    navController.navigate("edit_profile")
                },
                onChangePassword = {
                    navController.navigate("change_password")
                },
                onDeleteAccount = { password ->
                    profileViewModel.deleteAccount(password)
                },
                onSignOut = {
                    authViewModel.signOut()
                },
                viewModel = profileViewModel
            )
        }

        // Varlık ekranları
        composable(NavigationItem.Assets.route) {
            AssetsScreen(
                viewModel = assetViewModel,
                onAddClick = {
                    navController.navigate(NavigationItem.AddAsset.route)
                },
                onEditClick = { assetId ->
                    navController.navigate(NavigationItem.EditAsset.createRoute(assetId))
                },
                onItemClick = { assetId ->
                    navController.navigate(NavigationItem.AssetDetail.createRoute(assetId))
                }
            )
        }

        composable(NavigationItem.AddAsset.route) {
            AddAssetScreen(
                viewModel = assetViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NavigationItem.EditAsset.route,
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val assetId = backStackEntry.arguments?.getString("assetId")
            val asset = assetViewModel.assets.collectAsState().value.find { it.id == assetId }
            
            if (asset != null) {
                EditAssetScreen(
                    asset = asset,
                    viewModel = assetViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            route = NavigationItem.AssetDetail.route,
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val assetId = backStackEntry.arguments?.getString("assetId")
            val asset = assetViewModel.assets.collectAsState().value.find { it.id == assetId }
            
            if (asset != null) {
                AssetDetailScreen(
                    asset = asset,
                    viewModel = assetViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditClick = { id ->
                        navController.navigate(NavigationItem.EditAsset.createRoute(id))
                    },
                    onDeleteClick = { id ->
                        assetViewModel.deleteAsset(id)
                        navController.popBackStack()
                    }
                )
            }
        }

        // Yeni route'lar ekleyelim
        composable("edit_profile") {
            EditProfileScreen(
                user = user,
                onUpdateProfile = { newName ->
                    profileViewModel.updateProfile(newName)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = profileViewModel
            )
        }

        composable("change_password") {
            ChangePasswordScreen(
                onChangePassword = { currentPassword, newPassword ->
                    profileViewModel.changePassword(currentPassword, newPassword)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = profileViewModel
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
} 