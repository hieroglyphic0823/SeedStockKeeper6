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
        Log.d("SystemAppearance", "enableEdgeToEdge() 実行完了")
        
        // エッジトゥエッジ設定後のWindow状態をログ出力
        Log.d("SystemAppearance", "enableEdgeToEdge後のWindow状態:")
        Log.d("SystemAppearance", "  - statusBarColor: 0x${String.format("%08X", this.window.statusBarColor)}")
        Log.d("SystemAppearance", "  - navigationBarColor: 0x${String.format("%08X", this.window.navigationBarColor)}")
        
        // エッジトゥエッジ後にステータスバーの色を設定
        // onCreate内ではisSystemInDarkTheme()は使用できないため、デフォルトのライトテーマ色を使用
        val surfaceColor = 0xFFF0E8D0L // ライトテーマのsurface色
        this.window.statusBarColor = surfaceColor.toInt()
        this.window.navigationBarColor = surfaceColor.toInt()
        Log.d("SystemAppearance", "onCreateでステータスバー色を設定: 0x${String.format("%08X", surfaceColor)}")
        
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
                // テーマが適用された後にシステムバーの外観を制御
                SystemAppearance(isDarkTheme = isSystemInDarkTheme())
                
                Surface(                      // ★ これが"アプリ全体の背景"
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface  // background → surface に変更
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
        DisposableEffect(isDarkTheme) { // isDarkTheme の変更時にも再実行
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)

            // 現在の設定をログ出力
            Log.d("SystemAppearance", "=== SystemAppearance 詳細情報 ===")
            Log.d("SystemAppearance", "isDarkTheme: $isDarkTheme")
            Log.d("SystemAppearance", "テーマモード: ${if (isDarkTheme) "DARK MODE" else "LIGHT MODE"}")
            Log.d("SystemAppearance", "現在のステータスバー色: 0x${String.format("%08X", window.statusBarColor)}")
            Log.d("SystemAppearance", "現在のナビゲーションバー色: 0x${String.format("%08X", window.navigationBarColor)}")
            Log.d("SystemAppearance", "Window flags: ${window.attributes.flags}")
            Log.d("SystemAppearance", "Window decorView background: ${window.decorView.background}")
            Log.d("SystemAppearance", "Window decorView alpha: ${window.decorView.alpha}")
            if (window.decorView.background is android.graphics.drawable.ColorDrawable) {
                val colorDrawable = window.decorView.background as android.graphics.drawable.ColorDrawable
                Log.d("SystemAppearance", "ColorDrawable の実際の色: 0x${String.format("%08X", colorDrawable.color)}")
                
                // 期待される色と実際の色を比較
                val expectedColor = if (isDarkTheme) 0xFF15130BL else 0xFFF0E8D0L
                val actualColor = colorDrawable.color.toLong()
                Log.d("SystemAppearance", "期待される色: 0x${String.format("%08X", expectedColor)}")
                Log.d("SystemAppearance", "色の一致: ${expectedColor == actualColor}")
                if (expectedColor != actualColor) {
                    Log.w("SystemAppearance", "⚠️ 背景色が一致しません！期待: 0x${String.format("%08X", expectedColor)}, 実際: 0x${String.format("%08X", actualColor)}")
                }
            }
            Log.d("SystemAppearance", "設定された背景色: ${if (isDarkTheme) "#15130B (Color.kt の backgroundDark)" else "#F0E8D0 (Color.kt の backgroundLight)"}")
            Log.d("SystemAppearance", "TopAppBar背景色: MaterialTheme.colorScheme.surface")
            Log.d("SystemAppearance", "Surface背景色: MaterialTheme.colorScheme.surface")
            Log.d("SystemAppearance", "================================")

            // ステータスバーのアイコンの色 (true でダークアイコン = 明るい背景用)
            Log.d("SystemAppearance", "--- ステータスバー設定前 ---")
            Log.d("SystemAppearance", "設定前 isAppearanceLightStatusBars: ${insetsController.isAppearanceLightStatusBars}")
            insetsController.isAppearanceLightStatusBars = !isDarkTheme
            Log.d("SystemAppearance", "設定後 isAppearanceLightStatusBars: ${insetsController.isAppearanceLightStatusBars}")
            Log.d("SystemAppearance", "期待値: ${!isDarkTheme}")
            
            // ナビゲーションバーのアイコンの色 (true でダークアイコン = 明るい背景用)
            Log.d("SystemAppearance", "--- ナビゲーションバー設定前 ---")
            Log.d("SystemAppearance", "設定前 isAppearanceLightNavigationBars: ${insetsController.isAppearanceLightNavigationBars}")
            insetsController.isAppearanceLightNavigationBars = !isDarkTheme
            Log.d("SystemAppearance", "設定後 isAppearanceLightNavigationBars: ${insetsController.isAppearanceLightNavigationBars}")
            Log.d("SystemAppearance", "期待値: ${!isDarkTheme}")
            
            // アイコンの色をより暗くしてコントラストを改善
            // 基本的なアイコン色の制御
            val lightStatusBars = !isDarkTheme
            val lightNavigationBars = !isDarkTheme
            
            Log.d("SystemAppearance", "--- アイコン色設定 ---")
            Log.d("SystemAppearance", "isDarkTheme: $isDarkTheme")
            Log.d("SystemAppearance", "lightStatusBars: $lightStatusBars")
            Log.d("SystemAppearance", "lightNavigationBars: $lightNavigationBars")
            
            // ステータスバーのアイコン色を設定
            insetsController.isAppearanceLightStatusBars = lightStatusBars
            Log.d("SystemAppearance", "設定後 isAppearanceLightStatusBars: ${insetsController.isAppearanceLightStatusBars}")
            
            // ナビゲーションバーのアイコン色を設定
            insetsController.isAppearanceLightNavigationBars = lightNavigationBars
            Log.d("SystemAppearance", "設定後 isAppearanceLightNavigationBars: ${insetsController.isAppearanceLightNavigationBars}")
            
            // 設定の確認
            Log.d("SystemAppearance", "最終確認 - ステータスバー: ${insetsController.isAppearanceLightStatusBars}")
            Log.d("SystemAppearance", "最終確認 - ナビゲーションバー: ${insetsController.isAppearanceLightNavigationBars}")
            
            // システムが自動選択するアイコンの色をログ出力
            Log.d("SystemAppearance", "--- アイコン色の詳細情報 ---")
            Log.d("SystemAppearance", "isAppearanceLightStatusBars: ${insetsController.isAppearanceLightStatusBars}")
            Log.d("SystemAppearance", "isAppearanceLightNavigationBars: ${insetsController.isAppearanceLightNavigationBars}")
            if (isDarkTheme) {
                Log.d("SystemAppearance", "システム選択: ダークテーマ用の明るいアイコン色が適用されています")
                Log.d("SystemAppearance", "期待される効果: 背景色 #15130B に対して明るいアイコンでコントラスト向上")
            } else {
                Log.d("SystemAppearance", "システム選択: ライトテーマ用の暗いアイコン色が適用されています")
                Log.d("SystemAppearance", "期待される効果: 背景色 #F0E8D0 に対して暗いアイコンでコントラスト向上")
            }
            Log.d("SystemAppearance", "----------------------------------------")

            // システムバーの色をMaterialTheme.colorScheme.surfaceと同じ色に設定
            // 注意: この時点ではMaterialTheme.colorScheme.surfaceにアクセスできないため、
            // Color.ktで定義されたsurface色を使用
            val surfaceColor = if (isDarkTheme) 0xFF15130B else 0xFFF0E8D0
            window.statusBarColor = surfaceColor.toInt()
            window.navigationBarColor = surfaceColor.toInt()
            
            Log.d("SystemAppearance", "システムバー色を設定: 0x${String.format("%08X", surfaceColor)}")
            Log.d("SystemAppearance", "MaterialTheme.colorScheme.surfaceと同じ色を使用")
            Log.d("SystemAppearance", "ライトテーマ: #F0E8D0, ダークテーマ: #15130B")

            // 設定後の色をログ出力
            // 設定後の色をログ出力
            Log.d("SystemAppearance", "設定後のステータスバー色: 0x${String.format("%08X", window.statusBarColor)}")
            Log.d("SystemAppearance", "設定後のナビゲーションバー色: 0x${String.format("%08X", window.navigationBarColor)}")
            
            // 色の統一状況を確認
            Log.d("SystemAppearance", "=== 色の統一状況 ===")
            Log.d("SystemAppearance", "1. アプリ全体背景: MaterialTheme.colorScheme.surface")
            Log.d("SystemAppearance", "2. TopAppBar背景: MaterialTheme.colorScheme.surface")
            Log.d("SystemAppearance", "3. ステータスバー: Color.ktのsurface色 (${if (isDarkTheme) "#15130B" else "#F0E8D0"})")
            Log.d("SystemAppearance", "4. ナビゲーションバー: Color.ktのsurface色 (${if (isDarkTheme) "#15130B" else "#F0E8D0"})")
            Log.d("SystemAppearance", "すべてsurface色で統一されています")
            Log.d("SystemAppearance", "================================")
            
            Log.d("SystemAppearance", "SystemAppearance 完了")

            onDispose { }
        }
    } else {
        Log.w("SystemAppearance", "Activity が見つかりません")
    }
}
