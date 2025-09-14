package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.ui.screens.CompanionPlantsSection
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "コンパニオンプランツ - 表示モード", heightDp = 600)
@Composable
fun CompanionPlantsSectionPreview_DisplayMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val companionViewModel = createPreviewCompanionPlantsViewModel(isEditMode = false, hasExistingData = true)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("コンパニオンプランツと効果")
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                CompanionPlantsSection(companionViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "コンパニオンプランツ - 編集モード", heightDp = 800)
@Composable
fun CompanionPlantsSectionPreview_EditMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val companionViewModel = createPreviewCompanionPlantsViewModel(isEditMode = true, hasExistingData = true)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("コンパニオンプランツと効果")
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                CompanionPlantsSection(companionViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "コンパニオンプランツ - 新規作成", heightDp = 800)
@Composable
fun CompanionPlantsSectionPreview_NewCreation() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val companionViewModel = createPreviewCompanionPlantsViewModel(isEditMode = true, hasExistingData = false)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("コンパニオンプランツと効果")
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                CompanionPlantsSection(companionViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "コンパニオンプランツ - 空の状態", heightDp = 600)
@Composable
fun CompanionPlantsSectionPreview_Empty() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val companionViewModel = createPreviewCompanionPlantsViewModel(isEditMode = true, hasExistingData = false, hasCompanionPlants = false)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("コンパニオンプランツと効果")
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                CompanionPlantsSection(companionViewModel)
            }
        }
    }
}
