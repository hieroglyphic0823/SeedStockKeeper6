package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.navigation.NavController
import com.example.seedstockkeeper6.model.NotificationHistory
import com.example.seedstockkeeper6.model.NotificationType
import com.example.seedstockkeeper6.model.NotificationData
import com.example.seedstockkeeper6.service.NotificationHistoryService
import com.example.seedstockkeeper6.notification.NotificationContentGenerator
import com.example.seedstockkeeper6.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// プレビュー用のデモデータ
@Composable
fun createPreviewNotificationData(): List<NotificationData> {
    return listOf(
        NotificationData(
            id = "preview1",
            title = "弥生の風に乗せて――春の種まきの候、菜園より",
            summary = "お銀、菜園の弥生は1種類の種の播種時期です。恋むすめ（ニンジン）の栽培を楽しんでくださいね。",
            farmOwner = "お銀",
            region = "温暖地",
            prefecture = "東京都",
            month = 3,
            thisMonthSeeds = listOf(
                com.example.seedstockkeeper6.model.SeedInfo(
                    name = "恋むすめ",
                    variety = "ニンジン",
                    description = "春の種まきに最適な品種です"
                )
            ),
            endingSoonSeeds = listOf(
                com.example.seedstockkeeper6.model.SeedInfo(
                    name = "春菊",
                    variety = "中葉春菊",
                    description = "まき時終了間近です"
                )
            ),
            sentAt = "2024-03-15T12:00:00.000Z",
            userId = "preview",
            seedCount = 1,
            isRead = 0 // 未読
        ),
        NotificationData(
            id = "preview2",
            title = "卯月の雨に潤う――新緑の種まきの候、菜園より",
            summary = "お銀、菜園の卯月は2種類の種の播種時期です。レタスとネギの栽培を楽しんでくださいね。",
            farmOwner = "お銀",
            region = "温暖地",
            prefecture = "東京都",
            month = 4,
            thisMonthSeeds = listOf(
                com.example.seedstockkeeper6.model.SeedInfo(
                    name = "レタス",
                    variety = "サニーレタス",
                    description = "春の種まきに最適な品種です"
                ),
                com.example.seedstockkeeper6.model.SeedInfo(
                    name = "ネギ",
                    variety = "九条ネギ",
                    description = "春の種まきに最適な品種です"
                )
            ),
            endingSoonSeeds = emptyList(),
            sentAt = "2024-04-15T12:00:00.000Z",
            userId = "preview",
            seedCount = 2,
            isRead = 1 // 既読
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(
    navController: NavController,
    onRefreshUnreadCount: () -> Unit = {}
) {
    val historyService = remember { NotificationHistoryService() }
    val contentGenerator = remember { NotificationContentGenerator() }
    val scope = rememberCoroutineScope()
    var notificationDataList by remember { mutableStateOf<List<NotificationData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var deletingDocumentId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // 通知データを読み込み
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            errorMessage = ""
            val result = historyService.getUserNotificationData()
            notificationDataList = result
        } catch (e: Exception) {
            errorMessage = "通知データの読み込みに失敗しました: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
    ) { paddingValues ->
        // ローディング状態
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("通知履歴を読み込み中...")
                    }
                }
            }
        // エラーメッセージ
        else if (errorMessage.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ エラー",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        // 通知データリストが空の場合
        else if (notificationDataList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.kazaguruma_c),
                            contentDescription = "空の通知履歴",
                            tint = ComposeColor.Unspecified,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "通知履歴がありません",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "通知を送信すると、ここに履歴が表示されます",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
        }
        // 通知データがある場合
        else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp) // ← カード間の余白
            ) {
                    items(notificationDataList) { notificationData ->
                        NotificationDataCard(
                            notificationData = notificationData,
                            contentGenerator = contentGenerator,
                            historyService = historyService,
                            onDelete = { 
                                deletingDocumentId = notificationData.documentId
                                showDeleteDialog = true
                            },
                            onMarkAsRead = { documentId ->
                                scope.launch {
                                    try {
                                        val success = historyService.markNotificationAsRead(documentId)
                                        if (success) {
                                            // ローカルのリストも更新
                                            notificationDataList = notificationDataList.map { data ->
                                                if (data.documentId == documentId) {
                                                    data.copy(isRead = 1)
                                                } else {
                                                    data
                                                }
                                            }
                                            // 未読通知数を更新
                                            onRefreshUnreadCount()
                                        }
                                    } catch (e: Exception) {
                                    }
                                }
                            }
                        )
                    }
                }
        }
    }
    
    // 削除確認ダイアログ
    if (showDeleteDialog && deletingDocumentId != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                deletingDocumentId = null
            },
            title = { Text("通知履歴を削除") },
            text = { Text("この通知履歴を削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val documentId = deletingDocumentId
                        showDeleteDialog = false
                        deletingDocumentId = null
                        
                        if (documentId != null) {
                            scope.launch {
                                try {
                                    val success = historyService.deleteNotificationData(documentId)
                                    if (success) {
                                        // 削除成功時はリストからも削除
                                        notificationDataList = notificationDataList.filter { 
                                            it.documentId != documentId 
                                        }
                                        // 未読通知数を更新
                                        onRefreshUnreadCount()
                                    } else {
                                    }
                                } catch (e: Exception) {
                                }
                            }
                        } else {
                        }
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        deletingDocumentId = null
                    }
                ) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationDataCard(
    notificationData: NotificationData,
    contentGenerator: NotificationContentGenerator,
    historyService: NotificationHistoryService,
    onDelete: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDetailDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // 通知タイプに応じたタイトル行の色を取得
    val titleColor = when (notificationData.notificationType) {
        "MONTHLY" -> MaterialTheme.colorScheme.onPrimaryContainer
        "WEEKLY" -> MaterialTheme.colorScheme.onTertiaryContainer
        "CUSTOM" -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    // 本文の種情報部分の色（統一）
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    // 詳細ダイアログが表示された時に既読フラグを更新
    LaunchedEffect(showDetailDialog) {
        if (showDetailDialog && notificationData.isRead == 0 && notificationData.documentId != null) {
            onMarkAsRead(notificationData.documentId)
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
                ,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { showDetailDialog = true }
    ) {
        Column {
            // タイトル行（色分け）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = when (notificationData.notificationType) {
                            "MONTHLY" -> MaterialTheme.colorScheme.primaryContainer
                            "WEEKLY" -> MaterialTheme.colorScheme.tertiaryContainer
                            "CUSTOM" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp)// ← タイトル上下の余白
                    ,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )
                    
                    // Boxで囲んでサイズを固定し、内部で中央揃えにする
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.yabumi_shinshyu),
                            contentDescription = "矢文",
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = rotation
                                }
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = notificationData.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notificationData.isRead == 0) FontWeight.Bold else FontWeight.Normal,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(
                    onClick = { 
                        onDelete() 
                    }
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "削除",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // カード本体（3行: タイトルの下に「まきどき」「まき時終了」）
            Column(
                modifier = Modifier
                    .background(
                        when (notificationData.notificationType) {
                            "MONTHLY" -> MaterialTheme.colorScheme.surfaceContainerLowest
                            else -> MaterialTheme.colorScheme.surfaceContainerLowest
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    ,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sectionSummary = remember(notificationData) {
                    SectionSummary(
                        thisMonth = notificationData.thisMonthSeeds.take(3).joinToString("、") { it.name },
                        endingSoon = notificationData.endingSoonSeeds.take(3).joinToString("、") { seed ->
                            val expirationInfo = if (seed.expirationYear > 0 && seed.expirationMonth > 0) {
                                " (${seed.expirationYear}/${seed.expirationMonth})"
                            } else {
                                ""
                            }
                            "${seed.name}${expirationInfo}"
                        }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌱まきどき " + (sectionSummary.thisMonth.ifEmpty { "該当なし" }),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⏳期限間近 " + (sectionSummary.endingSoon.ifEmpty { "該当なし" }),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // おすすめの種（週次通知の3週目以降は来月のおすすめ）
                if (notificationData.recommendedSeeds.isNotEmpty()) {
                    val recommendedTitle = if (notificationData.notificationType == "WEEKLY") {
                        // 週次通知の場合は週番号を判定
                        val currentDate = java.time.LocalDate.now()
                        val weekFields = java.time.temporal.WeekFields.of(java.util.Locale.JAPAN)
                        val weekNumber = currentDate.get(weekFields.weekOfMonth())
                        if (weekNumber >= 3) {
                            "🔥 来月のおすすめ"
                        } else {
                            "🎯 今月のおすすめ"
                        }
                    } else {
                        "🎯 今月のおすすめ"
                    }
                    
                    // おすすめの種を3つ続けて表示
                    val recommendedSeedsText = notificationData.recommendedSeeds.take(3).joinToString("、") { it.name }
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$recommendedTitle $recommendedSeedsText",
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
    
    // 詳細表示ダイアログ
    if (showDetailDialog) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            modifier = Modifier
                .onSizeChanged { size ->
                }
                .padding(bottom = 4.dp),
            containerColor = when (notificationData.notificationType) {
                "MONTHLY" -> MaterialTheme.colorScheme.surfaceContainerLowest
                "WEEKLY" -> MaterialTheme.colorScheme.surfaceContainerLowest
                "CUSTOM" -> MaterialTheme.colorScheme.surfaceContainerLowest
                else -> MaterialTheme.colorScheme.surface
            },
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 回転アニメーション付きのyabumi_shinshyuアイコン
                        val infiniteTransition = rememberInfiniteTransition(label = "rotation")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "rotation"
                        )
                        
                        Image(
                            painter = painterResource(id = R.drawable.yabumi_shinshyu),
                            contentDescription = "矢文",
                            modifier = Modifier
                                .size(24.dp)
                                .onSizeChanged { size ->
                                }
                                .onGloballyPositioned { coordinates ->
                                }
                                .graphicsLayer {
                                    rotationZ = rotation
                                }
                        )
                        
                        // アイコンとタイトルの間にスペースを追加
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // タイトル表示（月次通知は1行、週次通知は2行）
                        if (notificationData.notificationType == "WEEKLY" && notificationData.title.contains("すけさん便り")) {
                            // 週次通知は2行に分けて表示
                            val titleParts = notificationData.title.split("すけさん便り")
                            if (titleParts.size >= 2) {
                                val firstPart = titleParts[0].trim()
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .onSizeChanged { size ->
                                        }
                                        .onGloballyPositioned { coordinates ->
                                        }
                                ) {
                                    Text(
                                        text = firstPart,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = titleColor
                                    )
                                    Text(
                                        text = "すけさん便り",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = titleColor
                                    )
                                }
                            } else {
                                // 分割できない場合は1行で表示
                                Text(
                                    text = notificationData.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = titleColor,
                                    modifier = Modifier
                                        .weight(1f)
                                        .onSizeChanged { size ->
                                        }
                                        .onGloballyPositioned { coordinates ->
                                        }
                                )
                            }
                        } else {
                            // 月次通知やその他は1行で表示
                            Text(
                                text = notificationData.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = titleColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .onSizeChanged { size ->
                                    }
                                    .onGloballyPositioned { coordinates ->
                                    }
                            )
                        }
                        
                        IconButton(
                            onClick = { showDetailDialog = false },
                            modifier = Modifier
                                .onSizeChanged { size ->
                                }
                                .onGloballyPositioned { coordinates ->
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "閉じる",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(800.dp)
                        .verticalScroll(rememberScrollState())
                        .onSizeChanged { size ->
                        }
                ) {
                    // 通知内容（JSONデータから生成）
                    val content = remember(notificationData) { contentGenerator.generateContent(notificationData) }
                    
                    // ヘッダー
                    if (notificationData.summary.isNotEmpty()) {
                        Text(
                            text = notificationData.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    // 今月まきどきの種
                    if (notificationData.thisMonthSeeds.isNotEmpty()) {
                        RichSection(
                            title = "🌱まきどき",
                            items = notificationData.thisMonthSeeds.map { it.name to it.description },
                            textColor = contentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    // 終了間近の種
                    if (notificationData.endingSoonSeeds.isNotEmpty()) {
                        val endingSoonItems = notificationData.endingSoonSeeds.map { seed ->
                            val expirationInfo = if (seed.expirationYear > 0 && seed.expirationMonth > 0) {
                                " (${seed.expirationYear}/${seed.expirationMonth})"
                            } else {
                                ""
                            }
                            val nameWithExpiration = "${seed.name}${expirationInfo}"
                            nameWithExpiration to seed.description
                        }
                        RichSectionWithExpiration(
                            title = "⏳期限間近",
                            items = endingSoonItems,
                            textColor = contentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    // おすすめの種（週次通知の3週目以降は来月のおすすめ）
                    if (notificationData.recommendedSeeds.isNotEmpty()) {
                        val recommendedTitle = if (notificationData.notificationType == "WEEKLY") {
                            // 週次通知の場合は週番号を判定
                            val currentDate = java.time.LocalDate.now()
                            val weekFields = java.time.temporal.WeekFields.of(java.util.Locale.JAPAN)
                            val weekNumber = currentDate.get(weekFields.weekOfMonth())
                            if (weekNumber >= 3) {
                                "🔥 来月のおすすめ"
                            } else {
                                "🎯 今月のおすすめ"
                            }
                        } else {
                            "🎯 今月のおすすめ"
                        }
                        RichSection(
                            title = recommendedTitle,
                            items = notificationData.recommendedSeeds.map { it.name to it.description },
                            textColor = contentColor
                        )
                    }
                    
                    // 結びの文を表示
                    if (notificationData.closingLine.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = notificationData.closingLine,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                    if (notificationData.signature.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = notificationData.signature,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        }
                    }
                    
                    // メタ情報（末尾に移動）
                    Spacer(modifier = Modifier.height(16.dp))
                    if (notificationData.farmOwner.isNotEmpty() || notificationData.region.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (notificationData.farmOwner.isNotEmpty()) {
                                Text(
                                    text = "👤 ${notificationData.farmOwner}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            if (notificationData.region.isNotEmpty()) {
                                Text(
                                    text = "📍 ${notificationData.region}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    // 送信日時（末尾に移動）
                    Text(
                        text = formatDateTime(notificationData.sentAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                // 空のボタンでスペースを確保
                TextButton(
                    onClick = { showDetailDialog = false },
                    modifier = Modifier.height(0.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("")
                }
            }
        )
    }
    
}

// 本文から種プレビュー（種名, 説明）を抽出
private fun extractSeedPreviewItems(content: String, maxItems: Int = 3): List<Pair<String, String>> {
    // セクション境界を考慮して、「• 」行から『種名』っぽいものと、その次行の簡潔説明を拾う
    val lines = content.lines()
    val items = mutableListOf<Pair<String, String>>()
    var i = 0
    while (i < lines.size && items.size < maxItems) {
        val line = lines[i].trim()
        // 箇条書き・種名候補（記号は「•」「*」「-」のいずれかを許容）
        if (line.startsWith("• ") || line.startsWith("* ") || line.startsWith("- ")) {
            val name = line.removePrefix("• ").removePrefix("* ").removePrefix("- ").trim()
            // 次行を説明文候補として取得（同じ箇条書きでない、かつ見出しでない）
            val desc = if (i + 1 < lines.size) {
                val next = lines[i + 1].trim()
                if (!next.startsWith("• ") && !next.startsWith("* ") && !next.startsWith("- ") && !next.startsWith("🌱") && !next.startsWith("⚠️") && !next.startsWith("🌟") && !next.startsWith("```")) next else ""
            } else ""
            if (name.isNotEmpty()) {
                items += name to desc
            }
        }
        i++
    }
    return items
}

// 「今月まきどき」「まき時終了間近」各セクションの先頭アイテム名を1行サマリに整形
private data class SectionSummary(val thisMonth: String, val endingSoon: String)

private fun extractSectionSummaries(content: String): SectionSummary {
    // JSONコードブロックがあれば最優先で使う
    val jsonStart = content.indexOf("```json")
    if (jsonStart != -1) {
        val jsonEnd = content.indexOf("```", startIndex = jsonStart + 7)
        if (jsonEnd != -1) {
            val jsonText = content.substring(jsonStart + 7, jsonEnd).trim()
            try {
                val obj = com.google.gson.JsonParser.parseString(jsonText).asJsonObject
                val tm = obj.getAsJsonArray("this_month")?.map { it.asString } ?: emptyList()
                val es = obj.getAsJsonArray("ending_soon")?.map { it.asString } ?: emptyList()
                return SectionSummary(
                    thisMonth = tm.firstOrNull() ?: "",
                    endingSoon = es.firstOrNull() ?: ""
                )
            } catch (_: Exception) {
                // fall through to text parsing
            }
        }
    }
    // テキストから抽出（見出し→次の箇条書き1件を拾う）
    var thisMonth = ""
    var endingSoon = ""
    val lines = content.lines()
    var i = 0
    while (i < lines.size) {
        val line = lines[i].trim()
        if (line.startsWith("🌱")) {
            // 次の箇条書き行
            var j = i + 1
            while (j < lines.size) {
                val l = lines[j].trim()
                if (l.startsWith("• ") || l.startsWith("* ") || l.startsWith("- ")) {
                    thisMonth = l.removePrefix("• ").removePrefix("* ").removePrefix("- ").trim()
                    break
                }
                if (l.startsWith("⚠️") || l.startsWith("🌟") || l.startsWith("```")) break
                j++
            }
        }
        if (line.startsWith("⚠️")) {
            var j = i + 1
            while (j < lines.size) {
                val l = lines[j].trim()
                if (l.startsWith("• ") || l.startsWith("* ") || l.startsWith("- ")) {
                    endingSoon = l.removePrefix("• ").removePrefix("* ").removePrefix("- ").trim()
                    break
                }
                if (l.startsWith("🌟") || l.startsWith("```")) break
                j++
            }
        }
        i++
    }
    return SectionSummary(thisMonth = thisMonth, endingSoon = endingSoon)
}

@Composable
private fun RichSectionWithExpiration(title: String, items: List<Pair<String, String>>, iconResource: Int? = null, textColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconResource != null) {
            Image(
                painter = painterResource(id = iconResource),
                contentDescription = title,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = textColor
        )
    }
    if (items.isEmpty()) {
        Text(
            text = "該当なし",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.8f)
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (nameWithExpiration, desc) ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = nameWithExpiration,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                if (desc.isNotEmpty()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RichSection(title: String, items: List<Pair<String, String>>, iconResource: Int? = null, textColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconResource != null) {
            Image(
                painter = painterResource(id = iconResource),
                contentDescription = title,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = textColor
        )
    }
    if (items.isEmpty()) {
        Text(
            text = "該当なし",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.8f)
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (name, desc) ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
                if (desc.isNotEmpty()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

// セクション毎に（種名, 説明）一覧を抽出
private fun extractSectionItems(content: String, sectionMarker: String): List<Pair<String, String>> {
    val text = removeJsonCodeBlock(content)
    val lines = text.lines()
    val results = mutableListOf<Pair<String, String>>()
    var i = 0
    while (i < lines.size) {
        val line = lines[i].trim()
        if (line.startsWith(sectionMarker)) {
            var j = i + 1
            while (j < lines.size) {
                val l = lines[j].trim()
                if (l.startsWith("🌱") || l.startsWith("⚠️") || l.startsWith("🌟") || l.startsWith("```")) break
                if (l.startsWith("• ") || l.startsWith("* ") || l.startsWith("- ")) {
                    val raw = l.removePrefix("• ").removePrefix("* ").removePrefix("- ").trim()
                    val nameInQuote = Regex("『([^』]+)』").find(raw)?.groupValues?.getOrNull(1)
                    val name = (nameInQuote ?: raw).replace("：", "").trim()
                    // 説明は次行（箇条書きや見出しでない）を説明として扱う
                    val desc = if (j + 1 < lines.size) {
                        val next = lines[j + 1].trim()
                        if (!next.startsWith("• ") && !next.startsWith("* ") && !next.startsWith("- ") && !next.startsWith("🌱") && !next.startsWith("⚠️") && !next.startsWith("🌟") && !next.startsWith("```")) {
                            // 説明文の先頭の「：」を削除
                            next.removePrefix("：").trim()
                        } else ""
                    } else ""
                    results += name to desc
                }
                j++
            }
        }
        i++
    }
    return results
}

// JSONコードブロック除去（履歴側にも再利用）
private fun removeJsonCodeBlock(content: String): String {
    val start = content.indexOf("```json")
    if (start == -1) return content
    val end = content.indexOf("```", startIndex = start + 7)
    return if (end == -1) content.substring(0, start).trimEnd() else (content.substring(0, start) + content.substring(end + 3)).trim()
}

private fun buildClosingLine(farmOwner: String): String {
    return when (farmOwner) {
        "水戸黄門" -> "かしこ\n佐々木助三郎 拝"
        "お銀" -> "ご自愛くだされ\n佐々木助三郎 拝"
        "八兵衛" -> "しっかり働けよ！\n助三郎 より"
        else -> "本日も良き栽培となりますよう。助さんより"
    }
}

// 通知内容から署名部分を抽出
private fun extractSignature(content: String): String {
    val lines = content.lines()
    // 最後の数行から署名を探す
    for (i in lines.size - 1 downTo maxOf(0, lines.size - 5)) {
        val line = lines[i].trim()
        if (line.contains("佐々木助三郎 拝") || line.contains("助三郎 より") || line.contains("助さんより")) {
            return line
        }
    }
    return ""
}

    // 通知内容からアドバイスと署名を抽出
    private fun extractAdviceAndSignature(content: String): Pair<String, String> {
        val lines = content.lines()
        var advice = ""
        var signature = ""

        for (i in maxOf(0, lines.size - 10) until lines.size) {
        }

        // 最後の数行からアドバイスと署名を探す
        for (i in lines.size - 1 downTo maxOf(0, lines.size - 10)) {
            val line = lines[i].trim()

            // 署名を探す
            if (line.contains("佐々木助三郎 拝") || line.contains("助三郎 より") || line.contains("助さんより")) {
                signature = line
            }
            // アドバイスを探す（署名の前の行で、短い文）
            else if (line.isNotEmpty() && line.length <= 50 && !line.startsWith("🌱") && !line.startsWith("⚠️") && !line.startsWith("🌟") && !line.startsWith("【") && !line.contains("佐々木助三郎") && !line.contains("助三郎") && !line.contains("助さん")) {
                if (advice.isEmpty()) {
                    advice = line
                }
            }
        }

        return advice to signature
    }

private fun formatDateTime(dateTimeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
        
        val date = inputFormat.parse(dateTimeString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateTimeString
    }
}

private fun extractAdviceFromContent(content: String): String {
    val lines = content.lines()
    val jsonStartIndex = content.indexOf("```json")
    val jsonEndIndex = if (jsonStartIndex != -1) content.indexOf("```", jsonStartIndex + 7) else -1
    
    // 最後の数行からアドバイス（結びの一言）を探す
    for (i in lines.size - 1 downTo maxOf(0, lines.size - 25)) {
        val line = lines[i].trim()
        
        // JSONブロック内の行は除外
        if (jsonStartIndex != -1 && jsonEndIndex != -1) {
            val lineStartIndex = content.indexOf(line)
            if (lineStartIndex >= jsonStartIndex && lineStartIndex <= jsonEndIndex) {
                continue
            }
        }
        
        // 署名の前の行で、アドバイス文を探す
        if (line.isNotEmpty() && line.length <= 100 && 
            !line.startsWith("🌱") && !line.startsWith("⚠️") && !line.startsWith("🌟") && 
            !line.startsWith("【") && !line.startsWith("```") && !line.startsWith("{") && !line.startsWith("}") &&
            !line.contains("佐々木助三郎") && !line.contains("助三郎") && !line.contains("助さん") &&
            !line.contains("\"") && !line.contains("name") && !line.contains("desc") &&
            !line.contains("```") && !line.contains("json") &&
            // アドバイスらしい文の条件を拡張
            (line.contains("ご無理") || line.contains("お祈り") || line.contains("心より") || 
             line.contains("どうぞ") || line.contains("季節") || line.contains("時節") ||
             line.contains("温かく") || line.contains("寒さ") || line.contains("作業") ||
             line.contains("実り") || line.contains("豊作") || line.contains("収穫") ||
             line.contains("ご自愛") || line.contains("励まれ") || line.contains("肌寒") ||
             line.contains("秋深") || line.contains("農作業") || line.contains("無理なき"))) {
            return line
        }
    }
    return ""
}

