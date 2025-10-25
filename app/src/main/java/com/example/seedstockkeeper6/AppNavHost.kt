package com.example.seedstockkeeper6

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.screens.SeedInputScreen
import com.example.seedstockkeeper6.ui.screens.SeedListScreen
import com.example.seedstockkeeper6.ui.screens.CalendarScreen
import com.example.seedstockkeeper6.ui.screens.CastleScreen
import com.example.seedstockkeeper6.ui.screens.NotificationPreviewScreen
import com.example.seedstockkeeper6.ui.screens.NotificationHistoryScreen
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.google.gson.Gson

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    selectedIds: MutableList<String>,
    settingsViewModel: com.example.seedstockkeeper6.viewmodel.SettingsViewModel? = null,
    onSaveRequest: () -> Unit = {}, // MainScaffoldからの保存リクエストコールバック
    onDeleteSelected: (List<String>) -> Unit = {}, // 削除処理コールバック
    onRefreshUnreadCount: () -> Unit = {} // 未読通知数更新コールバック
) {
    NavHost(
        navController = navController,
        startDestination = "castle",
        modifier = modifier
    ) {
        composable("castle") {
            val castleViewModel = viewModel<SeedListViewModel>()
            CastleScreen(
                navController = navController,
                viewModel = castleViewModel
            )
        }
        composable("list?filter={filter}") { backStackEntry ->
            val listViewModel = viewModel<SeedListViewModel>()
            SeedListScreen(
                navController = navController,
                viewModel = listViewModel,
                selectedIds = selectedIds,
                onDeleteSelected = onDeleteSelected,
                backStackEntry = backStackEntry
            )
        }
        composable("input/{packet}") { backStackEntry ->
            val json = backStackEntry.arguments?.getString("packet") ?: ""
            val packet = if (json.isNotEmpty()) Gson().fromJson(json, SeedPacket::class.java) else null
            val currentInputViewModel: SeedInputViewModel = viewModel(viewModelStoreOwner = backStackEntry)
            LaunchedEffect(json) {
                currentInputViewModel.setSeed(packet)
            }
            SeedInputScreen(
                navController = navController,
                viewModel = currentInputViewModel,
                settingsViewModel = settingsViewModel,
                onSaveRequest = onSaveRequest
            )
        }
        
        // プレースホルダー画面
        composable("search") {
            PlaceholderScreen(title = "検索", description = "種子の検索機能")
        }
        composable("calendar") {
            val calendarViewModel = viewModel<SeedListViewModel>()
            CalendarScreen(
                navController = navController,
                viewModel = calendarViewModel
            )
        }
        composable("settings") {
            val viewModel = settingsViewModel ?: viewModel<com.example.seedstockkeeper6.viewmodel.SettingsViewModel>()
            com.example.seedstockkeeper6.ui.screens.SettingsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable("notification_preview") {
            NotificationPreviewScreen(
                navController = navController,
                context = LocalContext.current,
                onRefreshUnreadCount = onRefreshUnreadCount
            )
        }
        composable(
            route = "notification_history",
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://seedstockkeeper6/notification_history" }
            )
        ) {
            NotificationHistoryScreen(
                navController = navController,
                onRefreshUnreadCount = onRefreshUnreadCount
            )
        }
        composable("map_selection") {
            val settingsViewModel = settingsViewModel ?: viewModel<com.example.seedstockkeeper6.viewmodel.SettingsViewModel>()
            com.example.seedstockkeeper6.ui.screens.MapSelectionScreen(
                initialLatitude = if (settingsViewModel.farmLatitude != 0.0) settingsViewModel.farmLatitude else 35.6762,
                initialLongitude = if (settingsViewModel.farmLongitude != 0.0) settingsViewModel.farmLongitude else 139.6503,
                onLocationSelected = { latitude, longitude, address ->
                    // 設定画面に戻って座標を保存
                    settingsViewModel.updateFarmLocation(latitude, longitude, address)
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
    }
}
