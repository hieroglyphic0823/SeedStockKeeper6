package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.seedstockkeeper6.FullScreenSaveAnimation
import com.example.seedstockkeeper6.viewmodel.SettingsViewModel
import com.example.seedstockkeeper6.ui.components.PrefectureSelectionBottomSheet
import kotlinx.coroutines.delay

// 地域ごとの色定義
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
@Composable
fun RegionSelectionBottomSheet(
    selectedRegion: String,
    onRegionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val regions = listOf(
        "寒地", "寒冷地", "温暖地", "暖地"
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Icon(
                    Icons.Filled.Public,
                    contentDescription = "地域選択",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "地域",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            regions.forEach { region ->
                Button(
                    onClick = { onRegionSelected(region) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getRegionColor(region),
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.large,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (region == selectedRegion) 4.dp else 2.dp
                    ),
                    border = if (region == selectedRegion) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                    } else null
                ) {
                    Text(
                        text = region,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (region == selectedRegion) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    var showRegionBottomSheet by remember { mutableStateOf(false) }
    var showPrefectureBottomSheet by remember { mutableStateOf(false) }
    // showSaveAnimationはMainScaffoldで管理されるため削除
    
    // 通知設定の状態（ViewModelから取得）
    val notificationFrequency = viewModel.notificationFrequency
    val selectedWeekday = viewModel.selectedWeekday
    val selectedPrefecture = viewModel.selectedPrefecture
    val seedInfoUrlProvider = viewModel.seedInfoUrlProvider
    val customSeedInfoUrl = viewModel.customSeedInfoUrl
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Snackbarの表示
    LaunchedEffect(viewModel.showSnackbar) {
        viewModel.showSnackbar?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }
    
    // 設定画面が表示された時の初期化処理
    LaunchedEffect(Unit) {
        // 設定画面が表示された時に設定を再読み込み
        // データの読み込みはViewModelのinitで自動実行される
    }
    
    
    
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (viewModel.isEditMode) {
                FloatingActionButton(
                    onClick = {
                        // 保存処理をシミュレート（実際の保存処理に合わせて調整）
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            kotlinx.coroutines.delay(1000) // 1秒の保存処理をシミュレート
                            viewModel.saveSettings()
                            viewModel.exitEditMode()
                        }
                    },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "保存"
                        )
                    }
                }
            }
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
                        
                        if (viewModel.isEditMode || !viewModel.hasExistingData) {
                            OutlinedTextField(
                                value = viewModel.farmName,
                                onValueChange = { newValue ->
                                    viewModel.updateFarmName(newValue)
                                },
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
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        } else {
                            // DisplayMode: リスト項目として表示
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = viewModel.farmName.ifEmpty { "未設定" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (viewModel.farmName.isEmpty()) 
                                        MaterialTheme.colorScheme.onSurfaceVariant 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 区切り線
                    HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )

                    // 農園主設定セクション
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = "農園主",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "農園主",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        if (viewModel.isEditMode || !viewModel.hasExistingData) {
                            // 編集モード: ラジオボタンで選択
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val farmOwnerOptions = listOf("水戸黄門", "お銀", "八兵衛", "その他")
                                
                                farmOwnerOptions.forEach { option ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                viewModel.updateFarmOwner(option)
                                                if (option != "その他") {
                                                    viewModel.updateCustomFarmOwner("")
                                                }
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = viewModel.farmOwner == option,
                                            onClick = { 
                                                viewModel.updateFarmOwner(option)
                                                if (option != "その他") {
                                                    viewModel.updateCustomFarmOwner("")
                                                }
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = MaterialTheme.colorScheme.primary,
                                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                
                                // その他選択時のフリー入力フィールド
                                if (viewModel.farmOwner == "その他") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = viewModel.customFarmOwner,
                                        onValueChange = { newValue ->
                                            viewModel.updateCustomFarmOwner(newValue)
                                        },
                                        label = { Text("農園主名を入力") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                        )
                                    )
                                }
                            }
                            
                            Text(
                                text = "あなたの農園の主を選択してください",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        } else {
                            // DisplayMode: リスト項目として表示
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (viewModel.farmOwner == "その他" && viewModel.customFarmOwner.isNotEmpty()) {
                                        viewModel.customFarmOwner
                                    } else {
                                        viewModel.farmOwner
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 区切り線
                    HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
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
                        
                        if (viewModel.isEditMode || !viewModel.hasExistingData) {
                            // 編集モード時は色付きボタン
                            Button(
                                onClick = { showRegionBottomSheet = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = getRegionColor(viewModel.defaultRegion),
                                    contentColor = Color.White
                                ),
                                shape = MaterialTheme.shapes.large,
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Text(
                                    text = viewModel.defaultRegion.ifEmpty { "地域" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            // 表示モード時は色付きSurface
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = getRegionColor(viewModel.defaultRegion).copy(alpha = 0.1f),
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
                                                color = getRegionColor(viewModel.defaultRegion),
                                                shape = CircleShape
                                            )
                                    )
                                    Text(
                                        text = viewModel.defaultRegion.ifEmpty { "未設定" },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (viewModel.defaultRegion.isEmpty()) 
                                            MaterialTheme.colorScheme.onSurfaceVariant 
                                        else 
                                            MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = "種子登録時の地域初期値として使用されます",
                            style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        // 県設定セクション
                        if (viewModel.isEditMode || !viewModel.hasExistingData) {
                            // EditMode: ボタンで県選択
                            Button(
                                onClick = { showPrefectureBottomSheet = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = MaterialTheme.shapes.large,
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Text(
                                    text = selectedPrefecture.ifEmpty { "県を選択" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            // DisplayMode: 表示のみ
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "県",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = selectedPrefecture.ifEmpty { "未設定" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selectedPrefecture.isEmpty()) 
                                        MaterialTheme.colorScheme.onSurfaceVariant 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    // 区切り線
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )
                    
                    // 通知タイミング設定セクション
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.NotificationsActive,
                                contentDescription = "通知設定",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "通知タイミング",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // 通知設定の表示/編集
                        if (viewModel.isEditMode || !viewModel.hasExistingData) {
                            // EditMode: ラジオボタンで編集可能
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 月一回
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.updateNotificationSettings("月一回") }
                                ) {
                                    RadioButton(
                                        selected = notificationFrequency == "月一回",
                                        onClick = { viewModel.updateNotificationSettings("月一回") },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "月一回",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                // 週１回
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.updateNotificationSettings("週１回") }
                                ) {
                                    RadioButton(
                                        selected = notificationFrequency == "週１回",
                                        onClick = { viewModel.updateNotificationSettings("週１回") },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "週１回",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                // なし
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.updateNotificationSettings("なし") }
                                ) {
                                    RadioButton(
                                        selected = notificationFrequency == "なし",
                                        onClick = { viewModel.updateNotificationSettings("なし") },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "なし",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                // 週１回の場合の曜日選択
                                if (notificationFrequency == "週１回") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "曜日を選択:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    
                                    val weekdays = listOf("月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "日曜日")
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        weekdays.forEach { weekday ->
                                            FilterChip(
                                                selected = selectedWeekday == weekday,
                                                onClick = { viewModel.updateNotificationSettings("週１回", weekday) },
                                                label = { Text(weekday) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // DisplayMode: 表示のみ
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 通知頻度の表示
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "通知頻度",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = notificationFrequency,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                // 週１回の場合の曜日表示
                                if (notificationFrequency == "週１回") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "通知曜日",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = selectedWeekday,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                        
                        Text(
                            text = "種まきのタイミングをお知らせします",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    // 区切り線
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )
                    
                    // 種情報URL設定セクション
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Link,
                                contentDescription = "種情報URL設定",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "種情報URL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // 種情報URL設定の表示/編集
                        if (viewModel.isEditMode || !viewModel.hasExistingData) {
                            // EditMode: ラジオボタンで編集可能
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // サカタのたね
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.updateSeedInfoUrlProvider("サカタのたね") }
                                ) {
                                    RadioButton(
                                        selected = seedInfoUrlProvider == "サカタのたね",
                                        onClick = { viewModel.updateSeedInfoUrlProvider("サカタのたね") },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "サカタのたね",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                // たねのタキイ
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.updateSeedInfoUrlProvider("たねのタキイ") }
                                ) {
                                    RadioButton(
                                        selected = seedInfoUrlProvider == "たねのタキイ",
                                        onClick = { viewModel.updateSeedInfoUrlProvider("たねのタキイ") },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "たねのタキイ",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                // その他
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.updateSeedInfoUrlProvider("その他") }
                                ) {
                                    RadioButton(
                                        selected = seedInfoUrlProvider == "その他",
                                        onClick = { viewModel.updateSeedInfoUrlProvider("その他") },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "その他",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                // その他選択時のURL入力欄
                                if (seedInfoUrlProvider == "その他") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = customSeedInfoUrl,
                                        onValueChange = { viewModel.updateCustomSeedInfoUrl(it) },
                                        label = { Text("URLを入力") },
                                        placeholder = { Text("https://example.com") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                        )
                                    )
                                }
                            }
                        } else {
                            // DisplayMode: 表示のみ
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 種情報URLプロバイダーの表示
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "種情報URL",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = seedInfoUrlProvider,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                // その他選択時のURL表示
                                if (seedInfoUrlProvider == "その他" && customSeedInfoUrl.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "カスタムURL",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = customSeedInfoUrl,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                        
                        Text(
                            text = "種情報の参照先URLを設定します",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        // 通知テストボタン（EditModeでのみ表示）
                        if (viewModel.isEditMode || !viewModel.hasExistingData) {
                            Button(
                                onClick = { 
                                    navController.navigate("notification_preview")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            ) {
                                Icon(
                                    Icons.Filled.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("通知テスト・プレビュー")
                            }
                        }
                    }
                }
            }
        }
    }
    
        // 地域選択ボトムシート
        if (showRegionBottomSheet) {
            RegionSelectionBottomSheet(
                selectedRegion = viewModel.defaultRegion,
                onRegionSelected = { region -> 
                    viewModel.updateDefaultRegion(region)
                    showRegionBottomSheet = false
                },
                onDismiss = { showRegionBottomSheet = false }
            )
        }
        
        // 県選択ボトムシート
        if (showPrefectureBottomSheet) {
            PrefectureSelectionBottomSheet(
                selectedPrefecture = selectedPrefecture,
                onPrefectureSelected = { prefecture -> 
                    viewModel.updateSelectedPrefecture(prefecture)
                    showPrefectureBottomSheet = false
                },
                onDismiss = { showPrefectureBottomSheet = false }
            )
        }
        
        // 保存アニメーション（MainScaffoldで管理されるため削除）
    }
}
