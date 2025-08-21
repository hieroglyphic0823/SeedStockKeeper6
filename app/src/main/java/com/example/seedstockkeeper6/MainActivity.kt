@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.seedstockkeeper6

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.auth.AuthGate
import com.example.seedstockkeeper6.ui.screens.SeedInputScreen
import com.example.seedstockkeeper6.ui.screens.SeedListScreen
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.ui.platform.LocalContext
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
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false
            ) {
                // ▼ 未ログインなら SignInScreen、ログイン済みならアプリ本体を表示
                AuthGate { _user ->
                    MainScaffold(navController = navController)
                }
            }
        }
    }
}

@Composable
private fun MainScaffold(navController: NavHostController) {
    val selectedIds = remember { mutableStateListOf<String>() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isListScreen = currentRoute == "list"
    val isInputScreen = currentRoute?.startsWith("input") == true

    val snackbarHostState = remember { SnackbarHostState() }
    val listViewModel: SeedListViewModel = viewModel()
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("たねすけさん") },
                actions = {
                    when {
                        // 1) リスト画面で選択あり → 削除ボタン
                        isListScreen && selectedIds.isNotEmpty() -> {
                            IconButton(onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    selectedIds.forEach { id ->
                                        listViewModel.deleteSeedPacketWithImages(id) { result ->
                                            CoroutineScope(Dispatchers.Main).launch {
                                                val message = if (result.isSuccess) "削除しました"
                                                else "削除に失敗しました: ${result.exceptionOrNull()?.localizedMessage ?: "不明なエラー"}"
                                                snackbarHostState.showSnackbar(message)
                                            }
                                        }
                                    }
                                    selectedIds.clear()
                                }
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }

                        // 2) 入力画面 → 保存ボタン
                        isInputScreen && navBackStackEntry != null -> {
                            val inputViewModel: SeedInputViewModel = viewModel(
                                viewModelStoreOwner = navBackStackEntry!!
                            )
                            val context = LocalContext.current

                            IconButton(onClick = {
                                inputViewModel.saveSeed(context) { result ->
                                    scope.launch(Dispatchers.Main) {
                                        val message = if (result.isSuccess) {
                                            navController.popBackStack()
                                            "保存しました"
                                        } else {
                                            "保存に失敗しました: ${result.exceptionOrNull()?.localizedMessage ?: "不明なエラー"}"
                                        }
                                        snackbarHostState.showSnackbar(message)
                                    }
                                }
                            }) {
                                Icon(Icons.Filled.Save, contentDescription = "Save")
                            }
                        }

                        // 3) リスト画面で選択なし & DEBUG → 🐞デバッグボタン
                        isListScreen && selectedIds.isEmpty() && BuildConfig.DEBUG -> {
                            val ctx = LocalContext.current
                            // ← 追加：サインアウト（左側）
                            IconButton(onClick = { signOut(ctx, scope) }) {
                                Icon(Icons.Outlined.Logout, contentDescription = "Sign out")
                            }
                            IconButton(onClick = { navController.navigate("debugDetectOuter") }) {
                                Icon(Icons.Outlined.BugReport, contentDescription = "Debug: Detect Outer")
                            }
                        }

                        else -> { /* 何も出さない */ }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isListScreen && selectedIds.isEmpty()) {
                FloatingActionButton(onClick = {
                    val emptyPacketJson = URLEncoder.encode(
                        Gson().toJson(SeedPacket()),
                        StandardCharsets.UTF_8.toString()
                    )
                    navController.navigate("input/$emptyPacketJson")
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }
        }
    ) { padding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(padding),
            selectedIds = selectedIds
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    selectedIds: MutableList<String>
) {
    NavHost(
        navController = navController,
        startDestination = "list",
        modifier = modifier
    ) {
        composable("list") {
            Log.d("BootTrace", "Screen: SeedListScreen初期化")
            val listViewModel: SeedListViewModel = viewModel()
            SeedListScreen(
                navController = navController,
                viewModel = listViewModel,
                selectedIds = selectedIds
            )
        }
        composable("input/{packet}") { backStackEntry ->
            Log.d("BootTrace", "Screen: SeedInputScreen初期化")
            val json = backStackEntry.arguments?.getString("packet") ?: ""
            val packet = if (json.isNotEmpty()) Gson().fromJson(json, SeedPacket::class.java) else null
            val currentInputViewModel: SeedInputViewModel = viewModel(viewModelStoreOwner = backStackEntry)
            LaunchedEffect(json) {
                currentInputViewModel.setSeed(packet)
            }
            SeedInputScreen(
                navController = navController,
                viewModel = currentInputViewModel
            )
        }
        // ★ DEBUGビルドのときだけ有効
        if (BuildConfig.DEBUG) {
            composable("debugDetectOuter") { com.example.seedstockkeeper6.debug.DebugDetectOuterScreen() }
        }
    }
}
fun signOut(
    context: Context,
    scope: CoroutineScope
) {
    // Firebase からログアウト → currentUser が null になる
    FirebaseAuth.getInstance().signOut()

    // Credential Manager の「自動選択」状態もクリア（任意）
    scope.launch {
        try {
            CredentialManager.create(context)
                .clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
            // ユーザー操作が必要な場合などは無視してOK
        }
    }
}