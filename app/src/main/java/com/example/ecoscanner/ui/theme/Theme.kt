package com.example.ecoscanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.ecoscanner.data.repository.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = GreenLigth,
    secondary = Green10,
    tertiary = Purple40,
    background = GreenSuperDark,
    surface = GreenSuperDark,
    onPrimary = GreenDark,
    onSecondary = White,
    onTertiary = Purple10,
    onBackground = GreenPrimary,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = Purple40,
    tertiary = Green10,
    background = White,
    surface = White,
    onPrimary = Purple40,
    onSecondary = White,
    onTertiary = GreenPrimary,
    onBackground = Purple10,
    onSurface = GreenSuperDark
)

/* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */

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