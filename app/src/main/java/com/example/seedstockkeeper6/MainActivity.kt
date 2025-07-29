package com.example.seedstockkeeper6

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.screens.SeedInputScreen
import com.example.seedstockkeeper6.ui.screens.SeedListScreen
import com.example.seedstockkeeper6.ui.screens.deleteSeedPacketWithImages
import com.example.seedstockkeeper6.ui.theme.SeedStockTheme
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeedStockTheme( // ← ここで壁紙連動テーマが有効に！
                dynamicColor = true
            ) {
                AppNavHost()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val selectedIds = remember { mutableStateListOf<String>() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isListScreen = currentRoute == "list"
    val isInputScreen = currentRoute?.startsWith("input") == true

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }

    // 入力画面で使うViewModelの一時保存
    var inputViewModel: SeedInputViewModel? = null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SeedStockKeeper6") },
                actions = {
                    when {
                        isListScreen && selectedIds.isNotEmpty() -> {
                            IconButton(onClick = {
                                selectedIds.forEach { id ->
                                    deleteSeedPacketWithImages(id)
                                }
                                selectedIds.clear()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }

                        isInputScreen -> {
                            IconButton(onClick = {
                                inputViewModel?.saveSeed {
                                    navController.popBackStack()
                                }
                            }) {
                                Icon(Icons.Default.Save, contentDescription = "Save")
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isListScreen && selectedIds.isEmpty()) {
                FloatingActionButton(
                    onClick = {
                        val emptyPacketJson = Uri.encode(Gson().toJson(SeedPacket()))
                        navController.navigate("input/$emptyPacketJson")
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            NavHost(navController = navController, startDestination = "list") {
                composable("list") {
                    SeedListScreen(navController, selectedIds)
                }
                composable(
                    route = "input/{packet}",
                    arguments = listOf(navArgument("packet") { defaultValue = "" })
                ) { backStackEntry ->
                    val json = backStackEntry.arguments?.getString("packet") ?: ""
                    val decodedJson = Uri.decode(json)
                    val packet = if (decodedJson.isNotEmpty()) Gson().fromJson(decodedJson, SeedPacket::class.java) else null

                    inputViewModel = viewModel()
                    SeedInputScreen(
                        navController = navController,
                        packet = packet,
                        viewModel = inputViewModel!!
                    )
                }
            }
        }
    }
}
