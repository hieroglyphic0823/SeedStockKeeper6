package com.example.seedstockkeeper6.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seedstockkeeper6.model.CalendarEntry
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.geometry.Size

data class MonthRange(
    val start: Int, // 月 (1-12)
    val end: Int,   // 月 (1-12)
    val startStage: String? = null, // 例: "上旬", "中旬", "下旬", または null
    val endStage: String? = null     // 例: "上旬", "中旬", "下旬", または null
)
enum class BandStyle { Dotted, Solid }

//data class CalendarBand(
//    val label: String,
//    val labelColor: Color,
//    val style: BandStyle,
//    val ranges: List<MonthRange>
//)
// 元の CalendarBand は RangeItem に名前変更するか、新しい構造に含める
data class RangeItem(
    val ranges: List<MonthRange>,
    val style: BandStyle,
    val color: Color, // 個別の色を持たせる
    val itemLabel: String // "播種", "収穫" など、必要であれば
)

data class GroupedCalendarBand(
    val groupLabel: String, // 例: "リージョンA"
    val labelColor: Color,  // グループのラベル色 (現在のband.labelColorに相当)
    val items: List<RangeItem>
)

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
    regionColors: Map<String, Color>,
    modifier: Modifier = Modifier.fillMaxWidth(),
    heightDp: Int = 160
) {
    val groupedBands = entries
        .groupBy { it.region } // まず region でグループ化
        .map { (region, regionEntries) ->
            val sowingItems = regionEntries.mapNotNull { entry ->
                // 播種期間が有効な場合のみ RangeItem を作成
                if (entry.sowing_start != 0 && entry.sowing_end != 0) { // または適切な有効期間チェック
                    RangeItem(
                        ranges = listOf(
                            MonthRange(
                                entry.sowing_start,
                                entry.sowing_end,
                                entry.sowing_start_stage,
                                entry.sowing_end_stage
                            )
                        ),
                        style = BandStyle.Dotted,
                        color = regionColors[region]?.let { adjustBrightness(it, 1.2f) }
                            ?: Color.Green, // 色を少し明るくするなど調整
                        itemLabel = "播種"
                    )
                } else null
            }
            val harvestItems = regionEntries.mapNotNull { entry ->
                // 収穫期間が有効な場合のみ RangeItem を作成
                if (entry.harvest_start != 0 && entry.harvest_end != 0) { // または適切な有効期間チェック
                    RangeItem(
                        ranges = listOf(
                            MonthRange(
                                entry.harvest_start,
                                entry.harvest_end,
                                entry.harvest_start_stage,
                                entry.harvest_end_stage
                            )
                        ),
                        style = BandStyle.Solid,
                        color = regionColors[region]?.let { adjustBrightness(it, 0.8f) }
                            ?: Color.Red, // 色を少し暗くするなど調整
                        itemLabel = "収穫"
                    )
                } else null
            }

            GroupedCalendarBand(
                groupLabel = region, // ラベルはリージョン名
                labelColor = regionColors[region] ?: Color.Gray, // グループのラベル色
                items = sowingItems + harvestItems // 播種と収穫のアイテムを結合
            )
        }
        .filter { it.items.isNotEmpty() } // 描画アイテムがないグループは除外

    // SeedCalendar を新しいデータ構造で呼び出すように修正が必要
    SeedCalendarGrouped(bands = groupedBands, modifier = modifier, heightDp = heightDp)
}

// 色の明るさを調整するヘルパー関数（例）
fun adjustBrightness(color: Color, factor: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt(),
        hsv
    )
    hsv[2] = (hsv[2] * factor).coerceIn(0f, 1f)
    val newColorInt = android.graphics.Color.HSVToColor(hsv)
    return Color(newColorInt)
}

@Composable
fun SeedCalendarGrouped( // 名前を変更
    bands: List<GroupedCalendarBand>, // GroupedCalendarBand を受け取る
    modifier: Modifier = Modifier.fillMaxWidth(),
    heightDp: Int = 140
) {
    val monthLabels = (1..12).map { it.toString() }
    val dash = remember { PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f) }
    val labelColWidthDp = 64.dp
    val headerHeightDp = 22.dp
    val gridStroke = 1f
    val textPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.DKGRAY
            textAlign = Paint.Align.CENTER
        }
    }

    fun getStageOffset(stage: String?): Float {
        return when (stage) {
            "上旬" -> 0.15f
            "中旬" -> 0.5f
            "下旬" -> 0.85f
            else -> 0.5f // デフォルトは中央
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
    ) {
        textPaint.textSize = 12.sp.toPx()

        val labelW = labelColWidthDp.toPx()
        val headerH = headerHeightDp.toPx()
        // ... (他の計算は同様) ...
        val gridLeft = labelW
        val gridTop = headerH
        val gridRight = size.width
        val gridBottom = size.height
        val gridW = gridRight - gridLeft
        val gridH = gridBottom - gridTop

        val rowCount = max(1, bands.size) // 行数は GroupedCalendarBand の数
        val rowH = gridH / rowCount
        val colW = gridW / 12f

        for (m in 0 until 12) {
            val xCenter = gridLeft + colW * (m + 0.5f)
            drawLine(
                color = Color(0x22000000),
                start = Offset(gridLeft + colW * m, gridTop),
                end = Offset(gridLeft + colW * m, gridBottom),
                strokeWidth = gridStroke
            )
            drawContext.canvas.nativeCanvas.drawText(
                monthLabels[m],
                xCenter,
                headerH - 4.dp.toPx(),
                textPaint
            )
        }

        drawLine(
            color = Color(0x22000000),
            start = Offset(gridRight, gridTop),
            end = Offset(gridRight, gridBottom),
            strokeWidth = gridStroke
        )


        bands.forEachIndexed { row, groupedBand -> // 各 GroupedCalendarBand が1行に対応
            val top = gridTop + rowH * row
            val centerY = top + rowH / 2f

            // グループラベル背景とテキスト
            drawRect(
                color = groupedBand.labelColor.copy(alpha = 0.15f),
                topLeft = Offset(0f, top),
                size = Size(labelW, rowH)
            )
            drawContext.canvas.nativeCanvas.drawText(
                groupedBand.groupLabel,
                labelW * 0.5f,
                centerY + (textPaint.textSize / 3f),
                textPaint.apply { color = android.graphics.Color.DKGRAY }
            )

            // 各アイテム (播種、収穫など) を同じ centerY を使って描画
            groupedBand.items.forEach { item ->
                item.ranges.forEach { r ->
                    val startX = gridLeft + colW * (r.start - 1 + getStageOffset(r.startStage))
                    val endX = gridLeft + colW * (r.end - 1 + getStageOffset(r.endStage))

                    when (item.style) {
                        BandStyle.Dotted -> {
                            drawLine(
                                color = item.color, // アイテムごとの色を使用
                                start = Offset(startX, centerY),
                                end = Offset(endX, centerY),
                                strokeWidth = 6f, // 細めに
                                pathEffect = dash
                            )
                            drawCircle(item.color, radius = 6f, center = Offset(startX, centerY))
                            drawCircle(item.color, radius = 6f, center = Offset(endX, centerY))
                        }

                        BandStyle.Solid -> {
                            drawLine(
                                color = item.color, // アイテムごとの色を使用
                                start = Offset(startX, centerY),
                                end = Offset(endX, centerY),
                                strokeWidth = 14f, // 太めに
                                cap = Stroke.DefaultCap
                            )
                        }
                    }
                }
            }

            // 行の区切り線
            drawLine(
                color = Color(0x22000000),
                start = Offset(gridLeft, top + rowH),
                end = Offset(gridRight, top + rowH),
                strokeWidth = gridStroke
            )
        }
    }
}
