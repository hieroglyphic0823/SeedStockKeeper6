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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
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
    // showSaveAnimationはMainScaffoldで管理されるため削除
    
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
        
        // 保存アニメーション（MainScaffoldで管理されるため削除）
    }
}
