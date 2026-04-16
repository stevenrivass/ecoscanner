package com.example.ecoscanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecoscanner.ui.screens.calculation.CalculationScreen
import com.example.ecoscanner.ui.screens.login.LoginScreen
import com.example.ecoscanner.ui.screens.scanner.ScannerScreen
import com.example.ecoscanner.ui.screens.statistics.StatisticsScreen


// Objeto para tener los nombres de las rutas sin equivocarno
object Routes {
    const val LOGIN = "login"
    const val SCANNER = "scanner"
    const val CALCULATION = "calculation"
    const val STATISTICS = "statistics"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }
        composable(Routes.SCANNER) {
            ScannerScreen(navController)
        }
        composable(Routes.CALCULATION) {
            CalculationScreen(navController)
        }
        composable(Routes.STATISTICS) {
            StatisticsScreen(navController)
        }
    }
}