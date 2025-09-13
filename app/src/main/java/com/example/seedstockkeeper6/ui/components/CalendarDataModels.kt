package com.example.seedstockkeeper6.ui.components

import androidx.compose.ui.graphics.Color

/**
 * カレンダー表示用のデータモデル
 */

/**
 * 期間の範囲を表すデータクラス
 */
data class MonthRange(
    val startDate: String,
    val endDate: String
)

/**
 * バンドのスタイルを表すenum
 */
enum class BandStyle {
    Solid,    // 実線
    Dotted    // 点線
}

/**
 * カレンダーの範囲アイテムを表すデータクラス
 */
data class RangeItem(
    val ranges: List<MonthRange>,
    val style: BandStyle,
    val color: Color,
    val itemLabel: String
)

/**
 * グループ化されたカレンダーバンドを表すデータクラス
 */
data class GroupedCalendarBand(
    val groupLabel: String,
    val expirationYear: Int,
    val expirationMonth: Int,
    val items: List<RangeItem>
)
