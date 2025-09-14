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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "農園情報画面 - 表示モード", heightDp = 1800)
@Composable
fun SettingsScreenPreview_DisplayMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = com.example.seedstockkeeper6.preview.createMockFirebaseUser()
        val settingsViewModel = com.example.seedstockkeeper6.preview.createPreviewSettingsViewModel(isEditMode = false, hasExistingData = true)
        
        // 農園情報セクション
        com.example.seedstockkeeper6.ui.screens.SettingsScreen(navController, settingsViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "農園情報画面 - 編集モード", heightDp = 1800)
@Composable
fun SettingsScreenPreview_EditMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val mockUser = com.example.seedstockkeeper6.preview.createMockFirebaseUser()
        val settingsViewModel = com.example.seedstockkeeper6.preview.createPreviewSettingsViewModel(isEditMode = true, hasExistingData = true)
        
        // 農園情報セクション
        com.example.seedstockkeeper6.ui.screens.SettingsScreen(navController, settingsViewModel)
    }
}
