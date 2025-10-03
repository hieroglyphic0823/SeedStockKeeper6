package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.model.SukesanMessage
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.model.MonthlyStatistics
import com.example.seedstockkeeper6.data.WeeklyWeatherData
import com.example.seedstockkeeper6.data.WeatherData
import com.example.seedstockkeeper6.service.SukesanMessageService
import com.example.seedstockkeeper6.service.StatisticsService
import com.example.seedstockkeeper6.service.WeatherService
import com.example.seedstockkeeper6.ui.components.WeeklyWeatherCard
import com.example.seedstockkeeper6.model.NotificationHistory
import com.example.seedstockkeeper6.service.NotificationHistoryService
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 集計データの表示用データクラス
 */
data class StatisticsData(
    val thisMonthSowingCount: Int,
    val urgentSeedsCount: Int,
    val totalSeeds: Int,
    val expiredSeedsCount: Int,
    val familyDistribution: List<Pair<String, Int>>
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastleScreen(
    navController: NavController,
    viewModel: SeedListViewModel,
    isPreview: Boolean = false,
    farmOwner: String = "水戸黄門",
    farmName: String = "菜園"
) {
    // コンテキストを取得
    val context = LocalContext.current
    
    // 集計サービス
    val statisticsService = remember { StatisticsService() }
    
    // 天気サービス
    val weatherService = remember { WeatherService(context) }
    
    // 集計データの状態
    var monthlyStatistics by remember { mutableStateOf<MonthlyStatistics?>(null) }
    var isLoadingStatistics by remember { mutableStateOf(false) }
    
    // 天気データの状態
    var weeklyWeatherData by remember { mutableStateOf<WeeklyWeatherData?>(null) }
    var isLoadingWeather by remember { mutableStateOf(false) }
    var weatherError by remember { mutableStateOf<String?>(null) }
    
    // 農園位置情報（設定から取得）
    val farmLatitude = 35.6762 // デフォルト値（東京）
    val farmLongitude = 139.6503 // デフォルト値（東京）
    
    // データの取得（プレビュー時は固定データ、実装時はViewModelから）
    val seeds = if (isPreview) {
        // プレビュー時：固定の種データを使用
        listOf(
            SeedPacket(
                id = "preview1",
                productName = "恋むすめ",
                variety = "ニンジン",
                family = "せり科",
                expirationYear = 2026,
                expirationMonth = 10,
                calendar = listOf(
                    CalendarEntry(
                        sowing_start_date = "2025-05-01",
                        sowing_end_date = "2025-05-31",
                        harvest_start_date = "2025-08-01",
                        harvest_end_date = "2025-08-31"
                    )
                )
            ),
            SeedPacket(
                id = "preview2",
                productName = "春菊",
                variety = "中葉春菊",
                family = "きく科",
                expirationYear = 2026,
                expirationMonth = 10,
                calendar = listOf(
                    CalendarEntry(
                        sowing_start_date = "2025-08-20",
                        sowing_end_date = "2025-09-15",
                        harvest_start_date = "2025-10-01",
                        harvest_end_date = "2025-10-31"
                    )
                )
            )
        )
    } else {
        // 実装時：ViewModelからデータを取得
        viewModel.seeds.value
    }
    
    // 農園名（設定から取得、プレビュー時は固定値）
    val farmName = if (isPreview) "田中さんの農園" else "農園名" // TODO: 設定から取得
    
    // 今月の日付
    val today = if (isPreview) {
        LocalDate.of(2025, 5, 1)
    } else {
        LocalDate.now()
    }
    val currentMonth = today.monthValue
    val currentYear = today.year
    
    // 集計データの取得（プレビュー時は固定データ、実装時は集計サービスから）
    val statisticsData = if (isPreview) {
        // プレビュー時：固定の集計データ
        StatisticsData(
            thisMonthSowingCount = 1,
            urgentSeedsCount = 0,
            totalSeeds = 2,
            expiredSeedsCount = 0,
            familyDistribution = listOf(Pair("せり科", 1), Pair("きく科", 1))
        )
    } else {
        // 実装時：集計データを取得
        LaunchedEffect(seeds.size) { // seedsのサイズが変更された時に再計算
            if (!isLoadingStatistics) {
                isLoadingStatistics = true
                try {
                    android.util.Log.d("CastleScreen", "=== 集計データ取得開始 ===")
                    android.util.Log.d("CastleScreen", "seeds.size: ${seeds.size}")
                    
                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    val uid = auth.currentUser?.uid
                    android.util.Log.d("CastleScreen", "uid: $uid")
                    
                    if (uid != null) {
                        // まず現在の集計データを取得
                        android.util.Log.d("CastleScreen", "現在の集計データ取得開始")
                        monthlyStatistics = statisticsService.getCurrentMonthStatistics(uid)
                        android.util.Log.d("CastleScreen", "現在の集計データ: ${monthlyStatistics?.totalSeeds}")
                        
                        // 集計データが古い場合、または種データが変更された場合は再計算
                        val needsRecalculation = monthlyStatistics == null || 
                            !monthlyStatistics!!.isValid() || 
                            monthlyStatistics!!.totalSeeds != seeds.size
                        
                        android.util.Log.d("CastleScreen", "再計算必要: $needsRecalculation")
                        android.util.Log.d("CastleScreen", "monthlyStatistics == null: ${monthlyStatistics == null}")
                        android.util.Log.d("CastleScreen", "!isValid(): ${monthlyStatistics?.let { !it.isValid() }}")
                        android.util.Log.d("CastleScreen", "totalSeeds != seeds.size: ${monthlyStatistics?.totalSeeds != seeds.size}")
                        
                        if (needsRecalculation) {
                            // 種データが0件の場合は集計をスキップして既存データを使用
                            if (seeds.isEmpty()) {
                                android.util.Log.w("CastleScreen", "種データが0件のため集計をスキップ")
                                android.util.Log.w("CastleScreen", "既存の集計データを使用: totalSeeds=${monthlyStatistics?.totalSeeds}")
                                
                                // 既存の集計データが0件の場合は修正を試行
                                if (monthlyStatistics?.totalSeeds == 0) {
                                    android.util.Log.d("CastleScreen", "集計データ修正を試行")
                                    try {
                                        val fixResult = statisticsService.fixStatisticsData(uid)
                                        if (fixResult.success) {
                                            monthlyStatistics = fixResult.statistics
                                            android.util.Log.d("CastleScreen", "集計データ修正完了: totalSeeds=${fixResult.statistics?.totalSeeds}")
                                        } else {
                                            android.util.Log.w("CastleScreen", "集計データ修正失敗: ${fixResult.message}")
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("CastleScreen", "集計データ修正エラー", e)
                                    }
                                }
                            } else {
                                android.util.Log.d("CastleScreen", "集計データを再計算: seeds.size=${seeds.size}")
                                val result = statisticsService.updateStatisticsOnSeedChange(uid, seeds)
                                if (result.success) {
                                    monthlyStatistics = result.statistics
                                    android.util.Log.d("CastleScreen", "=== 集計データ更新完了 ===")
                                    android.util.Log.d("CastleScreen", "totalSeeds: ${result.statistics?.totalSeeds}")
                                    android.util.Log.d("CastleScreen", "validSeeds: ${result.statistics?.validSeedsCount}")
                                    android.util.Log.d("CastleScreen", "thisMonthSowing: ${result.statistics?.thisMonthSowingCount}")
                                } else {
                                    android.util.Log.w("CastleScreen", "集計データ更新失敗: ${result.message}")
                                }
                            }
                        } else {
                            android.util.Log.d("CastleScreen", "集計データは最新のため再計算をスキップ")
                        }
                    } else {
                        android.util.Log.w("CastleScreen", "uidがnullのため集計データ取得をスキップ")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CastleScreen", "=== 集計データ取得エラー ===", e)
                    android.util.Log.e("CastleScreen", "エラー詳細: ${e.message}")
                } finally {
                    isLoadingStatistics = false
                    android.util.Log.d("CastleScreen", "集計データ取得処理完了")
                }
            }
        }
        
        // 天気データの取得
        LaunchedEffect(farmLatitude, farmLongitude, isPreview) {
            if (!isPreview) {
                try {
                    isLoadingWeather = true
                    weatherError = null
                    android.util.Log.d("CastleScreen", "天気予報取得開始: lat=$farmLatitude, lon=$farmLongitude")
                    weeklyWeatherData = weatherService.getWeeklyWeather(farmLatitude, farmLongitude)
                    android.util.Log.d("CastleScreen", "天気予報取得完了")
                } catch (e: Exception) {
                    android.util.Log.e("CastleScreen", "天気データ取得エラー", e)
                    weatherError = "天気予報の取得に失敗しました: ${e.message}"
                } finally {
                    isLoadingWeather = false
                }
            }
        }
        
        // 集計データから値を取得、データがない場合は従来の計算を使用
        if (monthlyStatistics != null) {
            StatisticsData(
                thisMonthSowingCount = monthlyStatistics!!.thisMonthSowingCount,
                urgentSeedsCount = monthlyStatistics!!.urgentSeedsCount,
                totalSeeds = monthlyStatistics!!.totalSeeds,
                expiredSeedsCount = monthlyStatistics!!.totalSeeds - monthlyStatistics!!.validSeedsCount,
                familyDistribution = monthlyStatistics!!.getTopFamilies(3)
            )
        } else {
            // フォールバック：従来の計算処理
            val thisMonthSowingSeeds = seeds.filter { seed ->
                seed.calendar?.any { entry ->
                    val sowingStartMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_start_date)
                    val sowingStartYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_start_date)
                    sowingStartMonth == currentMonth && sowingStartYear == currentYear
                } ?: false
            }
            
            val urgentSeeds = seeds.filter { seed ->
                seed.calendar?.any { entry ->
                    val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
                    val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
                    val sowingEndStage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDateToStage(entry.sowing_end_date)
                    // 今月内で播種期間が終了する種（上旬、中旬、下旬すべて対象）
                    sowingEndMonth == currentMonth && sowingEndYear == currentYear
                } ?: false
            }
            
            val currentDate = LocalDate.now()
            val validSeeds = seeds.filter { seed ->
                val expirationDate = LocalDate.of(seed.expirationYear, seed.expirationMonth, 1)
                currentDate.isBefore(expirationDate.plusMonths(1))
            }
            val expiredSeeds = seeds.filter { seed ->
                val expirationDate = LocalDate.of(seed.expirationYear, seed.expirationMonth, 1)
                currentDate.isAfter(expirationDate.plusMonths(1))
            }
            val familyDist = validSeeds.groupBy { it.family }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(3)
            
            StatisticsData(
                thisMonthSowingCount = thisMonthSowingSeeds.size,
                urgentSeedsCount = urgentSeeds.size,
                totalSeeds = seeds.size,
                expiredSeedsCount = expiredSeeds.size,
                familyDistribution = familyDist
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // ヘッダーは削除（AppTopBarのみ残す）
        
        // 週間天気予報
        WeeklyWeatherCard(
            weeklyWeatherData = weeklyWeatherData,
            isLoading = isLoadingWeather,
            error = weatherError
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // すけさんからのメッセージ
        SukesanMessageCard(
            seeds = seeds,
            currentMonth = currentMonth,
            currentYear = currentYear,
            isPreview = isPreview,
            farmOwner = farmOwner,
            farmName = farmName
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 今月の播種状況
        SowingSummaryCards(
            thisMonthSowingCount = statisticsData.thisMonthSowingCount,
            urgentSeedsCount = statisticsData.urgentSeedsCount,
            navController = navController
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 統計ウィジェット
        StatisticsWidgets(
            totalSeeds = statisticsData.totalSeeds,
            expiredSeedsCount = statisticsData.expiredSeedsCount,
            familyDistribution = statisticsData.familyDistribution,
            navController = navController
        )
        
    }
}

// CastleHeaderは削除（AppTopBarのみ残す）

@Composable
fun SukesanMessageCard(
    seeds: List<SeedPacket>,
    currentMonth: Int,
    currentYear: Int,
    isPreview: Boolean = false,
    farmOwner: String = "水戸黄門",
    farmName: String = "菜園"
) {
    var latestNotification by remember { mutableStateOf<NotificationHistory?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // メッセージの取得
    LaunchedEffect(seeds, currentMonth, currentYear, isPreview, farmOwner, farmName) {
        android.util.Log.d("CastleScreen", "=== 助さんメッセージ取得開始 ===")
        android.util.Log.d("CastleScreen", "プレビューモード: $isPreview")
        android.util.Log.d("CastleScreen", "農園主: $farmOwner, 農園名: $farmName")
        android.util.Log.d("CastleScreen", "現在の月: $currentMonth, 年: $currentYear")
        android.util.Log.d("CastleScreen", "登録種子数: ${seeds.size}")
        
        if (isPreview) {
            android.util.Log.d("CastleScreen", "プレビュー時は固定メッセージを生成")
            // プレビュー時は固定メッセージ
            latestNotification = NotificationHistory(
                id = "preview",
                title = "弥生の風に乗せて――春の種まきの候、菜園より",
                content = "お銀、菜園の弥生は1種類の種の播種時期です。恋むすめ（ニンジン）の栽培を楽しんでくださいね。",
                summary = "まき時：恋むすめ（ニンジン）\n終了間近：春菊（中葉春菊）",
                sentAt = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "T12:00:00.000Z",
                userId = "preview",
                type = com.example.seedstockkeeper6.model.NotificationType.MONTHLY
            )
            android.util.Log.d("CastleScreen", "プレビュー通知設定完了")
            isLoading = false
        } else {
            android.util.Log.d("CastleScreen", "実装時は通知履歴から最新を取得")
            try {
                val historyService = NotificationHistoryService()
                val histories = historyService.getUserNotificationHistory(limit = 1)
                if (histories.isNotEmpty()) {
                    latestNotification = histories.first()
                    android.util.Log.d("CastleScreen", "最新通知取得成功: ${latestNotification?.title}")
                } else {
                    android.util.Log.w("CastleScreen", "通知履歴が空です")
                    latestNotification = null
                }
            } catch (e: Exception) {
                android.util.Log.e("CastleScreen", "Error getting latest notification: ${e.message}")
                latestNotification = null
            }
            isLoading = false
        }
        android.util.Log.d("CastleScreen", "=== 助さんメッセージ取得完了 ===")
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // メッセージ部分の高さを取得するためのBox
            var messageHeight by remember { mutableStateOf(0.dp) }
            val density = LocalDensity.current
            
            // 吹き出し部分
            Card(
                modifier = Modifier
                    .weight(1f)
                    .onSizeChanged { size ->
                        messageHeight = with(density) { size.height.toDp() }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp) // 吹き出しの形（右下に変更）
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // 通知内容
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "通知を読み込み中...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else if (latestNotification != null) {
                        val notification = latestNotification!!
                        Column {
                            // 通知タイトル（1行まで）
                            Text(
                                text = notification.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 簡略表示（まき間近、今月まき時）
                            if (notification.summary.isNotEmpty()) {
                                // summaryを解析して「まき時：」「終了間近：」の形式で表示
                                val summaryLines = notification.summary.split("\n")
                                summaryLines.forEach { line ->
                                    if (line.isNotEmpty()) {
                                        val displayText = when {
                                            line.contains("今月まき時") -> line.replace("🌱 今月まき時：", "🌱 まき時：")
                                            line.contains("まき時終了間近") -> line.replace("⚠️ まき時終了間近：", "⚠️ 終了間近：")
                                            else -> line
                                        }
                                        Text(
                                            text = displayText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            } else {
                                // summaryがない場合はcontentの最初の部分を表示
                                Text(
                                    text = notification.content.take(100) + if (notification.content.length > 100) "..." else "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "通知がありません",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
                        )
                    }
                }
            }
            
            // すけさんアイコン（右側に移動）
            // CoilのImageLoaderを設定（GIFサポート付き）
            val context = LocalContext.current
            val imageLoader = remember {
                ImageLoader.Builder(context)
                    .components {
                        add(ImageDecoderDecoder.Factory()) // GIFをサポートするために必要
                    }
                    .build()
            }
            
            AsyncImage(
                model = R.drawable.suke_up_c,
                contentDescription = "すけさん",
                imageLoader = imageLoader,
                modifier = Modifier.size(
                    width = messageHeight,
                    height = messageHeight
                )
            )
        }
    }
}

@Composable
fun SowingSummaryCards(
    thisMonthSowingCount: Int,
    urgentSeedsCount: Int,
    navController: NavController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.grain),
                contentDescription = "種",
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "今月の種",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 播種予定種子数
            val onThisMonthClick = {
                // 種リスト画面に遷移し、「今月まける」チェックボックスをオンにする
                navController.navigate("list?filter=thisMonth")
            }
            
            SummaryCardWithImageIcon(
                iconResource = R.drawable.seed,
                title = "まきどき",
                value = "$thisMonthSowingCount",
                subtitle = "",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
                onClick = onThisMonthClick
            )
            
            // まき時終了間近の種子数
            val onUrgentClick = {
                // 種リスト画面に遷移し、「終了間近」チェックボックスをオンにする
                navController.navigate("list?filter=urgent")
            }
            
            SummaryCardWithImageIcon(
                iconResource = R.drawable.diamond_exclamation,
                title = "終了間近",
                value = "$urgentSeedsCount",
                subtitle = "",
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                onClick = onUrgentClick
            )
        }
    }
}

@Composable
fun StatisticsWidgets(
    totalSeeds: Int,
    expiredSeedsCount: Int,
    familyDistribution: List<Pair<String, Int>>,
    navController: NavController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.chart),
                contentDescription = "統計",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "統計",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 左側：登録総数と期限切れを縦に並べる
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 登録種子総数
                SummaryCardWithoutIcon(
                    title = "登録総数",
                    value = "$totalSeeds",
                    subtitle = "",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 期限切れ種子数
                val onExpiredClick = {
                    // 種リスト画面に遷移し、「期限切れ」チェックボックスをオンにする
                    navController.navigate("list?filter=expired")
                }
                
                SummaryCardWithoutIcon(
                    title = "期限切れ",
                    value = "$expiredSeedsCount",
                    subtitle = "",
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onExpiredClick
                )
            }
            
            // 右側：科別分布（縦長表示）
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "科別分布",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 円グラフ表示
                    if (familyDistribution.isNotEmpty()) {
                        PieChart(
                            data = familyDistribution,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    } else {
                        Text(
                            text = "有効期限内の種がありません",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCardWithEmojiIcon(
    emoji: String,
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 上段: アイコンとタイトル
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 24.sp
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 下段: 値
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
                
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCardWithImageIcon(
    iconResource: Int,
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.clickable { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 上段: アイコンとタイトル
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = iconResource),
                    contentDescription = title,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 下段: 値
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
                
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 上段: アイコンとタイトル
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 下段: 値
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
                
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// すけさんからのメッセージ生成関数
private fun generateSukesanMessage(
    seeds: List<SeedPacket>,
    currentMonth: Int,
    currentYear: Int,
    isPreview: Boolean,
    farmOwner: String = "水戸黄門",
    farmName: String = "菜園"
): String {
    android.util.Log.d("CastleScreen", "--- プレビューメッセージ生成開始 ---")
    android.util.Log.d("CastleScreen", "農園主: $farmOwner, 農園名: $farmName")
    android.util.Log.d("CastleScreen", "現在の月: $currentMonth, 年: $currentYear")
    android.util.Log.d("CastleScreen", "登録種子数: ${seeds.size}")
    
    val monthName = when (currentMonth) {
        1 -> "1月"
        2 -> "2月"
        3 -> "3月"
        4 -> "4月"
        5 -> "5月"
        6 -> "6月"
        7 -> "7月"
        8 -> "8月"
        9 -> "9月"
        10 -> "10月"
        11 -> "11月"
        12 -> "12月"
        else -> "${currentMonth}月"
    }
    
    val thisMonthSowingSeeds = seeds.filter { seed ->
        seed.calendar?.any { entry ->
            val sowingStartMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_start_date)
            val sowingStartYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_start_date)
            sowingStartMonth == currentMonth && sowingStartYear == currentYear
        } ?: false
    }
    
    val urgentSeeds = seeds.filter { seed ->
        seed.calendar?.any { entry ->
            val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
            val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
            val sowingEndStage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDateToStage(entry.sowing_end_date)
            // 今月内で播種期間が終了する種（上旬、中旬、下旬すべて対象）
            sowingEndMonth == currentMonth && sowingEndYear == currentYear
        } ?: false
    }
    
    android.util.Log.d("CastleScreen", "今日のまきどきの種子数: ${thisMonthSowingSeeds.size}")
    android.util.Log.d("CastleScreen", "まき時終了間近の種子数: ${urgentSeeds.size}")
    
    if (thisMonthSowingSeeds.isNotEmpty()) {
        android.util.Log.d("CastleScreen", "今日のまきどきの種子: ${thisMonthSowingSeeds.map { "${it.productName}（${it.variety}）" }}")
    }
    if (urgentSeeds.isNotEmpty()) {
        android.util.Log.d("CastleScreen", "まき時終了間近の種子: ${urgentSeeds.map { "${it.productName}（${it.variety}）" }}")
    }
    
    return when {
        urgentSeeds.isNotEmpty() -> {
            val seedNames = urgentSeeds.take(3).joinToString("、") { seed ->
                "${seed.productName}${if (seed.variety.isNotEmpty()) "（${seed.variety}）" else ""}"
            }
            when (farmOwner) {
                "水戸黄門" -> "黄門様、${farmName}の${monthName}は${urgentSeeds.size}種類の種のまき時が終了間近でございます。${seedNames}の播種を早急に完了させましょう。"
                "お銀" -> "お銀、${farmName}の${monthName}は${urgentSeeds.size}種類の種のまき時が終了間近です。${seedNames}の播種を急いで完了させてくださいね。"
                "八兵衛" -> "おい八、${farmName}の${monthName}は${urgentSeeds.size}種類の種のまき時が終了間近だぞ！${seedNames}の播種を急いでやれ！"
                else -> "${farmOwner}、${farmName}の${monthName}は${urgentSeeds.size}種類の種のまき時が終了間近です。${seedNames}の播種を早急に完了させましょう。"
            }
        }
        thisMonthSowingSeeds.isNotEmpty() -> {
            val seedNames = thisMonthSowingSeeds.take(3).joinToString("、") { seed ->
                "${seed.productName}${if (seed.variety.isNotEmpty()) "（${seed.variety}）" else ""}"
            }
            when (farmOwner) {
                "水戸黄門" -> "黄門様、${farmName}の${monthName}は${thisMonthSowingSeeds.size}種類の種の播種時期でございます。${seedNames}の栽培を計画的に進めましょう。"
                "お銀" -> "お銀、${farmName}の${monthName}は${thisMonthSowingSeeds.size}種類の種の播種時期です。${seedNames}の栽培を楽しんでくださいね。"
                "八兵衛" -> "おい八、${farmName}の${monthName}は${thisMonthSowingSeeds.size}種類の種の播種時期だぞ！${seedNames}の栽培を頑張れ！"
                else -> "${farmOwner}、${farmName}の${monthName}は${thisMonthSowingSeeds.size}種類の種の播種時期です。${seedNames}の栽培を計画的に進めましょう。"
            }
        }
        seeds.isEmpty() -> {
            when (farmOwner) {
                "水戸黄門" -> "黄門様、${farmName}へようこそ。種子を登録して、栽培計画を立てましょう。"
                "お銀" -> "お銀、${farmName}へようこそ。種子を登録して、栽培計画を立ててくださいね。"
                "八兵衛" -> "おい八、${farmName}へようこそ！種子を登録して、栽培計画を立てるぞ！"
                else -> "${farmOwner}、${farmName}へようこそ。種子を登録して、栽培計画を立てましょう。"
            }
        }
        else -> {
            when (farmOwner) {
                "水戸黄門" -> "黄門様、${farmName}の${monthName}は播種時期の種子はございませんが、他の管理作業に取り組む良い機会でございます。"
                "お銀" -> "お銀、${farmName}の${monthName}は播種時期の種子はありませんが、他の管理作業に取り組む良い機会です。"
                "八兵衛" -> "おい八、${farmName}の${monthName}は播種時期の種子はないが、他の管理作業に取り組む良い機会だぞ！"
                else -> "${farmOwner}、${farmName}の${monthName}は播種時期の種子はありませんが、他の管理作業に取り組む良い機会です。"
            }
        }
    }
    
    android.util.Log.d("CastleScreen", "--- プレビューメッセージ生成完了 ---")
}

@Composable
fun SummaryCardWithoutIcon(
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.clickable { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // タイトル
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 値
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )

                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "お城画面 - お銀")
@Composable
fun CastleScreenPreviewOgin() {
    MaterialTheme {
        CastleScreen(
            navController = rememberNavController(),
            viewModel = viewModel(),
            isPreview = true,
            farmOwner = "お銀",
            farmName = "田中さんの農園"
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "お城画面 - 水戸黄門")
@Composable
fun CastleScreenPreviewKomon() {
    MaterialTheme {
        CastleScreen(
            navController = rememberNavController(),
            viewModel = viewModel(),
            isPreview = true,
            farmOwner = "水戸黄門",
            farmName = "菜園"
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "お城画面 - 八兵衛")
@Composable
fun CastleScreenPreviewHachibei() {
    MaterialTheme {
        CastleScreen(
            navController = rememberNavController(),
            viewModel = viewModel(),
            isPreview = true,
            farmOwner = "八兵衛",
            farmName = "八兵衛の畑"
        )
    }
}

@Composable
fun PieChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.second }
    if (total == 0) return
    
    val colors = listOf(
        Color(0xFF2196F3),  // 鮮やかな青
        Color(0xFF4CAF50),  // 鮮やかな緑
        Color(0xFFFF9800),  // 鮮やかなオレンジ
        Color(0xFF9C27B0),  // 鮮やかな紫
        Color(0xFFE91E63)   // 鮮やかなピンク
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 円グラフ
        Canvas(
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = minOf(canvasWidth, canvasHeight) / 2f
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f
            
            var startAngle = -90f // 12時の位置から開始
            
            data.forEachIndexed { index, (_, count) ->
                val sweepAngle = (count.toFloat() / total) * 360f
                val color = colors[index % colors.size]
                
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        centerX - radius,
                        centerY - radius
                    ),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )
                
                startAngle += sweepAngle
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 凡例（円グラフの下に表示）
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            data.take(3).forEachIndexed { index, (family, count) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                colors[index % colors.size],
                                CircleShape
                            )
                    )
                    Text(
                        text = "$family ($count)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
