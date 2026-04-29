package com.example.ecoscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.ecoscanner.data.repository.ThemeMode
import com.example.ecoscanner.data.repository.UserPreferencesRepository
import com.example.ecoscanner.ui.navigation.AppNavigation
import com.example.ecoscanner.ui.theme.EcoScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Leemos las preferencias de tema reactivamente
            val prefsRepo = remember { UserPreferencesRepository(applicationContext) }
            val themeMode by prefsRepo.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            EcoScannerTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}