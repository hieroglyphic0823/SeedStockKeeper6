package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.ui.components.SeedCalendarGrouped
import com.example.seedstockkeeper6.ui.components.CalendarEntryEditor
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.model.CalendarEntry

@Composable
fun CalendarSection(viewModel: SeedInputViewModel) {
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
            Icon(
                Icons.Filled.CalendarMonth,
                contentDescription = "栽培カレンダー",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Text(
                "栽培カレンダー",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        // 地域設定（栽培カレンダーのタイトルとカレンダー図の間）
        val currentRegion = viewModel.packet.calendar?.firstOrNull()?.region ?: ""
        
        if (viewModel.isCalendarEditMode) {
            // 編集モード
            CalendarEditMode(viewModel)
        } else {
            // 通常の表示モード
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
            
            // カレンダー表示
            if (calendarEntries.isNotEmpty()) {
            SeedCalendarGrouped(
                entries = calendarEntries,
                    packetExpirationYear = viewModel.packet.expirationYear,
                    packetExpirationMonth = viewModel.packet.expirationMonth,
                modifier = Modifier.fillMaxWidth(),
                    heightDp = 140
            )
            }

            // 編集ボタン
            if (currentRegion.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.enterCalendarEditMode() },
        modifier = Modifier.fillMaxWidth()
    ) {
                    Text("栽培期間を編集")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarEditMode(viewModel: SeedInputViewModel) {
    val currentEntry = viewModel.packet.calendar.firstOrNull() ?: return
    
    CalendarEntryEditor(
        entry = currentEntry,
        onUpdate = { updatedEntry ->
            viewModel.updateCalendarEntry(
                index = 0,
                region = updatedEntry.region,
                sowing_start_date = updatedEntry.sowing_start_date,
                sowing_end_date = updatedEntry.sowing_end_date,
                harvest_start_date = updatedEntry.harvest_start_date,
                harvest_end_date = updatedEntry.harvest_end_date
            )
        },
        onSave = {
            viewModel.exitCalendarEditMode()
        },
        onCancel = {
            viewModel.exitCalendarEditMode()
        }
    )
}

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
