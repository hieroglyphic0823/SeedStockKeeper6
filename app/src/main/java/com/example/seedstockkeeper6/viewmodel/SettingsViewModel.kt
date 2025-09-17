package com.example.seedstockkeeper6.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seedstockkeeper6.notification.NotificationScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsViewModel(private val context: Context? = null) : ViewModel() {
    
    var farmName by mutableStateOf("")
        private set
    
    var defaultRegion by mutableStateOf("")
        private set
    
    var selectedPrefecture by mutableStateOf("")
        private set
    
    // 農園主設定の状態
    var farmOwner by mutableStateOf("水戸黄門") // "水戸黄門", "お銀", "八兵衛", "その他"
        private set
    
    var customFarmOwner by mutableStateOf("") // その他選択時のフリー入力
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var showSnackbar by mutableStateOf<String?>(null)
        private set
    
    var isEditMode by mutableStateOf(false)
        private set
    
    var hasExistingData by mutableStateOf(false)
    
    // 通知設定の状態
    var notificationFrequency by mutableStateOf("なし") // "なし", "月一回", "週１回"
        private set
    
    var selectedWeekday by mutableStateOf("月曜日") // 週１回の場合の曜日選択
        private set
    
    // 種情報URL設定の状態
    var seedInfoUrlProvider by mutableStateOf("サカタのたね") // "サカタのたね", "たねのタキイ", "その他"
        private set
    
    var customSeedInfoUrl by mutableStateOf("") // その他選択時のURL
        private set
    
    // 通知スケジューラー
    private var notificationScheduler: NotificationScheduler? = null
    
    init {
        loadSettings()
        // Contextが利用可能な場合のみ通知スケジューラーを初期化
        context?.let {
            notificationScheduler = NotificationScheduler(it)
        }
    }
    
    fun updateFarmName(name: String) {
        farmName = name
    }
    
    fun updateDefaultRegion(region: String) {
        defaultRegion = region
    }
    
    fun updateSelectedPrefecture(prefecture: String) {
        selectedPrefecture = prefecture
    }
    
    fun updateFarmOwner(owner: String) {
        farmOwner = owner
    }
    
    fun updateCustomFarmOwner(customOwner: String) {
        customFarmOwner = customOwner
    }
    
    fun updateNotificationFrequency(frequency: String) {
        notificationFrequency = frequency
    }
    
    fun updateSelectedWeekday(weekday: String) {
        selectedWeekday = weekday
    }
    
    fun updateSeedInfoUrlProvider(provider: String) {
        seedInfoUrlProvider = provider
    }
    
    fun updateCustomSeedInfoUrl(url: String) {
        customSeedInfoUrl = url
    }
    
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                isLoading = true
                val auth = FirebaseAuth.getInstance()
                val uid = auth.currentUser?.uid
                
                if (uid == null) {
                    // ユーザーがログインしていない場合はデフォルト値を設定
                    defaultRegion = "温暖地"
                    return@launch
                }
                
                val db = Firebase.firestore
                val settingsDoc = db.collection("users").document(uid).collection("settings").document("general")
                
                val snapshot = settingsDoc.get().await()
                if (snapshot.exists()) {
                    farmName = snapshot.getString("farmName") ?: ""
                    defaultRegion = snapshot.getString("defaultRegion") ?: ""
                    selectedPrefecture = snapshot.getString("selectedPrefecture") ?: ""
                    farmOwner = snapshot.getString("farmOwner") ?: "水戸黄門"
                    customFarmOwner = snapshot.getString("customFarmOwner") ?: ""
                    notificationFrequency = snapshot.getString("notificationFrequency") ?: "なし"
                    selectedWeekday = snapshot.getString("selectedWeekday") ?: "月曜日"
                    seedInfoUrlProvider = snapshot.getString("seedInfoUrlProvider") ?: "サカタのたね"
                    customSeedInfoUrl = snapshot.getString("customSeedInfoUrl") ?: ""
                    hasExistingData = farmName.isNotBlank() || defaultRegion.isNotBlank()
                } else {
                    // デフォルト値を設定
                    defaultRegion = "温暖地"
                    selectedPrefecture = ""
                    farmOwner = "水戸黄門"
                    customFarmOwner = ""
                    notificationFrequency = "なし"
                    selectedWeekday = "月曜日"
                    seedInfoUrlProvider = "サカタのたね"
                    customSeedInfoUrl = ""
                    hasExistingData = false
                }
            } catch (e: Exception) {
                // エラーログを出力
                android.util.Log.e("SettingsViewModel", "設定の読み込みに失敗", e)
                
                // エラーが発生してもデフォルト値は設定する
                defaultRegion = "温暖地"
                selectedPrefecture = ""
                notificationFrequency = "なし"
                selectedWeekday = "月曜日"
                seedInfoUrlProvider = "サカタのたね"
                customSeedInfoUrl = ""
                
                // 権限エラーの場合は詳細なメッセージを表示
                val errorMessage = when {
                    e.message?.contains("permission-denied", ignoreCase = true) == true -> 
                        "設定の読み込みに失敗しました（権限エラー）"
                    e.message?.contains("unavailable", ignoreCase = true) == true -> 
                        "設定の読み込みに失敗しました（ネットワークエラー）"
                    else -> "設定の読み込みに失敗しました"
                }
                
                // 権限エラーの場合はSnackbarを表示しない（ユーザーに混乱を与えないため）
                if (e.message?.contains("permission-denied", ignoreCase = true) != true) {
                    showSnackbar = errorMessage
                }
            } finally {
                isLoading = false
            }
        }
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            try {
                isLoading = true
                val auth = FirebaseAuth.getInstance()
                val uid = auth.currentUser?.uid
                
                // デバッグ用ログ
                android.util.Log.d("SettingsViewModel", "保存開始: farmName='$farmName', defaultRegion='$defaultRegion', selectedPrefecture='$selectedPrefecture', farmOwner='$farmOwner', customFarmOwner='$customFarmOwner', notificationFrequency='$notificationFrequency', selectedWeekday='$selectedWeekday', seedInfoUrlProvider='$seedInfoUrlProvider', customSeedInfoUrl='$customSeedInfoUrl'")
                
                if (uid == null) {
                    showSnackbar = "ログインが必要です"
                    return@launch
                }
                
                android.util.Log.d("SettingsViewModel", "ユーザーUID: $uid")
                
                val db = Firebase.firestore
                val settingsDoc = db.collection("users").document(uid).collection("settings").document("general")
                
                val settings = mapOf(
                    "farmName" to farmName,
                    "defaultRegion" to defaultRegion,
                    "selectedPrefecture" to selectedPrefecture,
                    "farmOwner" to farmOwner,
                    "customFarmOwner" to customFarmOwner,
                    "notificationFrequency" to notificationFrequency,
                    "selectedWeekday" to selectedWeekday,
                    "seedInfoUrlProvider" to seedInfoUrlProvider,
                    "customSeedInfoUrl" to customSeedInfoUrl,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                
                android.util.Log.d("SettingsViewModel", "保存するデータ: $settings")
                
                settingsDoc.set(settings).await()
                android.util.Log.d("SettingsViewModel", "保存完了")
                
                // 通知スケジュールを更新
                updateNotificationSchedule()
                
                showSnackbar = "設定を保存しました"
                
            } catch (e: Exception) {
                // エラーログを出力
                android.util.Log.e("SettingsViewModel", "設定の保存に失敗", e)
                
                // 権限エラーの場合は詳細なメッセージを表示
                val errorMessage = when {
                    e.message?.contains("permission-denied", ignoreCase = true) == true -> 
                        "設定の保存に失敗しました（権限エラー）"
                    e.message?.contains("unavailable", ignoreCase = true) == true -> 
                        "設定の保存に失敗しました（ネットワークエラー）"
                    else -> "設定の保存に失敗しました"
                }
                showSnackbar = errorMessage
            } finally {
                isLoading = false
            }
        }
    }
    
    fun clearSnackbar() {
        showSnackbar = null
    }
    
    fun enterEditMode() {
        isEditMode = true
    }
    
    fun exitEditMode() {
        isEditMode = false
    }
    
    fun toggleEditMode() {
        isEditMode = !isEditMode
    }
    
    /**
     * 通知スケジュールを更新
     */
    private fun updateNotificationSchedule() {
        notificationScheduler?.updateNotificationSchedule(notificationFrequency, selectedWeekday)
    }
    
    /**
     * 通知設定を更新（即座にスケジュールも更新）
     */
    fun updateNotificationSettings(frequency: String, weekday: String = selectedWeekday) {
        notificationFrequency = frequency
        selectedWeekday = weekday
        
        // 即座にスケジュールを更新
        updateNotificationSchedule()
    }
}
