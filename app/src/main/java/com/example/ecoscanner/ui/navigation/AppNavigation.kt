
package com.example.ecoscanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecoscanner.ui.screens.calculation.CalculationScreen
import com.example.ecoscanner.ui.screens.scanner.ScannerScreen
import com.example.ecoscanner.ui.screens.statistics.StatisticsScreen

// ─── Rutas de navegación ───────────────────────────────────────────────────

object Routes {
    const val LOGIN      = "login"
    const val SCANNER    = "scanner"
    const val CALCULATION = "calculation"
    const val STATISTICS = "statistics"
}

// ─── Grafo de navegación principal ────────────────────────────────────────

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = Routes.LOGIN
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.SCANNER) {
                        // Elimina Login del back-stack para que "atrás" no regrese
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SCANNER) {
            ScannerScreen(
                onScanClick       = { navController.navigate(Routes.CALCULATION) },
                onStatisticsClick = { navController.navigate(Routes.STATISTICS) }
            )
        }

        composable(Routes.CALCULATION) {
            CalculationScreen(
                onBack       = { navController.popBackStack() },
                onSaveAndGoHome = {
                    navController.navigate(Routes.SCANNER) {
                        popUpTo(Routes.SCANNER) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    TODO("Not yet implemented")
}