package com.example.seedstockkeeper6.ui.components

import android.content.res.Configuration
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.TextUnit
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
    entries: List<CalendarEntry>,
    packetExpirationYear: Int,
    packetExpirationMonth: Int,
    modifier: Modifier = Modifier.fillMaxWidth(),
    heightDp: Int = 140
) {
    val today = LocalDate.now()
    // MaterialTheme から直接取得
    val baseSowingColor = MaterialTheme.colorScheme.primary
    val baseHarvestColor = MaterialTheme.colorScheme.secondary

    val groupedBands = entries
        .groupBy { it.region }
        .map { (region, regionEntries) ->
            val items = regionEntries.flatMap { entry ->
                val sowingItem = if (entry.sowing_start != 0 && entry.sowing_end != 0) {
                    listOf(
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
                            color = baseSowingColor, // 基本色
                            itemLabel = "播種"
                        )
                    )
                } else emptyList()

                val harvestItem = if (entry.harvest_start != 0 && entry.harvest_end != 0) {
                    listOf(
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
                            color = baseHarvestColor, // 基本色
                            itemLabel = "収穫"
                        )
                    )
                } else emptyList()

                sowingItem + harvestItem
            }

            GroupedCalendarBand(
                groupLabel = region,
                expirationYear = packetExpirationYear,
                expirationMonth = packetExpirationMonth,
                items = items
            )
        }
        .filter { it.items.isNotEmpty() }

    SeedCalendarGroupedInternal(
        bands = groupedBands,
        modifier = modifier,
        heightDp = heightDp,
        currentMonth = today.monthValue,
        currentYear = today.year
    )
}

// SeedCalendar.kt

@Composable
fun SeedCalendarGroupedInternal(
    bands: List<GroupedCalendarBand>,
    modifier: Modifier = Modifier.fillMaxWidth(),
    heightDp: Int = 140,
    currentMonth: Int,
    currentYear: Int
) {
    val density = LocalDensity.current

    // AppColors とテーマから必要な値を取得 (Composable 関数のトップレベル)
    val actualTextPaintColor = MaterialTheme.colorScheme.onSurface
    val actualOutlineColor = MaterialTheme.colorScheme.outline
    val expiredColor = MaterialTheme.colorScheme.error
    // カレンダーの月背景色
    val calendarMonthBackgroundWithinExpiration= MaterialTheme.colorScheme.surfaceVariant
    val calendarMonthBackgroundExpired= MaterialTheme.colorScheme.errorContainer
    val calendarMonthBackground=MaterialTheme.colorScheme.surfaceVariant  // デフォルト背景

    val textPaintFontSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize // ← fontSizeをここで取得
    val configuration = LocalConfiguration.current // ← トップレベルで取得
    val screenWidth = configuration.screenWidthDp.dp // screenWidthもここで取得するのが自然
    val currentOrientation = configuration.orientation // ← orientationもここで取得

    val textPaint = remember(
        MaterialTheme.typography.bodyMedium, // typographyはrememberのキーとして適切
        density,
        actualTextPaintColor // ★ AppColorsから取得した色をrememberのキーとして渡す
    ) {
        // remember の計算ブロック内では @Composable 関数を呼び出さない
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = actualTextPaintColor.toArgb() // ★ rememberの外で取得した色を使用
            textAlign = Paint.Align.CENTER
            textSize = with(density) { textPaintFontSize.toPx() }
        }
    }
    val dash = remember { PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f) }


    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
    ) {

        val labelColWidth = screenWidth.toPx() / (if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) 13 else 14)
        val labelColWidthInPx = with(density) { screenWidth.toPx() } / (if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) 13 else 14)
        // labelColWidthInPx は既に Float (ピクセル値) なので、toPx() は不要
        val labelW = labelColWidthInPx
        val headerH = 22.dp.toPx()
        val gridLeft = labelW
        val gridTop = headerH
        val gridRight = size.width
        val gridBottom = size.height
        val gridW = gridRight - gridLeft
        val gridH = gridBottom - gridTop
        val colW = gridW / 12f
        val rowH = gridH / max(1, bands.size)

        // 月ヘッダと月の背景色描画 (ここは月ごとに有効期限判定している)
        for (m in 0 until 12) {
            val logicalMonth = ((currentMonth - 1 + m) % 12) + 1
            val x = gridLeft + colW * m
            val yearOffset =
                if (logicalMonth < currentMonth && bands.isNotEmpty()) 1 else 0 // bandsが空の場合のエラーを避ける
            val logicalYear = currentYear + yearOffset

            // 月の背景色 (有効期限を考慮)
            if (bands.isNotEmpty()) { // groupedBand がないと expirationYear/Month にアクセスできない
                val expirationForMonthBg = YearMonth.of(
                    bands.first().expirationYear,
                    bands.first().expirationMonth
                ) // 代表として最初のバンドの期限を使う
                val targetMonthForBg = YearMonth.of(logicalYear, logicalMonth)
                if (targetMonthForBg <= expirationForMonthBg) {
                    drawRect(
                        color = calendarMonthBackgroundWithinExpiration, // AppColors から
                        topLeft = Offset(x, gridTop),
                        size = Size(colW, gridH)
                    )
                } else {
                    drawRect(
                        color = calendarMonthBackgroundExpired, // AppColors から
                        topLeft = Offset(x, gridTop),
                        size = Size(colW, gridH)
                    )
                }
            } else { // バンドがない場合はデフォルトの背景
                drawRect(
                    color = calendarMonthBackground, // デフォルト背景
                    topLeft = Offset(x, gridTop),
                    size = Size(colW, gridH)
                )
            }


            drawLine(
                color = actualOutlineColor, // グリッド線の色
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
        // ... (右端の線)

        fun getStageOffset(stage: String?): Float = when (stage) {
            "上旬" -> 0.05f
            "中旬" -> 0.5f
            "下旬" -> 0.95f
            else -> 0.5f
        }

        bands.forEachIndexed { row, groupedBand ->
            val top = gridTop + rowH * row
            val centerY = top + rowH / 2f

            val expirationDate = try {
                YearMonth.of(groupedBand.expirationYear, groupedBand.expirationMonth)
            } catch (e: Exception) {
                YearMonth.of(1900, 1) // Fallback
            }

            groupedBand.items.forEach { item ->
                item.ranges.forEach { r ->
                    // 帯の開始月が属する年を計算
                    val bandStartActualYear =
                        currentYear + if (r.start < currentMonth && currentMonth - r.start >= 0) 1 else 0
                    val bandStartMonthForCheck = YearMonth.of(bandStartActualYear, r.start)

                    // 帯の開始月が有効期限内かで色を決定
                    val actualColor = if (!bandStartMonthForCheck.isAfter(expirationDate)) {
                        item.color // 基本色 (有効期限内)
                    } else {
                        expiredColor // 期限切れ色
                    }

                    // 帯のX座標計算 (カレンダー表示上の相対位置)
                    val startMonthIndexInCalendar = (r.start - currentMonth + 12) % 12
                    val endMonthIndexInCalendar = (r.end - currentMonth + 12) % 12

                    var startX =
                        gridLeft + colW * (startMonthIndexInCalendar + getStageOffset(r.startStage))
                    var endX =
                        gridLeft + colW * (endMonthIndexInCalendar + getStageOffset(r.endStage))

                    // 年をまたぐ帯の場合のX座標補正 (例: 11月～2月で、カレンダーが1月始まり)
                    // この補正は非常に複雑で、現在の計算では不十分な場合があります。
                    // 簡略化のため、帯が12ヶ月を超えるような極端なケースは考慮しない前提。
                    if (r.start > r.end) { // 年をまたいでいる (例: 11月(start) から 2月(end))
                        if (endX < startX) { // 描画順が逆転している場合 (例: カレンダー上で2月が11月より左に来る)
                            // このケースでは、帯を2つに分割して描画するか、
                            // もしくはstartX, endXのロジックをより精緻にする必要があります。
                            // ここでは、単純にカレンダーの右端/左端を使うなど、割り切りが必要かもしれません。
                            // 今回は、年またぎでも連続した一つの線として描画できるケースを想定。
                            // もし表示がおかしい場合、この部分のロジック見直しが必要です。
                            // 例: 11月～2月の帯で、カレンダーが1月始まりの場合、
                            // 11月～12月と、翌年の1月～2月を分けて考える必要がある。
                            // ただし、現在のstartX/endXの計算では、これを1本の線として扱おうとする。
                            // ここでは、年を跨いだ帯は、12ヶ月分の幅を上限として、
                            // (r.end - currentMonth + 12) と (r.start - currentMonth) の差から長さを出す方が
                            // 安定するかもしれない。
                            // ひとまず、現状の計算で進めます。
                        }
                    }


                    when (item.style) {
                        BandStyle.Dotted -> {
                            drawLine(
                                color = actualColor,
                                start = Offset(startX, centerY),
                                end = Offset(endX, centerY),
                                strokeWidth = 6f,
                                pathEffect = dash
                            )
                            drawCircle(actualColor, 6f, Offset(startX, centerY))
                            drawCircle(actualColor, 6f, Offset(endX, centerY))
                        }

                        BandStyle.Solid -> {
                            drawLine(
                                color = actualColor,
                                start = Offset(startX, centerY),
                                end = Offset(endX, centerY),
                                strokeWidth = 14f,
                                cap = Stroke.DefaultCap
                            )
                        }
                    }
                }
            }
            // 行の区切り線
            drawLine(
                color = actualOutlineColor,// 行の区切り線
                start = Offset(gridLeft, top + rowH),
                end = Offset(gridRight, top + rowH),
                strokeWidth = 1f
            )
        }
    }
}

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
