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

enum class ThemeFlavor { Onion, OnionLightColors, Renkon, Forest, SweetPotato, SweetP, M3CB3, Saien }

// --- パレット定義 ---

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

val Renkon = Palette(
    // メインのブランドカラー（TopAppBar、BottomNavigationBar）
    primary = Color(0xFF4F2702), onPrimary = Color(0xFFF7F4EA),
    // プライマリアクションのコンテナ（ボタン）
    primaryContainer = Color(0xFFEDE5DC), onPrimaryContainer = Color(0xFF333333),
    // セカンダリアクション（蓮根の節の色）
    secondary = Color(0xFF5D3A1A), onSecondary = Color(0xFFFFFBFE),
    // セカンダリアクションのコンテナ（カードの色）
    secondaryContainer = Color(0xFFD2A36C), onSecondaryContainer = Color(0xFF333333),
    // テルティアリアクション（蓮根のアクセント色）
    tertiary = Color(0xFFFFAF3C), onTertiary = Color(0xFFFFFBFE),
    // 背景色と表面色
    background = Color(0xFFFFFBFE), surface = Color(0xFFD2A36C), surfaceVariant = Color(0xFFF8F4F0),
    // エラー色とアウトライン
    error = Color(0xFFDB2B39), outline = Color(0xFFF7F4EA),
    // 表面のテキスト色
    onSurface = Color(0xFF333333)
)

val Forest = Palette(
    // メインのブランドカラー（深い森の緑）
    primary = Color(0xFF2E5A1A), onPrimary = Color(0xFFFFFFFF),
    // プライマリアクションのコンテナ（明るい森の緑）
    primaryContainer = Color(0xFFB8E6A3), onPrimaryContainer = Color(0xFF0F1F0A),
    // セカンダリアクション（茶色の木の幹）
    secondary = Color(0xFF8B4513), onSecondary = Color(0xFFFFFFFF),
    // セカンダリアクションのコンテナ（明るい茶色）
    secondaryContainer = Color(0xFFE6D3C0), onSecondaryContainer = Color(0xFF2D1A0A),
    // テルティアリアクション（苔の緑）
    tertiary = Color(0xFF6B8E23), onTertiary = Color(0xFFFFFFFF),
    // 背景色と表面色（自然な白）
    background = Color(0xFFF8F9F5), surface = Color(0xFFFFFFFF), surfaceVariant = Color(0xFFE8F0E0),
    // エラー色とアウトライン
    error = Color(0xFFBA1A1A), outline = Color(0xFF7A8C6A),
    // 表面のテキスト色
    onSurface = Color(0xFF1C1B1F)
)

private val Palettes = mapOf(
    ThemeFlavor.Onion to Onion,
    ThemeFlavor.Renkon to Renkon,
    ThemeFlavor.Forest to Forest
)

// Palette → Material3 ColorScheme へ
@Composable
fun flavorColorScheme(flavor: ThemeFlavor, dark: Boolean): ColorScheme {
    return when (flavor) {
        ThemeFlavor.Renkon -> {
            if (!dark) {
                renkonLightScheme
            } else {
                renkonDarkScheme
            }
        }
        ThemeFlavor.Onion -> {
            if (!dark) {
                onionLightScheme
            } else {
                onionDarkScheme
            }
        }
        ThemeFlavor.OnionLightColors -> {
            if (!dark) {
                onionLightScheme
            } else {
                onionDarkScheme
            }
        }
        ThemeFlavor.Forest -> {
            val keyColors = KeyColors(
                primary = ForestKeyColors.primary,
                secondary = ForestKeyColors.secondary,
                tertiary = ForestKeyColors.tertiary,
                neutral = ForestKeyColors.neutral,
                error = ForestKeyColors.error
            )
            generateColorScheme(keyColors, dark)
        }
        ThemeFlavor.SweetPotato -> {
            if (!dark) {
                sweetPotatoLightScheme
            } else {
                sweetPotatoDarkScheme
            }
        }
        ThemeFlavor.SweetP -> {
            if (!dark) {
                sweetPLightScheme
            } else {
                sweetPDarkScheme
            }
        }
        ThemeFlavor.M3CB3 -> {
            if (!dark) {
                m3cb3LightScheme
            } else {
                m3cb3DarkScheme
            }
        }
        ThemeFlavor.Saien -> {
            if (!dark) {
                saienLightScheme
            } else {
                saienDarkScheme
            }
        }
    }
}
