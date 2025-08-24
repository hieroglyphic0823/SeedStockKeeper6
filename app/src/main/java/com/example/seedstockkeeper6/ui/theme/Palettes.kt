package com.example.seedstockkeeper6.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Color を持つパレット定義
data class Palette(
    val primary: Color, val onPrimary: Color,
    val primaryContainer: Color, val onPrimaryContainer: Color,
    val secondary: Color, val onSecondary: Color,
    val secondaryContainer: Color, val onSecondaryContainer: Color,
    val tertiary: Color, val onTertiary: Color,
    val background: Color, val surface: Color, val surfaceVariant: Color,
    val error: Color, val outline: Color
)

enum class ThemeFlavor { Vitamin, Soil, Herb, Ocean, Plum, Sakura }

// --- 6パターン ---
val Vitamin = Palette(
    primary = Color(0xFFFF8A00), onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9A6), onPrimaryContainer = Color(0xFF422300),
    secondary = Color(0xFF43A047), onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7E6C9), onSecondaryContainer = Color(0xFF0E2B13),
    tertiary = Color(0xFF00ACC1), onTertiary = Color.White,
    background = Color(0xFFFFFBF7), surface = Color(0xFFFFFBF7), surfaceVariant = Color(0xFFF2E7DC),
    error = Color(0xFFB00020), outline = Color(0xFF8E8E8E)
)

val Soil = Palette(
    primary = Color(0xFF795548), onPrimary = Color.White,
    primaryContainer = Color(0xFFD7C2B8), onPrimaryContainer = Color(0xFF2E1E18),
    secondary = Color(0xFF8D6E63), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEBD6CF), onSecondaryContainer = Color(0xFF2F2320),
    tertiary = Color(0xFFA1887F), onTertiary = Color(0xFF1E1512),
    background = Color(0xFFFCFAF7), surface = Color(0xFFFCFAF7), surfaceVariant = Color(0xFFEDE0D8),
    error = Color(0xFFB00020), outline = Color(0xFF8B7E76)
)

val Herb = Palette(
    primary = Color(0xFF2E7D32), onPrimary = Color.White,
    primaryContainer = Color(0xFFBDE5BF), onPrimaryContainer = Color(0xFF0A2A0C),
    secondary = Color(0xFF43A047), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2F1D4), onSecondaryContainer = Color(0xFF09240C),
    tertiary = Color(0xFF00897B), onTertiary = Color.White,
    background = Color(0xFFF7FBF8), surface = Color(0xFFF7FBF8), surfaceVariant = Color(0xFFE0E9E2),
    error = Color(0xFFB00020), outline = Color(0xFF7A8A7E)
)

val Ocean = Palette(
    primary = Color(0xFF00796B), onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB), onPrimaryContainer = Color(0xFF062825),
    secondary = Color(0xFF455A64), onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFE3EA), onSecondaryContainer = Color(0xFF0E2127),
    tertiary = Color(0xFF1976D2), onTertiary = Color.White,
    background = Color(0xFFF7FAFA), surface = Color(0xFFF7FAFA), surfaceVariant = Color(0xFFE1ECEA),
    error = Color(0xFFB00020), outline = Color(0xFF7B8D8B)
)

val Plum = Palette(
    primary = Color(0xFF7B1FA2), onPrimary = Color.White,
    primaryContainer = Color(0xFFE1BEE7), onPrimaryContainer = Color(0xFF290833),
    secondary = Color(0xFF5E35B1), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1C4E9), onSecondaryContainer = Color(0xFF21173A),
    tertiary = Color(0xFF00ACC1), onTertiary = Color.White,
    background = Color(0xFFFBF7FD), surface = Color(0xFFFBF7FD), surfaceVariant = Color(0xFFECE3F3),
    error = Color(0xFFB00020), outline = Color(0xFF8B7D92)
)

val Sakura = Palette(
    primary = Color(0xFFD81B60), onPrimary = Color.White,
    primaryContainer = Color(0xFFF8BBD0), onPrimaryContainer = Color(0xFF4A0C22),
    secondary = Color(0xFF43A047), onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9), onSecondaryContainer = Color(0xFF0F2913),
    tertiary = Color(0xFF6D4C41), onTertiary = Color.White,
    background = Color(0xFFFEFBFE), surface = Color(0xFFFEFBFE), surfaceVariant = Color(0xFFEFE2EA),
    error = Color(0xFFB00020), outline = Color(0xFF8C7A83)
)

private val Palettes = mapOf(
    ThemeFlavor.Vitamin to Vitamin,
    ThemeFlavor.Soil to Soil,
    ThemeFlavor.Herb to Herb,
    ThemeFlavor.Ocean to Ocean,
    ThemeFlavor.Plum to Plum,
    ThemeFlavor.Sakura to Sakura
)

// Palette → Material3 ColorScheme へ
@Composable
fun flavorColorScheme(flavor: ThemeFlavor, dark: Boolean): ColorScheme {
    val p = Palettes.getValue(flavor)
    val base = if (!dark) lightColorScheme() else darkColorScheme()
    return base.copy(
        primary = p.primary, onPrimary = p.onPrimary,
        primaryContainer = p.primaryContainer, onPrimaryContainer = p.onPrimaryContainer,
        secondary = p.secondary, onSecondary = p.onSecondary,
        secondaryContainer = p.secondaryContainer, onSecondaryContainer = p.onSecondaryContainer,
        tertiary = p.tertiary, onTertiary = p.onTertiary,
        background = p.background, surface = p.surface, surfaceVariant = p.surfaceVariant,
        error = p.error, outline = p.outline
    )
}
