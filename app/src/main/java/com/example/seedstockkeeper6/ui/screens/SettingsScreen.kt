package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.seedstockkeeper6.viewmodel.SettingsViewModel
import com.example.seedstockkeeper6.ui.components.PrefectureSelectionBottomSheet
import com.example.seedstockkeeper6.ui.components.RegionSelectionBottomSheet
import com.example.seedstockkeeper6.ui.components.FarmNameSection
import com.example.seedstockkeeper6.ui.components.FarmLocationSection
import com.example.seedstockkeeper6.ui.components.FarmOwnerSection
import com.example.seedstockkeeper6.ui.components.RegionSettingsSection
import com.example.seedstockkeeper6.ui.components.NotificationSettingsSection
import com.example.seedstockkeeper6.ui.components.SeedInfoUrlSettingsSection
import com.example.seedstockkeeper6.model.SettingsConstants
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    var showRegionBottomSheet by remember { mutableStateOf(false) }
    var showPrefectureBottomSheet by remember { mutableStateOf(false) }
    
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
                            contentDescription = SettingsConstants.SAVE_BUTTON_DESCRIPTION
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // 農園設定カード
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                        FarmNameSection(
                            farmName = viewModel.farmName,
                            isEditMode = viewModel.isEditMode,
                            hasExistingData = viewModel.hasExistingData,
                            onFarmNameChange = { viewModel.updateFarmName(it) }
                        )

                        // 区切り線
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )

                        // 農園位置設定セクション
                        FarmLocationSection(
                            farmLatitude = viewModel.farmLatitude,
                            farmLongitude = viewModel.farmLongitude,
                            farmAddress = viewModel.farmAddress,
                            isEditMode = viewModel.isEditMode,
                            hasExistingData = viewModel.hasExistingData,
                            onNavigateToMap = { navController.navigate("map_selection") }
                        )

                        // 区切り線
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )

                        // 農園主設定セクション
                        FarmOwnerSection(
                            farmOwner = viewModel.farmOwner,
                            customFarmOwner = viewModel.customFarmOwner,
                            isEditMode = viewModel.isEditMode,
                            hasExistingData = viewModel.hasExistingData,
                            onFarmOwnerChange = { viewModel.updateFarmOwner(it) },
                            onCustomFarmOwnerChange = { viewModel.updateCustomFarmOwner(it) }
                        )

                        // 区切り線
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )

                        // 地域設定セクション
                        RegionSettingsSection(
                            defaultRegion = viewModel.defaultRegion,
                            selectedPrefecture = selectedPrefecture,
                            isEditMode = viewModel.isEditMode,
                            hasExistingData = viewModel.hasExistingData,
                            onShowRegionBottomSheet = { showRegionBottomSheet = true },
                            onShowPrefectureBottomSheet = { showPrefectureBottomSheet = true }
                        )
                        
                        // 区切り線
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                        
                        // 通知タイミング設定セクション
                        NotificationSettingsSection(
                            notificationFrequency = notificationFrequency,
                            selectedWeekday = selectedWeekday,
                            isEditMode = viewModel.isEditMode,
                            hasExistingData = viewModel.hasExistingData,
                            onNotificationSettingsChange = { frequency, weekday ->
                                viewModel.updateNotificationSettings(frequency, weekday)
                            }
                        )
                        
                        // 区切り線
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                        
                        // 種情報URL設定セクション
                        SeedInfoUrlSettingsSection(
                            seedInfoUrlProvider = seedInfoUrlProvider,
                            customSeedInfoUrl = customSeedInfoUrl,
                            isEditMode = viewModel.isEditMode,
                            hasExistingData = viewModel.hasExistingData,
                            onSeedInfoUrlProviderChange = { viewModel.updateSeedInfoUrlProvider(it) },
                            onCustomSeedInfoUrlChange = { viewModel.updateCustomSeedInfoUrl(it) },
                            onNavigateToNotificationPreview = { navController.navigate("notification_preview") }
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
}
