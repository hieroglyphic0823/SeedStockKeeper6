package com.example.seedstockkeeper6.ui.components

import android.content.res.Configuration
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import java.time.temporal.ChronoUnit
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.CalendarEntry
import java.time.LocalDate
import java.time.YearMonth
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas

/**
 * 栽培カレンダーのメインコンポーネント
 */

@Composable
fun SeedCalendarGrouped(
    entries: List<CalendarEntry>,
    packetExpirationYear: Int,
    packetExpirationMonth: Int,
    modifier: Modifier = Modifier.fillMaxWidth(),
    heightDp: Int = 114,
    previewDate: LocalDate? = null // プレビュー用の日付
) {
    val today = previewDate ?: LocalDate.now() // プレビュー用の日付があれば使用、なければ現在の日付
    val scrollState = rememberScrollState()
    
    // 播種期間と収穫期間の両方を考慮して、最も早い開始月を取得
    val allStartDates = entries.flatMap { entry ->
        listOfNotNull(
            entry.sowing_start_date.takeIf { it.isNotEmpty() },
            entry.harvest_start_date.takeIf { it.isNotEmpty() }
        )
    }
    
    val earliestDate = allStartDates.minOfOrNull { dateStr ->
        try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            today // パースエラーの場合は現在の日付
        }
    } ?: today
    
    // 当月から2年分のカレンダー期間を計算
    val calendarStartDate = LocalDate.of(today.year, today.monthValue, 1) // 当月から開始
    val calendarEndDate = calendarStartDate.plusYears(2).minusMonths(1)
    
    // デバッグログを追加
    android.util.Log.d("SeedCalendar", "カレンダー期間計算:")
    android.util.Log.d("SeedCalendar", "allStartDates=$allStartDates")
    android.util.Log.d("SeedCalendar", "earliestDate=$earliestDate")
    android.util.Log.d("SeedCalendar", "calendarStartDate=$calendarStartDate")
    android.util.Log.d("SeedCalendar", "calendarEndDate=$calendarEndDate")
    
    // 現在の月の位置を計算（スクロール初期位置用）
    // 当月から開始するため、常に0から開始
    val monthsFromStart = 0
    
    // 月幅を統一（実際の表示幅に基づく）
    // 画面幅を取得して12ヶ月分で割る
    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val monthWidth = screenWidth / 12f // 実際の表示幅に基づく月幅
    
    // カレンダー開始月が左端に表示されるようにスクロール位置を計算
    // 負の値の場合は0に設定（カレンダー開始月より前の場合は開始位置にスクロール）
    val initialScrollOffset = maxOf(0, monthsFromStart * monthWidth.value.toInt())
    
    // MaterialTheme から直接取得
    val baseSowingColor = MaterialTheme.colorScheme.primaryContainer
    val baseHarvestColor = MaterialTheme.colorScheme.primary

    val groupedBands = entries
        .groupBy { it.region }
        .map { (region, regionEntries) ->
            val items = regionEntries.flatMap { entry ->
                val sowingItem = if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
                    listOf(
                        RangeItem(
                            ranges = listOf(
                                MonthRange(
                                    entry.sowing_start_date,
                                    entry.sowing_end_date
                                )
                            ),
                            style = BandStyle.Solid, // 点線から棒線に変更
                            color = baseSowingColor,
                            itemLabel = "播種"
                        )
                    )
                } else emptyList()

                val harvestItem = if (entry.harvest_start_date.isNotEmpty() && entry.harvest_end_date.isNotEmpty()) {
                    listOf(
                        RangeItem(
                            ranges = listOf(
                                MonthRange(
                                    entry.harvest_start_date,
                                    entry.harvest_end_date
                                )
                            ),
                            style = BandStyle.Solid, // 点線から棒線に変更
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

    // 初期スクロール位置を設定
    LaunchedEffect(initialScrollOffset) {
        scrollState.animateScrollTo(initialScrollOffset)
    }

    Box(
        modifier = modifier
            .horizontalScroll(scrollState)
            .width(monthWidth * 24) // 2年分の幅（統一された月幅を使用）
    ) {
        SeedCalendarGroupedInternal(
            bands = groupedBands,
            modifier = Modifier.fillMaxWidth(),
            heightDp = heightDp,
            currentMonth = today.monthValue,
            currentYear = today.year,
            calendarStartDate = calendarStartDate,
            calendarEndDate = calendarEndDate
        )
    }
}

@Composable
fun SeedCalendarGroupedInternal(
    bands: List<GroupedCalendarBand>,
    modifier: Modifier = Modifier.fillMaxWidth(),
    heightDp: Int = 100,
    currentMonth: Int,
    currentYear: Int,
    calendarStartDate: LocalDate? = null,
    calendarEndDate: LocalDate? = null
) {
    val density = LocalDensity.current
    val context = LocalContext.current

    // AppColors とテーマから必要な値を取得 (Composable 関数のトップレベル)
    val actualTextPaintColor = MaterialTheme.colorScheme.onSurface
    val actualOutlineColor = MaterialTheme.colorScheme.background
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
    val onSecondaryContainerColor = MaterialTheme.colorScheme.onSecondaryContainer
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
        Paint().apply {
            color = actualTextPaintColor.toArgb()
            textSize = with(density) { textPaintFontSize.toPx() }
            isAntiAlias = true
        }
    }

    val iconPositions = remember { mutableStateListOf<Pair<Offset, Int>>() }
    
    // 破線効果を定義
    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
    
    // Canvas内で使用する色変数
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Canvas(
        modifier = modifier.height(with(density) { heightDp.dp })
    ) {
        // アイコンの位置をクリア
        iconPositions.clear()
        
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // ヘッダー部分の高さ
        val headerH = with(density) { 22.dp.toPx() }
        
        // グリッド部分の計算
        val gridTop = headerH
        val gridBottom = canvasHeight
        val gridLeft = 0f
        val gridRight = canvasWidth
        val gridW = gridRight - gridLeft
        val gridH = gridBottom - gridTop
        // カレンダーの表示期間を計算
        // 播種期間開始月から2年分のカレンダーを作成
        val startDate = calendarStartDate ?: LocalDate.of(currentYear, currentMonth, 1)
        val endDate = calendarEndDate ?: startDate.plusYears(2).minusMonths(1)
        val totalMonths = ChronoUnit.MONTHS.between(startDate, endDate).toInt() + 1
        
        // デバッグログを追加
        android.util.Log.d("SeedCalendar", "SeedCalendarGroupedInternal: calendarStartDate=$calendarStartDate, calendarEndDate=$calendarEndDate")
        android.util.Log.d("SeedCalendar", "SeedCalendarGroupedInternal: startDate=$startDate, endDate=$endDate, totalMonths=$totalMonths")
        
        // 実際の表示幅に基づいて月幅を計算
        val colW = gridW / 12f // 12ヶ月分の幅で計算（表示範囲は12ヶ月分）
        val rowH = with(density) { 118.dp.toPx() } // 栽培カレンダーの縦幅を118dpに設定（140dp - 22dp = 118dp）

        // 月ラベルの背景色を描画 (secondaryContainerLight) - 12ヶ月分のみ
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
        
        
        // 月ヘッダと月の背景色描画 (ここは月ごとに有効期限判定している) - 12ヶ月分のみ
        for (m in 0 until 12) {
            val currentMonthDate = startDate.plusMonths(m.toLong())
            val logicalMonth = currentMonthDate.monthValue
            val logicalYear = currentMonthDate.year
            val x = gridLeft + colW * m

            // 栽培期間行の背景色 (有効期限を考慮)
            if (bands.isNotEmpty()) { // groupedBand がないと expirationYear/Month にアクセスできない
                val expirationYear = bands.first().expirationYear
                val expirationMonth = bands.first().expirationMonth
                
                // expirationMonthが0以下の場合は有効期限なしとして扱う
                val expirationForMonthBg = if (expirationMonth > 0) {
                    YearMonth.of(expirationYear, expirationMonth)
                } else {
                    // 有効期限なしの場合は非常に遠い未来の日付を設定
                    YearMonth.of(9999, 12)
                }
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
            
            // 月ラベルを描画
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

        // 日付から月内での位置を計算する関数
        fun getDateOffsetInMonth(dateString: String): Float {
            if (dateString.isEmpty()) return 0.0f
            try {
                val day = dateString.substring(8, 10).toInt()
                val month = dateString.substring(5, 7).toInt()
                val year = dateString.substring(0, 4).toInt()
                val lastDayOfMonth = YearMonth.of(year, month).lengthOfMonth()
                return (day - 1).toFloat() / lastDayOfMonth.toFloat()
            } catch (e: Exception) {
                return 0.0f
            }
        }
        
        // 日付から月と年を取得
        fun getMonthFromDate(dateString: String): Int {
            if (dateString.isEmpty()) return 0
            return try {
                dateString.substring(5, 7).toInt()
            } catch (e: Exception) {
                0
            }
        }
        
        fun getYearFromDate(dateString: String): Int {
            if (dateString.isEmpty()) return 0
            return try {
                dateString.substring(0, 4).toInt()
            } catch (e: Exception) {
                0
            }
        }

        bands.forEachIndexed { row, groupedBand ->
            val top = gridTop + rowH * row
            val baseCenterY = top + rowH / 2f + with(density) { 8.dp.toPx() } // 上下に8dpの余白を追加

            val expirationDate = try {
                YearMonth.of(groupedBand.expirationYear, groupedBand.expirationMonth)
            } catch (e: Exception) {
                YearMonth.of(1900, 1) // Fallback
            }

            groupedBand.items.forEach { item ->
                item.ranges.forEach { r ->
                    // 日付から月と年を取得
                    val startMonth = getMonthFromDate(r.startDate)
                    val startYear = getYearFromDate(r.startDate)
                    val endMonth = getMonthFromDate(r.endDate)
                    val endYear = getYearFromDate(r.endDate)
                    
                    // 月が0の場合はスキップ
                    if (startMonth == 0 || endMonth == 0) {
                        return@forEach
                    }
                    
                    // 帯の開始月が有効期限内かで色を決定
                    val bandStartMonthForCheck = YearMonth.of(startYear, startMonth)
                    val actualColor = item.color // 常に基本色を使用（背景色で期限切れを表示）

                    // 帯のX座標計算 (カレンダー表示上の相対位置)
                    val startDateForRange = LocalDate.of(startYear, startMonth, 1)
                    val endDateForRange = LocalDate.of(endYear, endMonth, 1)
                    
                    val startMonthIndexInCalendar = ChronoUnit.MONTHS.between(startDate, startDateForRange).toInt()
                    val endMonthIndexInCalendar = ChronoUnit.MONTHS.between(startDate, endDateForRange).toInt()

                    var startX = gridLeft + colW * (startMonthIndexInCalendar + getDateOffsetInMonth(r.startDate))
                    var endX = gridLeft + colW * (endMonthIndexInCalendar + getDateOffsetInMonth(r.endDate))
                    
                    // デバッグログを追加
                    android.util.Log.d("SeedCalendar", "帯の位置計算: itemLabel=${item.itemLabel}, startDate=${r.startDate}, endDate=${r.endDate}")
                    android.util.Log.d("SeedCalendar", "startYear=$startYear, startMonth=$startMonth, endYear=$endYear, endMonth=$endMonth")
                    android.util.Log.d("SeedCalendar", "startDate=$startDate, startDateForRange=$startDateForRange, endDateForRange=$endDateForRange")
                    android.util.Log.d("SeedCalendar", "startMonthIndexInCalendar=$startMonthIndexInCalendar, endMonthIndexInCalendar=$endMonthIndexInCalendar")
                    android.util.Log.d("SeedCalendar", "startX=$startX, endX=$endX, colW=$colW")

                    // 年をまたぐ帯の場合のX座標補正
                    if (startYear < endYear || (startYear == endYear && startMonth > endMonth)) { // 年をまたいでいる
                        // 年をまたぐ場合は、実際の終了月まで表示
                        val actualEndMonthIndex = ChronoUnit.MONTHS.between(startDate, endDateForRange).toInt()
                        endX = gridLeft + colW * (actualEndMonthIndex + getDateOffsetInMonth(r.endDate))
                    }

                    when (item.style) {
                        BandStyle.Dotted -> {
                            // 点線の背景
                            drawRect(
                                color = surfaceContainerLowColor,
                                topLeft = Offset(startX - 2f, baseCenterY - 6f),
                                size = Size(endX - startX + 4f, 12f)
                            )
                            drawLine(
                                color = actualColor,
                                start = Offset(startX, baseCenterY),
                                end = Offset(endX, baseCenterY),
                                strokeWidth = 6f,
                                pathEffect = dash
                            )
                            drawCircle(actualColor, 6f, Offset(startX, baseCenterY))
                            drawCircle(actualColor, 6f, Offset(endX, baseCenterY))
                        }

                        BandStyle.Solid -> {
                            // 播種期間は上、収穫期間は下に配置
                            val adjustedCenterY = if (item.itemLabel == "収穫") {
                                // 収穫期間は元の位置に配置（上余白16dp + 播種棒グラフ22dp + 中間余白16dp + 収穫棒グラフの半分11dp）
                                top + with(density) { 65.dp.toPx() } // 16dp + 22dp + 16dp + 11dp = 65dp
                            } else {
                                // 播種期間は118dpの位置に配置（上余白16dp + 播種棒グラフの半分11dp）
                                top + with(density) { 27.dp.toPx() } // 16dp + 11dp = 27dp
                            }
                            
                            // 棒線の背景
                            val backgroundColor = if (item.itemLabel == "播種") {
                                // 播種期間の背景色はprimaryContainer
                                primaryContainerColor
                            } else {
                                // 収穫期間の背景色はsecondary
                                secondaryColor
                            }
                            drawRect(
                                color = backgroundColor,
                                topLeft = Offset(startX - 2f, adjustedCenterY - with(density) { 11.dp.toPx() }),
                                size = Size(endX - startX + 4f, with(density) { 22.dp.toPx() })
                            )
                            drawLine(
                                color = actualColor,
                                start = Offset(startX, adjustedCenterY),
                                end = Offset(endX, adjustedCenterY),
                                strokeWidth = 6f
                            )
                            
                            // 棒グラフの先頭にアイコンを表示
                            val iconSize = if (item.itemLabel == "収穫") {
                                with(density) { 20.dp.toPx() } // 収穫アイコンは20dp
                            } else {
                                with(density) { 24.dp.toPx() } // 播種アイコンは24dp
                            }
                            
                            // 播種期間はgrain、収穫期間はharvestアイコンを使用
                            val iconResource = if (item.itemLabel == "収穫") {
                                R.drawable.harvest
                            } else {
                                R.drawable.grain
                            }
                            
                            android.util.Log.d("SeedCalendar", "アイコン描画開始: itemLabel=${item.itemLabel}, iconResource=$iconResource, iconSize=$iconSize")
                            
                            
                            // アイコンを棒グラフ幅に横に繰り返し表示
                            val iconSpacing = iconSize * 1.5f // アイコン間隔（アイコンサイズの1.5倍）
                            val iconCount = ((endX - startX) / iconSpacing).toInt() + 1 // 繰り返し回数を計算
                            
                            // アイコンの位置とリソースを記録（複数個）
                            for (i in 0 until iconCount) {
                                val iconX = startX + i * iconSpacing
                                val iconPosition = Offset(iconX, adjustedCenterY)
                                iconPositions.add(iconPosition to iconResource)
                            }
                            
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
                                    val drawable = context.resources.getDrawable(iconResource, null)
                                    val bitmap = Bitmap.createBitmap(
                                        iconSize.toInt(), 
                                        iconSize.toInt(), 
                                        Bitmap.Config.ARGB_8888
                                    )
                                    val canvas = AndroidCanvas(bitmap)
                                    drawable.setBounds(0, 0, iconSize.toInt(), iconSize.toInt())
                                    drawable.draw(canvas)
                                    bitmap
                                }
                                
                                val iconImage = iconBitmap.asImageBitmap()
                                val iconDisplaySizeInt = iconSize.toInt()
                                
                                // 複数のアイコンを描画
                                for (i in 0 until iconCount) {
                                    val currentIconX = startX + i * iconSpacing
                                    
                                    // アイコンの位置を計算
                                    val iconY = if (item.itemLabel == "収穫") {
                                        // 収穫アイコンの上端が棒グラフの上端より4dp上
                                        adjustedCenterY - with(density) { 11.dp.toPx() } - with(density) { 4.dp.toPx() }
                                    } else {
                                        // 播種アイコンの上端が播種棒グラフの上端より10dp上
                                        adjustedCenterY - with(density) { 11.dp.toPx() } - with(density) { 10.dp.toPx() }
                                    }
                                    
                                    drawImage(
                                        image = iconImage,
                                        dstOffset = IntOffset(
                                            x = currentIconX.toInt(),
                                            y = iconY.toInt()
                                        ),
                                        dstSize = IntSize(iconDisplaySizeInt, iconDisplaySizeInt),
                                        colorFilter = if (item.itemLabel == "播種") {
                                            androidx.compose.ui.graphics.ColorFilter.tint(onPrimaryContainerColor)
                                        } else {
                                            null // 収穫アイコンは色付けなし（アイコンそのままの色）
                                        }
                                    )
                                }
                                
                            } catch (e: Exception) {
                                // アイコンの描画に失敗した場合はログ出力
                                android.util.Log.e("SeedCalendar", "アイコンの描画に失敗: ${e.message}")
                                android.util.Log.e("SeedCalendar", "アイコンリソース: $iconResource")
                                android.util.Log.e("SeedCalendar", "アイコンサイズ: $iconSize")
                            }
                        }
                    }
                }
            }
            // 地域が一つの場合は行の区切り線を削除
        }
    }
}
