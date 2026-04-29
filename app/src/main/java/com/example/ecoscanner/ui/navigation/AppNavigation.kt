package com.example.ecoscanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecoscanner.data.repository.AuthRepository
import com.example.ecoscanner.ui.main.MainScreen
import com.example.ecoscanner.ui.screens.calculation.CalculationScreen
import com.example.ecoscanner.ui.screens.login.LoginScreen

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val CALCULATION = "calculation"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val authRepo = remember { AuthRepository() }
    val startRoute = if (authRepo.currentUser() != null) Routes.MAIN else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }
        composable(Routes.MAIN) {
            MainScreen(navController)
        }
        composable(Routes.CALCULATION) {
            CalculationScreen(navController)
        }
    }
}