// SeedInputScreen.kt

package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.seedstockkeeper6.FullScreenSaveAnimation
import com.example.seedstockkeeper6.ui.components.AIDiffDialog
import com.example.seedstockkeeper6.ui.components.RegionSelectionDialog
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

// 分離したコンポーネントのインポート
import com.example.seedstockkeeper6.ui.screens.*



@Composable
fun SeedInputScreen(
    navController: NavController,
    viewModel: SeedInputViewModel,
    settingsViewModel: com.example.seedstockkeeper6.viewmodel.SettingsViewModel? = null,
    onSaveRequest: () -> Unit = {} // MainScaffoldからの保存リクエストコールバック
) {
    val scroll = rememberScrollState()
    val context = LocalContext.current
    
    // 農園情報の地域を設定
    LaunchedEffect(settingsViewModel?.defaultRegion) {
        settingsViewModel?.defaultRegion?.let { region ->
            viewModel.farmDefaultRegion = region
        }
    }

    Scaffold(
        floatingActionButton = {
            // AIで読み取り処理中、地域選択ダイアログ表示中はFABを非表示
            if ((viewModel.isEditMode || !viewModel.hasExistingData) && 
                !viewModel.isLoading && 
                !viewModel.showRegionSelectionDialog) {
                FloatingActionButton(
                    onClick = {
                        onSaveRequest() // MainScaffoldの保存アニメーションを表示
                        viewModel.saveSeedData(context) { result ->
                            if (result.isSuccess) {
                                viewModel.exitEditMode()
                                viewModel.markAsExistingData() // DisplayModeにする
                            }
                        }
                    },
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
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(scroll)
                    .padding(
                        top = 0.dp,  // 上パディングのみ0に
                        start = paddingValues.calculateLeftPadding(LocalLayoutDirection.current),
                        end = paddingValues.calculateRightPadding(LocalLayoutDirection.current),
                        bottom = paddingValues.calculateBottomPadding()
                    )
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
            // 画像管理セクション
            ImageManagementSection(viewModel)
            
            // 区切り線
            HorizontalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // 基本情報セクション
            BasicInfoSection(viewModel)
            
            // 区切り線
            HorizontalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // カレンダーセクション
            CalendarSection(viewModel)
            
            // 区切り線
            HorizontalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // メモ・コンパニオンプランツカード（DisplayModeのみ）
            NotesCardSection(viewModel)
            
            // 栽培情報セクション
            CultivationInfoSection(viewModel)
            
            // 区切り線
            HorizontalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            }
            
            // ダイアログ類
            if (viewModel.showCropConfirmDialog) {
                CropConfirmDialog(viewModel = viewModel)
            }
            
            // AI処理中のみSukesanGifAnimationを表示（保存処理中は表示しない）
            if (viewModel.isLoading && !viewModel.isSaving) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .zIndex(999f),
                    contentAlignment = Alignment.Center
                ) {
                    SukesanGifAnimation()
                }
            }
        }
    }

    // AI差分ダイアログ
    AIDiffDialog(
        showDialog = viewModel.showAIDiffDialog,
        diffList = viewModel.aiDiffList,
        onConfirm = { viewModel.applyAIDiffResult() },
        onDismiss = { viewModel.onAIDiffDialogDismiss() }
    )
    
    // 地域選択ダイアログ
    android.util.Log.d("SeedInputScreen", "RegionSelectionDialog呼び出し: showDialog=${viewModel.showRegionSelectionDialog}, regions=${viewModel.detectedRegions}")
    RegionSelectionDialog(
        showDialog = viewModel.showRegionSelectionDialog,
        regionList = viewModel.detectedRegions,
        ocrResult = viewModel.ocrResult,
        croppedCalendarBitmap = viewModel.croppedCalendarBitmap,
        editingCalendarEntry = viewModel.editingCalendarEntry,
        defaultRegion = viewModel.farmDefaultRegion, // 農園情報の地域を初期値として使用
        onRegionSelected = { viewModel.onRegionSelected(it) },
        onStartEditing = { viewModel.startEditingCalendarEntry(it) },
        onUpdateEditing = { viewModel.updateEditingCalendarEntry(it) },
        onSaveEditing = { viewModel.saveEditingCalendarEntry() },
        onCancelEditing = { viewModel.cancelEditingCalendarEntry() },
        onDismiss = { viewModel.onRegionSelectionDismiss() }
    )
    
    // 画像拡大表示ダイアログ
    ImageDialog(viewModel)
    
    // 保存アニメーション（MainScaffoldで管理されるため削除）
}
