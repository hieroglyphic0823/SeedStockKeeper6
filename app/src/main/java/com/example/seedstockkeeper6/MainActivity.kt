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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.LocalActivity
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
        
        FirebaseApp.initializeApp(this)

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
