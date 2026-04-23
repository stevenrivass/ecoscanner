package com.example.ecoscanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecoscanner.data.repository.AuthRepository
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

    // Si ya hay sesión activa, saltamos al Scanner directamente
    val authRepo = remember { AuthRepository() }
    val startRoute = if (authRepo.currentUser() != null) Routes.SCANNER else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startRoute
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