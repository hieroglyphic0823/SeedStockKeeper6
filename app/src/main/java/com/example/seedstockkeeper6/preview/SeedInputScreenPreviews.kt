package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種情報表示画面 - 表示モード", heightDp = 2000)
@Composable
fun SeedInputScreenPreview_DisplayMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = com.example.seedstockkeeper6.preview.createMockFirebaseUser()
        val seedInputViewModel = com.example.seedstockkeeper6.preview.createPreviewSeedInputViewModel(isEditMode = false, hasExistingData = true)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            com.example.seedstockkeeper6.ui.components.FamilyIcon(
                                family = "せり科",
                                size = 24.dp
                            )
                            Text(
                                text = "恋むすめ（ニンジン）",
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
                                modifier = Modifier.size(24.dp)
                            ) {
                            Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "編集",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
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
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 画像管理セクション
                ImageManagementSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // 基本情報セクション
                BasicInfoSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // カレンダーセクション
                CalendarSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // 栽培情報セクション
                CultivationInfoSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // 栽培メモ、収穫方法、コンパニオンプランツセクション
                NotesCardSection(seedInputViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種情報新規作成画面", heightDp = 2000)
@Composable
fun SeedInputScreenPreview_NewCreation() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = com.example.seedstockkeeper6.preview.createMockFirebaseUser()
        val seedInputViewModel = com.example.seedstockkeeper6.preview.createPreviewSeedInputViewModel(isEditMode = true, hasExistingData = false)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
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
                                text = "新規作成",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "保存",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 画像管理セクション
                ImageManagementSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // 基本情報セクション
                BasicInfoSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // カレンダーセクション
                CalendarSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // 栽培情報セクション
                CultivationInfoSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // 栽培メモ、収穫方法、コンパニオンプランツセクション
                NotesCardSection(seedInputViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種情報登録画面 - 編集モード", heightDp = 2000)
@Composable
fun SeedInputScreenPreview_EditMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = com.example.seedstockkeeper6.preview.createMockFirebaseUser()
        val seedInputViewModel = com.example.seedstockkeeper6.preview.createPreviewSeedInputViewModel(isEditMode = true, hasExistingData = true)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            com.example.seedstockkeeper6.ui.components.FamilyIcon(
                                family = "せり科",
                                size = 24.dp
                            )
                            Text(
                                text = "恋むすめ（ニンジン）（編集）",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "戻る")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "保存",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 画像管理セクション
                ImageManagementSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // 基本情報セクション
                BasicInfoSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // カレンダーセクション
                CalendarSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // 栽培情報セクション
                CultivationInfoSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // 栽培メモ、収穫方法、コンパニオンプランツセクション
                NotesCardSection(seedInputViewModel)
            }
        }
    }
}
