package com.example.seedstockkeeper6.ui.screens

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
import com.example.seedstockkeeper6.service.SukesanMessageService
import com.example.seedstockkeeper6.model.NotificationHistory
import com.example.seedstockkeeper6.service.NotificationHistoryService
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastleScreen(
    navController: NavController,
    viewModel: SeedListViewModel,
    isPreview: Boolean = false,
    farmOwner: String = "水戸黄門",
    farmName: String = "菜園"
) {
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
    
    // 今月の播種予定種子数
    val thisMonthSowingSeeds = seeds.filter { seed ->
        seed.calendar?.any { entry ->
            val sowingStartMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_start_date)
            val sowingStartYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_start_date)
            sowingStartMonth == currentMonth && sowingStartYear == currentYear
        } ?: false
    }
    
    // まき時終了間近の種子数（今月の下旬まで）
    val urgentSeeds = seeds.filter { seed ->
        seed.calendar?.any { entry ->
            val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
            val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
            val sowingEndStage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDateToStage(entry.sowing_end_date)
            sowingEndMonth == currentMonth && sowingEndYear == currentYear && sowingEndStage == "下旬"
        } ?: false
    }
    
    // 科別分布（有効期限内の種のみ）
    val currentDate = LocalDate.now()
    val validSeeds = seeds.filter { seed ->
        val expirationDate = LocalDate.of(seed.expirationYear, seed.expirationMonth, 1)
        currentDate.isBefore(expirationDate.plusMonths(1)) // 有効期限の月末まで
    }
    val familyDistribution = validSeeds.groupBy { it.family }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // ヘッダーは削除（AppTopBarのみ残す）
        
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
            thisMonthSowingSeeds = thisMonthSowingSeeds,
            urgentSeeds = urgentSeeds
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 統計ウィジェット
        StatisticsWidgets(
            totalSeeds = seeds.size,
            familyDistribution = familyDistribution
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
            
            // すけさんアイコン
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
            
            // 吹き出し部分
            Card(
                modifier = Modifier
                    .weight(1f)
                    .onSizeChanged { size ->
                        messageHeight = with(density) { size.height.toDp() }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp) // 吹き出しの形
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
                                            line.contains("今月まき時") -> line.replace("🌱 今月まき時：", "まき時：")
                                            line.contains("まき時終了間近") -> line.replace("⚠️ まき時終了間近：", "終了間近：")
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
        }
    }
}

@Composable
fun SowingSummaryCards(
    thisMonthSowingSeeds: List<SeedPacket>,
    urgentSeeds: List<SeedPacket>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Spa,
                contentDescription = "種",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "今月の種",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 播種予定種子数
            SummaryCard(
                icon = Icons.Filled.Inventory,
                title = "まき時",
                value = "${thisMonthSowingSeeds.size}",
                subtitle = "今月",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            // まき時終了間近の種子数
            SummaryCard(
                icon = Icons.Filled.Schedule,
                title = "終了間近",
                value = "${urgentSeeds.size}",
                subtitle = "今月",
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatisticsWidgets(
    totalSeeds: Int,
    familyDistribution: List<Pair<String, Int>>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "統計",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 登録種子総数
            SummaryCard(
                icon = Icons.Filled.Analytics,
                title = "登録種子総数",
                value = "$totalSeeds",
                subtitle = "件",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
            
            // 科別分布（上位3科）
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PieChart,
                            contentDescription = "科別分布",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "科別分布",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 円グラフ表示
                    if (familyDistribution.isNotEmpty()) {
                        PieChart(
                            data = familyDistribution,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    } else {
                        Text(
                            text = "有効期限内の種がありません",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
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
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.7f)
            )
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
            sowingEndMonth == currentMonth && sowingEndYear == currentYear && sowingEndStage == "下旬"
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

@Preview(showBackground = true, showSystemUi = true, name = "お城画面 - お銀")
@Composable
fun CastleScreenPreviewOgin() {
    MaterialTheme {
        CastleScreen(
            navController = rememberNavController(),
            viewModel = SeedListViewModel(),
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
            viewModel = SeedListViewModel(),
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
            viewModel = SeedListViewModel(),
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
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.outline
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
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
        
        // 凡例
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
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
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
