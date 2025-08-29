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
        // Primary colors - Material3 Key Colorルールに従う
        primary = keyColors.primary, // トーン40（Key Color）
        onPrimary = Color.White, // トーン100
        primaryContainer = keyColors.primary.copy(alpha = 0.12f), // トーン90（薄い色）
        onPrimaryContainer = keyColors.primary.copy(alpha = 0.9f), // トーン10（濃い色）
        
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
        // Primary colors - Material3 Key Colorルールに従う
        primary = keyColors.primary.copy(alpha = 0.8f), // トーン80（明るい色）
        onPrimary = keyColors.primary.copy(alpha = 0.2f), // トーン20（濃い色）
        primaryContainer = keyColors.primary.copy(alpha = 0.3f), // トーン30（濃い色）
        onPrimaryContainer = keyColors.primary.copy(alpha = 0.9f), // トーン90（明るい色）
        
        // Secondary colors
        secondary = keyColors.secondary.copy(alpha = 0.8f), // トーン80
        onSecondary = keyColors.secondary.copy(alpha = 0.2f), // トーン20
        secondaryContainer = keyColors.secondary.copy(alpha = 0.3f), // トーン30
        onSecondaryContainer = keyColors.secondary.copy(alpha = 0.9f), // トーン90
        
        // Tertiary colors
        tertiary = keyColors.tertiary.copy(alpha = 0.8f), // トーン80
        onTertiary = keyColors.tertiary.copy(alpha = 0.2f), // トーン20
        tertiaryContainer = keyColors.tertiary.copy(alpha = 0.3f), // トーン30
        onTertiaryContainer = keyColors.tertiary.copy(alpha = 0.9f), // トーン90
        
        // Error colors
        error = keyColors.error.copy(alpha = 0.8f), // トーン80
        onError = keyColors.error.copy(alpha = 0.2f), // トーン20
        errorContainer = keyColors.error.copy(alpha = 0.3f), // トーン30
        onErrorContainer = keyColors.error.copy(alpha = 0.9f), // トーン90
        
        // Neutral colors (Surface, Background, etc.)
        background = Color(0xFF121212), // トーン6
        onBackground = Color.White, // トーン90
        surface = Color(0xFF1E1E1E), // トーン6
        onSurface = Color.White, // トーン90
        surfaceVariant = Color(0xFF2D2D2D), // トーン12
        onSurfaceVariant = Color.White.copy(alpha = 0.7f), // トーン70
        
        // Outline colors
        outline = Color.White.copy(alpha = 0.5f), // トーン50
        outlineVariant = Color.White.copy(alpha = 0.3f), // トーン30
        
        // Inverse colors
        inverseSurface = Color.White, // トーン90
        inverseOnSurface = keyColors.neutral, // トーン10
        inversePrimary = keyColors.primary, // トーン40
        
        // Scrim
        scrim = Color.Black.copy(alpha = 0.32f),
        
        // Surface container colors
        surfaceDim = Color(0xFF121212), // トーン6
        surfaceBright = Color(0xFF2D2D2D), // トーン24
        surfaceContainerLowest = Color(0xFF0A0A0A), // トーン4
        surfaceContainerLow = Color(0xFF1A1A1A), // トーン8
        surfaceContainer = Color(0xFF1E1E1E), // トーン12
        surfaceContainerHigh = Color(0xFF2D2D2D), // トーン17
        surfaceContainerHighest = Color(0xFF383838) // トーン22
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
