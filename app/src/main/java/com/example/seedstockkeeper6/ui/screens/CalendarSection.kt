package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.seedstockkeeper6.ui.components.SeedCalendarGrouped
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

@Composable
fun CalendarSection(viewModel: SeedInputViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "栽培カレンダー",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 地域が選択されていない場合でも、空のカレンダーエントリを作成して表示
        val calendarEntries = if (viewModel.selectedRegion.isEmpty() && (viewModel.packet.calendar?.isEmpty() != false)) {
            // 地域が選択されていない場合は空のカレンダーエントリを作成
            listOf(
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "",
                    sowing_start = 0,
                    sowing_start_stage = "",
                    sowing_end = 0,
                    sowing_end_stage = "",
                    harvest_start = 0,
                    harvest_start_stage = "",
                    harvest_end = 0,
                    harvest_end_stage = ""
                )
            )
        } else {
            viewModel.packet.calendar ?: emptyList()
        }
        
        // ---- 地域別 まきどき / 収穫カレンダー ----
        SeedCalendarGrouped(
            entries = calendarEntries,
            packetExpirationYear = viewModel.packet.expirationYear,    // ★ 追加
            packetExpirationMonth = viewModel.packet.expirationMonth,  // ★ 追加
            modifier = Modifier.fillMaxWidth(),
            heightDp = 140
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalendarDetailSection(viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.addCalendarEntry() }, 
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("地域を追加")
        }
    }
}
