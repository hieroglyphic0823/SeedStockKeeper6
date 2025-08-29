package com.example.seedstockkeeper6.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Material3 Key ColorからColorSchemeを自動生成するユーティリティ
 */

/**
 * Key Colorから派生色を生成するためのデータクラス
 */
data class KeyColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val neutral: Color,
    val error: Color
)

/**
 * Key ColorからLight ColorSchemeを生成
 */
fun generateLightColorScheme(keyColors: KeyColors): ColorScheme {
    return lightColorScheme(
        // Primary colors
        primary = keyColors.primary,
        onPrimary = Color.White,
        primaryContainer = keyColors.primary.copy(alpha = 0.9f),
        onPrimaryContainer = keyColors.primary.copy(alpha = 0.1f),
        
        // Secondary colors
        secondary = keyColors.secondary,
        onSecondary = Color.White,
        secondaryContainer = keyColors.secondary.copy(alpha = 0.9f),
        onSecondaryContainer = keyColors.secondary.copy(alpha = 0.1f),
        
        // Tertiary colors
        tertiary = keyColors.tertiary,
        onTertiary = Color.White,
        tertiaryContainer = keyColors.tertiary.copy(alpha = 0.9f),
        onTertiaryContainer = keyColors.tertiary.copy(alpha = 0.1f),
        
        // Error colors
        error = keyColors.error,
        onError = Color.White,
        errorContainer = keyColors.error.copy(alpha = 0.9f),
        onErrorContainer = keyColors.error.copy(alpha = 0.1f),
        
        // Neutral colors (Surface, Background, etc.)
        background = Color.White,
        onBackground = keyColors.neutral,
        surface = Color.White,
        onSurface = keyColors.neutral,
        surfaceVariant = keyColors.neutral.copy(alpha = 0.05f),
        onSurfaceVariant = keyColors.neutral.copy(alpha = 0.7f),
        
        // Outline colors
        outline = keyColors.neutral.copy(alpha = 0.5f),
        outlineVariant = keyColors.neutral.copy(alpha = 0.3f),
        
        // Inverse colors
        inverseSurface = keyColors.neutral,
        inverseOnSurface = Color.White,
        inversePrimary = keyColors.primary.copy(alpha = 0.8f),
        
        // Scrim
        scrim = Color.Black.copy(alpha = 0.32f),
        
        // Surface container colors
        surfaceDim = Color(0xFFF5F5F5),
        surfaceBright = Color.White,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFFAFAFA),
        surfaceContainer = Color(0xFFF5F5F5),
        surfaceContainerHigh = Color(0xFFF0F0F0),
        surfaceContainerHighest = Color(0xFFEBEBEB)
    )
}

/**
 * Key ColorからDark ColorSchemeを生成
 */
fun generateDarkColorScheme(keyColors: KeyColors): ColorScheme {
    return darkColorScheme(
        // Primary colors
        primary = keyColors.primary.copy(alpha = 0.8f),
        onPrimary = keyColors.primary.copy(alpha = 0.1f),
        primaryContainer = keyColors.primary,
        onPrimaryContainer = Color.White,
        
        // Secondary colors
        secondary = keyColors.secondary.copy(alpha = 0.8f),
        onSecondary = keyColors.secondary.copy(alpha = 0.1f),
        secondaryContainer = keyColors.secondary,
        onSecondaryContainer = Color.White,
        
        // Tertiary colors
        tertiary = keyColors.tertiary.copy(alpha = 0.8f),
        onTertiary = keyColors.tertiary.copy(alpha = 0.1f),
        tertiaryContainer = keyColors.tertiary,
        onTertiaryContainer = Color.White,
        
        // Error colors
        error = keyColors.error.copy(alpha = 0.8f),
        onError = keyColors.error.copy(alpha = 0.1f),
        errorContainer = keyColors.error,
        onErrorContainer = Color.White,
        
        // Neutral colors (Surface, Background, etc.)
        background = Color(0xFF121212),
        onBackground = Color.White,
        surface = Color(0xFF1E1E1E),
        onSurface = Color.White,
        surfaceVariant = Color(0xFF2D2D2D),
        onSurfaceVariant = Color.White.copy(alpha = 0.7f),
        
        // Outline colors
        outline = Color.White.copy(alpha = 0.5f),
        outlineVariant = Color.White.copy(alpha = 0.3f),
        
        // Inverse colors
        inverseSurface = Color.White,
        inverseOnSurface = keyColors.neutral,
        inversePrimary = keyColors.primary,
        
        // Scrim
        scrim = Color.Black.copy(alpha = 0.32f),
        
        // Surface container colors
        surfaceDim = Color(0xFF121212),
        surfaceBright = Color(0xFF2D2D2D),
        surfaceContainerLowest = Color(0xFF0A0A0A),
        surfaceContainerLow = Color(0xFF1A1A1A),
        surfaceContainer = Color(0xFF1E1E1E),
        surfaceContainerHigh = Color(0xFF2D2D2D),
        surfaceContainerHighest = Color(0xFF383838)
    )
}

/**
 * Key ColorからColorSchemeを生成（ライト/ダーク自動判定）
 */
fun generateColorScheme(keyColors: KeyColors, isDark: Boolean): ColorScheme {
    return if (isDark) {
        generateDarkColorScheme(keyColors)
    } else {
        generateLightColorScheme(keyColors)
    }
}
