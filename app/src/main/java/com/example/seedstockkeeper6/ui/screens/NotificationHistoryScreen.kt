package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.navigation.NavController
import com.example.seedstockkeeper6.model.NotificationHistory
import com.example.seedstockkeeper6.model.NotificationType
import com.example.seedstockkeeper6.service.NotificationHistoryService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(
    navController: NavController
) {
    val historyService = remember { NotificationHistoryService() }
    val scope = rememberCoroutineScope()
    var histories by remember { mutableStateOf<List<NotificationHistory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    
    // 通知履歴を読み込み
    LaunchedEffect(Unit) {
        try {
            android.util.Log.d("NotificationHistoryScreen", "通知履歴読み込み開始")
            isLoading = true
            errorMessage = ""
            val result = historyService.getUserNotificationHistory()
            android.util.Log.d("NotificationHistoryScreen", "通知履歴読み込み完了 - 取得件数: ${result.size}")
            android.util.Log.d("NotificationHistoryScreen", "取得した履歴: $result")
            histories = result
        } catch (e: Exception) {
            android.util.Log.e("NotificationHistoryScreen", "通知履歴の読み込みに失敗", e)
            errorMessage = "通知履歴の読み込みに失敗しました: ${e.message}"
        } finally {
            isLoading = false
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
            // 通知履歴リスト
            else if (histories.isEmpty()) {
                android.util.Log.d("NotificationHistoryScreen", "空の履歴を表示 - histories.isEmpty() = true")
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
                android.util.Log.d("NotificationHistoryScreen", "履歴リストを表示 - 件数: ${histories.size}")
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(histories) { history ->
                        NotificationHistoryCard(
                            history = history,
                            onDelete = { documentId ->
                                scope.launch {
                                    val success = historyService.deleteNotificationHistory(documentId)
                                    if (success) {
                                        // 削除成功時はリストから除外
                                        histories = histories.filter { it.documentId != documentId }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationHistoryCard(
    history: NotificationHistory,
    onDelete: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (history.type) {
                NotificationType.MONTHLY -> MaterialTheme.colorScheme.primaryContainer
                NotificationType.WEEKLY -> MaterialTheme.colorScheme.secondaryContainer
                NotificationType.CUSTOM -> MaterialTheme.colorScheme.tertiaryContainer
            }
        ),
        onClick = { showDetailDialog = true }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ヘッダー（タイトルのみ）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = history.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "削除",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // カード本体（3行: タイトルの下に「今月まき時」「まき時終了」）
            val sectionSummary = remember(history) {
                if (history.thisMonthSeeds.isNotEmpty() || history.endingSoonSeeds.isNotEmpty()) {
                    SectionSummary(
                        thisMonth = history.thisMonthSeeds.firstOrNull() ?: "",
                        endingSoon = history.endingSoonSeeds.firstOrNull() ?: ""
                    )
                } else {
                    extractSectionSummaries(history.content)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "今月のまき時: " + (sectionSummary.thisMonth.ifEmpty { "該当なし" }),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "終了間近: " + (sectionSummary.endingSoon.ifEmpty { "該当なし" }),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // メタ情報
            if (history.farmOwner.isNotEmpty() || history.region.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (history.farmOwner.isNotEmpty()) {
                        Text(
                            text = "👤 ${history.farmOwner}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    if (history.region.isNotEmpty()) {
                        Text(
                            text = "📍 ${history.region}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    if (history.seedCount > 0) {
                        Text(
                            text = "🌱 ${history.seedCount}種類",
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
            title = { 
                Text(
                    text = history.title,
                    style = MaterialTheme.typography.headlineSmall
                ) 
            },
            modifier = Modifier.padding(vertical = 4.dp),
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 650.dp) // 表示領域をさらに拡張
                        .verticalScroll(rememberScrollState())
                ) {
                    // メタ情報
                    if (history.farmOwner.isNotEmpty() || history.region.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (history.farmOwner.isNotEmpty()) {
                                Text(
                                    text = "👤 ${history.farmOwner}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (history.region.isNotEmpty()) {
                                Text(
                                    text = "📍 ${history.region}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp)) // 余白を縮小
                    }
                    
                    // 送信日時
                    Text(
                        text = formatDateTime(history.sentAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp)) // 余白を縮小
                    
                    // 通知内容（全文表示・リッチテキスト風）
                    val display = remember(history.content) { removeJsonCodeBlock(history.content) }
                    val header = remember(display) { display.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }.orEmpty() }
                    if (header.isNotEmpty()) {
                        Text(
                            text = header,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp)) // 余白を縮小
                    }
                    val extractedThisMonth = if (history.thisMonthDetails.isNotEmpty()) history.thisMonthDetails.map { it.name to it.desc } else extractSectionItems(display, sectionMarker = "🌱")
                    val structuredThisMonth = if (history.thisMonthSeeds.isNotEmpty()) history.thisMonthSeeds.map { it to "" } else null
                    RichSection(
                        title = "🌱 今月まきどきの種",
                        items = if (extractedThisMonth.isNotEmpty()) extractedThisMonth else (structuredThisMonth ?: emptyList())
                    )
                    Spacer(modifier = Modifier.height(4.dp)) // 余白を縮小
                    val extractedEnding = if (history.endingSoonDetails.isNotEmpty()) history.endingSoonDetails.map { it.name to it.desc } else extractSectionItems(display, sectionMarker = "⚠️")
                    val structuredEnding = if (history.endingSoonSeeds.isNotEmpty()) history.endingSoonSeeds.map { it to "" } else null
                    RichSection(
                        title = "⚠️ まき時終了間近",
                        items = if (extractedEnding.isNotEmpty()) extractedEnding else (structuredEnding ?: emptyList())
                    )
                    Spacer(modifier = Modifier.height(4.dp)) // 余白を縮小
                    val extractedRec = if (history.recommendedDetails.isNotEmpty()) history.recommendedDetails.map { it.name to it.desc } else extractSectionItems(display, sectionMarker = "🌟")
                    val structuredRec = if (history.recommendedSeeds.isNotEmpty()) history.recommendedSeeds.map { it to "" } else null
                    RichSection(
                        title = "🌟 今月のおすすめ種",
                        items = if (extractedRec.isNotEmpty()) extractedRec else (structuredRec ?: emptyList())
                    )
                    
                    // 署名部分を抽出して右寄せで表示
                    val signature = extractSignature(display)
                    if (signature.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = signature,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDetailDialog = false }
                ) {
                    Text("閉じる")
                }
            }
        )
    }
    
    // 削除確認ダイアログ
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("通知履歴を削除") },
            text = { Text("この通知履歴を削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        history.documentId?.let { onDelete(it) }
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("キャンセル")
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
private fun RichSection(title: String, items: List<Pair<String, String>>) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
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
                    val name = nameInQuote ?: raw
                    // 説明は次行（箇条書きや見出しでない）を説明として扱う
                    val desc = if (j + 1 < lines.size) {
                        val next = lines[j + 1].trim()
                        if (!next.startsWith("• ") && !next.startsWith("* ") && !next.startsWith("- ") && !next.startsWith("🌱") && !next.startsWith("⚠️") && !next.startsWith("🌟") && !next.startsWith("```")) next else ""
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
