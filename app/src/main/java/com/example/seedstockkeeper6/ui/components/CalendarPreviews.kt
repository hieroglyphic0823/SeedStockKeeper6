package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme

/**
 * カレンダーコンポーネントのプレビュー
 */

@Preview(showBackground = true)
@Composable
fun SeedCalendarGroupedPreview() {
    SeedStockKeeper6Theme {
        val sampleEntries = listOf(
            CalendarEntry(
                region = "暖地",
                sowing_start_date = "2025-08-15",
                sowing_end_date = "2025-09-31",
                harvest_start_date = "2025-10-01",
                harvest_end_date = "2025-12-31"
            )
        )
        
        // プレビュー用にSeedCalendarGroupedを直接使用
        SeedCalendarGrouped(
            entries = sampleEntries,
            packetExpirationYear = 2026, // 有効期限2026年10月
            packetExpirationMonth = 10,
            modifier = Modifier.fillMaxWidth(),
            heightDp = 114
        )
    }
}
