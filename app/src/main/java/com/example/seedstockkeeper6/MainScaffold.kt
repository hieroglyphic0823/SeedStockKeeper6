package com.example.seedstockkeeper6

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.seedstockkeeper6.AccountMenuButton
import com.example.seedstockkeeper6.FullScreenSaveAnimation
import com.example.seedstockkeeper6.signOut
import com.example.seedstockkeeper6.AppNavHost
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.example.seedstockkeeper6.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffoldTopAppBar(
    currentRoute: String?,
    navController: NavHostController,
    user: FirebaseUser?,
    settingsViewModel: SettingsViewModel? = null,
    seedInputViewModel: com.example.seedstockkeeper6.viewmodel.SeedInputViewModel? = null
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        navigationIcon = {
            when (currentRoute) {
                "settings" -> {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
                "input" -> {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
                else -> {
                    if (currentRoute?.startsWith("input") == true) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                        }
                    } else if (user != null) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AccountMenuButton(
                                user = user,
                                size = 32.dp,
                                onSignOut = { signOut(ctx, scope) }
                            )
                        }
                    }
                }
            }
        },
        title = { 
            when (currentRoute) {
                "settings" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.garden_cart),
                            contentDescription = "農園設定",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "農園設定",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
                "input" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when {
                            // DisplayMode: Familyアイコンと商品名
                            seedInputViewModel?.isEditMode == false && seedInputViewModel?.hasExistingData == true -> {
                                com.example.seedstockkeeper6.ui.components.FamilyIcon(
                                    family = seedInputViewModel.packet.family,
                                    size = 24.dp
                                )
                                Text(
                                    text = seedInputViewModel.packet.productName.ifEmpty { "種情報" },
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }
                            // EditMode新規作成: 現行アイコン+「新規作成」
                            seedInputViewModel?.isEditMode == true && seedInputViewModel?.hasExistingData == false -> {
                                Image(
                                    painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.packet),
                                    contentDescription = "種情報",
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "新規作成",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }
                            // EditMode編集: Familyアイコンと商品名+「（編集）」
                            seedInputViewModel?.isEditMode == true && seedInputViewModel?.hasExistingData == true -> {
                                com.example.seedstockkeeper6.ui.components.FamilyIcon(
                                    family = seedInputViewModel.packet.family,
                                    size = 24.dp
                                )
                                Text(
                                    text = "${seedInputViewModel.packet.productName.ifEmpty { "種情報" }}（編集）",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }
                            // デフォルト: 現行アイコン+「種情報」
                            else -> {
                                Image(
                                    painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.packet),
                                    contentDescription = "種情報",
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "種情報",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
                else -> {
                    if (currentRoute?.startsWith("input") == true) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.packet),
                                contentDescription = "種情報",
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = if (seedInputViewModel?.isEditMode == true) "編集" else "種情報",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        },
        actions = {
            when (currentRoute) {
                "settings" -> {
                    // 設定画面では編集アイコンを表示
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { 
                                // 編集モードの切り替え
                                settingsViewModel?.let { viewModel ->
                                    if (viewModel.isEditMode) {
                                        viewModel.exitEditMode()
                                    } else {
                                        viewModel.enterEditMode()
                                    }
                                }
                            },
                        ) {
                                                    Icon(
                            imageVector = if (settingsViewModel?.isEditMode == true) Icons.Filled.Close else Icons.Filled.Edit,
                            contentDescription = if (settingsViewModel?.isEditMode == true) "キャンセル" else "編集",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        }
                    }
                }
                "input" -> {
                    // DisplayModeの時はEDITアイコン、EditModeの時は×ボタンを表示
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { 
                                if (seedInputViewModel?.isEditMode == true) {
                                    // 編集モードを終了
                                    seedInputViewModel?.exitEditMode()
                                } else {
                                    // 編集モードに切り替え
                                    seedInputViewModel?.enterEditMode()
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (seedInputViewModel?.isEditMode == true) Icons.Filled.Close else Icons.Filled.Edit,
                                contentDescription = if (seedInputViewModel?.isEditMode == true) "キャンセル" else "編集",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    if (currentRoute?.startsWith("input") == true) {
                        // DisplayModeの時はEDITアイコン、EditModeの時は×ボタンを表示
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { 
                                    if (seedInputViewModel?.isEditMode == true) {
                                        // 編集モードを終了
                                        seedInputViewModel?.exitEditMode()
                                    } else {
                                        // 編集モードに切り替え
                                        seedInputViewModel?.enterEditMode()
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = if (seedInputViewModel?.isEditMode == true) Icons.Filled.Close else Icons.Filled.Edit,
                                    contentDescription = if (seedInputViewModel?.isEditMode == true) "キャンセル" else "編集",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { navController.navigate("settings") },
                            ) {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = "設定",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffoldNavigationBar(
    currentRoute: String?,
    navController: NavHostController,
    selectedIds: List<String>,
    isListScreen: Boolean,
    isInputScreen: Boolean,
    inputViewModel: SeedInputViewModel?,
    settingsViewModel: SettingsViewModel,
    onSaveRequest: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // ホームアイコン
        NavigationBarItem(
            icon = { 
                Icon(
                    painter = painterResource(
                        id = com.example.seedstockkeeper6.R.drawable.home_and_garden
                    ),
                    contentDescription = "ホーム",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(if (currentRoute == "list") 28.dp else 24.dp)
                )
            },
            selected = currentRoute == "list",
            onClick = { navController.navigate("list") }
        )
        
        // 検索アイコン
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = if (currentRoute == "search") Icons.Filled.Search else Icons.Outlined.Search, 
                    contentDescription = "検索",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(if (currentRoute == "search") 28.dp else 24.dp)
                )
            },
            selected = currentRoute == "search",
            onClick = { navController.navigate("search") }
        )
        
        // 中央のFab
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                currentRoute == "settings" && settingsViewModel.isEditMode -> {
                    FloatingActionButton(
                        onClick = {
                            onSaveRequest()
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "保存"
                        )
                    }
                }
                isInputScreen -> {
                    FloatingActionButton(
                        onClick = { /* 入力画面の保存処理は別途実装 */ },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "保存")
                    }
                }
                isListScreen && selectedIds.isNotEmpty() -> {
                    FloatingActionButton(
                        onClick = { /* 削除処理 */ },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "削除")
                    }
                }
                else -> {
                    FloatingActionButton(
                        onClick = { navController.navigate("input/") },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "追加")
                    }
                }
            }
        }
        
        // カレンダーアイコン
        NavigationBarItem(
            icon = { 
                val isDarkTheme = isSystemInDarkTheme()
                if (isDarkTheme) {
                    Icon(
                        imageVector = if (currentRoute == "calendar") Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                        contentDescription = "カレンダー",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(if (currentRoute == "calendar") 28.dp else 24.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(
                            id = if (currentRoute == "calendar") 
                                com.example.seedstockkeeper6.R.drawable.calendar_dark 
                            else 
                                com.example.seedstockkeeper6.R.drawable.calendar_light
                        ),
                        contentDescription = "カレンダー",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(if (currentRoute == "calendar") 28.dp else 24.dp)
                    )
                }
            },
            selected = currentRoute == "calendar",
            onClick = { navController.navigate("calendar") }
        )
        
        // 通知アイコン
        NavigationBarItem(
            icon = { 
                Icon(
                    imageVector = if (currentRoute == "notifications") Icons.Filled.Notifications else Icons.Outlined.Notifications, 
                    contentDescription = "通知",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(if (currentRoute == "notifications") 28.dp else 24.dp)
                )
            },
            selected = currentRoute == "notifications",
            onClick = { navController.navigate("notifications") }
        )
    }
}

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
    val settingsViewModel: SettingsViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    
    // アプリ起動後の初期化完了フラグ
    var isAppInitialized by remember { mutableStateOf(false) }
    
    // アプリ起動から3秒後に初期化完了とする
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        isAppInitialized = true
        Log.d("MainScaffold", "アプリ初期化完了")
    }
    
    // 入力画面用のViewModel（条件付きで取得）
    val inputViewModel: SeedInputViewModel? = if (isInputScreen && navBackStackEntry != null) {
        viewModel(viewModelStoreOwner = navBackStackEntry!!)
    } else null
    
    // 全画面アニメーション用の状態
    var showSaveAnimation by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MainScaffoldTopAppBar(
                currentRoute = currentRoute,
                navController = navController,
                user = user,
                settingsViewModel = if (currentRoute == "settings") settingsViewModel else null,
                seedInputViewModel = if (currentRoute?.startsWith("input") == true) inputViewModel else null
            )
        },
        bottomBar = {
            // 設定画面と入力画面ではNavigationBarを表示しない
            if (currentRoute != "settings" && currentRoute?.startsWith("input") != true) {
                MainScaffoldNavigationBar(
                    currentRoute = currentRoute,
                    navController = navController,
                    selectedIds = selectedIds,
                    isListScreen = isListScreen,
                    isInputScreen = isInputScreen,
                    inputViewModel = inputViewModel,
                    settingsViewModel = settingsViewModel,
                    onSaveRequest = {
                        navController.popBackStack()
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(padding),
                selectedIds = selectedIds,
                settingsViewModel = settingsViewModel
            )
            
            // 全画面保存アニメーション
            if (showSaveAnimation) {
                FullScreenSaveAnimation()
            }
        }
    }
}
