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
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.res.painterResource
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
import androidx.compose.material3.SnackbarDuration
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
    seedInputViewModel: com.example.seedstockkeeper6.viewmodel.SeedInputViewModel? = null,
    selectedIds: List<String> = emptyList(),
    onDeleteSelected: () -> Unit = {},
    farmName: String = ""
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
                "notification_preview" -> {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
                "notification_history" -> {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
                "calendar" -> {
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
                "notification_preview" -> {
                    Text("通知テスト・プレビュー")
                }
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
                "notification_history" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "通知履歴",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
                "map_selection" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.google_maps_icon),
                            contentDescription = "Googleマップ",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Googleマップで農園位置を選択",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
                else -> {
                    if (currentRoute?.startsWith("input") == true) {
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
                                    text = if (seedInputViewModel.packet.productName.isNotEmpty() && seedInputViewModel.packet.variety.isNotEmpty()) {
                                        "${seedInputViewModel.packet.productName}（${seedInputViewModel.packet.variety}）"
                                    } else if (seedInputViewModel.packet.productName.isNotEmpty()) {
                                        seedInputViewModel.packet.productName
                                    } else {
                                        "種情報"
                                    },
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
                            // EditMode編集: Familyアイコンと商品名+（科名）
                            seedInputViewModel?.isEditMode == true && seedInputViewModel?.hasExistingData == true -> {
                                com.example.seedstockkeeper6.ui.components.FamilyIcon(
                                    family = seedInputViewModel.packet.family,
                                    size = 24.dp
                                )
                                Text(
                                    text = if (seedInputViewModel.packet.productName.isNotEmpty() && seedInputViewModel.packet.family.isNotEmpty()) {
                                        "${seedInputViewModel.packet.productName}（${seedInputViewModel.packet.family}）"
                                    } else {
                                        "${seedInputViewModel.packet.productName.ifEmpty { "種情報" }}"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }
                            // デフォルト: 商品名がある場合は商品名、ない場合は「種情報」
                            else -> {
                                if (seedInputViewModel?.packet?.productName?.isNotEmpty() == true) {
                                    // 商品名がある場合はFamilyアイコンと商品名を表示
                                    com.example.seedstockkeeper6.ui.components.FamilyIcon(
                                        family = seedInputViewModel.packet.family,
                                        size = 24.dp
                                    )
                                    Text(
                                        text = if (seedInputViewModel.packet.variety.isNotEmpty()) {
                                            "${seedInputViewModel.packet.productName}（${seedInputViewModel.packet.variety}）"
                                        } else {
                                            seedInputViewModel.packet.productName
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                } else {
                                    // 商品名がない場合は現行アイコン+「種情報」
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
                    } else if (currentRoute == "castle") {
                        // お城画面のタイトル表示
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.home),
                                contentDescription = "お城",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = if (farmName.isNotEmpty()) "お城（$farmName）" else "お城",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    } else if (currentRoute == "calendar") {
                        // 種暦画面のタイトル表示
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.calendar),
                                contentDescription = "種暦",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = "種暦",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    } else {
                        // 種一覧画面のタイトル表示
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.list),
                                contentDescription = "種リスト",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "種リスト",
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
                                                    if (settingsViewModel?.isEditMode == true) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "キャンセル",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.edit),
                                contentDescription = "編集",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        }
                    }
                }
                "calendar" -> {
                    // 種暦画面では何も表示しない
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
                            if (seedInputViewModel?.isEditMode == true) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "キャンセル",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.edit),
                                    contentDescription = "編集",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                "notification_history" -> {
                    // 通知テスト・プレビュー画面への遷移ボタン
                    IconButton(
                        onClick = { navController.navigate("notification_preview") }
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.yabumi0),
                            contentDescription = "通知テスト・プレビュー",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                "notification_preview" -> {
                    // 通知テスト・プレビュー画面では何も表示しない
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
                                if (seedInputViewModel?.isEditMode == true) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "キャンセル",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.edit),
                                        contentDescription = "編集",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 種一覧画面でチェックが入っている場合は削除ボタンを表示
                            if (currentRoute == "list" && selectedIds.isNotEmpty()) {
                                IconButton(
                                    onClick = onDeleteSelected
                                ) {
                                    Image(
                                        painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.delete),
                                        contentDescription = "削除",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            // 設定ボタン
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
        // お城アイコン
        NavigationBarItem(
            icon = { 
                Icon(
                    painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.home),
                    contentDescription = "お城",
                    tint = ComposeColor.Unspecified,
                    modifier = Modifier.size(if (currentRoute == "castle") 28.dp else 24.dp)
                )
            },
            label = { Text("お城") },
            selected = currentRoute == "castle",
            onClick = { navController.navigate("castle") }
        )
        
        // 一覧アイコン
        NavigationBarItem(
            icon = { 
                Icon(
                    painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.list),
                    contentDescription = "一覧",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(if (currentRoute == "list") 28.dp else 24.dp)
                )
            },
            label = { Text("一覧") },
            selected = currentRoute == "list",
            onClick = { navController.navigate("list") }
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
                isInputScreen && !(inputViewModel?.isLoading ?: false) -> {
                    FloatingActionButton(
                        onClick = { /* 入力画面の保存処理は別途実装 */ },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "保存")
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
                        tint = ComposeColor.Unspecified,
                        modifier = Modifier.size(if (currentRoute == "calendar") 28.dp else 24.dp)
                    )
                }
            },
            label = { Text("種暦") },
            selected = currentRoute == "calendar",
            onClick = { navController.navigate("calendar") }
        )
        
        // 通知アイコン（yabumi - 矢文）
        NavigationBarItem(
            icon = { 
                Icon(
                    painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.yabumi3),
                    contentDescription = "通知履歴",
                    tint = ComposeColor.Unspecified,
                    modifier = Modifier.size(if (currentRoute == "notification_history") 28.dp else 24.dp)
                )
            },
            label = { Text("通知") },
            selected = currentRoute == "notification_history",
            onClick = { navController.navigate("notification_history") }
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
    Log.d("MainScaffold", "MainScaffold recompose - currentRoute: $currentRoute, navBackStackEntry: $navBackStackEntry")
    val isListScreen = currentRoute == "list"
    val isCastleScreen = currentRoute == "castle"
    val isInputScreen = currentRoute?.startsWith("input") == true
    
    val snackbarHostState = remember { SnackbarHostState() }
    val listViewModel = viewModel<SeedListViewModel>()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(ctx) }
    
    // 農園名を取得
    var farmName by remember { mutableStateOf("") }
    
    // 農園名を設定から取得
    LaunchedEffect(Unit) {
        // 設定の読み込み完了を待つ
        kotlinx.coroutines.delay(1000)
        farmName = settingsViewModel.farmName
        Log.d("MainScaffold", "農園名取得: $farmName")
    }
    
    // アプリ起動後の初期化完了フラグ
    var isAppInitialized by remember { mutableStateOf(false) }
    
    // アプリ起動から3秒後に初期化完了とする
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        isAppInitialized = true
        Log.d("MainScaffold", "アプリ初期化完了")
    }
    
    // 入力画面用のViewModel（条件付きで取得）
    val inputViewModel: com.example.seedstockkeeper6.viewmodel.SeedInputViewModel? = if (isInputScreen && navBackStackEntry != null) {
        viewModel(viewModelStoreOwner = navBackStackEntry!!)
    } else null
    
    // 全画面アニメーション用の状態
    var showSaveAnimation by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Log.d("MainScaffold", "currentRoute: $currentRoute, notification_preview check: ${currentRoute != "notification_preview"}")
            MainScaffoldTopAppBar(
                currentRoute = currentRoute,
                navController = navController,
                user = user,
                settingsViewModel = if (currentRoute == "settings") settingsViewModel else null,
                seedInputViewModel = if (currentRoute?.startsWith("input") == true) inputViewModel else null,
                selectedIds = selectedIds,
                farmName = farmName,
                onDeleteSelected = {
                    // 選択された種情報を削除
                    val idsToDelete = selectedIds.toList() // コピーを作成
                    val deleteCount = idsToDelete.size
                    
                    // 選択状態をクリア
                    selectedIds.clear()
                    
                    // 削除処理を非同期で実行
                    scope.launch {
                        var successCount = 0
                        var failureCount = 0
                        
                        // 各削除処理を順次実行
                        for (id in idsToDelete) {
                            try {
                                val result = listViewModel.deleteSeedPacketWithImagesInternal(id)
                                if (result.isSuccess) {
                                    successCount++
                                    Log.d("MainScaffold", "削除成功: $id")
                                } else {
                                    failureCount++
                                    Log.e("MainScaffold", "削除失敗: $id", result.exceptionOrNull())
                                }
                            } catch (e: Exception) {
                                failureCount++
                                Log.e("MainScaffold", "削除エラー: $id", e)
                            }
                        }
                        
                        // 削除完了後にSnackbarでお知らせ
                        when {
                            successCount == deleteCount -> {
                                snackbarHostState.showSnackbar(
                                    message = "${deleteCount}件の種情報を削除しました",
                                    duration = SnackbarDuration.Short
                                )
                            }
                            successCount > 0 -> {
                                snackbarHostState.showSnackbar(
                                    message = "${successCount}件削除しました（${failureCount}件失敗）",
                                    duration = SnackbarDuration.Short
                                )
                            }
                            else -> {
                                snackbarHostState.showSnackbar(
                                    message = "削除に失敗しました",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                }
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
                settingsViewModel = settingsViewModel,
                onSaveRequest = {
                    showSaveAnimation = true
                    // 保存処理完了後にアニメーションを非表示にする
                    scope.launch {
                        kotlinx.coroutines.delay(2000) // 2秒間アニメーション表示
                        showSaveAnimation = false
                    }
                },
                onDeleteSelected = { idsToDelete ->
                    // 削除処理を非同期で実行
                    scope.launch {
                        var successCount = 0
                        var failureCount = 0
                        
                        // 各削除処理を順次実行
                        for (id in idsToDelete) {
                            try {
                                val result = listViewModel.deleteSeedPacketWithImagesInternal(id)
                                if (result.isSuccess) {
                                    successCount++
                                    Log.d("MainScaffold", "削除成功: $id")
                                } else {
                                    failureCount++
                                    Log.e("MainScaffold", "削除失敗: $id", result.exceptionOrNull())
                                }
                            } catch (e: Exception) {
                                failureCount++
                                Log.e("MainScaffold", "削除エラー: $id", e)
                            }
                        }
                        
                        // 結果に応じてSnackbarを表示
                        when {
                            successCount == idsToDelete.size -> {
                                snackbarHostState.showSnackbar(
                                    message = "${idsToDelete.size}件の種情報を削除しました",
                                    duration = SnackbarDuration.Short
                                )
                            }
                            successCount > 0 -> {
                                snackbarHostState.showSnackbar(
                                    message = "${successCount}件削除しました（${failureCount}件失敗）",
                                    duration = SnackbarDuration.Short
                                )
                            }
                            else -> {
                                snackbarHostState.showSnackbar(
                                    message = "削除に失敗しました",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                }
            )
            
            // 全画面保存アニメーション（sukesan.gif）
            if (showSaveAnimation) {
                FullScreenSaveAnimation()
            }
        }
    }
}
