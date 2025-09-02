@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.seedstockkeeper6

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.seedstockkeeper6.ui.auth.AuthGate
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.ui.theme.ThemeFlavor
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DebugTrace", "MainActivity.onCreate called")
        FirebaseApp.initializeApp(this)
        Log.d("DebugTrace", "FirebaseApp initialized")

        setContent {
            Log.d("DebugTrace", "setContent initializing")
            val navController = rememberNavController()

            SeedStockKeeper6Theme(
                flavor = ThemeFlavor.Tanesuke, //Onion, OnionLightColors, Renkon, Forest, SweetPotato, SweetP, M3CB3, Saien, Tanesuke  ← 試したい配色を指定（Onion/Renkon/SweetPotato/SweetP/M3CB3/Saien/TanesukeはColor.kt形式の色ファイルを使用）
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false          // パレットを見たい時は false 推奨
            ) {
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
