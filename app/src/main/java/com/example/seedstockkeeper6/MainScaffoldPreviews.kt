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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種情報表示画面 - 表示モード", heightDp = 1200)
@Composable
fun SeedInputScreenPreview_DisplayMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = createMockFirebaseUser()
        val seedInputViewModel = createPreviewSeedInputViewModel(isEditMode = false, hasExistingData = true)
        
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
                                modifier = Modifier.size(32.dp)
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
                    .padding(
                        top = 0.dp,  // 上パディングのみ0に
                        start = paddingValues.calculateLeftPadding(LocalLayoutDirection.current),
                        end = paddingValues.calculateRightPadding(LocalLayoutDirection.current),
                        bottom = paddingValues.calculateBottomPadding()
                    )
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 画像管理セクション
                PreviewImageManagementSection(seedInputViewModel)
                
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
                
                // コンパニオンプランツセクション
                CompanionPlantsSection(seedInputViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種情報登録画面 - 編集モード", heightDp = 1200)
@Composable
fun SeedInputScreenPreview_EditMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = createMockFirebaseUser()
        val seedInputViewModel = createPreviewSeedInputViewModel(isEditMode = true, hasExistingData = true)
        
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
                                modifier = Modifier.size(32.dp)
                            )
                        Text(
                                text = "種情報編集",
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
                    .padding(
                        top = 0.dp,  // 上パディングのみ0に
                        start = paddingValues.calculateLeftPadding(LocalLayoutDirection.current),
                        end = paddingValues.calculateRightPadding(LocalLayoutDirection.current),
                        bottom = paddingValues.calculateBottomPadding()
                    )
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 画像管理セクション
                PreviewImageManagementSection(seedInputViewModel)
                
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
                
                // コンパニオンプランツセクション
                CompanionPlantsSection(seedInputViewModel)
            }
        }
    }
}

// モックデータ作成関数
fun createMockFirebaseUser(): com.google.firebase.auth.FirebaseUser? {
    return null // プレビュー用なのでnullを返す
}

// プレビュー用のViewModelを作成（ViewModelの制約に対応）
fun createPreviewSeedInputViewModel(isEditMode: Boolean, hasExistingData: Boolean): com.example.seedstockkeeper6.viewmodel.SeedInputViewModel {
    val viewModel = com.example.seedstockkeeper6.viewmodel.SeedInputViewModel()
    
    // プレビュー用のデモデータを設定
    if (hasExistingData) {
        // デモデータを含むSeedPacketオブジェクトを作成
        val demoPacket = com.example.seedstockkeeper6.model.SeedPacket(
            id = "demo-seed-id", // DisplayModeの判定用にIDを設定
            documentId = "demo-document-id", // DisplayModeの判定用にdocumentIdを設定
            productName = "落花生",
            variety = "ラッカセイ",
            family = "マメ科",
            expirationYear = 2026,
            expirationMonth = 3,
            productNumber = "PK-2024-001",
            company = "タキイ種苗株式会社",
            contents = "甘みが強く、香り高い落花生です。暖地での栽培に適しています。",
            cultivation = com.example.seedstockkeeper6.model.Cultivation(
                notes = "日当たりの良い場所で栽培し、水はけの良い土壌を選んでください。",
                spacing_cm_row_min = 60,
                spacing_cm_row_max = 80,
                spacing_cm_plant_min = 20,
                spacing_cm_plant_max = 30,
                germinationTemp_c = "20-25",
                growingTemp_c = "18-28",
                harvesting = "莢が茶色くなったら収穫適期です。"
            ),
            calendar = listOf(
                                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "暖地",
                    sowing_start = 4,
                    sowing_start_stage = "下旬",
                    sowing_end = 6,
                    sowing_end_stage = "上旬",
                    harvest_start = 10,
                    harvest_start_stage = "中旬",
                    harvest_end = 11,
                    harvest_end_stage = "中旬"
                )
            ),
            companionPlants = listOf(
                com.example.seedstockkeeper6.model.CompanionPlant(
                    plant = "マリーゴールド",
                    effects = listOf("01", "06") // 害虫予防、土壌改善
                ),
                com.example.seedstockkeeper6.model.CompanionPlant(
                    plant = "バジル",
                    effects = listOf("01", "05") // 害虫予防、風味向上
                )
            )
        )
        
        // ViewModelのpublicメソッドを使用してデータを設定
        viewModel.setSeed(demoPacket)
        
        // プレビュー用の画像データを設定（Firebaseの参照を避ける）
        viewModel.imageUris = mutableStateListOf(
            android.net.Uri.parse("https://example.com/seed1.jpg"),
            android.net.Uri.parse("https://example.com/seed2.jpg")
        )
        
        // 編集モードの設定
        if (isEditMode) {
            viewModel.enterEditMode()
        }
        // setSeed()で既にisEditMode = falseが設定されているので、DisplayModeの場合は追加の処理は不要
    }
    
    return viewModel
}

// プレビュー用の画像管理セクション（Firebaseの参照を避ける）
@Composable
fun PreviewImageManagementSection(viewModel: com.example.seedstockkeeper6.viewmodel.SeedInputViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val imageSize = (screenWidth - 32.dp) / 3 // 3枚表示に合わせる
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = "画像管理",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(viewModel.imageUris) { index, uri ->
                Box(
                    modifier = Modifier
                        .size(imageSize)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    AsyncImage(
                        model = uri.toString(),
                        contentDescription = "画像$index",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "農園情報画面 - 表示モード", heightDp = 1200)
@Composable
fun SettingsScreenPreview_DisplayMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = createMockFirebaseUser()
        val settingsViewModel = createPreviewSettingsViewModel(isEditMode = false, hasExistingData = true)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.garden_cart),
                                contentDescription = "農園設定",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "農園設定",
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
                    .padding(
                        top = 0.dp,  // 上パディングのみ0に
                        start = paddingValues.calculateLeftPadding(LocalLayoutDirection.current),
                        end = paddingValues.calculateRightPadding(LocalLayoutDirection.current),
                        bottom = paddingValues.calculateBottomPadding()
                    )
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
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
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
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
                                    text = settingsViewModel.farmName.ifEmpty { "未設定" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (settingsViewModel.farmName.isEmpty()) 
                                        MaterialTheme.colorScheme.onSurfaceVariant 
                                    else 
                                        MaterialTheme.colorScheme.onSurface,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                )
                            }
                        }
                        
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
                                    contentDescription = "地域",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "地域",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // 表示モード時は色付きSurface
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = getRegionColor(settingsViewModel.defaultRegion).copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                color = getRegionColor(settingsViewModel.defaultRegion),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                    )
                                    Text(
                                        text = settingsViewModel.defaultRegion.ifEmpty { "未設定" },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (settingsViewModel.defaultRegion.isEmpty()) 
                                            MaterialTheme.colorScheme.onSurfaceVariant 
                                        else 
                                            MaterialTheme.colorScheme.onSurface,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
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

// プレビュー用のSettingsViewModelを作成
fun createPreviewSettingsViewModel(isEditMode: Boolean, hasExistingData: Boolean): com.example.seedstockkeeper6.viewmodel.SettingsViewModel {
    val viewModel = com.example.seedstockkeeper6.viewmodel.SettingsViewModel()
    
    // プレビュー用のデモデータを設定
    if (hasExistingData) {
        viewModel.updateFarmName("サンプル農園")
        viewModel.updateDefaultRegion("暖地")
    }
    
    // 編集モードの設定
    if (isEditMode) {
        viewModel.enterEditMode()
    }
    
    return viewModel
}

// 地域ごとの色定義（プレビュー用）
private fun getRegionColor(region: String): Color {
    return when (region) {
        "寒地" -> Color(0xFF1A237E) // 紺
        "寒冷地" -> Color(0xFF1976D2) // 青
        "温暖地" -> Color(0xFFFF9800) // オレンジ
        "暖地" -> Color(0xFFE91E63) // ピンク
        else -> Color(0xFF9E9E9E) // グレー（未設定時）
    }
}
