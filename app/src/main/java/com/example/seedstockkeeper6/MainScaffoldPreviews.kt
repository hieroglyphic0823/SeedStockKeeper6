package com.example.seedstockkeeper6

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseUser
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "ホーム画面 (ライトテーマ)")
@Composable
fun MainScaffoldPreview_Light_Home() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = createMockFirebaseUser()
        val settingsViewModel = createPreviewSettingsViewModel(isEditMode = false)
        
        Scaffold(
            topBar = {
                MainScaffoldTopAppBar(
                    currentRoute = "list",
                    navController = navController,
                    user = mockUser,
                    settingsViewModel = settingsViewModel
                )
            },
            bottomBar = {
                MainScaffoldNavigationBar(
                    currentRoute = "list",
                    navController = navController,
                    selectedIds = mutableListOf(),
                    isListScreen = true,
                    isInputScreen = false,
                    inputViewModel = null,
                    settingsViewModel = settingsViewModel,
                    onSaveRequest = {}
                )
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
    SeedStockKeeper6Theme(darkTheme = true, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = createMockFirebaseUser()
        val settingsViewModel = createPreviewSettingsViewModel(isEditMode = false)
        
        Scaffold(
            topBar = {
                MainScaffoldTopAppBar(
                    currentRoute = "list",
                    navController = navController,
                    user = mockUser,
                    settingsViewModel = settingsViewModel
                )
            },
            bottomBar = {
                MainScaffoldNavigationBar(
                    currentRoute = "list",
                    navController = navController,
                    selectedIds = mutableListOf(),
                    isListScreen = true,
                    isInputScreen = false,
                    inputViewModel = null,
                    settingsViewModel = settingsViewModel,
                    onSaveRequest = {}
                )
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
                    user = mockUser,
                    settingsViewModel = settingsViewModel
                )
            }
            // 設定画面ではNavigationBarを表示しない
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 農園設定カード
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {

                        // 農園名設定セクション
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocalFlorist,
                                    contentDescription = "農園名",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                        Text(
                                    text = "農園名",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // DisplayMode: リスト項目として表示
                            Row(
                            modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                            Text(
                                text = "みっちゃん農園",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            }
                        }

                        // 区切り線
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )

                        // 地域設定セクション
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                            Icon(
                                    Icons.Filled.Public,
                                    contentDescription = "地域設定",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                                Text(
                                    text = "地域設定",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // DisplayMode: リスト項目として表示
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                color = Color(0xFFFF9800),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                    )
                            Text(
                                text = "温暖地",
                                style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
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
                    user = mockUser,
                    settingsViewModel = settingsViewModel
                )
            },
            floatingActionButton = {
                        FloatingActionButton(
                            onClick = { },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "保存"
                    )
                }
            }
            // 設定画面ではNavigationBarを表示しない
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 農園設定カード
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // 農園名設定セクション
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocalFlorist,
                                    contentDescription = "農園名",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "農園名",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // 編集モード時はOutlinedTextField
                            OutlinedTextField(
                                value = "みっちゃん農園",
                                onValueChange = { },
                                label = { Text("農園名を入力") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                            
                            Text(
                                text = "あなたの農園の名前を設定してください",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 区切り線
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )

                        // 地域設定セクション
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Public,
                                    contentDescription = "地域設定",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "地域設定",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // 編集モード時は色付きボタン
                            Button(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9800), // 温暖地のオレンジ色
                                    contentColor = Color.White
                                ),
                                shape = MaterialTheme.shapes.large,
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                            Text(
                                text = "温暖地",
                                style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                            )
                            }
                            
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

// プレビュー用ヘルパー関数
@Composable
fun createMockFirebaseUser(): FirebaseUser? {
    // プレビュー用のモックユーザー
    return null
}

@Composable
fun createPreviewSettingsViewModel(isEditMode: Boolean): com.example.seedstockkeeper6.viewmodel.SettingsViewModel {
    val viewModel = viewModel<com.example.seedstockkeeper6.viewmodel.SettingsViewModel>()
    if (isEditMode) {
        viewModel.enterEditMode()
    }
    return viewModel
}

// 種情報登録画面のプレビュー用ヘルパー関数
@Composable
fun createPreviewSeedInputViewModel(isEditMode: Boolean, hasExistingData: Boolean = false): com.example.seedstockkeeper6.viewmodel.SeedInputViewModel {
    val viewModel = viewModel<com.example.seedstockkeeper6.viewmodel.SeedInputViewModel>()
    if (isEditMode) {
        viewModel.enterEditMode()
    }
    // hasExistingDataの設定はViewModelの内部状態なので、ここでは設定できない
    // プレビューでは実際のデータを使用する
    return viewModel
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種情報登録画面 - 表示モード")
@Composable
fun SeedInputScreenPreview_DisplayMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = createMockFirebaseUser()
        val seedInputViewModel = createPreviewSeedInputViewModel(isEditMode = false, hasExistingData = true)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
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
                                text = "種情報",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                            IconButton(
                            onClick = { },
                                modifier = Modifier.size(32.dp)
                            ) {
                            Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "編集",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 種情報表示カード
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 種名
                            Row(
                            modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                text = "トマト",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                        
                        // 品種
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                        Text(
                                text = "桃太郎",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // 地域
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = Color(0xFFFF9800),
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        )
                                )
                                Text(
                                    text = "温暖地",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                
                // 栽培カレンダーセクション
                Card(
                                modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "栽培カレンダー",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // 地域表示
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFFFF9800),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "温暖地",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 栽培カレンダーの図
                        com.example.seedstockkeeper6.ui.components.SeedCalendarGrouped(
                            entries = listOf(
                                com.example.seedstockkeeper6.model.CalendarEntry(
                                    region = "温暖地",
                                    sowing_start = 3,
                                    sowing_start_stage = "上旬",
                                    sowing_end = 4,
                                    sowing_end_stage = "下旬",
                                    harvest_start = 6,
                                    harvest_start_stage = "上旬",
                                    harvest_end = 8,
                                    harvest_end_stage = "下旬"
                                )
                            ),
                            packetExpirationYear = 2025,
                            packetExpirationMonth = 3,
                            modifier = Modifier.fillMaxWidth(),
                            heightDp = 80
                        )
                        
                        // 地域情報
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "地域 1",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "温暖地",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                        
                        // 播種期間
                        Text(
                            text = "播種期間",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "播種期間: 3月上旬 ～ 4月下旬",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // 収穫期間
                        Text(
                            text = "収穫期間",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "収穫期間: 6月上旬 ～ 8月下旬",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種情報登録画面 - 編集モード")
@Composable
fun SeedInputScreenPreview_EditMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = createMockFirebaseUser()
        val seedInputViewModel = createPreviewSeedInputViewModel(isEditMode = true, hasExistingData = true)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
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
                                text = "種情報",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(32.dp)
                            ) {
                            Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "キャンセル",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                        FloatingActionButton(
                            onClick = { },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "保存"
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 種情報編集カード
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 種名
                        OutlinedTextField(
                            value = "トマト",
                            onValueChange = { },
                            label = { Text("種名") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )
                        
                        // 品種
                            OutlinedTextField(
                            value = "桃太郎",
                                onValueChange = { },
                            label = { Text("品種") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )
                        
                        // 地域
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800), // 温暖地のオレンジ色
                                contentColor = Color.White
                            ),
                            shape = MaterialTheme.shapes.large,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = "温暖地",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // 栽培カレンダーセクション
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                        Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "栽培カレンダー",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // 地域表示
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFFFF9800),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "温暖地",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 栽培カレンダーの図
                        com.example.seedstockkeeper6.ui.components.SeedCalendarGrouped(
                            entries = listOf(
                                com.example.seedstockkeeper6.model.CalendarEntry(
                                    region = "温暖地",
                                    sowing_start = 3,
                                    sowing_start_stage = "上旬",
                                    sowing_end = 4,
                                    sowing_end_stage = "下旬",
                                    harvest_start = 6,
                                    harvest_start_stage = "上旬",
                                    harvest_end = 8,
                                    harvest_end_stage = "下旬"
                                )
                            ),
                            packetExpirationYear = 2025,
                            packetExpirationMonth = 3,
                            modifier = Modifier.fillMaxWidth(),
                            heightDp = 140
                        )
                        
                        // 地域情報
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "地域 1",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedTextField(
                                value = "温暖地",
                                onValueChange = { },
                                label = { Text("地域名") },
                                modifier = Modifier.width(120.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                        }
                        
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                        
                        // 播種期間
                                Text(
                            text = "播種期間",
                                    style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = "3",
                                onValueChange = { },
                                label = { Text("開始月") },
                                modifier = Modifier.width(70.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                            OutlinedTextField(
                                value = "上旬",
                                onValueChange = { },
                                label = { Text("開始旬") },
                                modifier = Modifier.width(70.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                            Text("～", style = MaterialTheme.typography.bodyLarge)
                            OutlinedTextField(
                                value = "4",
                                onValueChange = { },
                                label = { Text("終了月") },
                                modifier = Modifier.width(70.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                            OutlinedTextField(
                                value = "下旬",
                                onValueChange = { },
                                label = { Text("終了旬") },
                                modifier = Modifier.width(70.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                        }
                        
                        // 収穫期間
                        Text(
                            text = "収穫期間",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = "6",
                                onValueChange = { },
                                label = { Text("開始月") },
                                modifier = Modifier.width(70.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                            OutlinedTextField(
                                value = "上旬",
                                onValueChange = { },
                                label = { Text("開始旬") },
                                modifier = Modifier.width(70.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                            Text("～", style = MaterialTheme.typography.bodyLarge)
                            OutlinedTextField(
                                value = "8",
                                onValueChange = { },
                                label = { Text("終了月") },
                                modifier = Modifier.width(70.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                            OutlinedTextField(
                                value = "下旬",
                                onValueChange = { },
                                label = { Text("終了旬") },
                                modifier = Modifier.width(70.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            )
                        }
                        
                        // 地域追加ボタン
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("地域を追加")
                        }
                    }
                }
            }
        }
    }
}