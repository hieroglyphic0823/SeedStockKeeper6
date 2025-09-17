package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    
    // 画面表示時にユーザーデータを読み込む
    LaunchedEffect(Unit) {
        val (seeds, settings) = loadUserData(auth, db)
        userSeeds = seeds
        userSettings = settings
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 通知テストセクション
        NotificationTestCard(
            onMonthlyTest = {
                scope.launch {
                    try {
                        android.util.Log.d("NotificationPreviewScreen", "通知生成時のuserSettings: $userSettings")
                        val farmOwnerValue = userSettings["farmOwner"] ?: "水戸黄門"
                        android.util.Log.d("NotificationPreviewScreen", "使用するfarmOwner: $farmOwnerValue")
                        
                        val content = geminiService.generateMonthlyNotificationContent(
                            region = userSettings["defaultRegion"] ?: "温暖地",
                            prefecture = userSettings["selectedPrefecture"] ?: "",
                            seedInfoUrl = getSeedInfoUrl(userSettings),
                            currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                            userSeeds = userSeeds,
                            farmOwner = farmOwnerValue,
                            customFarmOwner = userSettings["customFarmOwner"] ?: ""
                        )
                        notificationManager.sendMonthlyRecommendationNotificationWithContent(content)
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
                        
                        val content = geminiService.generateWeeklyNotificationContent(
                            region = userSettings["defaultRegion"] ?: "温暖地",
                            prefecture = userSettings["selectedPrefecture"] ?: "",
                            seedInfoUrl = getSeedInfoUrl(userSettings),
                            userSeeds = userSeeds,
                            farmOwner = farmOwnerValue,
                            customFarmOwner = userSettings["customFarmOwner"] ?: ""
                        )
                        notificationManager.sendWeeklyReminderNotificationWithContent(content)
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
            onMonthlyPreviewToggle = {
                android.util.Log.d("NotificationPreviewScreen", "月次プレビューボタン押下 - showMonthlyPreview: $showMonthlyPreview")
                showMonthlyPreview = !showMonthlyPreview
                if (showMonthlyPreview) {
                    android.util.Log.d("NotificationPreviewScreen", "月次プレビュー生成開始 - userSeeds: ${userSeeds.size}件, userSettings: $userSettings")
                    
                    // まずデフォルト内容を表示
                    monthlyPreviewContent = buildMonthlyNotificationPreview()
                    android.util.Log.d("NotificationPreviewScreen", "デフォルト月次プレビュー表示 - content: $monthlyPreviewContent")
                    
                    // その後、実際の内容を生成
                    scope.launch {
                        try {
                            val farmOwnerValue = userSettings["farmOwner"] ?: "水戸黄門"
                            android.util.Log.d("NotificationPreviewScreen", "月次プレビュー生成 - farmOwner: $farmOwnerValue")
                            val generatedContent = geminiService.generateMonthlyNotificationContent(
                                region = userSettings["defaultRegion"] ?: "温暖地",
                                prefecture = userSettings["selectedPrefecture"] ?: "",
                                seedInfoUrl = getSeedInfoUrl(userSettings),
                                currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                                userSeeds = userSeeds,
                                farmOwner = farmOwnerValue,
                                customFarmOwner = userSettings["customFarmOwner"] ?: ""
                            )
                            monthlyPreviewContent = generatedContent
                            android.util.Log.d("NotificationPreviewScreen", "月次プレビュー生成完了 - content: $monthlyPreviewContent")
                        } catch (e: Exception) {
                            android.util.Log.e("NotificationPreviewScreen", "月次プレビュー生成エラー", e)
                            // エラーの場合はデフォルト内容を維持
                            android.util.Log.d("NotificationPreviewScreen", "エラーのためデフォルト内容を維持")
                        }
                    }
                }
            },
            onWeeklyPreviewToggle = {
                showWeeklyPreview = !showWeeklyPreview
                if (showWeeklyPreview) {
                    scope.launch {
                        try {
                            val farmOwnerValue = userSettings["farmOwner"] ?: "水戸黄門"
                            weeklyPreviewContent = geminiService.generateWeeklyNotificationContent(
                                region = userSettings["defaultRegion"] ?: "温暖地",
                                prefecture = userSettings["selectedPrefecture"] ?: "",
                                seedInfoUrl = getSeedInfoUrl(userSettings),
                                userSeeds = userSeeds,
                                farmOwner = farmOwnerValue,
                                customFarmOwner = userSettings["customFarmOwner"] ?: ""
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("NotificationPreviewScreen", "週次プレビュー生成エラー", e)
                            weeklyPreviewContent = buildWeeklyNotificationPreview()
                        }
                    }
                }
            }
        )
    }
}