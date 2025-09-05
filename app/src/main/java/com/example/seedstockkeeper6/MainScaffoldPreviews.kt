package com.example.seedstockkeeper6

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.lifecycle.viewmodel.compose.viewModel

// プレビュー用のモックFirebaseUser
@Composable
private fun createMockFirebaseUser(): FirebaseUser? {
    // プレビューでは実際のFirebaseUserは使用できないため、nullを返す
    // 実際のプレビューでは適切なモックデータを使用する
    return null
}

// プレビュー用のSettingsViewModel
@Composable
private fun createPreviewSettingsViewModel(isEditMode: Boolean = false): SettingsViewModel {
    val viewModel: SettingsViewModel = viewModel()
    // プレビュー用の状態を設定
    if (isEditMode) {
        viewModel.enterEditMode()
    } else {
        viewModel.exitEditMode()
    }
    return viewModel
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "ホーム画面 (ライトテーマ)")
@Composable
fun MainScaffoldPreview_Light_Home() {
    SeedStockKeeper6Theme(darkTheme = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Search, contentDescription = "検索")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "通知")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "ユーザー")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                painter = painterResource(
                                    id = com.example.seedstockkeeper6.R.drawable.home_light
                                ),
                                contentDescription = "ホーム",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        selected = true,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Search, contentDescription = "検索") },
                        selected = false,
                        onClick = { }
                    )
                    
                    // 中央のFAB
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(
                            onClick = { },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "追加")
                        }
                    }
                    
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                painter = painterResource(
                                    id = com.example.seedstockkeeper6.R.drawable.calendar_light
                                ),
                                contentDescription = "カレンダー",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        selected = false,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Notifications, contentDescription = "通知") },
                        selected = false,
                        onClick = { }
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                contentAlignment = Alignment.Center
            ) {
                Text("ホーム画面 (ライトテーマ)")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "ホーム画面 (ダークテーマ)")
@Composable
fun MainScaffoldPreview_Dark_Home() {
    SeedStockKeeper6Theme(darkTheme = true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Search, contentDescription = "検索")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "通知")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "ユーザー")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "ホーム",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        selected = true,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Search, contentDescription = "検索") },
                        selected = false,
                        onClick = { }
                    )
                    
                    // 中央のFAB
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(
                            onClick = { },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "追加")
                        }
                    }
                    
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "カレンダー",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        selected = false,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Notifications, contentDescription = "通知") },
                        selected = false,
                        onClick = { }
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                contentAlignment = Alignment.Center
            ) {
                Text("ホーム画面 (ダークテーマ)")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "設定画面 - 表示モード")
@Composable
fun SettingsScreenPreview_DisplayMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = createMockFirebaseUser()
        val settingsViewModel = createPreviewSettingsViewModel(isEditMode = false)
        
        Scaffold(
            topBar = {
                MainScaffoldTopAppBar(
                    currentRoute = "settings",
                    navController = navController,
                    user = mockUser
                )
            },
            bottomBar = {
                MainScaffoldNavigationBar(
                    currentRoute = "settings",
                    navController = navController,
                    selectedIds = emptyList(),
                    isListScreen = false,
                    isInputScreen = false,
                    inputViewModel = null,
                    settingsViewModel = settingsViewModel,
                    onSaveRequest = {},
                    onEditRequest = {}
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 農園名と地域設定を一つのカードにまとめる
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 農園名設定
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocalFlorist,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "農園名",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // 表示モード時は読み取り専用テキスト
                            Text(
                                text = "みっちゃん農園",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // 地域設定
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Public,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "地域設定",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // 表示モード時は読み取り専用テキスト
                            Text(
                                text = "温暖地",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = "種子登録時の地域初期値として使用されます",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "設定画面 - 編集モード")
@Composable
fun SettingsScreenPreview_EditMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = createMockFirebaseUser()
        val settingsViewModel = createPreviewSettingsViewModel(isEditMode = true)
        
        Scaffold(
            topBar = {
                MainScaffoldTopAppBar(
                    currentRoute = "settings",
                    navController = navController,
                    user = mockUser
                )
            },
            bottomBar = {
                MainScaffoldNavigationBar(
                    currentRoute = "settings",
                    navController = navController,
                    selectedIds = emptyList(),
                    isListScreen = false,
                    isInputScreen = false,
                    inputViewModel = null,
                    settingsViewModel = settingsViewModel,
                    onSaveRequest = {},
                    onEditRequest = {}
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 農園名と地域設定を一つのカードにまとめる
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 農園名設定
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocalFlorist,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "農園名",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // 編集モード時はTextField
                            TextField(
                                value = "みっちゃん農園",
                                onValueChange = { },
                                label = { Text("農園名") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                readOnly = true
                            )
                        }
                        
                        // 地域設定
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Public,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "地域設定",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // 編集モード時はTextField
                            TextField(
                                value = "温暖地",
                                onValueChange = { },
                                label = { Text("地域初期値") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                readOnly = true
                            )
                            
                            Text(
                                text = "種子登録時の地域初期値として使用されます",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "設定画面 - 新規登録")
@Composable
fun SettingsScreenPreview_NewRegistration() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = createMockFirebaseUser()
        val settingsViewModel = createPreviewSettingsViewModel(isEditMode = true)
        
        Scaffold(
            topBar = {
                MainScaffoldTopAppBar(
                    currentRoute = "settings",
                    navController = navController,
                    user = mockUser
                )
            },
            bottomBar = {
                MainScaffoldNavigationBar(
                    currentRoute = "settings",
                    navController = navController,
                    selectedIds = emptyList(),
                    isListScreen = false,
                    isInputScreen = false,
                    inputViewModel = null,
                    settingsViewModel = settingsViewModel,
                    onSaveRequest = {},
                    onEditRequest = {}
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 農園名と地域設定を一つのカードにまとめる
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 農園名設定
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocalFlorist,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "農園名",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // 新規登録時はOutlinedTextField
                            OutlinedTextField(
                                value = "",
                                onValueChange = { },
                                label = { Text("農園名") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                readOnly = true
                            )
                        }
                        
                        // 地域設定
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Public,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "地域設定",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // 新規登録時はOutlinedTextField
                            OutlinedTextField(
                                value = "温暖地",
                                onValueChange = { },
                                label = { Text("地域初期値") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                readOnly = true
                            )
                            
                            Text(
                                text = "種子登録時の地域初期値として使用されます",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
