package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import com.example.seedstockkeeper6.model.NotificationData
import com.example.seedstockkeeper6.model.SeedInfo
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
    
    // 設定ViewModelを取得
    val settingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.seedstockkeeper6.viewmodel.SettingsViewModel>()
    
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
    val farmLatitude = if (isPreview) 35.6762 else settingsViewModel.farmLatitude // プレビュー時はデフォルト値、実装時は設定から取得
    val farmLongitude = if (isPreview) 139.6503 else settingsViewModel.farmLongitude // プレビュー時はデフォルト値、実装時は設定から取得
    
    // データの取得（プレビュー時は固定データ、実装時はViewModelから）
    val seeds = if (isPreview) {
        // プレビュー時：固定の種データを使用
        android.util.Log.d("CastleScreen", "プレビュー時: 固定データを使用")
        listOf(
            SeedPacket(
                id = "preview1",
                productName = "食べきりミニ大根",
                variety = "ころっ娘",
                family = "アブラナ科",
                expirationYear = 2026,
                expirationMonth = 10,
                calendar = listOf(
                    CalendarEntry(
                        sowing_start_date = "2025-10-01",
                        sowing_end_date = "2025-10-31",
                        harvest_start_date = "2025-12-01",
                        harvest_end_date = "2025-12-31"
                    )
                )
            ),
            SeedPacket(
                id = "preview2",
                productName = "一寸そら豆",
                variety = "ソラマメ",
                family = "マメ科",
                expirationYear = 2026,
                expirationMonth = 10,
                calendar = listOf(
                    CalendarEntry(
                        sowing_start_date = "2025-10-01",
                        sowing_end_date = "2025-10-31",
                        harvest_start_date = "2026-05-01",
                        harvest_end_date = "2026-05-31"
                    )
                )
            ),
            SeedPacket(
                id = "preview3",
                productName = "サラダタマネギ",
                variety = "ゆめたま",
                family = "ユリ科",
                expirationYear = 2026,
                expirationMonth = 10,
                calendar = listOf(
                    CalendarEntry(
                        sowing_start_date = "2025-09-01",
                        sowing_end_date = "2025-10-31",
                        harvest_start_date = "2026-06-01",
                        harvest_end_date = "2026-06-30"
                    )
                )
            )
        )
    } else {
        // 実装時：ViewModelからデータを取得
        android.util.Log.d("CastleScreen", "実装時: ViewModelからデータを取得")
        viewModel.seeds.value
    }
    
    android.util.Log.d("CastleScreen", "取得した種子数: ${seeds.size}")
    seeds.forEach { seed ->
        android.util.Log.d("CastleScreen", "種: ${seed.productName}, カレンダー: ${seed.calendar}")
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
            if (!isPreview && farmLatitude != 0.0 && farmLongitude != 0.0) {
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
            } else if (!isPreview && (farmLatitude == 0.0 || farmLongitude == 0.0)) {
                android.util.Log.d("CastleScreen", "農園設定の緯度経度が未設定のため、天気予報取得をスキップ")
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
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 週間天気予報
        item {
            WeeklyWeatherCard(
                weeklyWeatherData = weeklyWeatherData,
                isLoading = isLoadingWeather,
                error = weatherError
            )
        }
        
        // すけさんからのメッセージ
        item {
            SukesanMessageCard(
                seeds = seeds,
                currentMonth = currentMonth,
                currentYear = currentYear,
                isPreview = isPreview,
                farmOwner = farmOwner,
                farmName = farmName,
                farmLatitude = farmLatitude,
                farmLongitude = farmLongitude
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // 今月の播種状況
        item {
            SowingSummaryCards(
                thisMonthSowingCount = statisticsData.thisMonthSowingCount,
                urgentSeedsCount = statisticsData.urgentSeedsCount,
                navController = navController
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // 統計ウィジェット
        item {
            StatisticsWidgets(
                totalSeeds = statisticsData.totalSeeds,
                expiredSeedsCount = statisticsData.expiredSeedsCount,
                familyDistribution = statisticsData.familyDistribution,
                navController = navController
            )
        }
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
    farmName: String = "菜園",
    farmLatitude: Double = 35.6762,
    farmLongitude: Double = 139.6503
) {
    var latestNotification by remember { mutableStateOf<NotificationData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    // メッセージの取得
    LaunchedEffect(seeds, currentMonth, currentYear, isPreview, farmOwner, farmName, farmLatitude, farmLongitude) {
        android.util.Log.d("CastleScreen", "=== 助さんメッセージ取得開始 ===")
        android.util.Log.d("CastleScreen", "プレビューモード: $isPreview")
        android.util.Log.d("CastleScreen", "農園主: $farmOwner, 農園名: $farmName")
        android.util.Log.d("CastleScreen", "現在の月: $currentMonth, 年: $currentYear")
        android.util.Log.d("CastleScreen", "登録種子数: ${seeds.size}")

        if (isPreview) {
            android.util.Log.d("CastleScreen", "プレビュー時は固定メッセージを生成")
            // プレビュー時は固定メッセージ
            latestNotification = NotificationData(
                id = "preview",
                title = "弥生の風に乗せて――春の種まきの候、菜園より",
                summary = "お銀、菜園の弥生は1種類の種の播種時期です。恋むすめ（ニンジン）の栽培を楽しんでくださいね。",
                farmOwner = farmOwner,
                region = "温暖地",
                prefecture = "東京都",
                month = currentMonth,
                thisMonthSeeds = listOf(
                    SeedInfo(
                        name = "恋むすめ",
                        variety = "ニンジン",
                        description = "春の種まきに最適な品種です"
                    )
                ),
                endingSoonSeeds = listOf(
                    SeedInfo(
                        name = "春菊",
                        variety = "中葉春菊",
                        description = "まき時終了間近です"
                    )
                ),
                sentAt = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "T12:00:00.000Z",
                userId = "preview",
                seedCount = 1
            )
            android.util.Log.d("CastleScreen", "プレビュー通知設定完了")
            isLoading = false
        } else {
            android.util.Log.d("CastleScreen", "実装時は通知履歴から最新を取得")
            try {
                val historyService = NotificationHistoryService()
                val notificationDataList = historyService.getUserNotificationData(limit = 1)
                if (notificationDataList.isNotEmpty()) {
                    latestNotification = notificationDataList.first()
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    }
                    .clickable { 
                        if (latestNotification != null) {
                            showNotificationDialog = true
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp) // 吹き出しの形（右下の角を小さく）
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
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
                        
                        // 通知の内容からまきどきの種と期限切れ間近の種情報を抽出
                        val (thisMonthSowingSeeds, urgentSeeds) = extractSeedInfoFromNotificationData(notification, seeds)
                        
                        android.util.Log.d("CastleScreen", "通知から抽出したまきどきの種子数: ${thisMonthSowingSeeds.size}")
                        android.util.Log.d("CastleScreen", "通知から抽出した期限切れ間近の種子数: ${urgentSeeds.size}")
                        
                        Column {
                            // 通知タイトル（1行）
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.yabumi_red),
                                    contentDescription = "矢文",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = notification.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // まきどきの種情報
                            if (thisMonthSowingSeeds.isNotEmpty()) {
                                val seedNames = thisMonthSowingSeeds.take(3).joinToString("、") { it.productName }
                                val displayText = if (thisMonthSowingSeeds.size > 3) {
                                    "$seedNames 他${thisMonthSowingSeeds.size - 3}種類"
                                } else {
                                    seedNames
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = "🌱 まきどき: $displayText",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Visible
                                    )
                                }
                            } else {
                                Text(
                                    text = "🌱 まきどき: 該当なし",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // まき時終了間近の種情報
                            if (urgentSeeds.isNotEmpty()) {
                                val seedNames = urgentSeeds.take(3).joinToString("、") { it.productName }
                                val displayText = if (urgentSeeds.size > 3) {
                                    "$seedNames 他${urgentSeeds.size - 3}種類"
                                } else {
                                    seedNames
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.crisis),
                                        contentDescription = "危機",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "終了間近: $displayText",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Visible
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.crisis),
                                        contentDescription = "危機",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "終了間近: 該当なし",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black
                                    )
                                }
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
                    width = 60.dp,
                    height = messageHeight
                )
            )
        }
    }
    
    // 通知詳細ダイアログ
    if (showNotificationDialog && latestNotification != null) {
        NotificationDetailDialog(
            notification = latestNotification!!,
            onDismiss = { showNotificationDialog = false }
        )
    }
}

@Composable
fun SowingSummaryCards(
    thisMonthSowingCount: Int,
    urgentSeedsCount: Int,
    navController: NavController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                iconResource = R.drawable.crisis,
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            fontWeight = FontWeight.Normal,
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
            // 上段: タイトルのみ
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = contentColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 下段: アイコンと値
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = iconResource),
                    contentDescription = title,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
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
                fontWeight = FontWeight.Normal,
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

/**
 * 通知の内容からまきどきの種と期限切れ間近の種情報を抽出
 */
private fun extractSeedInfoFromNotificationData(notificationData: NotificationData, allSeeds: List<SeedPacket>): Pair<List<SeedPacket>, List<SeedPacket>> {
    val thisMonthSowingSeeds = mutableListOf<SeedPacket>()
    val urgentSeeds = mutableListOf<SeedPacket>()
    
    android.util.Log.d("CastleScreen", "通知データから抽出開始")
    
    // 今月まきどきの種を抽出
    notificationData.thisMonthSeeds.forEach { seedInfo ->
        val matchingSeed = allSeeds.find { it.productName == seedInfo.name }
        if (matchingSeed != null) {
            thisMonthSowingSeeds.add(matchingSeed)
        }
    }
    
    // 終了間近の種を抽出
    notificationData.endingSoonSeeds.forEach { seedInfo ->
        val matchingSeed = allSeeds.find { it.productName == seedInfo.name }
        if (matchingSeed != null) {
            urgentSeeds.add(matchingSeed)
        }
    }
    
    return thisMonthSowingSeeds to urgentSeeds
}

private fun extractSeedInfoFromNotification(notificationContent: String, allSeeds: List<SeedPacket>): Pair<List<SeedPacket>, List<SeedPacket>> {
    val thisMonthSowingSeeds = mutableListOf<SeedPacket>()
    val urgentSeeds = mutableListOf<SeedPacket>()
    
    android.util.Log.d("CastleScreen", "通知内容全体: $notificationContent")
    
    // まずは機械可読なJSONブロックを優先して抽出
    parseSeedsFromJsonBlock(notificationContent)?.let { (tm, urgent) ->
        return tm to urgent
    }

    // 通知の内容から種の名前を抽出（表記揺れに強い緩和パターン）
    val thisMonthPattern = Regex("🌱\\s+(?:\\*\\*)?今月まきどきの種:?\\s*(?:\\*\\*)?")
    val urgentPattern = Regex("⚠️\\s+(?:\\*\\*)?まき時終了間近:?\\s*(?:\\*\\*)?")
    
    android.util.Log.d("CastleScreen", "まきどきのパターンマッチ: ${thisMonthPattern.find(notificationContent) != null}")
    android.util.Log.d("CastleScreen", "期限切れ間近のパターンマッチ: ${urgentPattern.find(notificationContent) != null}")
    
    // まきどきの種を抽出
    val thisMonthMatch = thisMonthPattern.find(notificationContent)
    if (thisMonthMatch != null) {
        val startIndex = thisMonthMatch.range.last + 1
        // 次のセクション（⚠️ or 🌟）までを取得
        val nextIdx1 = notificationContent.indexOf("⚠️", startIndex)
        val nextIdx2 = notificationContent.indexOf("🌟", startIndex)
        val endIndex = listOf(nextIdx1, nextIdx2).filter { it >= 0 }.minOrNull() ?: notificationContent.length
        val thisMonthText = notificationContent.substring(startIndex, endIndex).trim()
        
        android.util.Log.d("CastleScreen", "まきどきのテキスト: $thisMonthText")
        
        if (thisMonthText != "該当なし") {
            // 種の名前を抽出（『種名』の形式）
            val seedNamePattern = "『([^』]+)』".toRegex()
            val matches = seedNamePattern.findAll(thisMonthText)
            android.util.Log.d("CastleScreen", "まきどきの正規表現マッチ数: ${matches.count()}")
            matches.forEach { match ->
                val seedName = match.groupValues[1].trim()
                android.util.Log.d("CastleScreen", "抽出した種名: $seedName")
                
                // （）と（）内の文字を除去
                val cleanSeedName = seedName.replace(Regex("\\([^)]*\\)"), "").trim()
                android.util.Log.d("CastleScreen", "クリーンな種名: $cleanSeedName")
                
                // 通知から抽出した種名をそのまま使用（実際の種データとの照合は不要）
                val extractedSeed = SeedPacket(
                    id = "extracted_${System.currentTimeMillis()}",
                    productName = cleanSeedName,
                    variety = "",
                    family = "",
                    expirationYear = 0,
                    expirationMonth = 0,
                    calendar = emptyList()
                )
                thisMonthSowingSeeds.add(extractedSeed)
                android.util.Log.d("CastleScreen", "抽出した種を追加: $cleanSeedName")
            }
        }
    }
    
    // 期限切れ間近の種を抽出
    val urgentMatch = urgentPattern.find(notificationContent)
    if (urgentMatch != null) {
        val startIndex = urgentMatch.range.last + 1
        // 次のセクション（🌟 今月のおすすめ種:）までを取得
        val nextSectionIndex = notificationContent.indexOf("🌟", startIndex)
        val endIndex = if (nextSectionIndex == -1) notificationContent.length else nextSectionIndex
        val urgentText = notificationContent.substring(startIndex, endIndex).trim()
        
        android.util.Log.d("CastleScreen", "期限切れ間近のテキスト: $urgentText")
        
        if (urgentText != "該当なし") {
            // 種の名前を抽出（『種名』の形式）
            val seedNamePattern = "『([^』]+)』".toRegex()
            val matches = seedNamePattern.findAll(urgentText)
            android.util.Log.d("CastleScreen", "期限切れ間近の正規表現マッチ数: ${matches.count()}")
            matches.forEach { match ->
                val seedName = match.groupValues[1].trim()
                android.util.Log.d("CastleScreen", "抽出した期限切れ間近の種名: $seedName")
                
                // （）と（）内の文字を除去
                val cleanSeedName = seedName.replace(Regex("\\([^)]*\\)"), "").trim()
                android.util.Log.d("CastleScreen", "クリーンな期限切れ間近の種名: $cleanSeedName")
                
                // 通知から抽出した種名をそのまま使用（実際の種データとの照合は不要）
                val extractedSeed = SeedPacket(
                    id = "extracted_${System.currentTimeMillis()}",
                    productName = cleanSeedName,
                    variety = "",
                    family = "",
                    expirationYear = 0,
                    expirationMonth = 0,
                    calendar = emptyList()
                )
                urgentSeeds.add(extractedSeed)
                android.util.Log.d("CastleScreen", "抽出した期限切れ間近の種を追加: $cleanSeedName")
            }
        }
    }
    
    android.util.Log.d("CastleScreen", "通知内容から抽出: まきどき=${thisMonthSowingSeeds.map { it.productName }}, 期限切れ間近=${urgentSeeds.map { it.productName }}")
    
    return Pair(thisMonthSowingSeeds, urgentSeeds)
}

// 通知本文末尾に含まれる機械可読JSONブロックをパース
private fun parseSeedsFromJsonBlock(content: String): Pair<List<SeedPacket>, List<SeedPacket>>? {
    val codeStart = content.indexOf("```json")
    if (codeStart == -1) return null
    val codeEnd = content.indexOf("```", startIndex = codeStart + 7)
    if (codeEnd == -1) return null
    val jsonText = content.substring(codeStart + 7, codeEnd).trim()
    return try {
        val jsonObj = com.google.gson.JsonParser.parseString(jsonText).asJsonObject
        val tm = jsonObj.getAsJsonArray("this_month")?.map { it.asString } ?: emptyList()
        val urgent = jsonObj.getAsJsonArray("ending_soon")?.map { it.asString } ?: emptyList()
        val tmPackets = tm.map { name ->
            SeedPacket(
                id = "json_" + System.currentTimeMillis(),
                productName = name,
                variety = "",
                family = "",
                expirationYear = 0,
                expirationMonth = 0,
                calendar = emptyList()
            )
        }
        val urgentPackets = urgent.map { name ->
            SeedPacket(
                id = "json_" + System.currentTimeMillis(),
                productName = name,
                variety = "",
                family = "",
                expirationYear = 0,
                expirationMonth = 0,
                calendar = emptyList()
            )
        }
        tmPackets to urgentPackets
    } catch (_: Exception) {
        null
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

/**
 * 通知詳細ダイアログ
 */
@Composable
fun NotificationDetailDialog(
    notification: NotificationData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 通知内容
                Text(
                    text = notification.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // 要約がある場合は表示
                if (notification.summary.isNotEmpty()) {
                    Text(
                        text = "要約:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = notification.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 送信日時
                Text(
                    text = "送信日時: ${notification.sentAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}
