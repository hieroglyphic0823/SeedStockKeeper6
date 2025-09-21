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
    context: android.content.Context
) {
    android.util.Log.d("NotificationPreviewScreen", "NotificationPreviewScreen composable開始")
    
    val notificationManager = remember { NotificationManager(context) }
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
                android.util.Log.w("NotificationPreviewScreen", "ユーザーが認証されていません。デモデータを使用します。")
                errorMessage = "認証されていません。デモデータでプレビューを表示します。"
            }
            
            // Firestoreからデータを取得を試行し、失敗した場合はデモデータを使用
            val (seeds, settings) = try {
                loadUserData(auth, db)
            } catch (e: Exception) {
                android.util.Log.w("NotificationPreviewScreen", "Firestoreデータ取得に失敗、デモデータを使用: ${e.message}")
                getDemoData()
            }
            
                userSeeds = seeds
                userSettings = settings
                
                // OCR成功状態を判定（種データに画像URLが含まれている場合）
                isOcrSuccessful = seeds.any { seed -> 
                    seed.imageUrls.isNotEmpty() && seed.imageUrls.any { url -> url.isNotEmpty() }
                }
                android.util.Log.d("NotificationPreviewScreen", "データ読み込み完了 - 種: ${seeds.size}件, 設定: ${settings.size}件, OCR成功: $isOcrSuccessful")
        } catch (e: Exception) {
            android.util.Log.e("NotificationPreviewScreen", "データ読み込みエラー", e)
            
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
                        android.util.Log.d("NotificationPreviewScreen", "通知生成時のuserSettings: $userSettings")
                        val farmOwnerValue = userSettings["farmOwner"] ?: "水戸黄門"
                        android.util.Log.d("NotificationPreviewScreen", "使用するfarmOwner: $farmOwnerValue")
                        
                        android.util.Log.d("NotificationPreviewScreen", "GeminiAPI呼び出し開始")
                        val content = geminiService.generateMonthlyNotificationContent(
                            region = userSettings["defaultRegion"] ?: "温暖地",
                            prefecture = userSettings["selectedPrefecture"] ?: "",
                            seedInfoUrl = getSeedInfoUrl(userSettings),
                            currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                            userSeeds = userSeeds,
                            farmOwner = farmOwnerValue,
                            customFarmOwner = userSettings["customFarmOwner"] ?: ""
                        )
                        android.util.Log.d("NotificationPreviewScreen", "GeminiAPI呼び出し完了 - content: ${content.take(100)}...")
                        android.util.Log.d("NotificationPreviewScreen", "通知送信開始")
                        
                        // 通知権限をチェック
                        if (!notificationManager.hasNotificationPermission()) {
                            android.util.Log.w("NotificationPreviewScreen", "通知権限が許可されていません")
                            if (activity != null) {
                                (activity as com.example.seedstockkeeper6.MainActivity).requestNotificationPermission()
                            }
                            return@launch
                        }
                        
                        notificationManager.sendMonthlyRecommendationNotificationWithContent(
                            content = content,
                            farmOwner = farmOwnerValue,
                            region = userSettings["defaultRegion"] ?: "温暖地",
                            prefecture = userSettings["selectedPrefecture"] ?: "",
                            month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                            seedCount = userSeeds.size
                        )
                        android.util.Log.d("NotificationPreviewScreen", "通知送信完了")
                    } catch (e: Exception) {
                        android.util.Log.e("NotificationPreviewScreen", "月次通知送信エラー", e)
                    }
                }
            },
            onWeeklyTest = {
                scope.launch {
                    try {
                        android.util.Log.d("NotificationPreviewScreen", "週次通知生成時のuserSettings: $userSettings")
                        val farmOwnerValue = userSettings["farmOwner"] ?: "水戸黄門"
                        android.util.Log.d("NotificationPreviewScreen", "使用するfarmOwner: $farmOwnerValue")
                        
                        android.util.Log.d("NotificationPreviewScreen", "週次GeminiAPI呼び出し開始")
                        val content = geminiService.generateWeeklyNotificationContent(
                            region = userSettings["defaultRegion"] ?: "温暖地",
                            prefecture = userSettings["selectedPrefecture"] ?: "",
                            seedInfoUrl = getSeedInfoUrl(userSettings),
                            userSeeds = userSeeds,
                            farmOwner = farmOwnerValue,
                            customFarmOwner = userSettings["customFarmOwner"] ?: ""
                        )
                        android.util.Log.d("NotificationPreviewScreen", "週次GeminiAPI呼び出し完了 - content: ${content.take(100)}...")
                        android.util.Log.d("NotificationPreviewScreen", "週次通知送信開始")
                        
                        // 通知権限をチェック
                        if (!notificationManager.hasNotificationPermission()) {
                            android.util.Log.w("NotificationPreviewScreen", "通知権限が許可されていません")
                            if (activity != null) {
                                (activity as com.example.seedstockkeeper6.MainActivity).requestNotificationPermission()
                            }
                            return@launch
                        }
                        
                        notificationManager.sendWeeklyReminderNotificationWithContent(
                            content = content,
                            farmOwner = farmOwnerValue,
                            region = userSettings["defaultRegion"] ?: "温暖地",
                            prefecture = userSettings["selectedPrefecture"] ?: "",
                            seedCount = userSeeds.size
                        )
                        android.util.Log.d("NotificationPreviewScreen", "週次通知送信完了")
                    } catch (e: Exception) {
                        android.util.Log.e("NotificationPreviewScreen", "週次通知送信エラー", e)
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
                android.util.Log.d("NotificationPreviewScreen", "月次プレビューボタン押下 - showMonthlyPreview: $showMonthlyPreview")
                showMonthlyPreview = !showMonthlyPreview
                if (showMonthlyPreview) {
                    android.util.Log.d("NotificationPreviewScreen", "月次プレビュー生成開始 - userSeeds: ${userSeeds.size}件, userSettings: $userSettings")
                    
                    // ローディング状態を表示
                    monthlyPreviewContent = "通知内容を生成中..."
                    monthlyPreviewTitle = "通知タイトルを生成中..."
                    
                    // 実際の内容を生成
                    scope.launch {
                        try {
                            val farmOwnerValue = userSettings["farmOwner"] ?: "水戸黄門"
                            val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
                            android.util.Log.d("NotificationPreviewScreen", "月次プレビュー生成 - farmOwner: $farmOwnerValue")
                            
                            // タイトルとコンテンツを並行生成
                            val titleDeferred = scope.async {
                                geminiService.generateMonthlyNotificationTitle(
                                    currentMonth = currentMonth,
                                    farmOwner = farmOwnerValue,
                                    customFarmOwner = userSettings["customFarmOwner"] ?: ""
                                )
                            }
                            
                            val contentDeferred = scope.async {
                                android.util.Log.d("NotificationPreviewScreen", "月次プレビューGeminiAPI呼び出し開始")
                                geminiService.generateMonthlyNotificationContent(
                                    region = userSettings["defaultRegion"] ?: "温暖地",
                                    prefecture = userSettings["selectedPrefecture"] ?: "",
                                    seedInfoUrl = getSeedInfoUrl(userSettings),
                                    currentMonth = currentMonth,
                                    userSeeds = userSeeds,
                                    farmOwner = farmOwnerValue,
                                    customFarmOwner = userSettings["customFarmOwner"] ?: "",
                                    userSettings = userSettings
                                )
                            }
                            
                            // 両方の結果を待つ
                            monthlyPreviewTitle = titleDeferred.await()
                            monthlyPreviewContent = contentDeferred.await()
                            
                            android.util.Log.d("NotificationPreviewScreen", "月次プレビュー生成完了 - title: $monthlyPreviewTitle, content: ${monthlyPreviewContent.take(100)}...")
                        } catch (e: Exception) {
                            android.util.Log.e("NotificationPreviewScreen", "月次プレビュー生成エラー", e)
                            android.util.Log.e("NotificationPreviewScreen", "エラー詳細: ${e.javaClass.simpleName} - ${e.message}")
                            if (e.message?.contains("overloaded") == true || e.message?.contains("503") == true) {
                                android.util.Log.w("NotificationPreviewScreen", "API過負荷のため、通知を作成できません")
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
                            android.util.Log.d("NotificationPreviewScreen", "週次プレビューGeminiAPI呼び出し開始")
                            weeklyPreviewContent = geminiService.generateWeeklyNotificationContent(
                                region = userSettings["defaultRegion"] ?: "温暖地",
                                prefecture = userSettings["selectedPrefecture"] ?: "",
                                seedInfoUrl = getSeedInfoUrl(userSettings),
                                userSeeds = userSeeds,
                                farmOwner = farmOwnerValue,
                                customFarmOwner = userSettings["customFarmOwner"] ?: "",
                                userSettings = userSettings
                            )
                            weeklyPreviewTitle = "まき時終了の2週間前の種があります"
                            android.util.Log.d("NotificationPreviewScreen", "週次プレビューGeminiAPI呼び出し完了")
                        } catch (e: Exception) {
                            android.util.Log.e("NotificationPreviewScreen", "週次プレビュー生成エラー", e)
                            if (e.message?.contains("overloaded") == true || e.message?.contains("503") == true) {
                                android.util.Log.w("NotificationPreviewScreen", "API過負荷のため、通知を作成できません")
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