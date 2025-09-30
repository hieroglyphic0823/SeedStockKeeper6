package com.example.seedstockkeeper6

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    onDeleteSelected: (List<String>) -> Unit = {} // 削除処理コールバック
) {
    NavHost(
        navController = navController,
        startDestination = "castle",
        modifier = modifier
    ) {
        composable("castle") {
            Log.d("BootTrace", "Screen: CastleScreen初期化")
            val castleViewModel = viewModel<SeedListViewModel>()
            CastleScreen(
                navController = navController,
                viewModel = castleViewModel
            )
        }
        composable("list") {
            Log.d("BootTrace", "Screen: SeedListScreen初期化")
            val listViewModel = viewModel<SeedListViewModel>()
            SeedListScreen(
                navController = navController,
                viewModel = listViewModel,
                selectedIds = selectedIds,
                onDeleteSelected = onDeleteSelected
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
                context = LocalContext.current
            )
        }
        composable("notification_history") {
            NotificationHistoryScreen(
                navController = navController
            )
        }
    }
}
