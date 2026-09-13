package com.example.appfire.navigation

import android.annotation.SuppressLint
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appfire.gui.*
import com.example.appfire.viewmodel.AuthViewModel
import com.example.appfire.viewmodel.HomeViewModel
import com.example.appfire.viewmodel.OrderViewModel

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val isLoggedIn = authViewModel.isLoggedIn()

    NavHost(navController, startDestination = if (isLoggedIn) "home" else "login") {
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        composable("register") {
            RegisterScreen(navController, authViewModel)
        }
        composable("home") {
            HomeScreen(
                onCartClick = {},
                navController = navController,
                authViewModel = authViewModel,
                homeViewModel = homeViewModel
            )
        }
        composable("profile") {
            ProfileScreen(
                navController = navController,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onAdminOrderClick = {
                    navController.navigate("admin_orders")
                }
            )
        }
        composable("order_history") {
            OrderHistoryScreen(
                onBackClick = { navController.popBackStack() },
                onOrderClick = { order ->
                    orderViewModel.setSelectedOrder(order)
                    navController.navigate("orderdetail")
                }
            )
        }
        composable("orderdetail") {
            val order = orderViewModel.selectedOrder
            if (order != null) {
                OrderDetailScreen(
                    order = order,
                    onBackClick = {
                        orderViewModel.clearSelectedOrder()
                        navController.popBackStack()
                    }
                )
            }
        }

        composable("upload") {
            UploadScreen(navController, homeViewModel)
        }
        composable("admin_orders") {
            AdminOrderManagementScreen(
                onBackClick = { navController.popBackStack() },
                onOrderClick = { order, userId ->
                    // Truyền order và userId sang màn hình chi tiết
                    orderViewModel.setSelectedOrder(order)
                    navController.navigate("admin_order_detail")
                }
            )
        }
        composable("admin_order_detail") {
            val order = orderViewModel.selectedOrder
            if (order != null) {
                OrderDetailScreen(
                    order = order,
                    onBackClick = {
                        orderViewModel.clearSelectedOrder()
                        navController.popBackStack()
                    }
                )
            }
        }

    }
}