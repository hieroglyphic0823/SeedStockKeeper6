package com.example.seedstockkeeper6

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.seedstockkeeper6.ui.theme.SeedStockTheme
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeedStockTheme(
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

    val listViewModel: SeedListViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SeedStockKeeper6") },
                actions = {
                    when {
                        isListScreen && selectedIds.isNotEmpty() -> {
                            IconButton(onClick = {
                                selectedIds.forEach { id ->
                                    listViewModel.deleteSeedPacketWithImages(id) { result ->
                                        if (result.isSuccess) {
                                            Log.d("MainActivity", "Deleted $id successfully")
                                        } else {
                                            Log.e("MainActivity", "Failed to delete $id", result.exceptionOrNull())
                                        }
                                    }
                                }
                                selectedIds.clear()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }

                        isInputScreen -> {
                            val inputViewModel: SeedInputViewModel = viewModel()
                            IconButton(onClick = {
                                inputViewModel.saveSeed {
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
                    SeedListScreen(
                        navController = navController,
                        selectedIds = selectedIds
                    )
                }
                composable(
                    route = "input/{packet}",
                    arguments = listOf(navArgument("packet") { defaultValue = "" })
                ) { backStackEntry ->
                    val json = backStackEntry.arguments?.getString("packet") ?: ""
                    val packet = if (json.isNotEmpty()) Gson().fromJson(json, SeedPacket::class.java) else null

                    val currentInputViewModel: SeedInputViewModel = viewModel()

                    LaunchedEffect(json) {
                        Log.d("AppNavHost_LE", "Before setSeed, imageUris: ${currentInputViewModel.imageUris}")
                        currentInputViewModel.setSeed(packet)
                        Log.d("AppNavHost_LE", "After setSeed, imageUris: ${currentInputViewModel.imageUris}")
                    }

                    SeedInputScreen(
                        navController = navController,
                        viewModel = currentInputViewModel
                    )
                }
            }
        }
    }
}
