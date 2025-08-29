package com.example.seedstockkeeper6.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 新しいテーマ用のKey Colorテンプレート
 * 
 * 使用方法:
 * 1. このファイルをコピーして新しいテーマ名に変更
 * 2. Key Colorの値を変更
 * 3. Palettes.ktのflavorColorSchemeに新しいケースを追加
 * 4. ThemeFlavor enumに新しい値を追加
 * 
 * 例: Oceanテーマを作成する場合
 * - ファイル名: OceanKeyColors.kt
 * - [ThemeName]をOceanに置換
 * - Key Colorを実際の色に変更
 */

/*
// ---------- [ThemeName] Key Colors ----------
object [ThemeName]KeyColors {
    // Primary Key Color - メインのブランドカラー
    val primary = Color(0xFF[HEX_CODE])
    
    // Secondary Key Color - セカンダリアクション用
    val secondary = Color(0xFF[HEX_CODE])
    
    // Tertiary Key Color - テルティアリアクション用
    val tertiary = Color(0xFF[HEX_CODE])
    
    // Neutral Key Color - テキストや表面色のベース
    val neutral = Color(0xFF[HEX_CODE])
    
    // Error Key Color - エラー状態用
    val error = Color(0xFF[HEX_CODE])
}
*/

/**
 * Palettes.ktのflavorColorSchemeに追加するケース:
 * 
 * ThemeFlavor.[ThemeName] -> {
 *     val keyColors = KeyColors(
 *         primary = [ThemeName]KeyColors.primary,
 *         secondary = [ThemeName]KeyColors.secondary,
 *         tertiary = [ThemeName]KeyColors.tertiary,
 *         neutral = [ThemeName]KeyColors.neutral,
 *         error = [ThemeName]KeyColors.error
 *     )
 *     generateColorScheme(keyColors, dark)
 * }
 * 
 * ThemeFlavor enumに追加する値:
 * 
 * enum class ThemeFlavor { ..., [ThemeName] }
 * 
 * 実装例（Oceanテーマ）:
 * 
 * // OceanKeyColors.kt
 * object OceanKeyColors {
 *     val primary = Color(0xFF00796B)      // ティール
 *     val secondary = Color(0xFF455A64)    // ブルーグレー
 *     val tertiary = Color(0xFF1976D2)     // ブルー
 *     val neutral = Color(0xFF1C1B1F)      // ダークグレー
 *     val error = Color(0xFFB00020)        // レッド
 * }
 * 
 * // Palettes.kt
 * ThemeFlavor.Ocean -> {
 *     val keyColors = KeyColors(
 *         primary = OceanKeyColors.primary,
 *         secondary = OceanKeyColors.secondary,
 *         tertiary = OceanKeyColors.tertiary,
 *         neutral = OceanKeyColors.neutral,
 *         error = OceanKeyColors.error
 *     )
 *     generateColorScheme(keyColors, dark)
 * }
 * 
 * // ThemeFlavor enum
 * enum class ThemeFlavor { ..., Ocean }
 * 
 * Key Colorの選び方:
 * 
 * 1. Primary: ブランドのメインカラー（ボタン、リンクなど）
 * 2. Secondary: セカンダリアクション（補助的なアクション）
 * 3. Tertiary: アクセントカラー（強調したい要素）
 * 4. Neutral: テキストや表面色のベース（グレー系）
 * 5. Error: エラー状態を示す色（赤系）
 * 
 * 色の組み合わせのコツ:
 * - PrimaryとSecondaryは補色関係にあると良い
 * - TertiaryはPrimaryやSecondaryと調和する色
 * - Neutralは読みやすさを重視したグレー系
 * - Errorは目立つ赤系で統一
 */
