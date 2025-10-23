package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(
    navController: NavController
) {
    android.util.Log.d("NotificationHistoryScreen", "NotificationHistoryScreenが描画開始されました")
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
            android.util.Log.d("NotificationHistoryScreen", "通知データ読み込み開始")
            isLoading = true
            errorMessage = ""
            val result = historyService.getUserNotificationData()
            android.util.Log.d("NotificationHistoryScreen", "通知データ読み込み完了 - 取得件数: ${result.size}")
            android.util.Log.d("NotificationHistoryScreen", "取得したデータ: $result")
            notificationDataList = result
            android.util.Log.d("NotificationHistoryScreen", "notificationDataListを更新しました - 件数: ${notificationDataList.size}")
        } catch (e: Exception) {
            android.util.Log.e("NotificationHistoryScreen", "通知データの読み込みに失敗", e)
            errorMessage = "通知データの読み込みに失敗しました: ${e.message}"
        } finally {
            isLoading = false
            android.util.Log.d("NotificationHistoryScreen", "読み込み完了 - isLoading: $isLoading, errorMessage: $errorMessage")
        }
    }
    
    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ローディング状態
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
            // 通知データリスト
            else if (notificationDataList.isEmpty()) {
                android.util.Log.d("NotificationHistoryScreen", "空のデータを表示 - notificationDataList.isEmpty() = true")
                Box(
                    modifier = Modifier.fillMaxSize(),
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
            else {
                android.util.Log.d("NotificationHistoryScreen", "データリストを表示 - 件数: ${notificationDataList.size}")
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notificationDataList) { notificationData ->
                        android.util.Log.d("NotificationHistoryScreen", "NotificationDataCardを描画中 - documentId: ${notificationData.documentId}, title: ${notificationData.title}")
                        NotificationDataCard(
                            notificationData = notificationData,
                            contentGenerator = contentGenerator,
                            onDelete = { 
                                android.util.Log.d("NotificationHistoryScreen", "onDeleteコールバックが呼ばれました - documentId: ${notificationData.documentId}")
                                deletingDocumentId = notificationData.documentId
                                showDeleteDialog = true
                                android.util.Log.d("NotificationHistoryScreen", "削除ダイアログ状態を更新 - showDeleteDialog: $showDeleteDialog, deletingDocumentId: $deletingDocumentId")
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 削除確認ダイアログ
    android.util.Log.d("NotificationHistoryScreen", "削除ダイアログ条件チェック - showDeleteDialog: $showDeleteDialog, deletingDocumentId: $deletingDocumentId")
    if (showDeleteDialog && deletingDocumentId != null) {
        android.util.Log.d("NotificationHistoryScreen", "削除確認ダイアログを表示します")
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
                        android.util.Log.d("NotificationHistoryScreen", "削除確認ボタンがクリックされました - documentId: $deletingDocumentId")
                        val documentId = deletingDocumentId
                        showDeleteDialog = false
                        deletingDocumentId = null
                        android.util.Log.d("NotificationHistoryScreen", "削除ダイアログを閉じました")
                        
                        if (documentId != null) {
                            scope.launch {
                                try {
                                    val success = historyService.deleteNotificationData(documentId)
                                    if (success) {
                                        // 削除成功時はリストからも削除
                                        notificationDataList = notificationDataList.filter { 
                                            it.documentId != documentId 
                                        }
                                        android.util.Log.d("NotificationHistoryScreen", "通知データを削除しました")
                                    } else {
                                        android.util.Log.e("NotificationHistoryScreen", "通知データの削除に失敗しました")
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("NotificationHistoryScreen", "削除処理でエラーが発生", e)
                                }
                            }
                        } else {
                            android.util.Log.e("NotificationHistoryScreen", "削除対象のdocumentIdがnullです")
                        }
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        android.util.Log.d("NotificationHistoryScreen", "キャンセルボタンがクリックされました")
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
    onDelete: () -> Unit
) {
    android.util.Log.d("NotificationHistoryScreen", "NotificationDataCard関数が呼ばれました - documentId: ${notificationData.documentId}")
    var showDetailDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (notificationData.notificationType) {
                "MONTHLY" -> MaterialTheme.colorScheme.primaryContainer
                "WEEKLY" -> MaterialTheme.colorScheme.secondaryContainer
                "CUSTOM" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        ),
        onClick = { showDetailDialog = true }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ヘッダー（タイトルとアイコン）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.yabumi3),
                        contentDescription = "矢文",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = notificationData.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(
                    onClick = { 
                        android.util.Log.d("NotificationHistoryScreen", "削除ボタンがクリックされました - documentId: ${notificationData.documentId}")
                        onDelete() 
                    }
                ) {
                    android.util.Log.d("NotificationHistoryScreen", "削除アイコンを描画中 - documentId: ${notificationData.documentId}")
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "削除",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // カード本体（3行: タイトルの下に「まきどき」「まき時終了」）
            val sectionSummary = remember(notificationData) {
                SectionSummary(
                    thisMonth = notificationData.thisMonthSeeds.firstOrNull()?.name ?: "",
                    endingSoon = notificationData.endingSoonSeeds.firstOrNull()?.name ?: ""
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.seed),
                    contentDescription = "まきどき",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "今月のまき時 " + (sectionSummary.thisMonth.ifEmpty { "該当なし" }),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.warning),
                    contentDescription = "終了間近",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "終了間近 " + (sectionSummary.endingSoon.ifEmpty { "該当なし" }),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // メタ情報
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
                    if (notificationData.seedCount > 0) {
                        Text(
                            text = "🌱 ${notificationData.seedCount}種類",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                    android.util.Log.d("NotificationHistoryScreen", "AlertDialog全体サイズ: width=${size.width}, height=${size.height}")
                }
                .padding(bottom = 4.dp),
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.yabumi3),
                            contentDescription = "矢文",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp)
                        )
                        Text(
                            text = notificationData.title,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    IconButton(
                        onClick = { showDetailDialog = false },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "閉じる",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
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
                            android.util.Log.d("NotificationHistoryScreen", "本文Columnサイズ: width=${size.width}, height=${size.height}")
                        }
                ) {
                    // 通知内容（JSONデータから生成）
                    val content = remember(notificationData) { contentGenerator.generateContent(notificationData) }
                    android.util.Log.d("NotificationHistoryScreen", "表示する本文: $content")
                    
                    // ヘッダー
                    if (notificationData.summary.isNotEmpty()) {
                        Text(
                            text = notificationData.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    // 今月まきどきの種
                    if (notificationData.thisMonthSeeds.isNotEmpty()) {
                        RichSection(
                            title = "🌱 今月まきどきの種",
                            items = notificationData.thisMonthSeeds.map { it.name to it.description }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    // 終了間近の種
                    if (notificationData.endingSoonSeeds.isNotEmpty()) {
                        RichSection(
                            title = "終了間近",
                            items = notificationData.endingSoonSeeds.map { it.name to it.description },
                            iconResource = R.drawable.warning
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    // おすすめの種
                    if (notificationData.recommendedSeeds.isNotEmpty()) {
                        RichSection(
                            title = "🌟 今月のおすすめ種",
                            items = notificationData.recommendedSeeds.map { it.name to it.description }
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
private fun RichSection(title: String, items: List<Pair<String, String>>, iconResource: Int? = null) {
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
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    if (items.isEmpty()) {
        Text(
            text = "該当なし",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (desc.isNotEmpty()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
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

        android.util.Log.d("NotificationHistoryScreen", "extractAdviceAndSignature - 入力内容の行数: ${lines.size}")
        android.util.Log.d("NotificationHistoryScreen", "extractAdviceAndSignature - 最後の10行:")
        for (i in maxOf(0, lines.size - 10) until lines.size) {
            android.util.Log.d("NotificationHistoryScreen", "行${i}: '${lines[i].trim()}'")
        }

        // 最後の数行からアドバイスと署名を探す
        for (i in lines.size - 1 downTo maxOf(0, lines.size - 10)) {
            val line = lines[i].trim()

            // 署名を探す
            if (line.contains("佐々木助三郎 拝") || line.contains("助三郎 より") || line.contains("助さんより")) {
                signature = line
                android.util.Log.d("NotificationHistoryScreen", "署名を発見: '$signature'")
            }
            // アドバイスを探す（署名の前の行で、短い文）
            else if (line.isNotEmpty() && line.length <= 50 && !line.startsWith("🌱") && !line.startsWith("⚠️") && !line.startsWith("🌟") && !line.startsWith("【") && !line.contains("佐々木助三郎") && !line.contains("助三郎") && !line.contains("助さん")) {
                if (advice.isEmpty()) {
                    advice = line
                    android.util.Log.d("NotificationHistoryScreen", "アドバイスを発見: '$advice'")
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

