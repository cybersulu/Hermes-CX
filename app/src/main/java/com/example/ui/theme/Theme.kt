package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF96CCFF),
    onPrimary = Color(0xFF003353),
    primaryContainer = Color(0xFF004A76),
    onPrimaryContainer = Color(0xFFCEE5FF),
    secondary = Color(0xFFB8C8D9),
    onSecondary = Color(0xFF23323F),
    secondaryContainer = Color(0xFF394957),
    onSecondaryContainer = Color(0xFFD4E4F6),
    tertiary = AmberTertiaryDark,
    onTertiary = AmberOnTertiaryDark,
    tertiaryContainer = AmberTertiaryContainerDark,
    onTertiaryContainer = AmberOnTertiaryContainerDark,
    background = Color(0xFF101418),
    surface = Color(0xFF12171B),
    surfaceVariant = Color(0xFF222830),
    onSurface = Color(0xFFE2E2E6),
    onSurfaceVariant = Color(0xFFC2C7CE),
    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFF2E3842)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00639B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCEE5FF),
    onPrimaryContainer = Color(0xFF001D33),
    secondary = Color(0xFF51606F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD4E4F6),
    onSecondaryContainer = Color(0xFF0D1D2A),
    tertiary = AmberTertiaryLight,
    onTertiary = AmberOnTertiaryLight,
    tertiaryContainer = AmberTertiaryContainerLight,
    onTertiaryContainer = AmberOnTertiaryContainerLight,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    themeMode: Int = 0, // 0 = System, 1 = Light, 2 = Dark
    themeColorId: String = "ocean",
    customThemeColor: Int = 0xFF7C3AED.toInt(),
    dynamicColor: Boolean = false, // Bespoke brand palette default
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        1 -> false
        2 -> true
        else -> systemDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeColorId == "custom" -> {
            generateCustomColorScheme(androidx.compose.ui.graphics.Color(customThemeColor), isDark)
        }
        else -> {
            val preset = PRESET_THEMES.find { it.id == themeColorId } ?: PRESET_THEMES.first()
            if (isDark) preset.darkScheme else preset.lightScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
