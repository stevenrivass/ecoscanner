package com.example.ecoscanner.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.example.ecoscanner.ui.screens.CalculationScreen
import com.example.ecoscanner.ui.screens.LoginScreen
import com.example.ecoscanner.ui.screens.ScannerScreen
import com.example.ecoscanner.ui.screens.StatisticsScreen
import com.example.ecoscanner.ui.theme.EcoScannerTheme

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Scanner : Screen("scanner")
    object Calculation : Screen("calculation")
    object Statistics : Screen("statistics")
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Scanner.route) {
            ScannerScreen(navController = navController)
        }
        composable(Screen.Calculation.route) {
            CalculationScreen(navController = navController)
        }
        composable(Screen.Statistics.route) {
            StatisticsScreen(navController = navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    EcoScannerTheme {
        AppNavigation(navController = rememberNavController())
    }
}