@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.seedstockkeeper6

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.screens.SeedInputScreen
import com.example.seedstockkeeper6.ui.screens.SeedListScreen
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val selectedIds = remember { mutableStateListOf<String>() }
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val isListScreen = currentRoute == "list"
            val isInputScreen = currentRoute?.startsWith("input") == true

            val listViewModel: SeedListViewModel = viewModel()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("SeedStockKeeper6") },
                        actions = {
                            when {
                                isListScreen && selectedIds.isNotEmpty() -> {
                                    IconButton(onClick = {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            selectedIds.forEach { id ->
                                                listViewModel.deleteSeedPacketWithImages(documentId = id) {}
                                            }
                                            selectedIds.clear()
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                }
                                isInputScreen && navBackStackEntry != null -> {
                                    val inputViewModel: SeedInputViewModel = viewModel(
                                        viewModelStoreOwner = navBackStackEntry!!
                                    )
                                    val context = LocalContext.current

                                    IconButton(onClick = {
                                        inputViewModel.saveSeed(context) {
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
                        FloatingActionButton(onClick = {
                            val emptyPacketJson = URLEncoder.encode(Gson().toJson(SeedPacket()), StandardCharsets.UTF_8.toString())
                            navController.navigate("input/$emptyPacketJson")
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            ) { padding ->
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.padding(padding),
                    selectedIds = selectedIds
                )
            }
        }
    }
}

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
            val listViewModel: SeedListViewModel = viewModel()
            SeedListScreen(
                navController = navController,
                viewModel = listViewModel,
                selectedIds = selectedIds
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
                viewModel = currentInputViewModel
            )
        }
    }
}
