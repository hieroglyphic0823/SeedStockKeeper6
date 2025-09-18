package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
            isLoading = true
            errorMessage = ""
            histories = historyService.getUserNotificationHistory()
        } catch (e: Exception) {
            errorMessage = "通知履歴の読み込みに失敗しました: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通知履歴") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📭",
                            style = MaterialTheme.typography.displayLarge
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
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (history.type) {
                NotificationType.MONTHLY -> MaterialTheme.colorScheme.primaryContainer
                NotificationType.WEEKLY -> MaterialTheme.colorScheme.secondaryContainer
                NotificationType.CUSTOM -> MaterialTheme.colorScheme.tertiaryContainer
            }
        ),
        onClick = { /* 詳細表示など */ }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ヘッダー部分
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
                    Text(
                        text = history.type.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
            
            // 送信日時
            Text(
                text = formatDateTime(history.sentAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            // 通知内容（プレビュー）
            Text(
                text = history.content.take(100) + if (history.content.length > 100) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
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
