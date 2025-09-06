package com.example.seedstockkeeper6.ui.components

import android.content.res.Configuration
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
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
    val baseHarvestColor = MaterialTheme.colorScheme.tertiary

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
                            style = BandStyle.Solid, // 点線から棒線に変更
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

    // アイコンの位置を計算
    LaunchedEffect(entries, groupedBands) {
        // アイコンの位置計算は後で実装
    }

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
    val surfaceContainerLowColor = MaterialTheme.colorScheme.surfaceContainerLow
    val errorContainerColor = MaterialTheme.colorScheme.errorContainer
    val tertiaryContainerColor = MaterialTheme.colorScheme.tertiaryContainer
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    // カレンダーの月背景色
    val calendarMonthBackgroundWithinExpiration= tertiaryContainerColor // 月の数字が入っている枠の背景色（tertiaryContainerLight）
    val calendarMonthBackgroundExpired= errorContainerColor // errorContainerLight
    val calendarMonthBackground=tertiaryContainerColor  // デフォルト背景（tertiaryContainerLight）

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


    // アイコンの位置を計算するためのデータ
    val iconPositions = remember { mutableStateListOf<Pair<Offset, Int>>() }
    
    Column(modifier = modifier) {
        // 地域ラベルを栽培カレンダーの上に配置
        if (bands.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                bands.forEach { band ->
                    val regionColor = when {
                        "冷" in band.groupLabel -> Color(0xFF80DEEA)
                        "寒" in band.groupLabel -> Color(0xFF1565C0)
                        "涼" in band.groupLabel -> Color(0xFF039BE5)
                        "中" in band.groupLabel -> Color(0xFF388E3C)
                        "温" in band.groupLabel -> Color(0xFFFB8C00)
                        "暖" in band.groupLabel -> Color(0xFFD32F2F)
                        else -> Color.Gray
                    }
                    
                    // 農園情報と同じDisplayModeスタイル
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = regionColor.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = regionColor,
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                            Text(
                                text = band.groupLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (band.groupLabel.isEmpty()) 
                                    MaterialTheme.colorScheme.onSurfaceVariant 
                                else 
                                    MaterialTheme.colorScheme.onSurface,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        
        Box {
        
        // まき時、収穫の文字を栽培カレンダー全体の上に配置
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            // まき時アイコン
            Image(
                painter = painterResource(id = R.drawable.seeds),
                contentDescription = "まき時",
                modifier = Modifier.size(16.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "まき時",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            
            // 収穫アイコン
            Image(
                painter = painterResource(id = R.drawable.harvest),
                contentDescription = "収穫",
                modifier = Modifier.size(16.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "収穫",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
        ) {
            // 栽培カレンダー全体の背景色（月ラベルの背景色）
            drawRect(
                color = surfaceContainerLowColor, // surfaceContainerLowLight
                size = size
            )

        val headerH = 22.dp.toPx()
        val gridLeft = 0f // 左端を他の文字と合わせる
        val gridTop = headerH
        val gridRight = size.width
        val gridBottom = size.height
        val gridW = gridRight - gridLeft
        val gridH = gridBottom - gridTop
        val colW = gridW / 12f
        val rowH = 24.dp.toPx() // 棒グラフの幅に合わせて24dpに設定

        // 月ラベルの背景色を描画 (tertiaryContainerLight)
        for (m in 0 until 12) {
            val x = gridLeft + colW * m
            drawRect(
                color = tertiaryContainerColor, // tertiaryContainerLight
                topLeft = Offset(x, 0f),
                size = Size(colW, headerH)
            )
            // 月ラベル行の縦線
            drawLine(
                color = actualOutlineColor,
                start = Offset(x, 0f),
                end = Offset(x, headerH),
                strokeWidth = 1f
            )
        }
        
        // 月ラベル行の横線
        drawLine(
            color = actualOutlineColor,
            start = Offset(gridLeft, 0f),
            end = Offset(gridRight, 0f),
            strokeWidth = 1f
        )
        drawLine(
            color = actualOutlineColor,
            start = Offset(gridLeft, headerH),
            end = Offset(gridRight, headerH),
            strokeWidth = 1f
        )
        
        
        // 月ヘッダと月の背景色描画 (ここは月ごとに有効期限判定している)
        for (m in 0 until 12) {
            val logicalMonth = ((currentMonth - 1 + m) % 12) + 1
            val x = gridLeft + colW * m
            val yearOffset =
                if (logicalMonth < currentMonth && bands.isNotEmpty()) 1 else 0 // bandsが空の場合のエラーを避ける
            val logicalYear = currentYear + yearOffset

            // 栽培期間行の背景色 (有効期限を考慮)
            if (bands.isNotEmpty()) { // groupedBand がないと expirationYear/Month にアクセスできない
                val expirationForMonthBg = YearMonth.of(
                    bands.first().expirationYear,
                    bands.first().expirationMonth
                ) // 代表として最初のバンドの期限を使う
                val targetMonthForBg = YearMonth.of(logicalYear, logicalMonth)
                if (targetMonthForBg <= expirationForMonthBg) {
                    drawRect(
                        color = surfaceContainerLowColor, // surfaceContainerLowLight
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
                    color = surfaceContainerLowColor, // surfaceContainerLowLight
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
                headerH - 4.dp.toPx(), // 月ラベルの位置
                textPaint
            )
        }
        
        // 右端の線
        drawLine(
            color = actualOutlineColor,
            start = Offset(gridRight, gridTop),
            end = Offset(gridRight, gridBottom),
            strokeWidth = 1f
        )
        
        // 上端の線
        drawLine(
            color = actualOutlineColor,
            start = Offset(gridLeft, gridTop),
            end = Offset(gridRight, gridTop),
            strokeWidth = 1f
        )
        
        // 下端の線
        drawLine(
            color = actualOutlineColor,
            start = Offset(gridLeft, gridBottom),
            end = Offset(gridRight, gridBottom),
            strokeWidth = 1f
        )

        fun getStageOffset(stage: String?): Float = when (stage) {
            "上旬" -> 0.05f
            "中旬" -> 0.5f
            "下旬" -> 0.95f
            else -> 0.5f
        }

        bands.forEachIndexed { row, groupedBand ->
            val top = gridTop + rowH * row
            val centerY = top + rowH / 2f + 8.dp.toPx() // 上下に8dpの余白を追加

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
                            // 点線の背景
                            drawRect(
                                color = surfaceContainerLowColor,
                                topLeft = Offset(startX - 2f, centerY - 12f),
                                size = Size(endX - startX + 4f, 24f)
                            )
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
                            // 棒線の背景
                            drawRect(
                                color = surfaceContainerLowColor,
                                topLeft = Offset(startX - 2f, centerY - 12f),
                                size = Size(endX - startX + 4f, 24f)
                            )
                            drawLine(
                                color = actualColor,
                                start = Offset(startX, centerY),
                                end = Offset(endX, centerY),
                                strokeWidth = 24.dp.toPx(), // 棒線の太さを24dpに変更
                                cap = Stroke.DefaultCap
                            )
                            
                            // 棒グラフの先頭にアイコンを表示
                            val iconSize = 24.dp.toPx() // 24dpに変更
                            
                            // 播種期間の場合はseedsアイコン、収穫期間の場合はharvestアイコン
                            val iconResource = if (item.color == primaryColor) {
                                R.drawable.seeds
                            } else {
                                R.drawable.harvest
                            }
                            
                            // アイコンの位置を棒グラフの左端に合わせる
                            val iconX = startX - iconSize / 2 // 棒グラフの左端に合わせる
                            val iconPosition = Offset(iconX, centerY)
                            
                            // アイコンの位置とリソースは後でLaunchedEffectで計算
                        }
                    }
                }
            }
            // 地域が一つの場合は行の区切り線を削除
        }
        }
        
        // 棒グラフのアイコンを表示（一時的にコメントアウト）
        /*
        iconPositions.forEach { (position, resourceId) ->
            Image(
                painter = painterResource(id = resourceId),
                contentDescription = if (resourceId == R.drawable.seeds) "まき時" else "収穫",
                modifier = Modifier
                    .offset(
                        x = with(density) { position.x.toDp() },
                        y = with(density) { position.y.toDp() }
                    )
                    .size(24.dp),
                contentScale = ContentScale.Fit
            )
        }
        */
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

@Preview(showBackground = true)
@Composable
fun SeedCalendarGroupedPreview() {
    SeedStockKeeper6Theme {
        val sampleEntries = listOf(
            CalendarEntry(
                region = "暖地",
                sowing_start = 4,
                sowing_start_stage = "下旬",
                sowing_end = 6,
                sowing_end_stage = "上旬",
                harvest_start = 10,
                harvest_start_stage = "中旬",
                harvest_end = 11,
                harvest_end_stage = "中旬"
            )
        )
        
        // プレビュー用のデータ

        SeedCalendarGrouped(
            entries = sampleEntries,
            packetExpirationYear = 2026,
            packetExpirationMonth = 3,
            modifier = Modifier.fillMaxWidth(),
            heightDp = 140
        )
    }
}
