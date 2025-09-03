@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.seedstockkeeper6

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.seedstockkeeper6.ui.auth.AuthGate
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import androidx.core.view.WindowCompat
import android.app.Activity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // エッジトゥエッジを有効にする
        enableEdgeToEdge()
        Log.d("SystemAppearance", "enableEdgeToEdge() 実行完了")
        
        Log.d("DebugTrace", "MainActivity.onCreate called")
        FirebaseApp.initializeApp(this)
        Log.d("DebugTrace", "FirebaseApp initialized")

        setContent {
            Log.d("DebugTrace", "setContent initializing")
            val navController = rememberNavController()

            SeedStockKeeper6Theme(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = true
            ) {
                // MaterialThemeの色をログ出力
                LaunchedEffect(Unit) {
                    Log.d("MaterialTheme", "surface色: ${MaterialTheme.colorScheme.surface}")
                    Log.d("MaterialTheme", "onSurface色: ${MaterialTheme.colorScheme.onSurface}")
                    Log.d("MaterialTheme", "background色: ${MaterialTheme.colorScheme.background}")
                    Log.d("MaterialTheme", "isSystemInDarkTheme: ${isSystemInDarkTheme()}")
                }
                
                // システムバーの外観を制御
                SystemAppearance(isDarkTheme = isSystemInDarkTheme())
                
                Surface(                      // ★ これが"アプリ全体の背景"
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    AuthGate { user ->
                        MainScaffold(navController = navController, user = user)
                    }
                }
            }
        }
    }
}

// システムバーの外観を制御するヘルパーコンポーザブル
@Composable
private fun SystemAppearance(isDarkTheme: Boolean) {
    val activity = LocalContext.current as? Activity
    if (activity != null) {
        DisposableEffect(isDarkTheme) { // isDarkTheme の変更時にも再実行
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)

            // 現在の設定をログ出力
            Log.d("SystemAppearance", "isDarkTheme: $isDarkTheme")
            Log.d("SystemAppearance", "現在のステータスバー色: 0x${String.format("%08X", window.statusBarColor)}")
            Log.d("SystemAppearance", "現在のナビゲーションバー色: 0x${String.format("%08X", window.navigationBarColor)}")

            // ステータスバーのアイコンの色 (true でダークアイコン = 明るい背景用)
            insetsController.isAppearanceLightStatusBars = !isDarkTheme
            Log.d("SystemAppearance", "isAppearanceLightStatusBars: ${!isDarkTheme}")
            
            // ナビゲーションバーのアイコンの色 (true でダークアイコン = 明るい背景用)
            insetsController.isAppearanceLightNavigationBars = !isDarkTheme
            Log.d("SystemAppearance", "isAppearanceLightNavigationBars: ${!isDarkTheme}")

            // API 29+ でステータスバー、ナビゲーションバーの背景を透明にする
            // (enableEdgeToEdge で自動的に行われることが多いが、明示も可能)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT

            // 設定後の色をログ出力
            Log.d("SystemAppearance", "設定後のステータスバー色: 0x${String.format("%08X", window.statusBarColor)}")
            Log.d("SystemAppearance", "設定後のナビゲーションバー色: 0x${String.format("%08X", window.navigationBarColor)}")

            onDispose { }
        }
    }
}
