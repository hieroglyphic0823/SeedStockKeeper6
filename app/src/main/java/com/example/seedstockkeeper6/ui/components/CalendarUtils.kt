package com.example.seedstockkeeper6.ui.components

import androidx.compose.ui.graphics.Color

/**
 * カレンダー表示用のユーティリティ関数
 */

/**
 * ラベルに基づいて色を解決する関数
 */
fun resolveLabelColor(label: String): Color {
    return when {
        "冷" in label -> Color(0xFF80DEEA)
        "寒" in label -> Color(0xFF1565C0)
        "涼" in label -> Color(0xFF039BE5)
        "中" in label -> Color(0xFF388E3C)
        "温" in label -> Color(0xFFFB8C00)
        "暖" in label -> Color(0xFFD32F2F)
        else -> Color.Gray
    }
}
