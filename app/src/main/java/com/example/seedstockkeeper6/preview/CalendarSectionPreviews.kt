package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.ui.screens.CalendarSection
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "栽培カレンダー - 表示モード", heightDp = 800)
@Composable
fun CalendarSectionPreview_DisplayMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val calendarViewModel = createPreviewCalendarViewModel(isEditMode = false, hasExistingData = true)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("栽培カレンダー")
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
                CalendarSection(calendarViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "栽培カレンダー - 編集モード", heightDp = 800)
@Composable
fun CalendarSectionPreview_EditMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val calendarViewModel = createPreviewCalendarViewModel(isEditMode = true, hasExistingData = true)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("栽培カレンダー")
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
                CalendarSection(calendarViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "栽培カレンダー - 新規作成", heightDp = 800)
@Composable
fun CalendarSectionPreview_NewCreation() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val calendarViewModel = createPreviewCalendarViewModel(isEditMode = true, hasExistingData = false)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("栽培カレンダー")
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
                CalendarSection(calendarViewModel)
            }
        }
    }
}
