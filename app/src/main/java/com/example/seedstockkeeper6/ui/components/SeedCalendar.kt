package com.example.seedstockkeeper6.ui.components

import android.content.res.Configuration
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.animation.core.*
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
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas

/**
 * 種暦のメインコンポーネント
 */

@Composable
fun SeedCalendarGrouped(
    entries: List<CalendarEntry>,
    packetExpirationYear: Int,
    packetExpirationMonth: Int,
    modifier: Modifier = Modifier.fillMaxWidth(),
    heightDp: Int = 114,
    previewDate: LocalDate? = null, // プレビュー用の日付
    sowingDate: String = "" // まいた日（"YYYY-MM-DD"形式）
) {
    val today = previewDate ?: LocalDate.now() // プレビュー用の日付があれば使用、なければ現在の日付
    
    // 📅 現在の月から3カ月前を起点に、2年分先までのカレンダー期間を計算
    val calendarStartDate = LocalDate.of(today.year, today.monthValue, 1).minusMonths(3) // 過去3カ月分も表示
    val calendarEndDate = calendarStartDate.plusYears(2).minusMonths(1) // 開始から2年分先まで
    
    // カレンダーの総月数を計算（過去3カ月 + 2年分 = 27ヶ月）
    val totalMonths = ChronoUnit.MONTHS.between(calendarStartDate, calendarEndDate).toInt() + 1
    
    // カレンダー全体の月のリストを作成
    val months = List(totalMonths) { i -> 
        calendarStartDate.plusMonths(i.toLong()) 
    }
    
    // 📅 当月のインデックスを計算
    val todayMonthStart = LocalDate.of(today.year, today.monthValue, 1)
    val currentMonthIndex = ChronoUnit.MONTHS.between(calendarStartDate, todayMonthStart).toInt()
    
    // LazyRowのStateを作成し、初期表示位置を設定（当月が左端に表示される）
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = currentMonthIndex
    )
    
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
                items = items,
                sowingDate = sowingDate
            )
        }
        .filter { it.items.isNotEmpty() }

    // BoxWithConstraintsで実際の利用可能な幅を取得
    BoxWithConstraints(modifier = modifier) {
        // 月幅を統一（実際の表示幅に基づく）
        // 実際のコンテナ幅を取得して6ヶ月分で割る（画面に6ヶ月分を表示）
        val monthWidth = maxWidth / 6f // 6ヶ月分で1画面を構成
        
        // LazyRowで月ごとにカレンダーを表示
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            state = lazyListState
        ) {
            items(
                items = months,
                key = { month -> "${month.year}-${month.monthValue}" }
            ) { monthDate ->
                // 各月の描画
                Box(
                    modifier = Modifier.width(monthWidth)
                ) {
                    SeedCalendarGroupedInternal(
                        bands = groupedBands,
                        modifier = Modifier.fillMaxWidth(),
                        heightDp = heightDp,
                        currentMonth = monthDate.monthValue,
                        currentYear = monthDate.year,
                        calendarStartDate = calendarStartDate,
                        calendarEndDate = calendarEndDate,
                        targetMonth = monthDate // 表示対象の月を指定
                    )
                }
            }
        }
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
    calendarEndDate: LocalDate? = null,
    targetMonth: LocalDate? = null // 表示対象の月（LazyRow使用時）
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    
    // 🌾 播種期間の種アイコン上から徐々に表示アニメーション
    val infiniteTransition = rememberInfiniteTransition(label = "sowingReveal")
    val revealProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sowingRevealAnim"
    )
    
    // 🥕 収穫アイコンのぷるぷる揺れアニメーション
    val harvestShakeTransition = rememberInfiniteTransition(label = "harvestShake")
    val shakeRotation by harvestShakeTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                -6f at 0
                6f at 100
                -3f at 200
                3f at 300
                -6f at 400
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeRotation"
    )

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
    val surfaceContainerHighestColor = MaterialTheme.colorScheme.surfaceContainerHighest // お城画面の期限切れカードの表面色
    // カレンダーの月背景色
    val calendarMonthBackgroundWithinExpiration= tertiaryContainerColor // 月の数字が入っている枠の背景色（tertiaryContainerLight）
    val calendarMonthBackgroundExpired= errorContainerColor // errorContainerLight（全体背景用、今後は使用しない）
    val calendarMonthBackground=tertiaryContainerColor  // デフォルト背景（tertiaryContainerLight）
    // 播種期間の背景色定義
    val sowingExpiredBackgroundColor = com.example.seedstockkeeper6.ui.theme.backgroundLightMediumContrast // 有効期限の月の色
    val sowingExpiredGrayColor = surfaceContainerHighestColor // 有効期限の月以降（お城画面の期限切れカードと同じ色）

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
            textAlign = Paint.Align.CENTER // テキストを中央揃えにする
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
        // targetMonthが指定されている場合は1ヶ月分だけを描画
        val (startDate, endDate, totalMonths) = if (targetMonth != null) {
            val monthStart = LocalDate.of(targetMonth.year, targetMonth.monthValue, 1)
            val monthEnd = monthStart.plusMonths(1).minusDays(1)
            Triple(monthStart, monthEnd, 1)
        } else {
            // 全範囲を描画
            val start = calendarStartDate ?: LocalDate.of(currentYear, currentMonth, 1)
            val end = calendarEndDate ?: start.plusYears(2).minusMonths(1)
            val total = ChronoUnit.MONTHS.between(start, end).toInt() + 1
            Triple(start, end, total)
        }
        
        // デバッグログを追加
        
        // 実際の表示幅に基づいて月幅を計算
        // targetMonthが指定されている場合は1ヶ月分、そうでなければ全範囲で計算
        val colW = gridW / totalMonths.toFloat() // 月幅を計算
        val rowH = with(density) { 118.dp.toPx() } // 種暦の縦幅を118dpに設定（140dp - 22dp = 118dp）

        // 月ラベルの背景色を描画 (secondaryContainerLight) - 全範囲分
        for (m in 0 until totalMonths) {
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
        
        
        // 月ヘッダと月の背景色描画 (ここは月ごとに有効期限判定している) - 全範囲分
        for (m in 0 until totalMonths) {
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
                
                // カレンダーの月背景色を有効期限に応じて変更
                // 上半分（播種期間表示部分）のみ有効期限に応じて色を変更
                // 下半分（収穫期間表示部分）は常に通常色
                val halfHeight = gridH / 2f
                
                // 上半分の背景色（播種期間表示部分）
                val topHalfBackgroundColor = when {
                    targetMonthForBg < expirationForMonthBg -> {
                        // 有効期限の月より前：通常色
                        surfaceContainerLowColor
                    }
                    targetMonthForBg == expirationForMonthBg -> {
                        // 有効期限の月：backgroundLightMediumContrast
                        sowingExpiredBackgroundColor
                    }
                    else -> {
                        // 有効期限の月より後：グレーアウト
                        sowingExpiredGrayColor
                    }
                }
                
                // 上半分の背景を描画（播種期間表示部分）
                drawRect(
                    color = topHalfBackgroundColor,
                    topLeft = Offset(x, gridTop),
                    size = Size(colW, halfHeight)
                )
                
                // 下半分の背景を描画（収穫期間表示部分、常に通常色）
                drawRect(
                    color = surfaceContainerLowColor,
                    topLeft = Offset(x, gridTop + halfHeight),
                    size = Size(colW, halfHeight)
                )
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
                // expirationMonthが0以下の場合は有効期限なしとして扱う
                if (groupedBand.expirationMonth > 0) {
                    YearMonth.of(groupedBand.expirationYear, groupedBand.expirationMonth)
                } else {
                    // 有効期限なしの場合は非常に遠い未来の日付を設定（期限切れ判定を無効化）
                    YearMonth.of(9999, 12)
                }
            } catch (e: Exception) {
                YearMonth.of(9999, 12) // Fallback: 有効期限なしとして扱う
            }

            // 🌱 「まいた日」アイコンの描画情報を保存（最前面に表示するため）
            var plantingIconInfo: Triple<Float, Float, Float>? = null // (periodX, plantingY, plantingSize)
            var plantingBitmap: android.graphics.Bitmap? = null
            val sowingDateString = groupedBand.sowingDate ?: ""
            
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
                            
                            // 棒線の背景（播種バー自体の色は常に通常色）
                            val backgroundColor = if (item.itemLabel == "播種") {
                                // 播種期間の背景色は常にprimaryContainerColor（カレンダーの月背景色で有効期限を表現）
                                primaryContainerColor
                            } else {
                                // 収穫期間の背景色は常にsecondaryContainer（有効期限切れの色変更なし）
                                secondaryContainerColor
                            }
                            drawRect(
                                color = backgroundColor,
                                topLeft = Offset(startX - 2f, adjustedCenterY - with(density) { 11.dp.toPx() }),
                                size = Size(endX - startX + 4f, with(density) { 22.dp.toPx() })
                            )
                            // 播種期間の場合のみ、中央に線を引く
                            if (item.itemLabel == "播種") {
                                drawLine(
                                    color = actualColor,
                                    start = Offset(startX, adjustedCenterY),
                                    end = Offset(endX, adjustedCenterY),
                                    strokeWidth = 6f
                                )
                            }
                            
                            if (item.itemLabel == "播種") {
                                // 🌱 「まいた日」アイコンの位置を計算（後で描画するため情報を保存）
                                if (sowingDateString.isNotEmpty()) {
                                    try {
                                        val sowingDate = LocalDate.parse(sowingDateString)
                                        val sowingYear = sowingDate.year
                                        val sowingMonth = sowingDate.monthValue
                                        val sowingDay = sowingDate.dayOfMonth
                                        val lastDay = YearMonth.of(sowingYear, sowingMonth).lengthOfMonth()
                                        val dayRatio = sowingDay.toFloat() / lastDay.toFloat()
                                        
                                        // 月のインデックスを算出
                                        val sowingMonthIndex =
                                            ChronoUnit.MONTHS.between(startDate, LocalDate.of(sowingYear, sowingMonth, 1)).toInt()
                                        
                                        // カレンダー範囲内のみ描画
                                        if (!sowingDate.isBefore(startDate) && !sowingDate.isAfter(endDate)) {
                                            // どの旬に属するかを判断してアイコン位置を補正
                                            val periodX = when {
                                                dayRatio < 1f / 3f -> gridLeft + colW * (sowingMonthIndex + 1f / 6f)      // 上旬
                                                dayRatio < 2f / 3f -> gridLeft + colW * (sowingMonthIndex + 0.5f)         // 中旬
                                                else -> gridLeft + colW * (sowingMonthIndex + 5f / 6f)                    // 下旬
                                            }
                                            
                                            val plantingSize = with(density) { 22.dp.toPx() }
                                            val plantingY = adjustedCenterY - with(density) { 30.dp.toPx() }
                                            
                                            // アイコンbitmap取得（一度だけ取得）
                                            if (plantingBitmap == null) {
                                                plantingBitmap = try {
                                                    BitmapFactory.decodeResource(context.resources, R.drawable.planting)
                                                        ?: throw Exception("decode failed")
                                                } catch (e: Exception) {
                                                    val drawable = context.resources.getDrawable(R.drawable.planting, null)
                                                    val bmp = Bitmap.createBitmap(plantingSize.toInt(), plantingSize.toInt(), Bitmap.Config.ARGB_8888)
                                                    val c = AndroidCanvas(bmp)
                                                    drawable.setBounds(0, 0, plantingSize.toInt(), plantingSize.toInt())
                                                    drawable.draw(c)
                                                    bmp
                                                }
                                            }
                                            
                                            // 描画情報を保存（全ての描画処理の後に描画）
                                            plantingIconInfo = Triple(periodX, plantingY, plantingSize)
                                        }
                                    } catch (e: Exception) {
                                    }
                                }
                                
                                // 🌾 播種期間：各月を3分割して種アイコンを配置（点滅アニメーション付き）
                                val iconSize = with(density) { 16.dp.toPx() } // 少し小さめ
                                val iconResource = R.drawable.sesame
                                
                                // アイコン画像の準備
                                val iconBitmap = try {
                                    val bmp = android.graphics.BitmapFactory.decodeResource(context.resources, iconResource)
                                    bmp ?: throw Exception("decode failed")
                                } catch (e: Exception) {
                                    val drawable = context.resources.getDrawable(iconResource, null)
                                    val bmp = Bitmap.createBitmap(iconSize.toInt(), iconSize.toInt(), Bitmap.Config.ARGB_8888)
                                    val c = AndroidCanvas(bmp)
                                    drawable.setBounds(0, 0, iconSize.toInt(), iconSize.toInt())
                                    drawable.draw(c)
                                    bmp
                                }
                                val iconImage = iconBitmap.asImageBitmap()
                                val iconDisplaySizeInt = iconSize.toInt()
                                
                                // 播種期間の各月を順に処理
                                val startMonthDate = LocalDate.of(startYear, startMonth, 1)
                                val endMonthDate = LocalDate.of(endYear, endMonth, 1)
                                val monthSpan = ChronoUnit.MONTHS.between(startMonthDate, endMonthDate).toInt().coerceAtLeast(0)
                                
                                // 各月の開始位置を計算（カレンダー表示上の相対位置）
                                for (m in 0..monthSpan) {
                                    val monthX = gridLeft + colW * (startMonthIndexInCalendar + m)
                                    
                                    // 月を3分割してそれぞれの中心にアイコンを配置
                                    val positions = listOf(
                                        monthX + colW / 6f,      // 上旬
                                        monthX + colW / 2f,      // 中旬
                                        monthX + colW * 5f / 6f  // 下旬
                                    )
                                    
                                    // 各月の有効期限チェック
                                    val currentMonthForCheck = startMonthDate.plusMonths(m.toLong())
                                    val currentYearMonth = YearMonth.of(currentMonthForCheck.year, currentMonthForCheck.monthValue)
                                    val isExpired = currentYearMonth > expirationDate
                                    
                                    positions.forEach { iconX ->
                                        // 棒グラフの範囲内（startX から endX）にある旬のみアイコンを表示
                                        // 有効期限切れの期間はアイコンを表示しない
                                        if (iconX >= startX && iconX <= endX && !isExpired) {
                                            val iconY = adjustedCenterY - with(density) { 14.dp.toPx() } // 棒の上に配置
                                            val iconLeft = iconX - iconDisplaySizeInt / 2f
                                            val iconTop = iconY
                                            
                                            // 上から徐々に表示するアニメーション（clipRectで制御）
                                            val revealHeight = iconDisplaySizeInt * revealProgress
                                            val clipBottom = iconTop + revealHeight
                                            
                                            // clipRectで上から下に徐々に表示
                                            drawContext.canvas.save()
                                            drawContext.canvas.clipRect(
                                                left = iconLeft,
                                                top = iconTop,
                                                right = iconLeft + iconDisplaySizeInt,
                                                bottom = clipBottom
                                            )
                                            
                                            drawImage(
                                                image = iconImage,
                                                dstOffset = IntOffset(iconX.toInt() - iconDisplaySizeInt / 2, iconY.toInt()),
                                                dstSize = IntSize(iconDisplaySizeInt, iconDisplaySizeInt),
                                                colorFilter = ColorFilter.tint(onPrimaryContainerColor)
                                            )
                                            
                                            drawContext.canvas.restore()
                                        }
                                    }
                                }
                                
                            } else {
                                // 🌾 収穫期間：各月を3分割して収穫アイコンを配置（旬ごとに1つ）
                                val iconSize = with(density) { 20.dp.toPx() } // 収穫アイコンは20dp
                                val iconResource = R.drawable.harvest_b
                                
                                // アイコン画像の準備
                                val iconBitmap = try {
                                    val bmp = android.graphics.BitmapFactory.decodeResource(context.resources, iconResource)
                                    bmp ?: throw Exception("decode failed")
                                } catch (e: Exception) {
                                    val drawable = context.resources.getDrawable(iconResource, null)
                                    val bmp = Bitmap.createBitmap(iconSize.toInt(), iconSize.toInt(), Bitmap.Config.ARGB_8888)
                                    val c = AndroidCanvas(bmp)
                                    drawable.setBounds(0, 0, iconSize.toInt(), iconSize.toInt())
                                    drawable.draw(c)
                                    bmp
                                }
                                val iconImage = iconBitmap.asImageBitmap()
                                val iconDisplaySizeInt = iconSize.toInt()
                                
                                // 収穫期間の各月を順に処理
                                val startMonthDate = LocalDate.of(startYear, startMonth, 1)
                                val endMonthDate = LocalDate.of(endYear, endMonth, 1)
                                val monthSpan = ChronoUnit.MONTHS.between(startMonthDate, endMonthDate).toInt().coerceAtLeast(0)
                                
                                // 各月の開始位置を計算（カレンダー表示上の相対位置）
                                for (m in 0..monthSpan) {
                                    val monthX = gridLeft + colW * (startMonthIndexInCalendar + m)
                                    
                                    // 月を3分割してそれぞれの中心にアイコンを配置
                                    val positions = listOf(
                                        monthX + colW / 6f,      // 上旬
                                        monthX + colW / 2f,      // 中旬
                                        monthX + colW * 5f / 6f  // 下旬
                                    )
                                    
                                    positions.forEach { iconX ->
                                        // 棒グラフの範囲内（startX から endX）にある旬のみアイコンを表示
                                        if (iconX >= startX && iconX <= endX) {
                                            val iconY = adjustedCenterY - with(density) { 11.dp.toPx() } - with(density) { 4.dp.toPx() }
                                            val iconCenterX = iconX
                                            val iconCenterY = iconY + iconDisplaySizeInt / 2f
                                            
                                            // 🥕 収穫アイコンをぷるぷる揺らして描画
                                            val nativeCanvas = drawContext.canvas.nativeCanvas
                                            nativeCanvas.save()
                                            // 回転中心をアイコンの中央に設定
                                            val pivotX = iconCenterX
                                            val pivotY = iconCenterY
                                            // 回転中心を原点に移動 → 回転 → 元に戻す
                                            nativeCanvas.translate(pivotX, pivotY)
                                            nativeCanvas.rotate(shakeRotation)
                                            nativeCanvas.translate(-pivotX, -pivotY)
                                            
                                            // アイコンを描画（指定サイズで描画）
                                            val srcRect = android.graphics.Rect(0, 0, iconBitmap.width, iconBitmap.height)
                                            val dstRect = android.graphics.RectF(
                                                iconCenterX - iconDisplaySizeInt / 2f,
                                                iconY,
                                                iconCenterX + iconDisplaySizeInt / 2f,
                                                iconY + iconDisplaySizeInt
                                            )
                                            nativeCanvas.drawBitmap(
                                                iconBitmap,
                                                srcRect,
                                                dstRect,
                                                android.graphics.Paint()
                                            )
                                            nativeCanvas.restore()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 🌱 「まいた日」アイコンを最前面に描画（全ての描画処理の後）
            plantingIconInfo?.let { (periodX, plantingY, plantingSize) ->
                plantingBitmap?.let { bitmap ->
                    drawImage(
                        image = bitmap.asImageBitmap(),
                        dstOffset = IntOffset((periodX - plantingSize / 2).toInt(), plantingY.toInt()),
                        dstSize = IntSize(plantingSize.toInt(), plantingSize.toInt()),
                        colorFilter = null // 元の色をそのまま表示、点滅なし
                    )
                }
            }
            
            // 地域が一つの場合は行の区切り線を削除
        }
    }
}
