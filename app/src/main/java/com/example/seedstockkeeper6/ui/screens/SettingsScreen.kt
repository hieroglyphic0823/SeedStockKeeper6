package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.seedstockkeeper6.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel,
    onSaveRequest: (() -> Unit)? = null,
    onEditRequest: (() -> Unit)? = null
) {
    var showRegionBottomSheet by remember { mutableStateOf(false) }
    
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
        if (viewModel.defaultRegion.isEmpty()) {
            // デフォルト値が設定されていない場合は手動で設定
            viewModel.updateDefaultRegion("温暖地")
        }
    }
    
    // FABからの保存要求を処理
    LaunchedEffect(onSaveRequest) {
        onSaveRequest?.let { request ->
            if (viewModel.farmName.isNotBlank() && viewModel.defaultRegion.isNotBlank()) {
                viewModel.saveSettings()
                viewModel.exitEditMode()
                navController.popBackStack()
            }
        }
    }
    
    // FABからの編集要求を処理
    LaunchedEffect(onEditRequest) {
        onEditRequest?.let { request ->
            viewModel.enterEditMode()
        }
    }
    
    // 農園名が変更された時に自動保存（より確実な方法）
    LaunchedEffect(viewModel.farmName) {
        if (viewModel.farmName.isNotBlank() && viewModel.defaultRegion.isNotBlank()) {
            // 少し待ってから保存（連続入力中の保存を防ぐ）
            delay(1000)
            android.util.Log.d("SettingsScreen", "自動保存開始: farmName='${viewModel.farmName}', defaultRegion='${viewModel.defaultRegion}'")
            viewModel.saveSettings()
        }
    }
    
    // 地域が変更された時に自動保存（より確実な方法）
    LaunchedEffect(viewModel.defaultRegion) {
        if (viewModel.farmName.isNotBlank() && viewModel.defaultRegion.isNotBlank()) {
            // 少し待ってから保存（連続選択中の保存を防ぐ）
            delay(1000)
            android.util.Log.d("SettingsScreen", "自動保存開始: farmName='${viewModel.farmName}', defaultRegion='${viewModel.defaultRegion}'")
            viewModel.saveSettings()
        }
    }
    
              Scaffold(
         snackbarHost = { SnackbarHost(snackbarHostState) }
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
                        
                        // 編集モードまたは新規登録時は入力フィールド、表示モード時は読み取り専用テキスト
                        if (viewModel.isEditMode || !viewModel.hasExistingData) {
                            if (viewModel.hasExistingData) {
                                // 編集モード時はTextField
                                TextField(
                                    value = viewModel.farmName,
                                    onValueChange = { newValue ->
                                        viewModel.updateFarmName(newValue)
                                    },
                                    label = { Text("農園名") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            } else {
                                // 新規登録時はOutlinedTextField
                                OutlinedTextField(
                                    value = viewModel.farmName,
                                    onValueChange = { newValue ->
                                        viewModel.updateFarmName(newValue)
                                    },
                                    label = { Text("農園名") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        } else {
                            // 表示モード時は読み取り専用テキスト
                            Text(
                                text = viewModel.farmName.ifEmpty { "未設定" },
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (viewModel.farmName.isEmpty()) 
                                    MaterialTheme.colorScheme.onSurfaceVariant 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                         
                         // デバッグ用：農園名の表示値をログ出力
                         LaunchedEffect(viewModel.farmName) {
                             android.util.Log.d("SettingsScreen", "農園名表示値: '${viewModel.farmName}'")
                         }
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
                        
                        // 編集モードまたは新規登録時は入力フィールド、表示モード時は読み取り専用テキスト
                        if (viewModel.isEditMode || !viewModel.hasExistingData) {
                            if (viewModel.hasExistingData) {
                                // 編集モード時はTextField
                                TextField(
                                    value = viewModel.defaultRegion,
                                    onValueChange = { },
                                    label = { Text("地域初期値") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showRegionBottomSheet = true },
                                    readOnly = true
                                )
                            } else {
                                // 新規登録時はOutlinedTextField
                                OutlinedTextField(
                                    value = viewModel.defaultRegion,
                                    onValueChange = { },
                                    label = { Text("地域初期値") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showRegionBottomSheet = true },
                                    readOnly = true
                                )
                            }
                        } else {
                            // 表示モード時は読み取り専用テキスト
                            Text(
                                text = viewModel.defaultRegion.ifEmpty { "未設定" },
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (viewModel.defaultRegion.isEmpty()) 
                                    MaterialTheme.colorScheme.onSurfaceVariant 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                         
                         // デバッグ用：地域の表示値をログ出力
                         LaunchedEffect(viewModel.defaultRegion) {
                             android.util.Log.d("SettingsScreen", "地域表示値: '${viewModel.defaultRegion}'")
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
    
    // 地域選択ボトムシート
    if (showRegionBottomSheet) {
        RegionSelectionBottomSheet(
            selectedRegion = viewModel.defaultRegion,
            onRegionSelected = { 
                viewModel.updateDefaultRegion(it)
                showRegionBottomSheet = false
            },
            onDismiss = { showRegionBottomSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionSelectionBottomSheet(
    selectedRegion: String,
    onRegionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val regions = listOf(
        "寒地", "寒冷地", "温暖地", "暖地"
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "地域を選択",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            regions.forEach { region ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = region == selectedRegion,
                        onClick = { onRegionSelected(region) }
                    )
                    Text(
                        text = region,
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
