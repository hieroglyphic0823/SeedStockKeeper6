package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import com.example.seedstockkeeper6.notification.NotificationManager
import com.example.seedstockkeeper6.notification.NotificationScheduler
import com.example.seedstockkeeper6.service.GeminiNotificationService
import com.example.seedstockkeeper6.service.NotificationHistoryService
import com.example.seedstockkeeper6.model.NotificationType
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.activity.compose.LocalActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreviewScreen(
    navController: NavController,
    context: android.content.Context,
    onRefreshUnreadCount: () -> Unit = {}
) {
    
    val notificationManager = remember { NotificationManager(context, onRefreshUnreadCount) }
    val notificationScheduler = remember { NotificationScheduler(context) }
    val geminiService = remember { GeminiNotificationService() }
    val historyService = remember { NotificationHistoryService() }
    val scope = rememberCoroutineScope()
    val seedListViewModel = viewModel<SeedListViewModel>()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { Firebase.firestore }
    val activity = LocalActivity.current
    
    var showMonthlyPreview by remember { mutableStateOf(false) }
    var showWeeklyPreview by remember { mutableStateOf(false) }
    var monthlyPreviewContent by remember { mutableStateOf("") }
    var weeklyPreviewContent by remember { mutableStateOf("") }
    var monthlyPreviewTitle by remember { mutableStateOf("") }
    var weeklyPreviewTitle by remember { mutableStateOf("") }
    var userSeeds by remember { mutableStateOf<List<com.example.seedstockkeeper6.model.SeedPacket>>(emptyList()) }
    var userSettings by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var isOcrSuccessful by remember { mutableStateOf(false) } // OCR成功状態
    
    // 画面表示時にユーザーデータを読み込む
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            errorMessage = ""
            
            // 認証状態を確認
            val currentUser = auth.currentUser
            if (currentUser == null) {
                errorMessage = "認証されていません。デモデータでプレビューを表示します。"
            }
            
            // Firestoreからデータを取得を試行し、失敗した場合はデモデータを使用
            val (seeds, settings) = try {
                loadUserData(auth, db)
            } catch (e: Exception) {
                getDemoData()
            }
            
                userSeeds = seeds
                userSettings = settings
                
                // OCR成功状態を判定（種データに画像URLが含まれている場合）
                isOcrSuccessful = seeds.any { seed -> 
                    seed.imageUrls.isNotEmpty() && seed.imageUrls.any { url -> url.isNotEmpty() }
                }
        } catch (e: Exception) {
            
            // エラーの種類に応じて適切なメッセージを設定
            errorMessage = when {
                e.message?.contains("PERMISSION_DENIED") == true -> 
                    "Firestoreの権限エラーが発生しました。認証が必要です。"
                e.message?.contains("UNAVAILABLE") == true -> 
                    "ネットワーク接続エラーが発生しました。"
                else -> 
                    "データの読み込みに失敗しました: ${e.message}"
            }
        } finally {
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ローディング状態の表示
        if (isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("データを読み込み中...")
                }
            }
        }
        
        // エラーメッセージの表示
        if (errorMessage.isNotEmpty()) {
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (errorMessage.contains("認証")) {
                            "デモデータを使用してプレビューを表示します。\n実際のデータを使用するにはログインしてください。"
                        } else {
                            "デモデータを使用してプレビューを表示します。"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // 通知テストセクション
        NotificationTestCard(
            onMonthlyTest = {
                scope.launch {
                    try {
                        val farmOwnerValue = userSettings["farmOwner"] ?: "水戸黄門"
                        val title = geminiService.generateMonthlyNotificationTitle(
                            region = userSettings["defaultRegion"] ?: "温暖地",
                            prefecture = userSettings["selectedPrefecture"] ?: "",
                            seedInfoUrl = getSeedInfoUrl(userSettings),
                            userSeeds = userSeeds,
                            currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                            farmOwner = farmOwnerValue,
                            customFarmOwner = userSettings["customFarmOwner"] ?: ""
                        )
                        val content = geminiService.generateMonthlyNotificationContent(
                            region = userSettings["defaultRegion"] ?: "温暖地",
                            prefecture = userSettings["selectedPrefecture"] ?: "",
                            seedInfoUrl = getSeedInfoUrl(userSettings),
                            currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                            userSeeds = userSeeds,
                            farmOwner = farmOwnerValue,
                            customFarmOwner = userSettings["customFarmOwner"] ?: ""
                        )
                        
                        // 通知権限をチェック
                        if (!notificationManager.hasNotificationPermission()) {
                            if (activity != null) {
                                (activity as com.example.seedstockkeeper6.MainActivity).requestNotificationPermission()
                            }
                            return@launch
                        }
                        
                        // 現在のユーザーIDを取得
                        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                        
                        notificationManager.sendMonthlyRecommendationNotificationWithContent(
                            title = title,
                            content = content,
                            farmOwner = farmOwnerValue,
                            region = userSettings["defaultRegion"] ?: "温暖地",
                            prefecture = userSettings["selectedPrefecture"] ?: "",
                            month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                            seedCount = userSeeds.size,
                            userId = currentUserId ?: "unknown_user"
                        )
                        // 通知作成後に少し遅延してから未読数を更新
                        android.util.Log.d("NotificationPreviewScreen", "月次通知作成完了、未読数更新を開始")
                        kotlinx.coroutines.delay(1000)
                        onRefreshUnreadCount()
                        android.util.Log.d("NotificationPreviewScreen", "未読数更新完了")
                    } catch (e: Exception) {
                        android.util.Log.e("NotificationPreviewScreen", "月次通知作成エラー", e)
                    }
                }
            },
            onWeeklyTest = {
                scope.launch {
                    try {
                        val farmOwnerValue = userSettings["farmOwner"] ?: "水戸黄門"
                        val title = geminiService.generateWeeklyNotificationTitle(
                            userSeeds = userSeeds,
                            farmOwner = farmOwnerValue,
                            customFarmOwner = userSettings["customFarmOwner"] ?: ""
                        )
                        val content = geminiService.generateWeeklyNotificationContent(
                            userSeeds = userSeeds,
                            farmOwner = farmOwnerValue,
                            customFarmOwner = userSettings["customFarmOwner"] ?: ""
                        )
                        
                        // 通知権限をチェック
                        if (!notificationManager.hasNotificationPermission()) {
                            if (activity != null) {
                                (activity as com.example.seedstockkeeper6.MainActivity).requestNotificationPermission()
                            }
                            return@launch
                        }
                        
                        notificationManager.sendWeeklyReminderNotificationWithContent(
                            title = title,
                            content = content,
                            farmOwner = farmOwnerValue,
                            region = userSettings["defaultRegion"] ?: "温暖地",
                            prefecture = userSettings["selectedPrefecture"] ?: "",
                            month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                            seedCount = userSeeds.size
                        )
                        // 通知作成後に少し遅延してから未読数を更新
                        android.util.Log.d("NotificationPreviewScreen", "週次通知作成完了、未読数更新を開始")
                        kotlinx.coroutines.delay(1000)
                        onRefreshUnreadCount()
                        android.util.Log.d("NotificationPreviewScreen", "未読数更新完了")
                    } catch (e: Exception) {
                        android.util.Log.e("NotificationPreviewScreen", "週次通知作成エラー", e)
                    }
                }
            }
        )
        
        // 通知プレビューセクション
        NotificationPreviewCard(
            showMonthlyPreview = showMonthlyPreview,
            showWeeklyPreview = showWeeklyPreview,
            monthlyPreviewContent = monthlyPreviewContent,
            weeklyPreviewContent = weeklyPreviewContent,
            monthlyPreviewTitle = monthlyPreviewTitle,
            weeklyPreviewTitle = weeklyPreviewTitle,
            isOcrSuccessful = isOcrSuccessful,
            onMonthlyPreviewToggle = {
                showMonthlyPreview = !showMonthlyPreview
                if (showMonthlyPreview) {
                    
                    // ローディング状態を表示
                    monthlyPreviewContent = "通知内容を生成中..."
                    monthlyPreviewTitle = "通知タイトルを生成中..."
                    
                    // 実際の内容を生成
                    scope.launch {
                        try {
                            val farmOwnerValue = userSettings["farmOwner"] ?: "水戸黄門"
                            val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
                            
                            // タイトルとコンテンツを並行生成
                            val titleDeferred = scope.async {
                                val farmAddressValue = userSettings["farmAddress"] ?: ""
                                geminiService.generateMonthlyNotificationTitle(
                                    region = userSettings["defaultRegion"] ?: "温暖地",
                                    prefecture = userSettings["selectedPrefecture"] ?: "",
                                    seedInfoUrl = getSeedInfoUrl(userSettings),
                                    userSeeds = userSeeds,
                                    currentMonth = currentMonth,
                                    farmOwner = farmOwnerValue,
                                    customFarmOwner = userSettings["customFarmOwner"] ?: "",
                                    farmAddress = farmAddressValue
                                )
                            }
                            
                            val contentDeferred = scope.async {
                                geminiService.generateMonthlyNotificationContent(
                                    region = userSettings["defaultRegion"] ?: "温暖地",
                                    prefecture = userSettings["selectedPrefecture"] ?: "",
                                    seedInfoUrl = getSeedInfoUrl(userSettings),
                                    userSeeds = userSeeds,
                                    currentMonth = currentMonth,
                                    farmOwner = farmOwnerValue,
                                    customFarmOwner = userSettings["customFarmOwner"] ?: ""
                                )
                            }
                            
                            // 両方の結果を待つ
                            monthlyPreviewTitle = titleDeferred.await()
                            monthlyPreviewContent = contentDeferred.await()
                            
                        } catch (e: Exception) {
                            if (e.message?.contains("overloaded") == true || e.message?.contains("503") == true) {
                                monthlyPreviewTitle = "API過負荷"
                                monthlyPreviewContent = "API過負荷のため通知を作成できません。しばらく時間をおいてから再度お試しください。"
                            } else {
                                monthlyPreviewTitle = "今月の種まきおすすめ"
                                monthlyPreviewContent = "通知内容の生成に失敗しました。\n\n${buildMonthlyNotificationPreview()}"
                            }
                        }
                    }
                }
            },
            onWeeklyPreviewToggle = {
                showWeeklyPreview = !showWeeklyPreview
                if (showWeeklyPreview) {
                    // ローディング状態を表示
                    weeklyPreviewContent = "通知内容を生成中..."
                    weeklyPreviewTitle = "通知タイトルを生成中..."
                    
                    scope.launch {
                        try {
                            val farmOwnerValue = userSettings["farmOwner"] ?: "水戸黄門"
                            weeklyPreviewContent = geminiService.generateWeeklyNotificationContent(
                                userSeeds = userSeeds,
                                farmOwner = farmOwnerValue,
                                customFarmOwner = userSettings["customFarmOwner"] ?: ""
                            )
                            weeklyPreviewTitle = "まき時終了の2週間前の種があります"
                        } catch (e: Exception) {
                            if (e.message?.contains("overloaded") == true || e.message?.contains("503") == true) {
                                weeklyPreviewTitle = "API過負荷"
                                weeklyPreviewContent = "API過負荷のため通知を作成できません。しばらく時間をおいてから再度お試しください。"
                            } else {
                                weeklyPreviewTitle = "まき時終了の2週間前の種があります"
                                weeklyPreviewContent = "通知内容の生成に失敗しました。\n\n${buildWeeklyNotificationPreview()}"
                            }
                        }
                    }
                }
            }
        )
    }
}
