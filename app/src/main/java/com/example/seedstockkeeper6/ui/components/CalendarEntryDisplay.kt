package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CalendarEntryDisplay(entry: com.example.seedstockkeeper6.model.CalendarEntry) {
    Column {
        // 播種期間
        if (entry.sowing_start > 0 || entry.sowing_end > 0) {
            Text(
                "播種期間: ${if (entry.sowing_start_year > 0) "${entry.sowing_start_year}年" else ""}${entry.sowing_start}月${if (entry.sowing_start_stage.isNotEmpty()) "(${entry.sowing_start_stage})" else ""} ～ ${if (entry.sowing_end_year > 0) "${entry.sowing_end_year}年" else ""}${entry.sowing_end}月${if (entry.sowing_end_stage.isNotEmpty()) "(${entry.sowing_end_stage})" else ""}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
            )
        }
        
        // 収穫期間
        if (entry.harvest_start > 0 || entry.harvest_end > 0) {
            Text(
                "収穫期間: ${if (entry.harvest_start_year > 0) "${entry.harvest_start_year}年" else ""}${entry.harvest_start}月${if (entry.harvest_start_stage.isNotEmpty()) "(${entry.harvest_start_stage})" else ""} ～ ${if (entry.harvest_end_year > 0) "${entry.harvest_end_year}年" else ""}${entry.harvest_end}月${if (entry.harvest_end_stage.isNotEmpty()) "(${entry.harvest_end_stage})" else ""}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
            )
        }
        
        // データがない場合
        if ((entry.sowing_start == 0 && entry.sowing_end == 0) &&
            (entry.harvest_start == 0 && entry.harvest_end == 0)) {
            Text(
                "播種・収穫期間の情報がありません",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
