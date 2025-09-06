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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Bitmap
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
    val context = LocalContext.current

    // AppColors とテーマから必要な値を取得 (Composable 関数のトップレベル)
    val actualTextPaintColor = MaterialTheme.colorScheme.onSurface
    val actualOutlineColor = MaterialTheme.colorScheme.outline
    val expiredColor = MaterialTheme.colorScheme.error
    val surfaceContainerLowColor = MaterialTheme.colorScheme.surfaceContainerLow
    val errorContainerColor = MaterialTheme.colorScheme.errorContainer
    val tertiaryContainerColor = MaterialTheme.colorScheme.tertiaryContainer
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
    val onTertiaryColor = MaterialTheme.colorScheme.onTertiary
    val onErrorColor = MaterialTheme.colorScheme.onError
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
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

        val headerH = with(density) { 22.dp.toPx() }
        val gridLeft = 0f // 左端を他の文字と合わせる
        val gridTop = headerH
        val gridRight = size.width
        val gridBottom = size.height
        val gridW = gridRight - gridLeft
        val gridH = gridBottom - gridTop
        val colW = gridW / 12f
        val rowH = with(density) { 24.dp.toPx() } // 棒グラフの幅に合わせて24dpに設定

        // 月ラベルの背景色を描画 (secondaryContainerLight)
        for (m in 0 until 12) {
            val x = gridLeft + colW * m
            drawRect(
                color = secondaryContainerColor, // secondaryContainerLight
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
            
            // 各月の旬の境界を示す縦のグリッド線（薄線）
            val thinLineColor = actualOutlineColor.copy(alpha = 0.3f) // 薄い線の色
            val thinStrokeWidth = 0.5f // 薄い線の太さ
            
            // 上旬と中旬の境界線（月の1/3の位置）
            val firstThirdX = x + colW / 3f
            drawLine(
                color = thinLineColor,
                start = Offset(firstThirdX, gridTop),
                end = Offset(firstThirdX, gridBottom),
                strokeWidth = thinStrokeWidth
            )
            
            // 中旬と下旬の境界線（月の2/3の位置）
            val secondThirdX = x + colW * 2f / 3f
            drawLine(
                color = thinLineColor,
                start = Offset(secondThirdX, gridTop),
                end = Offset(secondThirdX, gridBottom),
                strokeWidth = thinStrokeWidth
            )
            drawContext.canvas.nativeCanvas.drawText(
                logicalMonth.toString(),
                x + colW / 2,
                with(density) { headerH - 4.dp.toPx() }, // 月ラベルの位置
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
            "上旬" -> 0.0f      // 月の開始位置
            "中旬" -> 1.0f/3.0f // 月の1/3の位置（中旬の開始）
            "下旬" -> 2.0f/3.0f // 月の2/3の位置（下旬の開始）
            else -> 0.0f
        }
        
        fun getStageEndOffset(stage: String?): Float = when (stage) {
            "上旬" -> 1.0f/3.0f // 上旬の終了位置（中旬の開始）
            "中旬" -> 2.0f/3.0f // 中旬の終了位置（下旬の開始）
            "下旬" -> 1.0f      // 下旬の終了位置（次の月の開始）
            else -> 1.0f/3.0f
        }

        bands.forEachIndexed { row, groupedBand ->
            val top = gridTop + rowH * row
            val centerY = top + rowH / 2f + with(density) { 8.dp.toPx() } // 上下に8dpの余白を追加

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
                        gridLeft + colW * (endMonthIndexInCalendar + getStageEndOffset(r.endStage))

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
                            // 収穫期間の場合は12dp下げて表示
                            val adjustedCenterY = if (item.itemLabel == "収穫") {
                                centerY + with(density) { 12.dp.toPx() }
                            } else {
                                centerY
                            }
                            
                            // 棒線の背景
                            val backgroundColor = if (item.itemLabel == "播種") {
                                // 播種期間の背景色はprimaryContainer
                                primaryContainerColor
                            } else {
                                // 収穫期間の背景色はtertiaryContainer
                                tertiaryContainerColor
                            }
                            drawRect(
                                color = backgroundColor,
                                topLeft = Offset(startX - 2f, adjustedCenterY - 12f),
                                size = Size(endX - startX + 4f, 24f)
                            )
                            drawLine(
                                color = actualColor,
                                start = Offset(startX, adjustedCenterY),
                                end = Offset(endX, adjustedCenterY),
                                strokeWidth = with(density) { 24.dp.toPx() }, // 棒線の太さを24dpに変更
                                cap = Stroke.DefaultCap
                            )
                            
                            // 棒グラフの先頭にアイコンを表示
                            val iconSize = if (item.itemLabel == "収穫") {
                                with(density) { 20.dp.toPx() } // 収穫アイコンは20dp
                            } else {
                                with(density) { 16.dp.toPx() } // 播種アイコンは16dp
                            }
                            
                            // 播種期間の場合はgrainアイコン、収穫期間の場合はharvestアイコン
                            val iconResource = if (item.itemLabel == "播種") {
                                R.drawable.grain // grainアイコンに変更
                            } else {
                                R.drawable.harvest
                            }
                            
                            // アイコン描画開始のデバッグログ
                            android.util.Log.d("SeedCalendar", "アイコン描画開始: itemLabel=${item.itemLabel}, iconResource=$iconResource, startX=$startX, endX=$endX")
                            
                            // アイコンの左端を棒グラフの左端に合わせる
                            val iconX = startX // アイコンの左端を棒グラフの左端に合わせる
                            val iconPosition = Offset(iconX, adjustedCenterY)
                            
                            // アイコンの位置とリソースを記録
                            iconPositions.add(iconPosition to iconResource)
                            
                            // Canvas内でアイコンを描画
                            try {
                                // Vector Drawableを適切に処理
                                val iconBitmap = try {
                                    // まず通常のBitmapとして試行
                                    val bitmap = android.graphics.BitmapFactory.decodeResource(
                                        context.resources, 
                                        iconResource
                                    )
                                    if (bitmap != null) {
                                        bitmap
                                    } else {
                                        throw Exception("Bitmap decode failed")
                                    }
                                } catch (e: Exception) {
                                    // Vector Drawableの場合は、適切なサイズでBitmapを作成
                                    android.util.Log.d("SeedCalendar", "Vector Drawable処理開始: iconResource=$iconResource")
                                    val drawable = context.resources.getDrawable(iconResource, null)
                                    val bitmap = Bitmap.createBitmap(
                                        iconSize.toInt(), 
                                        iconSize.toInt(), 
                                        Bitmap.Config.ARGB_8888
                                    )
                                    val canvas = AndroidCanvas(bitmap)
                                    drawable.setBounds(0, 0, iconSize.toInt(), iconSize.toInt())
                                    drawable.draw(canvas)
                                    android.util.Log.d("SeedCalendar", "Vector Drawable処理完了: bitmap=$bitmap")
                                    bitmap
                                }
                                
                                if (iconBitmap != null) {
                                    val iconImage = iconBitmap.asImageBitmap()
                                    val iconDisplaySizeInt = iconSize.toInt() // 描画用にIntに変換
                                    
                                    // アイコンの色を設定（播種のみOnカラー、収穫は元の色）
                                    val colorFilter = if (item.itemLabel == "播種") {
                                        // 播種アイコンのみOnカラーを使用
                                        val iconColor = if (actualColor == expiredColor) {
                                            onErrorColor // 期限切れの場合はonError
                                        } else {
                                            onPrimaryContainerColor // 通常の場合はonPrimaryContainer
                                        }
                                        // デバッグログを追加
                                        android.util.Log.d("SeedCalendar", "播種アイコン描画: itemLabel=${item.itemLabel}, actualColor=$actualColor, expiredColor=$expiredColor, iconColor=$iconColor")
                                        androidx.compose.ui.graphics.ColorFilter.tint(iconColor)
                                    } else {
                                        // 収穫アイコンは元の色のまま（色フィルターなし）
                                        null
                                    }
                                    
                                    // アイコンの位置を計算
                                    val iconY = if (item.itemLabel == "収穫") {
                                        // 収穫アイコンは棒グラフの上辺より6dp上
                                        adjustedCenterY - with(density) { 6.dp.toPx() } - iconSize / 2
                                    } else {
                                        // 播種アイコンは棒グラフの上辺より3dp上
                                        adjustedCenterY - with(density) { 3.dp.toPx() } - iconSize / 2
                                    }
                                    
                                    drawImage(
                                        image = iconImage,
                                        dstOffset = IntOffset(
                                            x = iconX.toInt(),
                                            y = iconY.toInt()
                                        ),
                                        dstSize = IntSize(iconDisplaySizeInt, iconDisplaySizeInt),
                                        colorFilter = colorFilter
                                    )
                                    
                                    // アイコン描画成功のログ
                                    android.util.Log.d("SeedCalendar", "アイコン描画成功: itemLabel=${item.itemLabel}, iconX=$iconX, iconY=$iconY, iconSize=$iconDisplaySizeInt")
                                }
                            } catch (e: Exception) {
                                // アイコンの描画に失敗した場合は無視
                                android.util.Log.e("SeedCalendar", "Icon drawing failed", e)
                            }
                        }
                    }
                }
            }
            // 地域が一つの場合は行の区切り線を削除
        }
        }
        
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
                sowing_start = 11,
                sowing_start_stage = "上旬",
                sowing_end = 3,
                sowing_end_stage = "下旬",
                harvest_start = 4,
                harvest_start_stage = "上旬",
                harvest_end = 7,
                harvest_end_stage = "中旬"
            )
        )
        
        // プレビュー用のカスタムSeedCalendarGroupedを作成
        PreviewSeedCalendarGrouped(
            entries = sampleEntries,
            packetExpirationYear = 2026, // 有効期限2026年10月
            packetExpirationMonth = 10,
            modifier = Modifier.fillMaxWidth(),
            heightDp = 140
        )
    }
}

@Composable
private fun PreviewSeedCalendarGrouped(
    entries: List<CalendarEntry>,
    packetExpirationYear: Int,
    packetExpirationMonth: Int,
    modifier: Modifier = Modifier.fillMaxWidth(),
    heightDp: Int = 140
) {
    // プレビュー用に2025年3月を固定
    val previewToday = LocalDate.of(2025, 3, 1)
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
                            style = BandStyle.Solid,
                            color = baseSowingColor,
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
                            color = baseHarvestColor,
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

    // プレビュー用のデバッグログ
    android.util.Log.d("SeedCalendar", "プレビュー: groupedBands=${groupedBands.size}, 播種期間データ=${groupedBands.flatMap { it.items }.filter { it.itemLabel == "播種" }.size}")
    
    SeedCalendarGroupedInternal(
        bands = groupedBands,
        modifier = modifier,
        heightDp = heightDp,
        currentMonth = previewToday.monthValue, // 2025年3月
        currentYear = previewToday.year // 2025年
    )
}
