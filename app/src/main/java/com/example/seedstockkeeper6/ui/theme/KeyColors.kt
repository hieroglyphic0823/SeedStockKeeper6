package com.example.seedstockkeeper6.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Material3 Key Colorシステム
 * 5つのKey Colorを定義し、派生色を自動生成する
 */

// ---------- Forest Theme Key Colors ----------
object ForestKeyColors {
    // Primary Key Color - 深い森の緑
    val primary = Color(0xFF2E5A1A)
    
    // Secondary Key Color - 茶色の木の幹
    val secondary = Color(0xFF8B4513)
    
    // Tertiary Key Color - 苔の緑
    val tertiary = Color(0xFF6B8E23)
    
    // Neutral Key Color - テキストや表面色のベース
    val neutral = Color(0xFF1C1B1F)
    
    // Error Key Color - エラー状態用
    val error = Color(0xFFBA1A1A)
}

// ---------- Renkon Theme Key Colors ----------
object RenkonKeyColors {
    val primary = Color(0xFF4F2702)      // ダークブラウン
    val secondary = Color(0xFF5D3A1A)    // ライトブラウン
    val tertiary = Color(0xFFC67A4A)     // オレンジブラウン
    val neutral = Color(0xFF333333)      // ダークグレー
    val error = Color(0xFFBA1A1A)        // レッド
}

// ---------- SweetPotato Theme Key Colors ----------
object SweetPotatoKeyColors {
    val primary = Color(0xFFffdd3d)      // さつまいも色（メイン）
    val secondary = Color(0xFF80725E)    // さつまいもの皮色
    val tertiary = Color(0xFFD2A36C)     // さつまいもの茎色
    val neutral = Color(0xFF211A13)      // ダークブラウン（テキスト用）
    val error = Color(0xFF93000A)        // エラー色
}
