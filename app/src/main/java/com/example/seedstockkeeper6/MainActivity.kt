@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.seedstockkeeper6

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.auth.AuthGate
import com.example.seedstockkeeper6.ui.screens.SeedInputScreen
import com.example.seedstockkeeper6.ui.screens.SeedListScreen
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.ui.theme.ThemeFlavor
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest


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
                flavor = ThemeFlavor.Herb , //Vitamin, Soil  ,Herb ,Ocean ,Plum ,Sakura  ← 試したい配色を指定
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false          // パレットを見たい時は false 推奨
            ) {
                Surface(                      // ★ これが“アプリ全体の背景”
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthGate { user ->
                        MainScaffold(navController = navController, user = user)
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    user: FirebaseUser
) {
    val selectedIds = remember { mutableStateListOf<String>() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isListScreen = currentRoute == "list"
    val isInputScreen = currentRoute?.startsWith("input") == true

    val snackbarHostState = remember { SnackbarHostState() }
    val listViewModel: SeedListViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp) // 丸の大きさ（アイコンより少し大きめ）
                            .background(
                                color = MaterialTheme.colorScheme.tertiary, // 好きな色に
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center // アイコンを真ん中に配置
                    ) {
                        AccountMenuButton(
                            user = user,
                            size = 38.dp, // 中のアイコンを少し小さめに
                            onSignOut = { signOut(ctx, scope) }
                        )
                    }
                },
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
                            IconButton(onClick = {
                                inputViewModel.saveSeed(ctx) { result ->
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
                            IconButton(onClick = { navController.navigate("debugDetectOuter") }) {
                                Icon(Icons.Outlined.BugReport, contentDescription = "Debug: Detect Outer")
                            }
                        }
                        else -> Unit
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
        if (BuildConfig.DEBUG) {
            composable("debugDetectOuter") { com.example.seedstockkeeper6.debug.DebugDetectOuterScreen() }
        }
    }
}

fun signOut(
    context: Context,
    scope: CoroutineScope
) {
    FirebaseAuth.getInstance().signOut()
    scope.launch {
        try {
            CredentialManager.create(context)
                .clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
            // ignore
        }
    }
}

@Composable
fun AccountMenuButton(
    user: FirebaseUser?,
    size: Dp = 32.dp,
    onSignOut: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val photo = user?.photoUrl
    val emailOrName = user?.displayName ?: user?.email ?: "未ログイン"

    Box {
        IconButton(onClick = { expanded = true }) {
            if (photo != null) {
                AsyncImage(
                    model = photo,
                    contentDescription = "プロフィール",
                    modifier = Modifier.size(size).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "プロフィール",
                    modifier = Modifier.size(size),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(emailOrName) },
                onClick = { /* no-op */ },
                enabled = false,
                leadingIcon = {
                    if (photo != null) {
                        AsyncImage(
                            model = photo,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = null)
                    }
                }
            )
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
                text = { Text("サインアウト") },
                onClick = {
                    expanded = false
                    onSignOut()
                }
            )
        }
    }
}
