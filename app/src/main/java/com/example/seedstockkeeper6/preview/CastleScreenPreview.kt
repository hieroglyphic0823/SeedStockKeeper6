package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.example.seedstockkeeper6.service.SukesanMessageService
import com.example.seedstockkeeper6.service.StatisticsService
import com.example.seedstockkeeper6.model.NotificationHistory
import com.example.seedstockkeeper6.service.NotificationHistoryService
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * お城画面のプレビュー専用画面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastleScreenPreviewContent(
    navController: NavController = rememberNavController(),
    farmOwner: String = "水戸黄門",
    farmName: String = "菜園"
) {
    // プレビュー用の固定データ
    val previewSeeds = listOf(
        SeedPacket(
            id = "preview1",
            productName = "恋むすめ",
            variety = "ニンジン",
            family = "せり科",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(
                CalendarEntry(
                    region = "暖地",
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
                    region = "暖地",
                    sowing_start_date = "2025-08-20",
                    sowing_end_date = "2025-09-15",
                    harvest_start_date = "2025-10-01",
                    harvest_end_date = "2025-10-31"
                )
            )
        ),
        SeedPacket(
            id = "preview3",
            productName = "大根",
            variety = "青首大根",
            family = "アブラナ科",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(
                CalendarEntry(
                    region = "暖地",
                    sowing_start_date = "2025-10-01",
                    sowing_end_date = "2025-10-31",
                    harvest_start_date = "2025-12-01",
                    harvest_end_date = "2025-12-31"
                )
            )
        )
    )
    
    // プレビュー用の集計データ
    val previewStatistics = StatisticsData(
        thisMonthSowingCount = 1,
        urgentSeedsCount = 0,
        totalSeeds = 3,
        familyDistribution = listOf(
            Pair("せり科", 1),
            Pair("きく科", 1),
            Pair("アブラナ科", 1)
        )
    )
    
    // プレビュー用の通知データ
    val previewNotification = NotificationHistory(
        id = "preview",
        title = "弥生の風に乗せて――春の種まきの候、菜園より",
        content = "お銀、菜園の弥生は1種類の種の播種時期です。恋むすめ（ニンジン）の栽培を楽しんでくださいね。",
        summary = "まき時：恋むすめ（ニンジン）\n終了間近：春菊（中葉春菊）",
        sentAt = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "T12:00:00.000Z",
        userId = "preview",
        type = com.example.seedstockkeeper6.model.NotificationType.MONTHLY
    )
    
    // プレビュー用の日付
    val today = LocalDate.of(2025, 5, 1)
    val currentMonth = today.monthValue
    val currentYear = today.year
    
    CastleScreenContent(
        seeds = previewSeeds,
        statisticsData = previewStatistics,
        notification = previewNotification,
        currentMonth = currentMonth,
        currentYear = currentYear,
        farmOwner = farmOwner,
        farmName = farmName,
        isPreview = true
    )
}

/**
 * お城画面のコンテンツ部分（プレビュー用）
 */
@Composable
fun CastleScreenContent(
    seeds: List<SeedPacket>,
    statisticsData: StatisticsData,
    notification: NotificationHistory?,
    currentMonth: Int,
    currentYear: Int,
    farmOwner: String,
    farmName: String,
    isPreview: Boolean = false
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ヘッダー部分
        CastleHeader(
            farmOwner = farmOwner,
            farmName = farmName,
            currentMonth = currentMonth,
            currentYear = currentYear
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 統計情報カード
        StatisticsCard(
            statisticsData = statisticsData,
            currentMonth = currentMonth
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 助さんのメッセージ
        notification?.let { notif ->
            SukesanMessageCard(
                notification = notif,
                farmOwner = farmOwner
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 今月の種一覧
        ThisMonthSeedsSection(
            seeds = seeds,
            currentMonth = currentMonth,
            currentYear = currentYear
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 終了間近の種一覧
        UrgentSeedsSection(
            seeds = seeds,
            currentMonth = currentMonth,
            currentYear = currentYear
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * お城のヘッダー部分
 */
@Composable
fun CastleHeader(
    farmOwner: String,
    farmName: String,
    currentMonth: Int,
    currentYear: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏰 ${farmName}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${farmName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${currentYear}年${currentMonth}月",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * 統計情報カード
 */
@Composable
fun StatisticsCard(
    statisticsData: StatisticsData,
    currentMonth: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 今月の統計",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    label = "総種子数",
                    value = statisticsData.totalSeeds.toString(),
                    icon = Icons.Filled.Circle
                )
                
                StatisticItem(
                    label = "今月まき時",
                    value = statisticsData.thisMonthSowingCount.toString(),
                    icon = Icons.Filled.Schedule
                )
                
                StatisticItem(
                    label = "終了間近",
                    value = statisticsData.urgentSeedsCount.toString(),
                    icon = Icons.Filled.Warning
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 科別分布
            if (statisticsData.familyDistribution.isNotEmpty()) {
                Text(
                    text = "科別分布",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn {
                    items(statisticsData.familyDistribution) { (family, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = family,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${count}種",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 統計項目
 */
@Composable
fun StatisticItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 助さんのメッセージカード
 */
@Composable
fun SukesanMessageCard(
    notification: NotificationHistory,
    farmOwner: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "助さん",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "助さんからのお知らせ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = notification.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * 今月の種一覧セクション
 */
@Composable
fun ThisMonthSeedsSection(
    seeds: List<SeedPacket>,
    currentMonth: Int,
    currentYear: Int
) {
    val thisMonthSeeds = seeds.filter { seed ->
        seed.calendar.any { entry ->
            val sowingStartMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_start_date)
            val sowingStartYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_start_date)
            sowingStartMonth == currentMonth && sowingStartYear == currentYear
        }
    }
    
    if (thisMonthSeeds.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🌱 今月のまき時",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn {
                    items(thisMonthSeeds) { seed ->
                        SeedItem(seed = seed)
                    }
                }
            }
        }
    }
}

/**
 * 終了間近の種一覧セクション
 */
@Composable
fun UrgentSeedsSection(
    seeds: List<SeedPacket>,
    currentMonth: Int,
    currentYear: Int
) {
    val urgentSeeds = seeds.filter { seed ->
        seed.calendar.any { entry ->
            val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
            val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
            val sowingEndStage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDateToStage(entry.sowing_end_date)
            // 今月内で播種期間が終了する種（上旬、中旬、下旬すべて対象）
            sowingEndMonth == currentMonth && sowingEndYear == currentYear
        }
    }
    
    if (urgentSeeds.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "⚠️ まき時終了間近",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn {
                    items(urgentSeeds) { seed ->
                        SeedItem(seed = seed)
                    }
                }
            }
        }
    }
}

/**
 * 種アイテム
 */
@Composable
fun SeedItem(seed: SeedPacket) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Circle,
            contentDescription = "種",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${seed.productName}（${seed.variety}）",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = seed.family,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 統計データ
 */
data class StatisticsData(
    val thisMonthSowingCount: Int,
    val urgentSeedsCount: Int,
    val totalSeeds: Int,
    val familyDistribution: List<Pair<String, Int>>
)

/**
 * プレビュー
 */
@Preview(showBackground = true)
@Composable
fun CastleScreenPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // ヘッダー
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏰 菜園",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "菜園",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "2025年5月",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 統計カード
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // まき時カード
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Inventory,
                            contentDescription = "まき時",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "1",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "まき時",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // 終了間近カード
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = "終了間近",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        Text(
                            text = "終了間近",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 登録総数カード
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = "登録総数",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "登録総数",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = "1",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
