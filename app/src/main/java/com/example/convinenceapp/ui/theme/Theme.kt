package com.example.convinenceapp.ui.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppThemeMode(val title: String) {
    SYSTEM("System Default"),
    LIGHT("Classic Light"),
    DARK("Midnight Dark"),
    CYBERPUNK("Neon Cyber"),
    WARHAMMER("Grim Dark (40K)"),
    FOREST("Emerald Nature")
}

object ThemeState {
    private val _theme = MutableStateFlow(AppThemeMode.SYSTEM)
    val theme: StateFlow<AppThemeMode> = _theme

    fun setTheme(mode: AppThemeMode) {
        _theme.value = mode
    }
}

// 1. Cyberpunk Colors
private val CyberpunkDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00FFCC),
    onPrimary = Color(0xFF00382E),
    secondary = Color(0xFFFF007F),
    onSecondary = Color(0xFF3F001C),
    tertiary = Color(0xFFFFD700),
    background = Color(0xFF101018),
    surface = Color(0xFF181824),
    onBackground = Color(0xFFE0E0FF),
    onSurface = Color(0xFFE0E0FF)
)

// 2. Warhammer 40K Colors (Imperial Gold & Crimson)
private val WarhammerDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE6C229),      // Imperial Gold
    onPrimary = Color(0xFF261D00),
    secondary = Color(0xFF9E1919),    // Mephiston Crimson
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF6B7280),     // Cermaite Iron
    background = Color(0xFF141312),
    surface = Color(0xFF1E1C1A),
    onBackground = Color(0xFFE5E0D8),
    onSurface = Color(0xFFE5E0D8)
)

// 3. Emerald Nature Colors
private val ForestColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF558B2F),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF8D6E63),
    background = Color(0xFFF1F8E9),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1B5E20),
    onSurface = Color(0xFF1B5E20)
)

private val StandardDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

private val StandardLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFAF9FD),
    surface = Color(0xFFFFFFFF)
)

@Composable
fun ConvinenceappTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    systemDark: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.SYSTEM -> if (systemDark) StandardDarkColorScheme else StandardLightColorScheme
        AppThemeMode.LIGHT -> StandardLightColorScheme
        AppThemeMode.DARK -> StandardDarkColorScheme
        AppThemeMode.CYBERPUNK -> CyberpunkDarkColorScheme
        AppThemeMode.WARHAMMER -> WarhammerDarkColorScheme
        AppThemeMode.FOREST -> ForestColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
