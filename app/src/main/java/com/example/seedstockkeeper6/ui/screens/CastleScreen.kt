package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.model.createPreviewSeedData
import com.example.seedstockkeeper6.model.StatisticsData
import com.example.seedstockkeeper6.ui.components.WeeklyWeatherCard
import com.example.seedstockkeeper6.ui.components.SukesanMessageCard
import com.example.seedstockkeeper6.ui.components.SowingSummaryCards
import com.example.seedstockkeeper6.ui.components.StatisticsWidgets
import com.example.seedstockkeeper6.ui.components.NotificationDetailDialog
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.example.seedstockkeeper6.viewmodel.CastleViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastleScreen(
    navController: NavController,
    viewModel: SeedListViewModel,
    isPreview: Boolean = false,
    farmOwner: String = "水戸黄門",
    farmName: String = "菜園"
) {
    val context = LocalContext.current
    val castleViewModel = remember { CastleViewModel(context) }
    
    // 設定ViewModelを取得
    val settingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.seedstockkeeper6.viewmodel.SettingsViewModel>()
    
    // 農園位置情報（設定から取得）
    val farmLatitude = if (isPreview) 35.6762 else settingsViewModel.farmLatitude
    val farmLongitude = if (isPreview) 139.6503 else settingsViewModel.farmLongitude
    
    // 種データの状態監視（引数で渡されたviewModelを使用）
    val seeds = viewModel.seeds.value
    
    // 表示用データの切り分け
    val displaySeeds = if (isPreview) {
        createPreviewSeedData()
    } else {
        seeds
    }
    
    // 農園名（設定から取得、プレビュー時は固定値）
    val displayFarmName = if (isPreview) "田中さんの農園" else "農園名"
    
    // 今月の日付
    val today = if (isPreview) {
        LocalDate.of(2025, 5, 1)
    } else {
        LocalDate.now()
    }
    val currentMonth = today.monthValue
    val currentYear = today.year
    
    // ViewModelの状態を監視
    val monthlyStatistics by castleViewModel.monthlyStatistics.collectAsStateWithLifecycle()
    val isLoadingStatistics by castleViewModel.isLoadingStatistics.collectAsStateWithLifecycle()
    val weeklyWeatherData by castleViewModel.weeklyWeatherData.collectAsStateWithLifecycle()
    val isLoadingWeather by castleViewModel.isLoadingWeather.collectAsStateWithLifecycle()
    val weatherError by castleViewModel.weatherError.collectAsStateWithLifecycle()
    val latestNotification by castleViewModel.latestNotification.collectAsStateWithLifecycle()
    val isLoadingNotification by castleViewModel.isLoadingNotification.collectAsStateWithLifecycle()
    
    // 集計データの取得
    LaunchedEffect(displaySeeds.size) {
        castleViewModel.loadStatistics(displaySeeds, isPreview)
    }
    
    // 天気データの取得
    LaunchedEffect(farmLatitude, farmLongitude, isPreview) {
        castleViewModel.loadWeatherData(farmLatitude, farmLongitude, isPreview)
    }
    
    // 通知データの取得
    LaunchedEffect(isPreview) {
        castleViewModel.loadNotificationData(isPreview)
    }
    
    // 集計データを生成
    val statisticsData = castleViewModel.generateStatisticsData(displaySeeds, isPreview)
    
    // 通知詳細ダイアログの状態
    var showNotificationDialog by remember { mutableStateOf(false) }
    
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
        
        // すけさんからのメッセージ
        item {
            SukesanMessageCard(
                seeds = seeds,
                currentMonth = currentMonth,
                currentYear = currentYear,
                isPreview = isPreview,
                farmOwner = farmOwner,
                farmName = displayFarmName,
                farmLatitude = farmLatitude,
                farmLongitude = farmLongitude,
                latestNotification = latestNotification,
                isLoading = isLoadingNotification,
                onNotificationClick = { showNotificationDialog = true }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // 統計ウィジェット
        item {
            StatisticsWidgets(
                totalSeeds = statisticsData.totalSeeds,
                finishedSeedsCount = statisticsData.finishedSeedsCount,
                expiredSeedsCount = statisticsData.expiredSeedsCount,
                familyDistribution = statisticsData.familyDistribution,
                navController = navController
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

