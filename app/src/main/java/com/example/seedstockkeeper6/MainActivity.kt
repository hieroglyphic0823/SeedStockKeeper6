@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.seedstockkeeper6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.LocalActivity
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.seedstockkeeper6.ui.auth.AuthGate
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.notification.NotificationManager
import com.example.seedstockkeeper6.audio.BgmManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import androidx.core.view.WindowCompat
import android.app.Activity
import android.os.Build

class MainActivity : ComponentActivity() {
    
    private lateinit var notificationManager: NotificationManager
    private lateinit var bgmManager: BgmManager
    
    // 通知権限リクエストのランチャー
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
        } else {
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // スプラッシュスクリーンのテーマから通常のテーマに切り替え
        setTheme(com.example.seedstockkeeper6.R.style.Theme_SeedStockKeeper6)
        
        // エッジトゥエッジを有効にする
        enableEdgeToEdge()
        
        FirebaseApp.initializeApp(this)
        
        // 重い処理はバックグラウンドで実行
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // App Checkを初期化（デバッグ用）
                com.google.firebase.appcheck.FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
                )
            } catch (e: Exception) {
            }
            
            try {
                // Firestoreのオフライン対応を有効化
                com.example.seedstockkeeper6.utils.FirestoreUtils.enableOfflinePersistence()
            } catch (e: Exception) {
            }
            
            try {
                // ネットワーク接続状態を取得
                val networkInfo = com.example.seedstockkeeper6.utils.NetworkUtils.getNetworkInfo(this@MainActivity)
            } catch (e: Exception) {
            }
        }
        
        // 通知マネージャーを初期化（軽量な処理のみ）
        notificationManager = NotificationManager(this)
        
        // BGMマネージャーを初期化
        bgmManager = BgmManager.getInstance(this)

        setContent {
            val navController = rememberNavController()

            SeedStockKeeper6Theme(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = true
            ) {
                // テーマが適用された後にシステムバーの外観を制御
                SystemAppearance(isDarkTheme = isSystemInDarkTheme())
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    var showVideo = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
                    if (showVideo.value) {
                        com.example.seedstockkeeper6.ui.screens.VideoSplashScreen(
                            onVideoEnd = { showVideo.value = false }
                        )
                    } else {
                        AuthGate { user ->
                            MainScaffold(navController = navController, user = user)
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 通知権限をリクエスト
     */
    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationManager.hasNotificationPermission()) {
                requestPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            // 通知マネージャーのクリーンアップ
            if (::notificationManager.isInitialized) {
                notificationManager.cancelAllNotifications()
            }
        } catch (e: Exception) {
        }
    }
    
    override fun onPause() {
        super.onPause()
        // アプリがバックグラウンドに行ったらBGMを一時停止
        try {
            if (::bgmManager.isInitialized) {
                bgmManager.pause()
            }
        } catch (e: Exception) {
        }
    }
    
    override fun onResume() {
        super.onResume()
        // アプリがフォアグラウンドに戻ったらBGMを再開（設定が有効な場合のみ）
        try {
            if (::bgmManager.isInitialized) {
                bgmManager.play()
            }
        } catch (e: Exception) {
        }
    }
}

// システムバーの外観を制御するヘルパーコンポーザブル
@Composable
private fun SystemAppearance(isDarkTheme: Boolean) {
    val activity = LocalActivity.current
    if (activity != null) {
        DisposableEffect(isDarkTheme) {
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)

            // ステータスバーとナビゲーションバーのアイコン色を設定
            insetsController.isAppearanceLightStatusBars = !isDarkTheme
            insetsController.isAppearanceLightNavigationBars = !isDarkTheme

            // エッジトゥエッジではシステムバーの色設定は不要
            // enableEdgeToEdge()が自動的に透明に設定する

            onDispose { }
        }
    }
}
