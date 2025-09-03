package com.example.seedstockkeeper6

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import android.util.Log
import com.example.seedstockkeeper6.ui.theme.surfaceContainerLight
import com.example.seedstockkeeper6.ui.theme.surfaceContainerDark
import com.example.seedstockkeeper6.ui.theme.secondaryLight
import com.example.seedstockkeeper6.ui.theme.secondaryDark
import com.example.seedstockkeeper6.ui.theme.tertiaryLight
import com.example.seedstockkeeper6.ui.theme.tertiaryDark
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
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
    
    // 入力画面用のViewModel（条件付きで取得）
    val inputViewModel: SeedInputViewModel? = if (isInputScreen && navBackStackEntry != null) {
        viewModel(viewModelStoreOwner = navBackStackEntry!!)
    } else null
    
    // 全画面アニメーション用の状態
    var showSaveAnimation by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                navigationIcon = {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AccountMenuButton(
                            user = user,
                            size = 32.dp, // BottomToolBarのアイコンと同じサイズ
                            onSignOut = { signOut(ctx, scope) }
                        )
                    }
                },
                title = { 
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("たねすけさん")
                    }
                },
                actions = {
                    when {
                        // 3) リスト画面で選択なし & DEBUG → 🐞デバッグボタン
                        isListScreen && selectedIds.isEmpty() && false -> { // デバッグボタンを無効化
                            IconButton(onClick = { navController.navigate("debugDetectOuter") }) {
                                Icon(Icons.Outlined.BugReport, contentDescription = "Debug: Detect Outer")
                            }
                        }
                        else -> {
                            // 設定アイコン（常に表示）
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { /* 設定画面に遷移 */ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Settings,
                                        contentDescription = "設定",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
                bottomBar = {
            // デバッグで色を出力
            Log.d("Color", "=== THEME DEBUG INFO ===")
            Log.d("Color", "Theme - surfaceContainer: ${MaterialTheme.colorScheme.surfaceContainer}")
            Log.d("Color", "Theme - onSurface: ${MaterialTheme.colorScheme.onSurface}")
            Log.d("Color", "Theme - secondary: ${MaterialTheme.colorScheme.secondary}")
            Log.d("Color", "Theme - onSecondary: ${MaterialTheme.colorScheme.onSecondary}")
            Log.d("Color", "Theme - tertiary: ${MaterialTheme.colorScheme.tertiary}")
            Log.d("Color", "Theme - primaryContainer: ${MaterialTheme.colorScheme.primaryContainer}")
            Log.d("Color", "Theme - primary: ${MaterialTheme.colorScheme.primary}")
            Log.d("Color", "Theme - background: ${MaterialTheme.colorScheme.background}")
            Log.d("Color", "Theme - surface: ${MaterialTheme.colorScheme.surface}")
            Log.d("Color", "=== CUSTOM COLOR VALUES ===")
            Log.d("Color", "Custom - surfaceContainerLight: $surfaceContainerLight")
            Log.d("Color", "Custom - surfaceContainerDark: $surfaceContainerDark")
            Log.d("Color", "Custom - secondaryLight: $secondaryLight")
            Log.d("Color", "Custom - secondaryDark: $secondaryDark")
            Log.d("Color", "Custom - tertiaryLight: $tertiaryLight")
            Log.d("Color", "Custom - tertiaryDark: $tertiaryDark")
            Log.d("Color", "=== END THEME DEBUG ===")
            
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                // ホームアイコン
                NavigationBarItem(
                    label = { Text("ホーム") },
                    icon = { 
                        Icon(
                            painter = painterResource(
                                id = if (currentRoute == "list") 
                                    com.example.seedstockkeeper6.R.drawable.home_dark 
                                else 
                                    com.example.seedstockkeeper6.R.drawable.home_light
                            ),
                            contentDescription = "ホーム",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(
                                if (currentRoute == "list") 28.dp else 24.dp
                            )
                        )
                    },
                    selected = currentRoute == "list",
                    onClick = { navController.navigate("list") }
                )
                
                // 検索アイコン
                NavigationBarItem(
                    label = { Text("検索") },
                    icon = { 
                        Icon(
                            imageVector = if (currentRoute == "search") Icons.Filled.Search else Icons.Outlined.Search, 
                            contentDescription = "検索",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(
                                if (currentRoute == "search") 28.dp else 24.dp
                            )
                        )
                    },
                    selected = currentRoute == "search",
                    onClick = { navController.navigate("search") }
                )
                
                // 中央のFab（状況に応じてアイコンとラベルが変わる）
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = {
                            when {
                                isInputScreen -> {
                                    // 入力画面の時は保存処理
                                    if (inputViewModel != null) {
                                        inputViewModel.saveSeed(ctx) { result ->
                                            scope.launch {
                                                val message = if (result.isSuccess) {
                                                    navController.popBackStack()
                                                    "保存しました"
                                                } else {
                                                    "保存に失敗しました: ${result.exceptionOrNull()?.localizedMessage ?: "不明なエラー"}"
                                                }
                                                snackbarHostState.showSnackbar(message)
                                            }
                                        }
                                    } else {
                                        // inputViewModelがnullの場合はホームに戻る
                                        navController.navigate("list")
                                    }
                                }
                                isListScreen && selectedIds.isNotEmpty() -> {
                                    // チェックボックスがオンの時は削除処理
                                    scope.launch {
                                        selectedIds.forEach { id ->
                                            listViewModel.deleteSeedPacketWithImages(id) { result ->
                                                scope.launch {
                                                    val message = if (result.isSuccess) "削除しました"
                                                    else "削除に失敗しました: ${result.exceptionOrNull()?.localizedMessage ?: "不明なエラー"}"
                                                    snackbarHostState.showSnackbar(message)
                                                }
                                            }
                                        }
                                        selectedIds.clear()
                                    }
                                }
                                else -> {
                                    // 通常時は入力画面に遷移（新規登録）
                                    try {
                                        navController.navigate("input/") {
                                            launchSingleTop = true
                                            popUpTo(navController.graph.startDestinationId) { 
                                                saveState = true 
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // エラーが発生した場合はログを出力
                                        Log.e("Navigation", "Navigation error: ${e.message}", e)
                                        // 代替ルートに遷移
                                        navController.navigate("list")
                                    }
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        when {
                            isInputScreen -> {
                                Icon(
                                    imageVector = Icons.Filled.Save,
                                    contentDescription = "保存",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            isListScreen && selectedIds.isNotEmpty() -> {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "削除",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "追加",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    

                }
                
                // カレンダーアイコン
                NavigationBarItem(
                    label = { Text("カレンダー") },
                    icon = { 
                        Icon(
                            painter = painterResource(
                                id = if (currentRoute == "calendar") 
                                    com.example.seedstockkeeper6.R.drawable.calendar_dark 
                                else 
                                    com.example.seedstockkeeper6.R.drawable.calendar_light
                            ),
                            contentDescription = "カレンダー",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(
                                if (currentRoute == "calendar") 28.dp else 24.dp
                            )
                        )
                    },
                    selected = currentRoute == "calendar",
                    onClick = { navController.navigate("calendar") }
                )
                
                // 通知アイコン
                NavigationBarItem(
                    label = { Text("通知") },
                    icon = { 
                        Icon(
                            imageVector = if (currentRoute == "notifications") Icons.Filled.Notifications else Icons.Outlined.Notifications, 
                            contentDescription = "通知",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(
                                if (currentRoute == "notifications") 28.dp else 24.dp
                            )
                        )
                    },
                    selected = currentRoute == "notifications",
                    onClick = { navController.navigate("notifications") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(padding),
                selectedIds = selectedIds
            )
            
            // 全画面保存アニメーション
            if (showSaveAnimation) {
                FullScreenSaveAnimation()
            }
        }
    }
}
