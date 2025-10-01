@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.seedstockkeeper6

import android.os.Bundle
import android.util.Log
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
import com.example.seedstockkeeper6.ui.auth.AuthGate
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.notification.NotificationManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import androidx.core.view.WindowCompat
import android.app.Activity
import android.os.Build

class MainActivity : ComponentActivity() {
    
    private lateinit var notificationManager: NotificationManager
    
    // 通知権限リクエストのランチャー
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "通知権限が許可されました")
        } else {
            Log.w("MainActivity", "通知権限が拒否されました")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // スプラッシュスクリーンのテーマから通常のテーマに切り替え
        setTheme(com.example.seedstockkeeper6.R.style.Theme_SeedStockKeeper6)
        
        // エッジトゥエッジを有効にする
        enableEdgeToEdge()
        
        FirebaseApp.initializeApp(this)
        
        // App Checkを初期化（デバッグ用）
        try {
            com.google.firebase.appcheck.FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
            )
            Log.d("MainActivity", "App Check initialized with debug provider")
        } catch (e: Exception) {
            Log.w("MainActivity", "Failed to initialize App Check: ${e.message}")
        }
        
        // Firestoreのオフライン対応を有効化
        try {
            com.example.seedstockkeeper6.utils.FirestoreUtils.enableOfflinePersistence()
            Log.d("MainActivity", "Firestore offline persistence enabled")
        } catch (e: Exception) {
            Log.w("MainActivity", "Failed to enable Firestore offline persistence: ${e.message}")
        }
        
        // ネットワーク接続状態をログ出力
        try {
            val networkInfo = com.example.seedstockkeeper6.utils.NetworkUtils.getNetworkInfo(this)
            Log.d("MainActivity", "Network info: $networkInfo")
        } catch (e: Exception) {
            Log.w("MainActivity", "Failed to get network info: ${e.message}")
        }
        
        // 通知マネージャーを初期化
        notificationManager = NotificationManager(this)

        setContent {
            val navController = rememberNavController()

            SeedStockKeeper6Theme(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = true
            ) {
                // テーマが適用された後にシステムバーの外観を制御
                SystemAppearance(isDarkTheme = isSystemInDarkTheme())
                
                Surface(                      // ★ これが"アプリ全体の背景"
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface  // surface を使用
                ) {
                    AuthGate { user ->
                        MainScaffold(navController = navController, user = user)
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
