package com.example.seedstockkeeper6.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seedstockkeeper6.model.CalendarEntry
import kotlin.math.max
import kotlin.math.min

data class MonthRange(val start: Int, val end: Int) // 1..12
enum class BandStyle { Dotted, Solid }
data class CalendarBand(
    val label: String,
    val labelColor: Color,
    val style: BandStyle,
    val ranges: List<MonthRange>
)

/** "3-5,9,10-11" → listOf(3..5,9..9,10..11) */
fun parseMonthRanges(expr: String?): List<MonthRange> {
    if (expr.isNullOrBlank()) return emptyList()
    return expr.split(",")
        .mapNotNull { token ->
            val t = token.trim()
            if (t.isEmpty()) null
            else if (t.contains("-")) {
                val (s, e) = t.split("-").mapNotNull { it.trim().toIntOrNull() }
                val s1 = s.coerceIn(1, 12); val e1 = e.coerceIn(1, 12)
                MonthRange(min(s1, e1), max(s1, e1))
            } else {
                val m = t.toIntOrNull()?.coerceIn(1, 12)
                m?.let { MonthRange(it, it) }
            }
        }
}
@Composable
fun SeedCalendarFromEntries(
    entries: List<CalendarEntry>,
    regionColors: Map<String, Color>, // 地域ごとに色を変えたいときに使えます（例: regionColors["関東"]）
    modifier: Modifier = Modifier.fillMaxWidth(),
    heightDp: Int = 160
) {
    val bands = entries.map { entry ->
        CalendarBand(
            label = entry.region,
            labelColor = regionColors[entry.region] ?: Color.Gray,
            style = BandStyle.Dotted,
            ranges = listOf(
                MonthRange(entry.sowing_start, entry.sowing_end)
            )
        )
    }
    SeedCalendar(bands = bands, modifier = modifier, heightDp = heightDp)
}

@Composable
fun SeedCalendar(
    bands: List<CalendarBand>,
    modifier: Modifier = Modifier.fillMaxWidth(),
    heightDp: Int = 140
) {
    val monthLabels = (1..12).map { it.toString() }
    val dash = remember { PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f) }
    val labelColWidthDp = 64.dp
    val headerHeightDp = 22.dp
    val gridStroke = 1f
    // textPaint を Canvas の外側で remember する
    val textPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.DKGRAY
            textAlign = Paint.Align.CENTER
            // textSize は Canvas 内で toPx() を使って設定する方が良い場合もあるが、
            // ここで設定しても通常は問題ない。ただし、Density が必要な場合は注意。
            // textSize = 12.sp.toPx() // ← もしここで設定す
        }
    }
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            // .then(Modifier) // 冗長なので削除を検討
            .height(heightDp.dp)
    ) { // ここからは DrawScope
        // Canvas 内で Density を取得して textSize を設定
        textPaint.textSize = 12.sp.toPx() // DrawScope内では size や LocalDensity.current.density が使える

        val labelW = labelColWidthDp.toPx()
        val headerH = headerHeightDp.toPx()
        val gridLeft = labelW
        val gridTop = headerH
        val gridRight = size.width
        val gridBottom = size.height
        val gridW = gridRight - gridLeft
        val gridH = gridBottom - gridTop

        val rowCount = max(1, bands.size)
        val rowH = gridH / rowCount
        val colW = gridW / 12f

        // ヘッダ背景
        // ヘッダ（月）描画
        for (m in 0 until 12) {
            val xCenter = gridLeft + colW * (m + 0.5f)
            // 目盛り線
            drawLine(
                color = Color(0x22000000),
                start = androidx.compose.ui.geometry.Offset(gridLeft + colW * m, gridTop),
                end = androidx.compose.ui.geometry.Offset(gridLeft + colW * m, gridBottom),
                strokeWidth = gridStroke
            )
            // 月ラベル
            drawContext.canvas.nativeCanvas.drawText(
                monthLabels[m],
                xCenter,
                headerH - 4.dp.toPx(),
                textPaint
            )
        }
        // 右端の縦線
        drawLine(
            color = Color(0x22000000),
            start = androidx.compose.ui.geometry.Offset(gridRight, gridTop),
            end = androidx.compose.ui.geometry.Offset(gridRight, gridBottom),
            strokeWidth = gridStroke
        )

        // 各バンド描画
        bands.forEachIndexed { row, band ->
            val top = gridTop + rowH * row
            val centerY = top + rowH / 2f

            // ラベル背景
            drawRect(
                color = band.labelColor.copy(alpha = 0.15f),
                topLeft = androidx.compose.ui.geometry.Offset(0f, top),
                size = androidx.compose.ui.geometry.Size(labelW, rowH)
            )
            // ラベル文字
            drawContext.canvas.nativeCanvas.drawText(
                band.label,
                labelW * 0.5f,
                centerY + (textPaint.textSize / 3f),
                textPaint.apply {
                    color = android.graphics.Color.DKGRAY
                    textAlign = Paint.Align.CENTER
                }
            )

            // バー描画
            band.ranges.forEach { r ->
                val startX = gridLeft + colW * (r.start - 1 + 0.1f)
                val endX = gridLeft + colW * (r.end - 0.1f)

                when (band.style) {
                    BandStyle.Dotted -> {
                        // 黒の点線 + 両端に●
                        drawLine(
                            color = Color(0xFF333333),
                            start = androidx.compose.ui.geometry.Offset(startX, centerY),
                            end = androidx.compose.ui.geometry.Offset(endX, centerY),
                            strokeWidth = 6f,
                            pathEffect = dash
                        )
                        drawCircle(Color(0xFF333333), radius = 6f,
                            center = androidx.compose.ui.geometry.Offset(startX, centerY))
                        drawCircle(Color(0xFF333333), radius = 6f,
                            center = androidx.compose.ui.geometry.Offset(endX, centerY))
                    }
                    BandStyle.Solid -> {
                        // 赤い実線バー（少し太め）
                        drawLine(
                            color = Color(0xFFE53935),
                            start = androidx.compose.ui.geometry.Offset(startX, centerY),
                            end = androidx.compose.ui.geometry.Offset(endX, centerY),
                            strokeWidth = 14f,
                            cap = Stroke.DefaultCap
                        )
                    }
                }
            }

            // 各行の下線
            drawLine(
                color = Color(0x22000000),
                start = androidx.compose.ui.geometry.Offset(gridLeft, top + rowH),
                end = androidx.compose.ui.geometry.Offset(gridRight, top + rowH),
                strokeWidth = gridStroke
            )
        }
    }
}
fun buildBands(
    sowing: String? = null,
    nursery: String? = null,
    harvest: String? = null
): List<CalendarBand> {
    val list = mutableListOf<CalendarBand>()

    parseMonthRanges(sowing).takeIf { it.isNotEmpty() }?.let {
        list += CalendarBand(
            label = "播種",
            labelColor = Color(0xFF2E7D32),
            style = BandStyle.Dotted,
            ranges = it
        )
    }
    parseMonthRanges(nursery).takeIf { it.isNotEmpty() }?.let {
        list += CalendarBand(
            label = "育苗/定植",
            labelColor = Color(0xFF1976D2),
            style = BandStyle.Dotted,
            ranges = it
        )
    }
    parseMonthRanges(harvest).takeIf { it.isNotEmpty() }?.let {
        list += CalendarBand(
            label = "収穫",
            labelColor = Color(0xFFE53935),
            style = BandStyle.Solid,
            ranges = it
        )
    }

    return list
}
