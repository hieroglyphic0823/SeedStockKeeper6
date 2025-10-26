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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.seedstockkeeper6.model.NotificationHistory
import com.example.seedstockkeeper6.model.NotificationType
import com.example.seedstockkeeper6.model.NotificationData
import com.example.seedstockkeeper6.model.createPreviewNotificationData
import com.example.seedstockkeeper6.model.formatDateTime
import com.example.seedstockkeeper6.model.SectionSummary
import com.example.seedstockkeeper6.notification.NotificationContentGenerator
import com.example.seedstockkeeper6.viewmodel.NotificationHistoryViewModel
import com.example.seedstockkeeper6.R



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(
    navController: NavController,
    onRefreshUnreadCount: () -> Unit = {}
) {
    val viewModel = remember { NotificationHistoryViewModel() }
    val contentGenerator = remember { NotificationContentGenerator() }
    
    // ViewModelの状態を監視
    val notificationDataList by viewModel.notificationDataList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val deletingDocumentId by viewModel.deletingDocumentId.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()
    
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
                            onDelete = { 
                                viewModel.showDeleteDialog(notificationData.documentId ?: "")
                            },
                            onMarkAsRead = { documentId ->
                                viewModel.markNotificationAsRead(documentId, onRefreshUnreadCount)
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
                viewModel.hideDeleteDialog()
            },
            title = { Text("通知履歴を削除") },
            text = { Text("この通知履歴を削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNotificationData(onRefreshUnreadCount)
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        viewModel.hideDeleteDialog()
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
    onDelete: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDetailDialog by remember { mutableStateOf(false) }
    
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


