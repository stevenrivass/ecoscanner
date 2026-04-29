package com.example.ecoscanner.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ecoscanner.ui.screens.history.HistoryScreen
import com.example.ecoscanner.ui.screens.home.HomeScreen
import com.example.ecoscanner.ui.screens.profile.ProfileScreen
import com.example.ecoscanner.ui.screens.stats.StatsScreen

// ---------- Tabs del bottom bar ----------

private sealed class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home    : BottomTab("home",    "Inicio",       Icons.Filled.Home)
    data object Stats   : BottomTab("stats",   "Estadísticas", Icons.Filled.BarChart)
    data object History : BottomTab("history", "Historial",    Icons.Filled.History)
    data object Profile : BottomTab("profile", "Perfil",       Icons.Filled.Person)
}

private val tabs = listOf(BottomTab.Home, BottomTab.Stats, BottomTab.History, BottomTab.Profile)

// ---------- Pantalla principal con BottomNav ----------

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = { AppBottomBar(bottomNavController) }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomTab.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(BottomTab.Home.route)    { HomeScreen(rootNavController) }
            composable(BottomTab.Stats.route)   { StatsScreen() }
            composable(BottomTab.History.route) { HistoryScreen() }
            composable(BottomTab.Profile.route) { ProfileScreen(rootNavController) }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController) {
    NavigationBar {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        tabs.forEach { tab ->
            val selected = backStackEntry?.destination?.hierarchy
                ?.any { it.route == tab.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}