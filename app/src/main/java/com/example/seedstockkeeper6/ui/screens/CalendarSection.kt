package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.ui.components.SeedCalendarGrouped
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.utils.DateConversionUtils

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
                heightDp = 100
            )
            
            // 有効期限表示
            if (viewModel.packet.expirationYear > 0 && viewModel.packet.expirationMonth > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    // 有効期限アイコン（背景付き）
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = "有効期限",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "有効期限: ${viewModel.packet.expirationYear}年${viewModel.packet.expirationMonth}月",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 播種期間と収穫期間の表示
            val calendarEntry = calendarEntries.firstOrNull()
            if (calendarEntry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            
                // 播種期間
                val sowingPeriod = formatDateRange(calendarEntry.sowing_start_date, calendarEntry.sowing_end_date)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    // 播種期間アイコン（背景付き）
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.grain),
                            contentDescription = "播種期間",
                            modifier = Modifier.size(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        text = "播種期間: $sowingPeriod",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 収穫期間
                val harvestPeriod = formatDateRange(calendarEntry.harvest_start_date, calendarEntry.harvest_end_date)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    // 収穫期間アイコン（背景付き）
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.harvest),
                            contentDescription = "収穫期間",
                            modifier = Modifier.size(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        text = "収穫期間: $harvestPeriod",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
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

// 日付範囲を旬形式でフォーマットするヘルパー関数
private fun formatDateRange(startDate: String, endDate: String): String {
    if (startDate.isEmpty() && endDate.isEmpty()) {
        return "未設定"
    }
    
    val startFormatted = if (startDate.isNotEmpty()) {
        val year = DateConversionUtils.getYearFromDate(startDate)
        val month = DateConversionUtils.getMonthFromDate(startDate)
        val stage = DateConversionUtils.convertDateToStage(startDate)
        "${year}年${month}月(${stage})"
    } else {
        "未設定"
    }
    
    val endFormatted = if (endDate.isNotEmpty()) {
        val year = DateConversionUtils.getYearFromDate(endDate)
        val month = DateConversionUtils.getMonthFromDate(endDate)
        val stage = DateConversionUtils.convertDateToStage(endDate)
        "${year}年${month}月(${stage})"
    } else {
        "未設定"
    }
    
    return if (startDate.isEmpty() || endDate.isEmpty()) {
        if (startDate.isEmpty()) endFormatted else startFormatted
    } else {
        "$startFormatted ～ $endFormatted"
    }
}
