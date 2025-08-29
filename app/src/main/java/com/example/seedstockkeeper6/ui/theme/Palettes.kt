package com.example.seedstockkeeper6.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.seedstockkeeper6.ui.theme.*

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

enum class ThemeFlavor { Onion, OnionLightColors, Renkon, Forest }

// --- 8パターン ---


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
                lightColorScheme(
                    primary = Color(0xFF4F2702),       // メインのブランドカラー（TopAppBar、BottomNavigationBar）
                    onPrimary = Color(0xFFF7F4EA),     // プライマリ上のテキスト色
                    primaryContainer = Color(0xFFEDE5DC), // プライマリアクションのコンテナ（アクション付きカード、ボタン）
                    onPrimaryContainer = Color(0xFF333333), // プライマリコンテナ上のテキスト色

                    secondary = Color(0xFF5D3A1A),     // セカンダリアクション（蓮根の節の色）
                    onSecondary = Color(0xFFFFFBFE),
                    secondaryContainer = Color(0xFFD2A36C), // セカンダリアクションのコンテナ（カードの色）
                    onSecondaryContainer = Color(0xFF333333),

                    tertiary = Color(0xFFC67A4A),      // テルティアリアクション（蓮根のアクセント色）
                    onTertiary = Color(0xFFFFFBFE),

                    background = Color(0xFFFFFBFE),    // 背景色
                    onBackground = Color(0xFF333333),

                    surface = Color(0xFFD2A36C),       // 表面色（Cardの色）
                    onSurface = Color(0xFF333333),

                    surfaceVariant = Color(0xFFF8F4F0), // 表面バリアント
                    onSurfaceVariant = Color(0xFF333333),

                    error = Color(0xFFBA1A1A),
                    onError = Color(0xFFFFFBFE),

                    outline = Color(0xFFFBF6F0)
                )
            } else {
                darkColorScheme()
            }
        }
        ThemeFlavor.Onion -> {
            if (!dark) {
                lightColorScheme(
                    primary = Color(0xFFA1887F),       // 外皮ブラウン
                    onPrimary = Color(0xFFFFFFFF),     // 白文字
                    primaryContainer = Color(0xFFD7CCC8), // 外皮ベージュ
                    onPrimaryContainer = Color(0xFF3E2723),

                    secondary = Color(0xFFA5D6A7),     // 芽のグリーン
                    onSecondary = Color(0xFF1B5E20),
                    secondaryContainer = Color(0xFFC8E6C9),
                    onSecondaryContainer = Color(0xFF2E7D32),

                    background = Color(0xFFFAFAFA),    // 内側ホワイト
                    onBackground = Color(0xFF3E2723),

                    surface = Color(0xFFF5F5F5),       // 優しい白
                    onSurface = Color(0xFF3E2723),

                    error = Color(0xFFB00020),
                    onError = Color(0xFFFFFFFF)
                )
            } else {
                darkColorScheme()
            }
        }
        ThemeFlavor.OnionLightColors -> {
            if (!dark) {
                lightColorScheme(
                    primary = Color(0xFFA1887F),       // 外皮ブラウン
                    onPrimary = Color(0xFFFFFFFF),     // 白文字
                    primaryContainer = Color(0xFFD7CCC8), // 外皮ベージュ
                    onPrimaryContainer = Color(0xFF3E2723),

                    secondary = Color(0xFFA5D6A7),     // 芽のグリーン
                    onSecondary = Color(0xFF1B5E20),
                    secondaryContainer = Color(0xFFC8E6C9),
                    onSecondaryContainer = Color(0xFF2E7D32),

                    background = Color(0xFFFAFAFA),    // 内側ホワイト
                    onBackground = Color(0xFF3E2723),

                    surface = Color(0xFFF5F5F5),       // 優しい白
                    onSurface = Color(0xFF3E2723),

                    error = Color(0xFFB00020),
                    onError = Color(0xFFFFFFFF)
                )
            } else {
                darkColorScheme()
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

        ThemeFlavor.Renkon -> {
            val keyColors = KeyColors(
                primary = RenkonKeyColors.primary,
                secondary = RenkonKeyColors.secondary,
                tertiary = RenkonKeyColors.tertiary,
                neutral = RenkonKeyColors.neutral,
                error = RenkonKeyColors.error
            )
            generateColorScheme(keyColors, dark)
        }
        else -> {
            val p = Palettes.getValue(flavor)
            val base = if (!dark) lightColorScheme() else darkColorScheme()
            base.copy(
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
    }
}
