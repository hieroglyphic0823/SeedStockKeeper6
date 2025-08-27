// SeedInputScreen.kt

package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.seedstockkeeper6.ui.components.AIDiffDialog
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

// 分離したコンポーネントのインポート
import com.example.seedstockkeeper6.ui.screens.*



@Composable
fun SeedInputScreen(
    navController: NavController,
    viewModel: SeedInputViewModel
) {
    val scroll = rememberScrollState()

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(scroll)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // 画像管理セクション
            ImageManagementSection(viewModel)
            
            // 基本情報セクション
            BasicInfoSection(viewModel)
            
            // カレンダーセクション
            CalendarSection(viewModel)
            
            // 栽培情報セクション
            CultivationInfoSection(viewModel)
            
            // コンパニオンプランツセクション
            CompanionPlantsSection(viewModel)
        }
        
        // ダイアログ類
        if (viewModel.showCropConfirmDialog) {
            CropConfirmDialog(viewModel = viewModel)
        }
        
        if (viewModel.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(999f),
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation()
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
    
    // 画像拡大表示ダイアログ
    ImageDialog(viewModel)
}
