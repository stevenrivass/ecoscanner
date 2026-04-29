package com.example.ecoscanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.ecoscanner.data.repository.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = GreenDark,
    tertiary = GreenPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = Purple40,
    tertiary = GreenLigth,
    background = White,
    surface = GreenDark,
    onPrimary = GreenDark
)

@Composable
fun EcoScannerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
        ThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}