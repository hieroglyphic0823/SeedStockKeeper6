package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.utils.DateConversionUtils

@Composable
fun CalendarEntryDisplayNew(entry: CalendarEntry) {
    Column {
        // 播種期間（日付ベースの表示）
        if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
            val startYear = DateConversionUtils.getYearFromDate(entry.sowing_start_date)
            val startMonth = DateConversionUtils.getMonthFromDate(entry.sowing_start_date)
            val startStage = DateConversionUtils.convertDateToStage(entry.sowing_start_date)
            val endYear = DateConversionUtils.getYearFromDate(entry.sowing_end_date)
            val endMonth = DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
            val endStage = DateConversionUtils.convertDateToStage(entry.sowing_end_date)
            
            Text(
                "${if (startYear > 0) "${startYear}年" else ""}${startMonth}月${if (startStage.isNotEmpty()) "(${startStage})" else ""} ～ ${if (endYear > 0) "${endYear}年" else ""}${endMonth}月${if (endStage.isNotEmpty()) "(${endStage})" else ""}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // 収穫期間（日付ベースの表示）
        if (entry.harvest_start_date.isNotEmpty() && entry.harvest_end_date.isNotEmpty()) {
            val startYear = DateConversionUtils.getYearFromDate(entry.harvest_start_date)
            val startMonth = DateConversionUtils.getMonthFromDate(entry.harvest_start_date)
            val startStage = DateConversionUtils.convertDateToStage(entry.harvest_start_date)
            val endYear = DateConversionUtils.getYearFromDate(entry.harvest_end_date)
            val endMonth = DateConversionUtils.getMonthFromDate(entry.harvest_end_date)
            val endStage = DateConversionUtils.convertDateToStage(entry.harvest_end_date)
            
            Text(
                "${if (startYear > 0) "${startYear}年" else ""}${startMonth}月${if (startStage.isNotEmpty()) "(${startStage})" else ""} ～ ${if (endYear > 0) "${endYear}年" else ""}${endMonth}月${if (endStage.isNotEmpty()) "(${endStage})" else ""}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
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

@Preview(showBackground = true)
@Composable
fun CalendarEntryDisplayNewPreview() {
    SeedStockKeeper6Theme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 播種期間と収穫期間の両方がある場合
            CalendarEntryDisplayNew(
                entry = CalendarEntry(
                    region = "温暖地",
                    sowing_start_date = "2025-03-01",
                    sowing_end_date = "2025-05-31",
                    harvest_start_date = "2025-07-01",
                    harvest_end_date = "2025-09-30"
                )
            )
            
            // 播種期間のみの場合
            CalendarEntryDisplayNew(
                entry = CalendarEntry(
                    region = "寒冷地",
                    sowing_start_date = "2025-04-11",
                    sowing_end_date = "2025-06-20"
                )
            )
            
            // 収穫期間のみの場合
            CalendarEntryDisplayNew(
                entry = CalendarEntry(
                    region = "暖地",
                    harvest_start_date = "2025-08-01",
                    harvest_end_date = "2025-10-31"
                )
            )
            
            // データがない場合
            CalendarEntryDisplayNew(
                entry = CalendarEntry(
                    region = "寒地"
                )
            )
        }
    }
}
