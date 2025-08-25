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
    val error: Color, val outline: Color,
    val onSurface: Color
)

enum class ThemeFlavor { Vitamin, Soil, Herb, Ocean, Plum, Sakura, WB, Onion }

// --- 8パターン ---
val Vitamin = Palette(
    primary = Color(0xFFFF8A00), onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9A6), onPrimaryContainer = Color(0xFF422300),
    secondary = Color(0xFF43A047), onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7E6C9), onSecondaryContainer = Color(0xFF0E2B13),
    tertiary = Color(0xFF00ACC1), onTertiary = Color.White,
    background = Color(0xFFFFFBF7), surface = Color(0xFFFFFBF7), surfaceVariant = Color(0xFFF2E7DC),
    error = Color(0xFFB00020), outline = Color(0xFF8E8E8E),
    onSurface = Color(0xFF1C1B1F)
)

val Soil = Palette(
    primary = Color(0xFF795548), onPrimary = Color.White,
    primaryContainer = Color(0xFFD7C2B8), onPrimaryContainer = Color(0xFF2E1E18),
    secondary = Color(0xFF8D6E63), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEBD6CF), onSecondaryContainer = Color(0xFF2F2320),
    tertiary = Color(0xFFA1887F), onTertiary = Color(0xFF1E1512),
    background = Color(0xFFFCFAF7), surface = Color(0xFFFCFAF7), surfaceVariant = Color(0xFFEDE0D8),
    error = Color(0xFFB00020), outline = Color(0xFF8B7E76),
    onSurface = Color(0xFF1C1B1F)
)

val Herb = Palette(
    primary = Color(0xFF2E7D32), onPrimary = Color.White,
    primaryContainer = Color(0xFFBDE5BF), onPrimaryContainer = Color(0xFF0A2A0C),
    secondary = Color(0xFF43A047), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2F1D4), onSecondaryContainer = Color(0xFF09240C),
    tertiary = Color(0xFF00897B), onTertiary = Color.White,
    background = Color(0xFFF7FBF8), surface = Color(0xFFF7FBF8), surfaceVariant = Color(0xFFE0E9E2),
    error = Color(0xFFB00020), outline = Color(0xFF7A8A7E),
    onSurface = Color(0xFF1C1B1F)
)

val Ocean = Palette(
    primary = Color(0xFF00796B), onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB), onPrimaryContainer = Color(0xFF062825),
    secondary = Color(0xFF455A64), onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFE3EA), onSecondaryContainer = Color(0xFF0E2127),
    tertiary = Color(0xFF1976D2), onTertiary = Color.White,
    background = Color(0xFFF7FAFA), surface = Color(0xFFF7FAFA), surfaceVariant = Color(0xFFE1ECEA),
    error = Color(0xFFB00020), outline = Color(0xFF7B8D8B),
    onSurface = Color(0xFF1C1B1F)
)

val Plum = Palette(
    primary = Color(0xFF7B1FA2), onPrimary = Color.White,
    primaryContainer = Color(0xFFE1BEE7), onPrimaryContainer = Color(0xFF290833),
    secondary = Color(0xFF5E35B1), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1C4E9), onSecondaryContainer = Color(0xFF21173A),
    tertiary = Color(0xFF00ACC1), onTertiary = Color.White,
    background = Color(0xFFFBF7FD), surface = Color(0xFFFBF7FD), surfaceVariant = Color(0xFFECE3F3),
    error = Color(0xFFB00020), outline = Color(0xFF8B7D92),
    onSurface = Color(0xFF1C1B1F)
)

val Sakura = Palette(
    primary = Color(0xFFD81B60), onPrimary = Color.White,
    primaryContainer = Color(0xFFF8BBD0), onPrimaryContainer = Color(0xFF4A0C22),
    secondary = Color(0xFF43A047), onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9), onSecondaryContainer = Color(0xFF0F2913),
    tertiary = Color(0xFF6D4C41), onTertiary = Color.White,
    background = Color(0xFFFEFBFE), surface = Color(0xFFFEFBFE), surfaceVariant = Color(0xFFEFE2EA),
    error = Color(0xFFB00020), outline = Color(0xFF8C7A83),
    onSurface = Color(0xFF1C1B1F)
)

val WB = Palette(
    primary = Color(0xFF897056), onPrimary = Color.White,
    primaryContainer = Color(0xFF8F6C6C), onPrimaryContainer = Color(0xFFFFFBFF),
    secondary = Color(0xFF594B56), onSecondary = Color.White,
    secondaryContainer = Color(0xFF72636E), onSecondaryContainer = Color(0xFFF5E1EE),
    tertiary = Color(0xFF6F583F), onTertiary = Color.White,
    background = Color(0xFFFFF8F7), surface = Color(0xFFF4ECEB), surfaceVariant = Color(0xFFF0DEDE),
    error = Color(0xFFBA1A1A), outline = Color(0xFF827473),
    onSurface = Color(0xFF1C1B1F)
)

val Onion = Palette(
            // 上と下のバー（統一された茶色）
        primary = Color(0xFF884D00), onPrimary = Color.White,  // 上と下のバーの色（統一された茶色）
            // カードのアイコン背景とバッジ（統一された茶色）
        primaryContainer = Color(0xFF884D00),
    onPrimaryContainer = Color(0xFFF0E8D8),  // カードの文字色
    // 玉ねぎの根元部分（自然な玉ねぎ色）
    secondary = Color(0xFFE8A85A), onSecondary = Color.White,  // 玉ねぎの根元の自然な色
            // カードの背景（指定された黄色）
        secondaryContainer = Color(0xFFFEF7B2),  // カードの色（白っぽい黄色）
    onSecondaryContainer = Color(0xFF3D2A1A),  // カードの文字色（土のようなこげ茶）
    // 玉ねぎの茎部分（自然な玉ねぎ色）
    tertiary = Color(0xFFD47A4A), onTertiary = Color.White,  // 玉ねぎの茎の自然な色
            // 玉ねぎの果肉（自然な玉ねぎ色のベージュ）
        background = Color(0xFFFFF9E6), surface = Color(0xFFFFF9E6), surfaceVariant = Color(0xFFFFF9E6),  // 薄い卵色（彩度を上げた）
    // エラー色（赤）とアウトライン（自然なグレー）
    error = Color(0xFFBA1A1A), outline = Color(0xFFB0A898),  // アウトラインは自然なグレー
    // 玉ねぎの皮の自然な部分（自然な茶色）
    onSurface = Color(0xFF5A3F2A)  // 玉ねぎの外皮の自然な茶色
)

private val Palettes = mapOf(
    ThemeFlavor.Vitamin to Vitamin,
    ThemeFlavor.Soil to Soil,
    ThemeFlavor.Herb to Herb,
    ThemeFlavor.Ocean to Ocean,
    ThemeFlavor.Plum to Plum,
    ThemeFlavor.Sakura to Sakura,
    ThemeFlavor.WB to WB,
    ThemeFlavor.Onion to Onion
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
        error = p.error, outline = p.outline,
        onSurface = p.onSurface
    )
}
