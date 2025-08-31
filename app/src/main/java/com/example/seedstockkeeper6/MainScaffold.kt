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
    
    // 全画面アニメーション用の状態
    var showSaveAnimation by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
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
                        // 入力画面 → 保存ボタン
                        isInputScreen && navBackStackEntry != null -> {
                            val inputViewModel: SeedInputViewModel = viewModel(
                                viewModelStoreOwner = navBackStackEntry!!
                            )
                            IconButton(onClick = {
                                // 全画面アニメーションを表示
                                showSaveAnimation = true
                                
                                // アニメーション完了後に保存処理を実行
                                scope.launch {
                                    delay(1500) // アニメーション時間
                                    showSaveAnimation = false
                                    
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
                                }
                            }) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Save, 
                                        contentDescription = "Save",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
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
                                         tint = MaterialTheme.colorScheme.onSecondaryContainer,
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
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                bottomNavItems.forEach { item ->
                                            NavigationBarItem(
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.tertiary,
                                selectedTextColor = MaterialTheme.colorScheme.tertiary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                        icon = { 
                            when (item.iconRes) {
                                0 -> AnimatedIcon(
                                    painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.indoor_plants), 
                                    contentDescription = "ホーム",
                                    tint = Color.Unspecified
                                )
                                                                                                 1 -> AnimatedIcon(
                                    icon = Icons.Filled.Search, 
                                    contentDescription = "検索",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                 2 -> {
                                    if (isListScreen && selectedIds.isNotEmpty()) {
                                        // チェックボックスがオンの時はゴミ箱アイコン
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.error,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "削除",
                                                tint = MaterialTheme.colorScheme.onError,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    } else {
                                        // 通常時は＋アイコン
                                        AnimatedIcon(
                                            icon = Icons.Filled.Add, 
                                            contentDescription = "追加",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                 3 -> AnimatedIcon(
                                     painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.calendar), 
                                     contentDescription = "カレンダー",
                                     tint = Color.Unspecified
                                 )
                                
                                else -> AnimatedIcon(
                                    painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.indoor_plants), 
                                    contentDescription = "ホーム",
                                    tint = Color.Unspecified
                                )
                            }
                        },
                        selected = currentRoute == item.route,
                        onClick = {
                            when (item) {
                                is BottomNavItem.Add -> {
                                    if (isListScreen && selectedIds.isNotEmpty()) {
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
                                    } else {
                                        // 通常時は追加画面に遷移
                                        val emptyPacketJson = URLEncoder.encode(
                                            Gson().toJson(SeedPacket()),
                                            StandardCharsets.UTF_8.toString()
                                        )
                                        navController.navigate("input/$emptyPacketJson")
                                    }
                                }
                                else -> {
                                    // その他のボタンは通常のナビゲーション
                                    navController.navigate(item.route) {
                                        launchSingleTop = true
                                        popUpTo(navController.graph.startDestinationId) { 
                                            saveState = true 
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
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
