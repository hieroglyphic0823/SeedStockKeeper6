package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.ui.components.SeedCalendarGrouped
import com.example.seedstockkeeper6.ui.components.ExpirationSelectionBottomSheet
import com.example.seedstockkeeper6.ui.components.PeriodSelectionBottomSheet
import com.example.seedstockkeeper6.ui.screens.CalendarSection
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.utils.DateConversionUtils
import java.time.LocalDate

// プレビュー用のカレンダーセクション（2025年9月を現在の日付として設定）
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewCalendarSection(viewModel: SeedInputViewModel) {
    // 現在のカレンダーエントリを取得
    val currentEntry = viewModel.packet.calendar?.firstOrNull() ?: CalendarEntry()
    
    // 地域が選択されていない場合でも、空のカレンダーエントリを作成して表示
    val calendarEntries = if (viewModel.selectedRegion.isEmpty() && (viewModel.packet.calendar?.isEmpty() != false)) {
        // 地域が選択されていない場合は空のカレンダーエントリを作成
        listOf(
            CalendarEntry(
                region = "",
                sowing_start_date = "",
                sowing_end_date = "",
                harvest_start_date = "",
                harvest_end_date = ""
            )
        )
    } else {
        viewModel.packet.calendar ?: emptyList()
    }
    
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
                painter = painterResource(id = R.drawable.calendar),
                contentDescription = "種暦",
                modifier = Modifier.size(24.dp)
            )
            Text(
                "種暦",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        // カレンダー表示（2025年9月を現在の日付として設定）
        if (calendarEntries.isNotEmpty()) {
            SeedCalendarGrouped(
                entries = calendarEntries,
                packetExpirationYear = viewModel.packet.expirationYear,
                packetExpirationMonth = viewModel.packet.expirationMonth,
                modifier = Modifier.fillMaxWidth(),
                heightDp = 114,
                previewDate = LocalDate.of(2025, 9, 1) // 2025年9月を現在の日付として設定
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種暦 - 表示モード（2025年9月）", heightDp = 800)
@Composable
fun CalendarSectionPreview_DisplayMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val calendarViewModel = createPreviewCalendarViewModel(isEditMode = false, hasExistingData = true)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("種暦 - 2025年9月表示")
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
                // プレビュー用のカレンダーセクション（2025年9月を現在の日付として設定）
                PreviewCalendarSection(calendarViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種暦 - 編集モード（修正版）", heightDp = 800)
@Composable
fun CalendarSectionPreview_EditMode() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val calendarViewModel = createPreviewCalendarViewModel(isEditMode = true, hasExistingData = true)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("種暦 - 12ヶ月表示修正版")
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
@Preview(showBackground = true, name = "種暦 - 新規作成（修正版）", heightDp = 800)
@Composable
fun CalendarSectionPreview_NewCreation() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val calendarViewModel = createPreviewCalendarViewModel(isEditMode = true, hasExistingData = false)
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text("種暦 - 12ヶ月表示修正版")
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
