package com.example.seedstockkeeper6

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.seedstockkeeper6.FullScreenSaveAnimation
import com.example.seedstockkeeper6.FullScreenLoadingAnimation
import com.example.seedstockkeeper6.AppNavHost
import com.example.seedstockkeeper6.ui.components.MainScaffoldTopAppBar
import com.example.seedstockkeeper6.ui.components.MainScaffoldNavigationBar
import com.example.seedstockkeeper6.model.NavigationConstants
import com.example.seedstockkeeper6.model.SnackbarMessageConstants
import com.example.seedstockkeeper6.model.getDeleteSuccessMessage
import com.example.seedstockkeeper6.model.getDeletePartialSuccessMessage
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.example.seedstockkeeper6.viewmodel.SettingsViewModel
import com.example.seedstockkeeper6.audio.BgmManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


@Composable
fun MainScaffold(
    navController: NavHostController,
    user: FirebaseUser
) {
    val selectedIds = remember { mutableStateListOf<String>() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isListScreen = currentRoute?.startsWith(NavigationConstants.LIST_ROUTE) == true
    val isCastleScreen = currentRoute?.startsWith(NavigationConstants.CASTLE_ROUTE) == true
    val isInputScreen = currentRoute?.startsWith(NavigationConstants.INPUT_ROUTE_PREFIX) == true
    
    val snackbarHostState = remember { SnackbarHostState() }
    val listViewModel = viewModel<SeedListViewModel>()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(ctx) }
    
    // BGMマネージャーを初期化
    val bgmManager = remember { BgmManager.getInstance(ctx) }
    
    // 未読通知数
    var unreadNotificationCount by remember { mutableStateOf(0) }
    val historyService = remember { com.example.seedstockkeeper6.service.NotificationHistoryService() }
    val badgeService = remember { com.example.seedstockkeeper6.service.ShortcutBadgeService(ctx) }
    
    // 農園名を取得
    var farmName by remember { mutableStateOf("") }
    
    // 農園名を設定から取得
    LaunchedEffect(Unit) {
        // 設定の読み込み完了を待つ
        delay(com.example.seedstockkeeper6.model.AnimationConstants.SETTINGS_LOAD_DELAY_MS.toLong())
        farmName = settingsViewModel.farmName
    }
    
    // MainScaffoldが表示されたらBGMを開始（1回のみ）
    LaunchedEffect(Unit) {
        // 設定の読み込み完了を少し待つ
        delay(300)
        
        // BGM設定が有効な場合のみ再生開始
        if (settingsViewModel.isBgmEnabled) {
            bgmManager.play()
        }
    }
    
    // BGM設定の変更を監視（設定画面でON/OFF切り替え時）
    LaunchedEffect(settingsViewModel.isBgmEnabled) {
        bgmManager.setEnabled(settingsViewModel.isBgmEnabled)
    }
    
    // 未読通知数を取得（画面が表示されるたびに更新）
    LaunchedEffect(currentRoute) {
        try {
            // バックグラウンドで通知数を取得
            val count = withContext(Dispatchers.IO) {
                historyService.getUnreadNotificationCount()
            }
            unreadNotificationCount = count
            
            // バッジを更新（UIスレッドで実行）
            badgeService.setBadgeCount(unreadNotificationCount)
        } catch (e: kotlinx.coroutines.CancellationException) {
            // コルーチンスコープがキャンセルされた場合は無視
        } catch (e: Exception) {
        }
    }
    
    
    // 未読通知数を更新する関数
    val refreshUnreadCount: () -> Unit = {
        scope.launch {
            try {
                // バックグラウンドで通知数を取得
                val count = withContext(Dispatchers.IO) {
                    historyService.getUnreadNotificationCount()
                }
                unreadNotificationCount = count
                
                // バッジを更新（UIスレッドで実行）
                badgeService.setBadgeCount(unreadNotificationCount)
            } catch (e: kotlinx.coroutines.CancellationException) {
                // コルーチンスコープがキャンセルされた場合は無視
            } catch (e: Exception) {
            }
        }
    }
    
    // アプリ起動後の初期化完了フラグ
    var isAppInitialized by remember { mutableStateOf(false) }
    
    // アプリ起動から3秒後に初期化完了とする
    LaunchedEffect(Unit) {
        delay(com.example.seedstockkeeper6.model.AnimationConstants.APP_INITIALIZATION_DELAY_MS.toLong())
        isAppInitialized = true
    }
    
    // 入力画面用のViewModel（条件付きで取得）
    val inputViewModel: com.example.seedstockkeeper6.viewmodel.SeedInputViewModel? = if (isInputScreen && navBackStackEntry != null) {
        viewModel(viewModelStoreOwner = navBackStackEntry!!)
    } else null
    
    // 全画面アニメーション用の状態
    var showSaveAnimation by remember { mutableStateOf(false) }
    var showLoadingAnimation by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MainScaffoldTopAppBar(
                currentRoute = currentRoute,
                navController = navController,
                user = user,
                settingsViewModel = if (currentRoute == NavigationConstants.SETTINGS_ROUTE) settingsViewModel else null,
                seedInputViewModel = if (currentRoute?.startsWith(NavigationConstants.INPUT_ROUTE_PREFIX) == true) inputViewModel else null,
                selectedIds = selectedIds,
                farmName = farmName,
                onDeleteSelected = {
                    // 選択された種情報を削除
                    val idsToDelete = selectedIds.toList() // コピーを作成
                    val deleteCount = idsToDelete.size
                    
                    // 選択状態をクリア
                    selectedIds.clear()
                    
                    // 削除処理を非同期で実行（バックグラウンドスレッドで実行）
                    scope.launch(Dispatchers.IO) {
                        var successCount = 0
                        var failureCount = 0
                        val context = ctx // LocalContextから取得したContextを使用
                        
                        // 各削除処理を順次実行
                        for (id in idsToDelete) {
                            try {
                                val result = listViewModel.deleteSeedPacketWithImagesInternal(id, context)
                                if (result.isSuccess) {
                                    successCount++
                                } else {
                                    failureCount++
                                }
                            } catch (e: Exception) {
                                failureCount++
                            }
                        }
                        
                        // UIスレッドでSnackbarを表示
                        withContext(Dispatchers.Main) {
                            // 削除完了後にSnackbarでお知らせ
                            when {
                                successCount == deleteCount -> {
                                    snackbarHostState.showSnackbar(
                                        message = getDeleteSuccessMessage(deleteCount),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                successCount > 0 -> {
                                    snackbarHostState.showSnackbar(
                                        message = getDeletePartialSuccessMessage(successCount, failureCount),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                else -> {
                                    snackbarHostState.showSnackbar(
                                        message = SnackbarMessageConstants.DELETE_FAILURE,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            // 設定画面と入力画面ではNavigationBarを表示しない
            if (currentRoute != NavigationConstants.SETTINGS_ROUTE && currentRoute?.startsWith(NavigationConstants.INPUT_ROUTE_PREFIX) != true) {
                MainScaffoldNavigationBar(
                    currentRoute = currentRoute,
                    navController = navController,
                    selectedIds = selectedIds,
                    isListScreen = isListScreen,
                    isInputScreen = isInputScreen,
                    // isInputScreenがtrueの時だけinputViewModelを渡し、それ以外はnullを渡す
                    inputViewModel = if (isInputScreen) inputViewModel else null,
                    settingsViewModel = settingsViewModel,
                    unreadNotificationCount = unreadNotificationCount,
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
                onRefreshUnreadCount = refreshUnreadCount,
                onSaveRequest = {
                    // アニメーションを開始（保存処理開始時）
                    showSaveAnimation = true
                },
                onSaveComplete = {
                    // 保存完了時にアニメーションを非表示にする
                    scope.launch {
                        delay(500) // 少し待ってから非表示（視認性のため）
                        showSaveAnimation = false
                    }
                },
                onLoadingChange = { isLoading ->
                    // AI処理中のアニメーション表示状態を更新
                    showLoadingAnimation = isLoading
                },
                onDeleteSelected = { idsToDelete ->
                    // 削除処理を非同期で実行（バックグラウンドスレッドで実行）
                    scope.launch(Dispatchers.IO) {
                        var successCount = 0
                        var failureCount = 0
                        val context = ctx // LocalContextから取得したContextを使用
                        
                        // 各削除処理を順次実行
                        for (id in idsToDelete) {
                            try {
                                val result = listViewModel.deleteSeedPacketWithImagesInternal(id, context)
                                if (result.isSuccess) {
                                    successCount++
                                } else {
                                    failureCount++
                                }
                            } catch (e: Exception) {
                                failureCount++
                            }
                        }
                        
                        // UIスレッドでSnackbarを表示
                        withContext(Dispatchers.Main) {
                            // 結果に応じてSnackbarを表示
                            when {
                                successCount == idsToDelete.size -> {
                                    snackbarHostState.showSnackbar(
                                        message = getDeleteSuccessMessage(idsToDelete.size),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                successCount > 0 -> {
                                    snackbarHostState.showSnackbar(
                                        message = getDeletePartialSuccessMessage(successCount, failureCount),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                else -> {
                                    snackbarHostState.showSnackbar(
                                        message = SnackbarMessageConstants.DELETE_FAILURE,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    }
    
    // 全画面保存アニメーション（Scaffoldの外側に配置してTopAppBarも含めてグレーアウト）
    if (showSaveAnimation) {
        FullScreenSaveAnimation()
    }
    
    // AI処理中のアニメーション（Scaffoldの外側に配置してTopAppBarも含めてグレーアウト）
    if (showLoadingAnimation) {
        FullScreenLoadingAnimation()
    }
    }
}
