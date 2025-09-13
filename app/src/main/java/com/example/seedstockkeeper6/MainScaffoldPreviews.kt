package com.example.seedstockkeeper6

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
@Preview(showBackground = true, name = "種情報表示画面 - 表示モード", heightDp = 1400)
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
                
                // メモ・コンパニオンプランツカード（DisplayModeのみ）
                NotesCardSection(seedInputViewModel)
                
                // 栽培情報セクション
                PreviewCultivationInfoSection(seedInputViewModel)
                
                // 区切り線
                        HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // コンパニオンプランツセクション（編集モードのみ）
                if (seedInputViewModel.isEditMode || !seedInputViewModel.hasExistingData) {
                    CompanionPlantsSection(seedInputViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種情報新規作成画面", heightDp = 1400)
@Composable
fun SeedInputScreenPreview_NewCreation() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = createMockFirebaseUser()
        val seedInputViewModel = createPreviewSeedInputViewModel(isEditMode = false, hasExistingData = false)
        
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
                                text = "種情報新規作成",
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
                
                // メモ・コンパニオンプランツカード（DisplayModeのみ）
                NotesCardSection(seedInputViewModel)
                
                // 栽培情報セクション
                PreviewCultivationInfoSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // コンパニオンプランツセクション（編集モードのみ）
                if (seedInputViewModel.isEditMode || !seedInputViewModel.hasExistingData) {
                    CompanionPlantsSection(seedInputViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種情報登録画面 - 編集モード", heightDp = 1400)
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
                        contentDescription = "保存",
                        modifier = Modifier.size(28.dp)
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
                
                // メモ・コンパニオンプランツカード（DisplayModeのみ）
                NotesCardSection(seedInputViewModel)
                
                // 栽培情報セクション
                PreviewCultivationInfoSection(seedInputViewModel)
                
                // 区切り線
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // コンパニオンプランツセクション（編集モードのみ）
                if (seedInputViewModel.isEditMode || !seedInputViewModel.hasExistingData) {
                    CompanionPlantsSection(seedInputViewModel)
                }
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
            productName = "恋むすめ",
            variety = "ニンジン",
            family = "せり科",
            expirationYear = 2026,
            expirationMonth = 10,
            productNumber = "CR-2024-001",
            company = "タキイ種苗株式会社",
            contents = "甘みが強く、香り高いニンジンです。暖地での栽培に適しています。",
            cultivation = com.example.seedstockkeeper6.model.Cultivation(
                notes = "日当たりの良い場所で栽培し、水はけの良い土壌を選んでください。",
                spacing_cm_row_min = 30,
                spacing_cm_row_max = 40,
                spacing_cm_plant_min = 5,
                spacing_cm_plant_max = 10,
                germinationTemp_c = "15-20",
                growingTemp_c = "15-25",
                harvesting = "根が太くなったら収穫適期です。"
            ),
            calendar = listOf(
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "暖地",
                    sowing_start_date = "2025-08-15",
                    sowing_end_date = "2025-09-31",
                    harvest_start_date = "2025-10-01",
                    harvest_end_date = "2025-12-31"
                )
            ),
            companionPlants = listOf(
                com.example.seedstockkeeper6.model.CompanionPlant(
                    plant = "マリーゴールド",
                    effects = listOf("01", "06") // 害虫予防、土壌改善
                ),
                com.example.seedstockkeeper6.model.CompanionPlant(
                    plant = "ネギ",
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
        
        // OCR対象画像を最初の画像に設定（プレビューで枠を表示するため）
        viewModel.setOcrTarget(0)
        
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.image),
                contentDescription = "画像管理",
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "画像管理",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(viewModel.imageUris) { index, uri ->
                Box(
                    modifier = Modifier
                        .size(imageSize)
                        .padding(end = 2.dp)
                        .then(
                            if (viewModel.ocrTargetIndex == index) {
                                Modifier.border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                            } else {
                                Modifier
                            }
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
                                    style = MaterialTheme.typography.bodyMedium,
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
                                        style = MaterialTheme.typography.bodyMedium,
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "地域選択ダイアログ（実際のDialog）", heightDp = 800)
@Composable
fun RegionSelectionDialogPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        // プレビュー用のデモデータ（extractRegionsFromOcrResultで検出される地域）
        val demoOcrResult = com.example.seedstockkeeper6.model.SeedPacket(
            id = "demo-ocr-result",
            productName = "恋むすめ",
            variety = "ニンジン",
            family = "せり科",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "暖地",
                    sowing_start_date = "2025-08-15",
                    sowing_end_date = "2025-09-31",
                    harvest_start_date = "2025-10-01",
                    harvest_end_date = "2025-12-31"
                ),
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "温暖地",
                    sowing_start_date = "2025-08-15",
                    sowing_end_date = "2025-09-31",
                    harvest_start_date = "2025-10-01",
                    harvest_end_date = "2025-12-31"
                )
            )
        )
        
        // 実際のRegionSelectionDialogを使用（プレビュー用にshowDialog=trueで固定）
        com.example.seedstockkeeper6.ui.components.RegionSelectionDialog(
            showDialog = true,
            regionList = listOf("暖地", "温暖地"), // extractRegionsFromOcrResultで検出される地域
            ocrResult = demoOcrResult,
            croppedCalendarBitmap = null,
            editingCalendarEntry = null,
            defaultRegion = "暖地",
            onRegionSelected = { },
            onStartEditing = { },
            onUpdateEditing = { },
            onSaveEditing = { },
            onCancelEditing = { },
            onDismiss = { }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "地域選択ダイアログ（内容表示）", heightDp = 800)
@Composable
fun RegionSelectionDialogContentPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        // プレビュー用のデモデータ
        val demoOcrResult = com.example.seedstockkeeper6.model.SeedPacket(
            id = "demo-ocr-result",
            productName = "恋むすめ",
            variety = "ニンジン",
            family = "せり科",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "暖地",
                    sowing_start_date = "2025-08-15",
                    sowing_end_date = "2025-09-31",
                    harvest_start_date = "2025-10-01",
                    harvest_end_date = "2025-12-31"
                ),
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "温暖地",
                    sowing_start_date = "2025-08-15",
                    sowing_end_date = "2025-09-31",
                    harvest_start_date = "2025-10-01",
                    harvest_end_date = "2025-12-31"
                )
            )
        )
        
        // Dialogの内容を直接表示（プレビュー用）
        androidx.compose.ui.window.Dialog(onDismissRequest = { }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "地域区分を選択してください",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 地域選択ボタン
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("暖地", "温暖地").forEach { region ->
                            Button(
                                onClick = { },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (region == "暖地") 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = region,
                                    color = if (region == "暖地") 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 選択された地域の栽培期間表示
                    val selectedRegionEntry = demoOcrResult.calendar?.find { it.region == "暖地" }
                    selectedRegionEntry?.let { entry ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "暖地の栽培期間",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                // 播種期間と収穫期間の表示（色変更が反映される）
                                com.example.seedstockkeeper6.ui.components.CalendarEntryDisplay(entry)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種一覧画面", heightDp = 800)
@Composable
fun SeedListScreenPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        // プレビュー用のデモデータ
        val demoSeeds = listOf(
            com.example.seedstockkeeper6.model.SeedPacket(
                id = "demo-seed-1",
                productName = "恋むすめ",
                variety = "ニンジン",
                family = "せり科",
                expirationYear = 2026,
                expirationMonth = 10,
                companionPlants = listOf(
                    com.example.seedstockkeeper6.model.CompanionPlant(
                        plant = "ネギ",
                        effects = listOf("01", "02") // 害虫予防、病気予防
                    ),
                    com.example.seedstockkeeper6.model.CompanionPlant(
                        plant = "レタス",
                        effects = listOf("03", "04") // 生育促進、空間活用
                    ),
                    com.example.seedstockkeeper6.model.CompanionPlant(
                        plant = "ラディッシュ",
                        effects = listOf("05") // 風味向上
                    )
                )
            ),
            com.example.seedstockkeeper6.model.SeedPacket(
                id = "demo-seed-2",
                productName = "サラダ菜",
                variety = "レタス",
                family = "キク科",
                expirationYear = 2025,
                expirationMonth = 12,
                companionPlants = listOf(
                    com.example.seedstockkeeper6.model.CompanionPlant(
                        plant = "ニンジン",
                        effects = listOf("01", "03") // 害虫予防、生育促進
                    ),
                    com.example.seedstockkeeper6.model.CompanionPlant(
                        plant = "ラディッシュ",
                        effects = listOf("04") // 空間活用
                    )
                )
            ),
            com.example.seedstockkeeper6.model.SeedPacket(
                id = "demo-seed-3",
                productName = "二十日大根",
                variety = "ラディッシュ",
                family = "アブラナ科",
                expirationYear = 2026,
                expirationMonth = 3,
                companionPlants = listOf(
                    com.example.seedstockkeeper6.model.CompanionPlant(
                        plant = "レタス",
                        effects = listOf("03", "06") // 生育促進、土壌改善
                    ),
                    com.example.seedstockkeeper6.model.CompanionPlant(
                        plant = "ニンジン",
                        effects = listOf("01", "05") // 害虫予防、風味向上
                    )
                )
            )
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "種一覧",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // 種一覧のアイテム
            items(demoSeeds) { seed ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    // FamilyIcon（色変更が反映される）
                    com.example.seedstockkeeper6.ui.components.FamilyIcon(
                        family = seed.family,
                        size = 50.dp,
                        cornerRadius = 8.dp,
                        rotationLabel = "3年", // 連作バッジのデモ
                        badgeProtrusion = 4.dp
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${seed.productName} (${seed.variety})", 
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "有効期限: ${seed.expirationYear}年 ${seed.expirationMonth}月", 
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        // コンパニオンプランツの表示（色変更が反映される）
                        if (seed.companionPlants.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Text(
                                    "コンパニオンプランツ: ",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                com.example.seedstockkeeper6.ui.components.CompanionEffectIcon(
                                    effects = listOf("PEST_PREVENTION", "GROWTH_PROMOTION", "FLAVOR_ENHANCEMENT")
                                )
                            }
                        }
                    }
                }
                
                // 区切り線（最後のアイテム以外）
                if (demoSeeds.indexOf(seed) < demoSeeds.size - 1) {
                    androidx.compose.material3.HorizontalDivider(
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

// プレビュー専用の栽培情報セクション（播種期間・収穫期間を確実に表示）
@Composable
fun PreviewCultivationInfoSection(viewModel: com.example.seedstockkeeper6.viewmodel.SeedInputViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.monitoring),
                contentDescription = "栽培情報",
                modifier = Modifier.size(24.dp)
            )
            Text(
                "栽培情報",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        // 商品番号
        Text(
            text = "商品番号: ${viewModel.packet.productNumber.ifEmpty { "未設定" }}",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 会社
        Text(
            text = "会社: ${viewModel.packet.company.ifEmpty { "未設定" }}",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 原産国
        Text(
            text = "原産国: ${viewModel.packet.originCountry.ifEmpty { "未設定" }}",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 内容量
        Text(
            text = "内容量: ${viewModel.packet.contents.ifEmpty { "未設定" }}",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 発芽率
        Text(
            text = "発芽率: ${viewModel.packet.germinationRate.ifEmpty { "未設定" }}",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 種子処理
        Text(
            text = "種子処理: ${viewModel.packet.seedTreatment.ifEmpty { "未設定" }}",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 条間
        Text(
            text = "条間: ${viewModel.packet.cultivation.spacing_cm_row_min}～${viewModel.packet.cultivation.spacing_cm_row_max}cm",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 株間
        Text(
            text = "株間: ${viewModel.packet.cultivation.spacing_cm_plant_min}～${viewModel.packet.cultivation.spacing_cm_plant_max}cm",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 発芽温度
        Text(
            text = "発芽温度: ${viewModel.packet.cultivation.germinationTemp_c.ifEmpty { "未設定" }}",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 生育温度
        Text(
            text = "生育温度: ${viewModel.packet.cultivation.growingTemp_c.ifEmpty { "未設定" }}",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 堆肥
        Text(
            text = "堆肥: ${viewModel.packet.cultivation.soilPrep_per_sqm.compost_kg}kg/㎡",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 苦土石灰
        Text(
            text = "苦土石灰: ${viewModel.packet.cultivation.soilPrep_per_sqm.dolomite_lime_g}g/㎡",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 化成肥料
        Text(
            text = "化成肥料: ${viewModel.packet.cultivation.soilPrep_per_sqm.chemical_fertilizer_g}g/㎡",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 播種期間
        val calendarEntry = viewModel.packet.calendar?.firstOrNull()
        val sowingPeriod = if (calendarEntry != null) {
            formatDateRange(calendarEntry.sowing_start_date, calendarEntry.sowing_end_date)
        } else {
            "未設定"
        }
        Text(
            text = "播種期間: $sowingPeriod",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 収穫期間
        val harvestPeriod = if (calendarEntry != null) {
            formatDateRange(calendarEntry.harvest_start_date, calendarEntry.harvest_end_date)
        } else {
            "未設定"
        }
        Text(
            text = "収穫期間: $harvestPeriod",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// 日付範囲を旬形式でフォーマットするヘルパー関数
private fun formatDateRange(startDate: String, endDate: String): String {
    if (startDate.isEmpty() && endDate.isEmpty()) {
        return "未設定"
    }
    
    val startFormatted = if (startDate.isNotEmpty()) {
        val year = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(startDate)
        val month = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(startDate)
        val stage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDateToStage(startDate)
        "${year}年${month}月(${stage})"
    } else {
        "未設定"
    }
    
    val endFormatted = if (endDate.isNotEmpty()) {
        val year = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(endDate)
        val month = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(endDate)
        val stage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDateToStage(endDate)
        "${year}年${month}月(${stage})"
    } else {
        "未設定"
    }
    
    return if (startDate.isEmpty() || endDate.isEmpty()) {
        if (startDate.isEmpty()) endFormatted else startFormatted
    } else {
        "$startFormatted ～ $endFormatted"
    }
}
