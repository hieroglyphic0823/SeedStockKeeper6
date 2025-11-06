package com.example.seedstockkeeper6.viewmodel

import android.accounts.Account
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seedstockkeeper6.notification.NotificationScheduler
import com.example.seedstockkeeper6.service.GoogleCalendarService
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsViewModel(private val context: Context? = null) : ViewModel() {
    
    var farmName by mutableStateOf("")
        private set
    
    // 農園位置（座標）
    var farmLatitude by mutableStateOf(0.0)
        private set
    
    var farmLongitude by mutableStateOf(0.0)
        private set
    
    var farmAddress by mutableStateOf("")
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
    
    // BGM設定の状態
    var isBgmEnabled by mutableStateOf(true) // BGM有効/無効
        private set
    
    // Googleカレンダー設定の状態
    var calendarId by mutableStateOf<String?>(null) // 選択されたカレンダーID
        private set
    
    var calendarName by mutableStateOf<String?>(null) // 選択されたカレンダー名
        private set
    
    // カレンダー一覧の状態
    var calendarList by mutableStateOf<List<com.google.api.services.calendar.model.CalendarListEntry>>(emptyList())
        private set
    
    var isLoadingCalendars by mutableStateOf(false) // カレンダー一覧読み込み中
        private set
    
    var calendarError by mutableStateOf<String?>(null) // カレンダー取得エラー
        private set
    
    // Google Sign-Inが必要な状態（UI層でIntentを起動するために使用）
    var needsGoogleSignIn by mutableStateOf(false)
        private set
    
    // Google Sign-In Client（カレンダー用）
    private var googleSignInClient: GoogleSignInClient? = null
    
    // 通知スケジューラー
    private var notificationScheduler: NotificationScheduler? = null
    
    init {
        loadSettings()
        // Contextが利用可能な場合のみ通知スケジューラーとGoogleSignInClientを初期化
        context?.let { ctx ->
            notificationScheduler = NotificationScheduler(ctx)
            initializeGoogleSignIn(ctx)
        }
    }
    
    /**
     * Google Sign-In Clientを初期化（カレンダーAPI用のスコープをリクエスト）
     */
    private fun initializeGoogleSignIn(context: Context) {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestScopes(Scope(CalendarScopes.CALENDAR))
                .build()
            
            googleSignInClient = GoogleSignIn.getClient(context, gso)
        } catch (e: SecurityException) {
            // Google Play Servicesの内部エラー（エミュレーター環境でよく発生）
            // アプリの機能には影響しないため、無視する
        } catch (e: Exception) {
            // その他のエラーも無視
        }
    }
    
    fun updateFarmName(name: String) {
        farmName = name
    }
    
    fun updateFarmLocation(latitude: Double, longitude: Double, address: String = "") {
        farmLatitude = latitude
        farmLongitude = longitude
        farmAddress = address
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
    
    fun updateBgmEnabled(enabled: Boolean) {
        isBgmEnabled = enabled
    }
    
    fun updateCalendarId(calendarId: String?, calendarName: String?) {
        this.calendarId = calendarId
        this.calendarName = calendarName
    }
    
    /**
     * Googleカレンダー一覧を取得
     * GoogleSignInからアクセストークンを取得して、GoogleCalendarServiceに渡します
     */
    fun loadCalendarList() {
        viewModelScope.launch {
            try {
                android.util.Log.d("SettingsViewModel", "loadCalendarList() 開始")
                isLoadingCalendars = true
                calendarError = null
                
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser
                
                android.util.Log.d("SettingsViewModel", "Firebase Auth確認: user=${user?.uid}")
                
                if (user == null) {
                    android.util.Log.e("SettingsViewModel", "ログインが必要です")
                    calendarError = "ログインが必要です"
                    return@launch
                }
                
                context?.let { ctx ->
                    // 1. 既存のGoogleSignInアカウントを取得
                    var account = GoogleSignIn.getLastSignedInAccount(ctx)
                    
                    // 2. アカウントがない、またはスコープが不足している場合は再サインイン
                    if (account == null || !hasCalendarScope(account)) {
                        android.util.Log.d("SettingsViewModel", "Google Sign-Inが必要です（初回またはスコープ不足）")
                        try {
                            account = googleSignInClient?.silentSignIn()?.await()
                        } catch (e: ApiException) {
                            android.util.Log.w("SettingsViewModel", "silentSignIn失敗: ${e.statusCode} - ${e.message}")
                            // SIGN_IN_REQUIRED (4) の場合は明示的なサインインが必要
                            if (e.statusCode == CommonStatusCodes.SIGN_IN_REQUIRED) {
                                android.util.Log.d("SettingsViewModel", "SIGN_IN_REQUIRED: 明示的なサインインが必要です")
                                needsGoogleSignIn = true
                                calendarError = "Googleカレンダーへのアクセス許可が必要です。サインイン画面が表示されます。"
                                return@launch
                            } else {
                                // その他のAPIエラー
                                android.util.Log.e("SettingsViewModel", "予期しないAPIエラー: ${e.statusCode}")
                                calendarError = "Googleサインインに失敗しました (Code: ${e.statusCode})"
                                return@launch
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("SettingsViewModel", "silentSignIn予期しないエラー: ${e.javaClass.simpleName} - ${e.message}")
                            calendarError = "サインイン処理でエラーが発生しました: ${e.message}"
                            return@launch
                        }
                    }
                    
                    // 3. それでもアカウントが取得できない場合は、明示的なサインインが必要
                    if (account == null) {
                        android.util.Log.e("SettingsViewModel", "Google Sign-Inが必要です（明示的なサインインが必要）")
                        needsGoogleSignIn = true
                        calendarError = "Googleカレンダーへのアクセス許可が必要です。サインイン画面が表示されます。"
                        return@launch
                    }
                    
                    // 4. アクセストークンを取得
                    val accountEmail = account.email
                    if (accountEmail.isNullOrBlank()) {
                        android.util.Log.e("SettingsViewModel", "アカウントEmailが取得できません")
                        calendarError = "アカウント情報が取得できません"
                        return@launch
                    }
                    
                    android.util.Log.d("SettingsViewModel", "アクセストークン取得開始: email=${accountEmail.take(10)}...")
                    val accessToken = withContext(Dispatchers.IO) {
                        try {
                            GoogleAuthUtil.getToken(
                                ctx,
                                Account(accountEmail, "com.google"),
                                "oauth2:${CalendarScopes.CALENDAR}"
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("SettingsViewModel", "アクセストークン取得エラー: ${e.javaClass.simpleName} - ${e.message}")
                            null
                        }
                    }
                    
                    if (accessToken.isNullOrBlank()) {
                        android.util.Log.e("SettingsViewModel", "アクセストークンが取得できませんでした")
                        calendarError = "カレンダーへのアクセス許可が必要です"
                        return@launch
                    }
                    
                    // 5. GoogleCalendarServiceを呼び出し
                    android.util.Log.d("SettingsViewModel", "GoogleCalendarService呼び出し開始")
                    val calendarService = GoogleCalendarService(ctx)
                    val result = calendarService.getCalendarList(accessToken)
                    
                    result.onSuccess { calendars ->
                        android.util.Log.d("SettingsViewModel", "カレンダー一覧取得成功: ${calendars.size}件")
                        calendarList = calendars
                        calendarError = null
                    }.onFailure { exception ->
                        android.util.Log.e("SettingsViewModel", "カレンダー一覧取得失敗: ${exception.javaClass.simpleName} - ${exception.message}")
                        android.util.Log.e("SettingsViewModel", "スタックトレース: ${exception.stackTraceToString()}")
                        calendarError = "カレンダー一覧の取得に失敗しました: ${exception.message}"
                        calendarList = emptyList()
                    }
                } ?: run {
                    android.util.Log.e("SettingsViewModel", "コンテキストが利用できません")
                    calendarError = "コンテキストが利用できません"
                }
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "予期しないエラー: ${e.javaClass.name} - ${e.message}")
                android.util.Log.e("SettingsViewModel", "スタックトレース: ${e.stackTraceToString()}")
                calendarError = "エラーが発生しました: ${e.message}"
                calendarList = emptyList()
            } finally {
                isLoadingCalendars = false
                android.util.Log.d("SettingsViewModel", "loadCalendarList() 完了")
            }
        }
    }
    
    /**
     * GoogleSignInAccountにカレンダースコープが含まれているか確認
     */
    private fun hasCalendarScope(account: GoogleSignInAccount): Boolean {
        val grantedScopes = account.grantedScopes ?: return false
        return grantedScopes.any { it.toString() == CalendarScopes.CALENDAR }
    }
    
    /**
     * 明示的なGoogle Sign-Inを開始するためのIntentを取得
     * UI層でstartActivityForResultなどで使用
     */
    fun getGoogleSignInIntent() = googleSignInClient?.signInIntent
    
    /**
     * Google Sign-Inの結果を処理
     * UI層でActivityResultを受け取った後に呼び出す
     */
    fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
        needsGoogleSignIn = false
        if (account != null) {
            android.util.Log.d("SettingsViewModel", "Google Sign-In成功: ${account.email}")
            // サインイン成功後、再度カレンダー一覧を取得
            loadCalendarList()
        } else {
            android.util.Log.e("SettingsViewModel", "Google Sign-In失敗: accountがnull")
            calendarError = "Googleサインインに失敗しました"
        }
    }
    
    /**
     * Google Sign-Inの要求をキャンセル
     */
    fun cancelGoogleSignIn() {
        needsGoogleSignIn = false
        calendarError = null
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
                    farmLatitude = snapshot.getDouble("farmLatitude") ?: 0.0
                    farmLongitude = snapshot.getDouble("farmLongitude") ?: 0.0
                    farmAddress = snapshot.getString("farmAddress") ?: ""
                    defaultRegion = snapshot.getString("defaultRegion") ?: ""
                    selectedPrefecture = snapshot.getString("selectedPrefecture") ?: ""
                    farmOwner = snapshot.getString("farmOwner") ?: "水戸黄門"
                    customFarmOwner = snapshot.getString("customFarmOwner") ?: ""
                    notificationFrequency = snapshot.getString("notificationFrequency") ?: "なし"
                    selectedWeekday = snapshot.getString("selectedWeekday") ?: "月曜日"
                    seedInfoUrlProvider = snapshot.getString("seedInfoUrlProvider") ?: "サカタのたね"
                    customSeedInfoUrl = snapshot.getString("customSeedInfoUrl") ?: ""
                    isBgmEnabled = snapshot.getBoolean("isBgmEnabled") ?: true
                    calendarId = snapshot.getString("calendarId")
                    calendarName = snapshot.getString("calendarName")
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
                    isBgmEnabled = true
                    calendarId = null
                    calendarName = null
                    hasExistingData = false
                }
            } catch (e: Exception) {
                // エラーログを出力
                
                // エラーが発生してもデフォルト値は設定する
                defaultRegion = "温暖地"
                selectedPrefecture = ""
                notificationFrequency = "なし"
                selectedWeekday = "月曜日"
                seedInfoUrlProvider = "サカタのたね"
                customSeedInfoUrl = ""
                isBgmEnabled = true
                calendarId = null
                calendarName = null
                
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
                
                if (uid == null) {
                    showSnackbar = "ログインが必要です"
                    return@launch
                }
                
                
                val db = Firebase.firestore
                val settingsDoc = db.collection("users").document(uid).collection("settings").document("general")
                
                val settings = mapOf(
                    "farmName" to farmName,
                    "farmLatitude" to farmLatitude,
                    "farmLongitude" to farmLongitude,
                    "farmAddress" to farmAddress,
                    "defaultRegion" to defaultRegion,
                    "selectedPrefecture" to selectedPrefecture,
                    "farmOwner" to farmOwner,
                    "customFarmOwner" to customFarmOwner,
                    "notificationFrequency" to notificationFrequency,
                    "selectedWeekday" to selectedWeekday,
                    "seedInfoUrlProvider" to seedInfoUrlProvider,
                    "customSeedInfoUrl" to customSeedInfoUrl,
                    "isBgmEnabled" to isBgmEnabled,
                    "calendarId" to (calendarId ?: ""),
                    "calendarName" to (calendarName ?: ""),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                
                
                settingsDoc.set(settings).await()
                
                // 通知スケジュールを更新
                updateNotificationSchedule()
                
                showSnackbar = "設定を保存しました"
                
            } catch (e: Exception) {
                // エラーログを出力
                
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

