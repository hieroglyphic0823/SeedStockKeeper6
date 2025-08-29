package com.example.seedstockkeeper6.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 新しいテーマを作成するためのテンプレート
 * 
 * 使用方法:
 * 1. このファイルをコピーして新しいテーマ名に変更
 * 2. 色の値を変更
 * 3. Theme.ktにcreate[ThemeName]Theme関数を追加
 * 4. Palettes.ktのflavorColorSchemeに新しいケースを追加
 * 5. ThemeFlavor enumに新しい値を追加
 * 
 * 例: Oceanテーマを作成する場合
 * - ファイル名: OceanColors.kt
 * - [ThemeName]をOceanに置換
 * - [HEX_CODE]を実際の色コードに置換
 */

/*
// ---------- [ThemeName] Light Theme ----------
val primary[ThemeName]Light = Color(0xFF[HEX_CODE]) // プライマリ色の説明
val onPrimary[ThemeName]Light = Color(0xFFFFFFFF)
val primaryContainer[ThemeName]Light = Color(0xFF[HEX_CODE]) // プライマリコンテナ色の説明
val onPrimaryContainer[ThemeName]Light = Color(0xFF[HEX_CODE])

val secondary[ThemeName]Light = Color(0xFF[HEX_CODE]) // セカンダリ色の説明
val onSecondary[ThemeName]Light = Color(0xFFFFFFFF)
val secondaryContainer[ThemeName]Light = Color(0xFF[HEX_CODE]) // セカンダリコンテナ色の説明
val onSecondaryContainer[ThemeName]Light = Color(0xFF[HEX_CODE])

val tertiary[ThemeName]Light = Color(0xFF[HEX_CODE]) // テルティアリ色の説明
val onTertiary[ThemeName]Light = Color(0xFFFFFFFF)
val tertiaryContainer[ThemeName]Light = Color(0xFF[HEX_CODE]) // テルティアリコンテナ色の説明
val onTertiaryContainer[ThemeName]Light = Color(0xFF[HEX_CODE])

val error[ThemeName]Light = Color(0xFFBA1A1A)
val onError[ThemeName]Light = Color(0xFFFFFFFF)
val errorContainer[ThemeName]Light = Color(0xFFFFDAD6)
val onErrorContainer[ThemeName]Light = Color(0xFF410002)

val background[ThemeName]Light = Color(0xFF[HEX_CODE]) // 背景色の説明
val onBackground[ThemeName]Light = Color(0xFF1C1B1F)
val surface[ThemeName]Light = Color(0xFFFFFFFF)
val onSurface[ThemeName]Light = Color(0xFF1C1B1F)
val surfaceVariant[ThemeName]Light = Color(0xFF[HEX_CODE]) // 表面バリアント色の説明
val onSurfaceVariant[ThemeName]Light = Color(0xFF4C4732)

val outline[ThemeName]Light = Color(0xFF[HEX_CODE])
val outlineVariant[ThemeName]Light = Color(0xFF[HEX_CODE])
val scrim[ThemeName]Light = Color(0xFF000000)

val inverseSurface[ThemeName]Light = Color(0xFF333024)
val inverseOnSurface[ThemeName]Light = Color(0xFFF8F0DD)
val inversePrimary[ThemeName]Light = Color(0xFF[HEX_CODE])

val surfaceDim[ThemeName]Light = Color(0xFFE0DAC7)
val surfaceBright[ThemeName]Light = Color(0xFFFFFFFF)
val surfaceContainerLowest[ThemeName]Light = Color(0xFFFFFFFF)
val surfaceContainerLow[ThemeName]Light = Color(0xFFFAF3E0)
val surfaceContainer[ThemeName]Light = Color(0xFFF5EDDA)
val surfaceContainerHigh[ThemeName]Light = Color(0xFFEFE8D5)
val surfaceContainerHighest[ThemeName]Light = Color(0xFFE9E2CF)

// ---------- [ThemeName] Dark Theme ----------
val primary[ThemeName]Dark = Color(0xFF[HEX_CODE]) // ダークテーマ用プライマリ色
val onPrimary[ThemeName]Dark = Color(0xFF[HEX_CODE])
val primaryContainer[ThemeName]Dark = Color(0xFF[HEX_CODE])
val onPrimaryContainer[ThemeName]Dark = Color(0xFF[HEX_CODE])

val secondary[ThemeName]Dark = Color(0xFF[HEX_CODE]) // ダークテーマ用セカンダリ色
val onSecondary[ThemeName]Dark = Color(0xFF[HEX_CODE])
val secondaryContainer[ThemeName]Dark = Color(0xFF[HEX_CODE])
val onSecondaryContainer[ThemeName]Dark = Color(0xFF[HEX_CODE])

val tertiary[ThemeName]Dark = Color(0xFF[HEX_CODE]) // ダークテーマ用テルティアリ色
val onTertiary[ThemeName]Dark = Color(0xFF[HEX_CODE])
val tertiaryContainer[ThemeName]Dark = Color(0xFF[HEX_CODE])
val onTertiaryContainer[ThemeName]Dark = Color(0xFF[HEX_CODE])

val error[ThemeName]Dark = Color(0xFFFFB4AB)
val onError[ThemeName]Dark = Color(0xFF690005)
val errorContainer[ThemeName]Dark = Color(0xFF93000A)
val onErrorContainer[ThemeName]Dark = Color(0xFFFFDAD6)

val background[ThemeName]Dark = Color(0xFF1C1B1F)
val onBackground[ThemeName]Dark = Color(0xFFE9E2CF)
val surface[ThemeName]Dark = Color(0xFF2D2A1E)
val onSurface[ThemeName]Dark = Color(0xFFE9E2CF)
val surfaceVariant[ThemeName]Dark = Color(0xFF4C4732)
val onSurfaceVariant[ThemeName]Dark = Color(0xFFD8D1BF)

val outline[ThemeName]Dark = Color(0xFF[HEX_CODE])
val outlineVariant[ThemeName]Dark = Color(0xFF4C4732)
val scrim[ThemeName]Dark = Color(0xFF000000)

val inverseSurface[ThemeName]Dark = Color(0xFFE9E2CF)
val inverseOnSurface[ThemeName]Dark = Color(0xFF2D2A1E)
val inversePrimary[ThemeName]Dark = Color(0xFF[HEX_CODE])

val surfaceDim[ThemeName]Dark = Color(0xFF161308)
val surfaceBright[ThemeName]Dark = Color(0xFF2A2820)
val surfaceContainerLowest[ThemeName]Dark = Color(0xFF0A0A07)
val surfaceContainerLow[ThemeName]Dark = Color(0xFF1E1C10)
val surfaceContainer[ThemeName]Dark = Color(0xFF222014)
val surfaceContainerHigh[ThemeName]Dark = Color(0xFF2D2A1E)
val surfaceContainerHighest[ThemeName]Dark = Color(0xFF383528)
*/

/**
 * Theme.ktに追加するファクトリー関数のテンプレート:
 * 
 * @Composable
 * fun create[ThemeName]Theme(darkTheme: Boolean): ColorScheme {
 *     return if (darkTheme) [themeName]DarkScheme else [themeName]LightScheme
 * }
 * 
 * Palettes.ktのflavorColorSchemeに追加するケース:
 * 
 * ThemeFlavor.[ThemeName] -> {
 *     create[ThemeName]Theme(dark)
 * }
 * 
 * ThemeFlavor enumに追加する値:
 * 
 * enum class ThemeFlavor { ..., [ThemeName] }
 * 
 * 実装例（Oceanテーマ）:
 * 
 * // OceanColors.kt
 * val primaryOceanLight = Color(0xFF00796B)
 * val onPrimaryOceanLight = Color(0xFFFFFFFF)
 * // ... 他の色定義
 * 
 * // Theme.kt
 * @Composable
 * fun createOceanTheme(darkTheme: Boolean): ColorScheme {
 *     return if (darkTheme) oceanDarkScheme else oceanLightScheme
 * }
 * 
 * // Palettes.kt
 * ThemeFlavor.Ocean -> {
 *     createOceanTheme(dark)
 * }
 * 
 * // ThemeFlavor enum
 * enum class ThemeFlavor { ..., Ocean }
 */
