package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.seedstockkeeper6.notification.NotificationManager
import com.example.seedstockkeeper6.notification.NotificationScheduler
import com.example.seedstockkeeper6.service.GeminiNotificationService
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreviewScreen(
    navController: NavController,
    context: android.content.Context
) {
    val notificationManager = remember { NotificationManager(context) }
    val notificationScheduler = remember { NotificationScheduler(context) }
    val geminiService = remember { GeminiNotificationService() }
    val scope = rememberCoroutineScope()
    val seedListViewModel: SeedListViewModel = viewModel()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { Firebase.firestore }
    
    var showMonthlyPreview by remember { mutableStateOf(false) }
    var showWeeklyPreview by remember { mutableStateOf(false) }
    var monthlyPreviewContent by remember { mutableStateOf("") }
    var weeklyPreviewContent by remember { mutableStateOf("") }
    var userSeeds by remember { mutableStateOf<List<com.example.seedstockkeeper6.model.SeedPacket>>(emptyList()) }
    var userSettings by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    
    // 実際のユーザーデータを取得する関数
    suspend fun loadUserData() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                // ユーザーの種データを取得（今月関連のみをフィルタリング）
                try {
                    android.util.Log.d("NotificationPreviewScreen", "種データ取得開始 - UID: $uid")
                    val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
                    android.util.Log.d("NotificationPreviewScreen", "現在の月: $currentMonth")
                    
                    val seedsSnapshot = db.collection("seeds")
                        .whereEqualTo("ownerUid", uid)
                        .get().await()
                    
                    val seedsThisMonth = mutableListOf<com.example.seedstockkeeper6.model.SeedPacket>()
                    val seedsEndingThisMonth = mutableListOf<com.example.seedstockkeeper6.model.SeedPacket>()
                    
                    val seeds = seedsSnapshot.documents.mapNotNull { doc ->
                        try {
                            val seed = doc.toObject(com.example.seedstockkeeper6.model.SeedPacket::class.java)
                            if (seed != null) {
                                val seedWithId = seed.copy(id = doc.id, documentId = doc.id)
                                
                                var isThisMonthSowing = false
                                var isEndingThisMonth = false
                                
                                // 今月関連の種かどうかをチェック
                                seedWithId.calendar.forEach { entry ->
                                    if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
                                        try {
                                            val startMonth = entry.sowing_start_date.split("-")[1].toInt()
                                            val endMonth = entry.sowing_end_date.split("-")[1].toInt()
                                            
                                            // 今月が播種期間内かチェック
                                            if (startMonth <= currentMonth && endMonth >= currentMonth) {
                                                isThisMonthSowing = true
                                            }
                                            
                                            // 今月が播種期間の終了月かチェック
                                            if (currentMonth == endMonth) {
                                                isEndingThisMonth = true
                                            }
                                        } catch (e: Exception) {
                                            // 日付解析エラーはスキップ
                                        }
                                    }
                                }
                                
                                if (isThisMonthSowing || isEndingThisMonth) {
                                    if (isThisMonthSowing) {
                                        seedsThisMonth.add(seedWithId)
                                        android.util.Log.d("NotificationPreviewScreen", "今月蒔ける種発見: ${seedWithId.productName}")
                                    }
                                    if (isEndingThisMonth) {
                                        seedsEndingThisMonth.add(seedWithId)
                                        android.util.Log.d("NotificationPreviewScreen", "今月蒔き時終了の種発見: ${seedWithId.productName}")
                                    }
                                    seedWithId
                                } else {
                                    null
                                }
                            } else {
                                android.util.Log.w("NotificationPreviewScreen", "Failed to convert document ${doc.id} to SeedPacket")
                                null
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("NotificationPreviewScreen", "種データ変換エラー - docId: ${doc.id}", e)
                            null
                        }
                    }
                    userSeeds = seeds
                    android.util.Log.d("NotificationPreviewScreen", "種データ取得完了 - 全件数: ${seedsSnapshot.documents.size}, 今月関連: ${seeds.size}")
                    android.util.Log.d("NotificationPreviewScreen", "今月蒔ける種: ${seedsThisMonth.size}件, 今月蒔き時終了の種: ${seedsEndingThisMonth.size}件")
                } catch (e: Exception) {
                    android.util.Log.e("NotificationPreviewScreen", "種データ取得に失敗", e)
                    userSeeds = emptyList()
                }
                
                // ユーザーの設定を取得
                try {
                    android.util.Log.d("NotificationPreviewScreen", "Firebase設定取得開始 - UID: $uid")
                    val settingsDoc = db.collection("users").document(uid)
                        .collection("settings").document("general").get().await()
                    android.util.Log.d("NotificationPreviewScreen", "Firebase設定取得完了")
                    
                    android.util.Log.d("NotificationPreviewScreen", "Firebase設定ドキュメント存在: ${settingsDoc.exists()}")
                    
                    val settings = if (settingsDoc.exists()) {
                        val farmOwnerFromFirebase = settingsDoc.getString("farmOwner")
                        val customFarmOwnerFromFirebase = settingsDoc.getString("customFarmOwner")
                        android.util.Log.d("NotificationPreviewScreen", "Firebaseから取得 - farmOwner: $farmOwnerFromFirebase, customFarmOwner: $customFarmOwnerFromFirebase")
                        
                        mapOf(
                            "defaultRegion" to (settingsDoc.getString("defaultRegion") ?: "温暖地"),
                            "selectedPrefecture" to (settingsDoc.getString("selectedPrefecture") ?: ""),
                            "farmOwner" to (farmOwnerFromFirebase ?: "水戸黄門"),
                            "customFarmOwner" to (customFarmOwnerFromFirebase ?: ""),
                            "seedInfoUrlProvider" to (settingsDoc.getString("seedInfoUrlProvider") ?: "サカタのたね"),
                            "customSeedInfoUrl" to (settingsDoc.getString("customSeedInfoUrl") ?: "")
                        )
                    } else {
                        android.util.Log.d("NotificationPreviewScreen", "Firebase設定ドキュメントが存在しません")
                        mapOf(
                            "defaultRegion" to "温暖地",
                            "selectedPrefecture" to "",
                            "farmOwner" to "水戸黄門",
                            "customFarmOwner" to "",
                            "seedInfoUrlProvider" to "サカタのたね",
                            "customSeedInfoUrl" to ""
                        )
                    }
                    userSettings = settings
                    android.util.Log.d("NotificationPreviewScreen", "取得した設定: $settings")
                    android.util.Log.d("NotificationPreviewScreen", "農園主設定 - farmOwner: ${settings["farmOwner"]}, customFarmOwner: ${settings["customFarmOwner"]}")
                    android.util.Log.d("NotificationPreviewScreen", "userSettings更新後: $userSettings")
                } catch (e: Exception) {
                    android.util.Log.e("NotificationPreviewScreen", "Firebase設定取得に失敗", e)
                    // エラー時はデフォルト値を設定
                    userSettings = mapOf(
                        "defaultRegion" to "温暖地",
                        "selectedPrefecture" to "",
                        "farmOwner" to "水戸黄門",
                        "customFarmOwner" to "",
                        "seedInfoUrlProvider" to "サカタのたね",
                        "customSeedInfoUrl" to ""
                    )
                }
                
            } catch (e: Exception) {
                android.util.Log.e("NotificationPreviewScreen", "ユーザーデータの取得に失敗", e)
                // エラー時はデフォルト値を設定
                userSettings = mapOf(
                    "defaultRegion" to "温暖地",
                    "selectedPrefecture" to "",
                    "farmOwner" to "水戸黄門",
                    "customFarmOwner" to "",
                    "seedInfoUrlProvider" to "サカタのたね",
                    "customSeedInfoUrl" to ""
                )
            }
        }
    }
    
    // 画面表示時にユーザーデータを読み込む
    LaunchedEffect(Unit) {
        loadUserData()
    }
    
    // 種情報URLを取得する関数
    fun getSeedInfoUrl(): String {
        val provider = userSettings["seedInfoUrlProvider"] ?: "サカタのたね"
        val customUrl = userSettings["customSeedInfoUrl"] ?: ""
        
        return when (provider) {
            "サカタのたね" -> "https://sakata-netshop.com/shop/default.aspx"
            "たねのタキイ" -> "https://sakata-netshop.com/shop/pages/sowingcalendar.aspx"
            "その他" -> customUrl
            else -> "https://sakata-netshop.com/shop/default.aspx"
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通知テスト・プレビュー") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 通知テストセクション
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.NotificationsActive,
                            contentDescription = "通知テスト",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "通知テスト",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Text(
                        text = "実際の通知を送信してテストできます",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        // 実際のユーザーデータを取得
                                        loadUserData()
                                        
                                        android.util.Log.d("NotificationPreviewScreen", "通知生成時のuserSettings: $userSettings")
                                        val farmOwnerValue = userSettings["farmOwner"] ?: "水戸黄門"
                                        android.util.Log.d("NotificationPreviewScreen", "使用するfarmOwner: $farmOwnerValue")
                                        
                                        val content = geminiService.generateMonthlyNotificationContent(
                                            region = userSettings["defaultRegion"] ?: "温暖地",
                                            prefecture = userSettings["selectedPrefecture"] ?: "",
                                            seedInfoUrl = getSeedInfoUrl(),
                                            currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                                            userSeeds = userSeeds,
                                            farmOwner = farmOwnerValue,
                                            customFarmOwner = userSettings["customFarmOwner"] ?: ""
                                        )
                                        notificationManager.sendMonthlyRecommendationNotificationWithContent(content)
                                    } catch (e: Exception) {
                                        // エラーハンドリング
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text("月次通知テスト")
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        // 実際のユーザーデータを取得
                                        loadUserData()
                                        
                                        val content = geminiService.generateWeeklyNotificationContent(
                                            region = userSettings["defaultRegion"] ?: "温暖地",
                                            prefecture = userSettings["selectedPrefecture"] ?: "",
                                            seedInfoUrl = getSeedInfoUrl(),
                                            userSeeds = userSeeds,
                                            farmOwner = userSettings["farmOwner"] ?: "水戸黄門",
                                            customFarmOwner = userSettings["customFarmOwner"] ?: ""
                                        )
                                        notificationManager.sendWeeklyReminderNotificationWithContent(content)
                                    } catch (e: Exception) {
                                        // エラーハンドリング
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text("週次通知テスト")
                        }
                    }
                    
                    Button(
                        onClick = { notificationManager.cancelAllNotifications() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            Icons.Filled.NotificationsOff,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("すべての通知をキャンセル")
                    }
                }
            }
            
            // 通知プレビューセクション
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "通知プレビュー",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "通知プレビュー",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Text(
                        text = "通知の内容をプレビューできます",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                showMonthlyPreview = !showMonthlyPreview
                                if (showMonthlyPreview && monthlyPreviewContent.isEmpty()) {
                                    // プレビュー内容を生成
                                    scope.launch {
                                        try {
                                            // 実際のユーザーデータを取得
                                            loadUserData()
                                            
                                            android.util.Log.d("NotificationPreviewScreen", "プレビュー生成時のuserSettings: $userSettings")
                                            val farmOwnerValue = userSettings["farmOwner"] ?: "水戸黄門"
                                            android.util.Log.d("NotificationPreviewScreen", "プレビューで使用するfarmOwner: $farmOwnerValue")
                                            
                                            monthlyPreviewContent = geminiService.generateMonthlyNotificationContent(
                                                region = userSettings["defaultRegion"] ?: "温暖地",
                                                prefecture = userSettings["selectedPrefecture"] ?: "",
                                                seedInfoUrl = getSeedInfoUrl(),
                                                currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                                                userSeeds = userSeeds,
                                                farmOwner = farmOwnerValue,
                                                customFarmOwner = userSettings["customFarmOwner"] ?: ""
                                            )
                                        } catch (e: Exception) {
                                            monthlyPreviewContent = buildMonthlyNotificationPreview()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showMonthlyPreview) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = if (showMonthlyPreview) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("月次プレビュー")
                        }
                        
                        Button(
                            onClick = { 
                                showWeeklyPreview = !showWeeklyPreview
                                if (showWeeklyPreview && weeklyPreviewContent.isEmpty()) {
                                    // プレビュー内容を生成
                                    scope.launch {
                                        try {
                                            // 実際のユーザーデータを取得
                                            loadUserData()
                                            
                                            weeklyPreviewContent = geminiService.generateWeeklyNotificationContent(
                                                region = userSettings["defaultRegion"] ?: "温暖地",
                                                prefecture = userSettings["selectedPrefecture"] ?: "",
                                                seedInfoUrl = getSeedInfoUrl(),
                                                userSeeds = userSeeds,
                                                farmOwner = userSettings["farmOwner"] ?: "水戸黄門",
                                                customFarmOwner = userSettings["customFarmOwner"] ?: ""
                                            )
                                        } catch (e: Exception) {
                                            weeklyPreviewContent = buildWeeklyNotificationPreview()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showWeeklyPreview) 
                                    MaterialTheme.colorScheme.secondary 
                                else 
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = if (showWeeklyPreview) 
                                    MaterialTheme.colorScheme.onSecondary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("週次プレビュー")
                        }
                    }
                }
            }
            
            // 月次通知プレビュー
            if (showMonthlyPreview) {
                NotificationPreviewCard(
                    title = "今月の種まきおすすめ",
                    content = if (monthlyPreviewContent.isNotEmpty()) monthlyPreviewContent else "読み込み中...",
                    iconColor = MaterialTheme.colorScheme.primary
                )
            }
            
            // 週次通知プレビュー
            if (showWeeklyPreview) {
                NotificationPreviewCard(
                    title = "種まきタイミングリマインダー",
                    content = if (weeklyPreviewContent.isNotEmpty()) weeklyPreviewContent else "読み込み中...",
                    iconColor = MaterialTheme.colorScheme.secondary
                )
            }
            
            // 通知スケジュール情報
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "通知スケジュール情報",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    val scheduleStatus = remember { notificationScheduler.getNotificationStatus() }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "月次通知",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = if (scheduleStatus["monthly"] == true) "スケジュール済み" else "未設定",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (scheduleStatus["monthly"] == true) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "週次通知",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = if (scheduleStatus["weekly"] == true) "スケジュール済み" else "未設定",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (scheduleStatus["weekly"] == true) 
                                MaterialTheme.colorScheme.secondary 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationPreviewCard(
    title: String,
    content: String,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 通知アイコン
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = iconColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}

private fun buildMonthlyNotificationPreview(): String {
    return """🌱 今月まき時の種:
• 恋むすめ (ニンジン)
• サラダミックス (レタス)

🌟 季節のおすすめ:
• 春野菜の種まきシーズンです
• トマト、ナス、ピーマンの準備を始めましょう
• レタス、キャベツの種まきが最適です

⚠️ まき時終了間近:
• 春菊 (中葉春菊)"""
}

private fun buildWeeklyNotificationPreview(): String {
    return """⏰ まき時終了の2週間前の種があります:

• 恋むすめ (ニンジン)
  土づくりすれば間に合います！

• 大根 (青首大根)
  土づくりすれば間に合います！"""
}
