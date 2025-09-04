package com.example.seedstockkeeper6

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.screens.SeedInputScreen
import com.example.seedstockkeeper6.ui.screens.SeedListScreen
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.google.gson.Gson

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
        
        // プレースホルダー画面
        composable("search") {
            PlaceholderScreen(title = "検索", description = "種子の検索機能")
        }
        composable("calendar") {
            PlaceholderScreen(title = "カレンダー", description = "種子のカレンダー機能")
        }
        composable("settings") {
            val settingsViewModel: com.example.seedstockkeeper6.viewmodel.SettingsViewModel = viewModel()
            com.example.seedstockkeeper6.ui.screens.SettingsScreen(
                navController = navController,
                viewModel = settingsViewModel,
                onSaveRequest = {
                    // FABからの保存要求を処理
                    settingsViewModel.saveSettings()
                },
                onEditRequest = {
                    // FABからの編集要求を処理
                    settingsViewModel.enterEditMode()
                }
            )
        }
    }
}
