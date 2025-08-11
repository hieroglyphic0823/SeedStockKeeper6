package com.example.seedstockkeeper6.ui.components

import android.content.res.Configuration
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.model.CalendarEntry
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.max
import kotlin.math.min


data class MonthRange(
    val start: Int, // 月 (1-12)
    val end: Int,   // 月 (1-12)
    val startStage: String? = null, // 例: "上旬", "中旬", "下旬", または null
    val endStage: String? = null     // 例: "上旬", "中旬", "下旬", または null
)
enum class BandStyle { Dotted, Solid }
data class RangeItem(
    val ranges: List<MonthRange>,
    val style: BandStyle,
    val color: Color, // 個別の色を持たせる
    val itemLabel: String // "播種", "収穫" など、必要であれば
)

// expirationYear と expirationMonth を含む構造に変更
data class GroupedCalendarBand(
    val groupLabel: String,
    val expirationYear: Int,
    val expirationMonth: Int,
    val items: List<RangeItem>
)

@Composable
fun SeedCalendarGrouped(
    bands: List<GroupedCalendarBand>,
    modifier: Modifier = Modifier.fillMaxWidth(),
    heightDp: Int = 140
) {
    val today = LocalDate.now()
    val currentMonth = today.monthValue
    val currentYear = today.year
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columnCount = if (isLandscape) 12 else 13

    val bodyMediumTextStyle = androidx.compose.material3.MaterialTheme.typography.bodyMedium
    val density = LocalDensity.current
    val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme

    val textPaint = remember(bodyMediumTextStyle, density, colorScheme.onPrimary) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorScheme.onPrimary.toArgb()
            textAlign = Paint.Align.CENTER
            textSize = with(density) { bodyMediumTextStyle.fontSize.toPx() }
        }
    }

    val dash = remember { PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
    ) {
        val labelColWidth = screenWidth / (columnCount + 1)
        val labelW = labelColWidth.toPx()
        val headerH = 22.dp.toPx()
        val gridLeft = labelW
        val gridTop = headerH
        val gridRight = size.width
        val gridBottom = size.height
        val gridW = gridRight - gridLeft
        val gridH = gridBottom - gridTop
        val colW = gridW / 12f
        val rowH = gridH / max(1, bands.size)

        for (m in 0 until 12) {
            val logicalMonth = ((currentMonth - 1 + m) % 12) + 1
            val x = gridLeft + colW * m
            drawRect(
                color = Color(colorScheme.surfaceVariant.toArgb()),
                topLeft = Offset(x, gridTop),
                size = Size(colW, gridH)
            )
            drawLine(
                color = Color(colorScheme.primary.toArgb()),
                start = Offset(x, gridTop),
                end = Offset(x, gridBottom),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText(
                logicalMonth.toString(),
                x + colW / 2,
                headerH - 4.dp.toPx(),
                textPaint
            )
        }

        drawLine(
            color = Color(0x22000000),
            start = Offset(gridRight, gridTop),
            end = Offset(gridRight, gridBottom),
            strokeWidth = 1f
        )

        fun getStageOffset(stage: String?): Float = when (stage) {
            "上旬" -> 0.15f
            "中旬" -> 0.5f
            "下旬" -> 0.85f
            else -> 0.5f
        }

        bands.forEachIndexed { row, groupedBand ->
            val top = gridTop + rowH * row
            val centerY = top + rowH / 2f
            val labelColor = resolveLabelColor(groupedBand.groupLabel)
            val labelText = groupedBand.groupLabel.replace("地", "")

            // 有効期限内背景 (年月判定)
            val now = YearMonth.of(currentYear, currentMonth)
            val expiration = YearMonth.of(groupedBand.expirationYear, groupedBand.expirationMonth)
            val monthsRemaining = (expiration.monthValue - currentMonth + (expiration.year - currentYear) * 12).coerceIn(0, 12)

            val bgStartX = gridLeft
            val bgEndX = gridLeft + colW * monthsRemaining
            drawRect(
                color = colorScheme.surfaceVariant.copy(alpha = 0.2f),
                topLeft = Offset(bgStartX, top),
                size = Size(bgEndX - bgStartX, rowH)
            )

            drawRect(
                color = labelColor.copy(alpha = 0.3f),
                topLeft = Offset(0f, top),
                size = Size(labelW, rowH)
            )
            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                labelW / 2,
                centerY + (textPaint.textSize / 3f),
                textPaint
            )

            groupedBand.items.forEach { item ->
                item.ranges.forEach { r ->
                    val startX = gridLeft + colW * ((r.start - currentMonth + 12) % 12 + getStageOffset(r.startStage))
                    val endX = gridLeft + colW * ((r.end - currentMonth + 12) % 12 + getStageOffset(r.endStage))

                    when (item.style) {
                        BandStyle.Dotted -> {
                            drawLine(
                                color = item.color,
                                start = Offset(startX, centerY),
                                end = Offset(endX, centerY),
                                strokeWidth = 6f,
                                pathEffect = dash
                            )
                            drawCircle(item.color, 6f, Offset(startX, centerY))
                            drawCircle(item.color, 6f, Offset(endX, centerY))
                        }

                        BandStyle.Solid -> {
                            drawLine(
                                color = item.color,
                                start = Offset(startX, centerY),
                                end = Offset(endX, centerY),
                                strokeWidth = 14f,
                                cap = Stroke.DefaultCap
                            )
                        }
                    }
                }
            }

            drawLine(
                color = Color(0x22000000),
                start = Offset(gridLeft, top + rowH),
                end = Offset(gridRight, top + rowH),
                strokeWidth = 1f
            )
        }
    }
}


// ラベル色をラベルの内容に応じて決定する関数
fun resolveLabelColor(label: String): Color {
    return when {
        "冷" in label -> Color(0xFF80DEEA) // 水色
        "寒" in label -> Color(0xFF1565C0) // 青
        "涼" in label -> Color(0xFF039BE5) // 水色
        "中" in label -> Color(0xFF388E3C) // 緑
        "温" in label -> Color(0xFFFB8C00) // オレンジ
        "暖" in label -> Color(0xFFD32F2F) // ピンク
        else -> Color.Gray
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
        .groupBy { it.region }
        .map { (region, regionEntries) ->
            val items = regionEntries.flatMap { entry ->
                val sowingItem = if (entry.sowing_start != 0 && entry.sowing_end != 0) {
                    listOf(RangeItem(
                        ranges = listOf(
                            MonthRange(
                                entry.sowing_start,
                                entry.sowing_end,
                                entry.sowing_start_stage,
                                entry.sowing_end_stage
                            )
                        ),
                        style = BandStyle.Dotted,
                        color = regionColors[region] ?: Color.Green,
                        itemLabel = "播種"
                    ))
                } else emptyList()

                val harvestItem = if (entry.harvest_start != 0 && entry.harvest_end != 0) {
                    listOf(RangeItem(
                        ranges = listOf(
                            MonthRange(
                                entry.harvest_start,
                                entry.harvest_end,
                                entry.harvest_start_stage,
                                entry.harvest_end_stage
                            )
                        ),
                        style = BandStyle.Solid,
                        color = regionColors[region] ?: Color.Red,
                        itemLabel = "収穫"
                    ))
                } else emptyList()

                sowingItem + harvestItem
            }

            val firstEntry = regionEntries.firstOrNull()
            GroupedCalendarBand(
                groupLabel = region,
                expirationYear = firstEntry?.expirationYear ?: 9999,
                expirationMonth = firstEntry?.expirationMonth ?: 12,
                items = items
            )
        }
        .filter { it.items.isNotEmpty() }

    SeedCalendarGrouped(bands = groupedBands, modifier = modifier, heightDp = heightDp)
}
