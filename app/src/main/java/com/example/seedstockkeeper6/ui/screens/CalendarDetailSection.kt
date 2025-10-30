package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.ui.components.CalendarEntryDisplay
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.utils.DateConversionUtils
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

@Composable
fun CalendarDetailSection(
    viewModel: SeedInputViewModel,
    index: Int
) {
    val entry = viewModel.packet.calendar?.getOrNull(index) ?: return
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 地域名
            Text(
                text = entry.region.ifEmpty { "地域未設定" },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 播種期間
            if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.grain),
                        contentDescription = "播種期間",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "播種期間: ${formatDateRange(entry.sowing_start_date, entry.sowing_end_date)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 収穫期間
            if (entry.harvest_start_date.isNotEmpty() && entry.harvest_end_date.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.grain),
                        contentDescription = "収穫期間",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "収穫期間: ${formatDateRange(entry.harvest_start_date, entry.harvest_end_date)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // データがない場合
            if (entry.sowing_start_date.isEmpty() && entry.sowing_end_date.isEmpty() &&
                entry.harvest_start_date.isEmpty() && entry.harvest_end_date.isEmpty()) {
                Text(
                    "播種・収穫期間の情報がありません",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// 日付範囲をフォーマットする関数
@androidx.compose.runtime.Composable
private fun formatDateRange(startDate: String, endDate: String): String {
    if (startDate.isEmpty() || endDate.isEmpty()) return ""
    return androidx.compose.runtime.remember(startDate, endDate) {
        try {
            val start = java.time.LocalDate.parse(startDate, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            val end = java.time.LocalDate.parse(endDate, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            val startStage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDayToStage(start.dayOfMonth)
            val endStage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDayToStage(end.dayOfMonth)
            val startText = "${start.year}年${start.monthValue}月(${startStage})"
            val endText = "${end.year}年${end.monthValue}月(${endStage})"
            "$startText ～ $endText"
        } catch (e: java.time.format.DateTimeParseException) {
            android.util.Log.e("formatDateRange", "日付のパースに失敗しました: $startDate, $endDate", e)
            "$startDate ～ $endDate"
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarDetailSectionPreview() {
    SeedStockKeeper6Theme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 播種期間と収穫期間の両方がある場合
            CalendarDetailSectionPreview(
                entry = CalendarEntry(
                    region = "温暖地",
                    sowing_start_date = "2025-03-01",
                    sowing_end_date = "2025-05-31",
                    harvest_start_date = "2025-07-01",
                    harvest_end_date = "2025-09-30"
                )
            )
            
            // 播種期間のみの場合
            CalendarDetailSectionPreview(
                entry = CalendarEntry(
                    region = "寒冷地",
                    sowing_start_date = "2025-04-11",
                    sowing_end_date = "2025-06-20"
                )
            )
            
            // データがない場合
            CalendarDetailSectionPreview(
                entry = CalendarEntry(
                    region = "寒地"
                )
            )
        }
    }
}

@Composable
fun CalendarDetailSectionPreview(entry: CalendarEntry) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 地域名
            Text(
                text = entry.region.ifEmpty { "地域未設定" },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 播種期間
            if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.grain),
                        contentDescription = "播種期間",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "播種期間: ${formatDateRange(entry.sowing_start_date, entry.sowing_end_date)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 収穫期間
            if (entry.harvest_start_date.isNotEmpty() && entry.harvest_end_date.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.grain),
                        contentDescription = "収穫期間",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "収穫期間: ${formatDateRange(entry.harvest_start_date, entry.harvest_end_date)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // データがない場合
            if (entry.sowing_start_date.isEmpty() && entry.sowing_end_date.isEmpty() &&
                entry.harvest_start_date.isEmpty() && entry.harvest_end_date.isEmpty()) {
                Text(
                    "播種・収穫期間の情報がありません",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
