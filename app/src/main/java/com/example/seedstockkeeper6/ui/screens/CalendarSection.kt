package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.ui.components.SeedCalendarGrouped
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

@Composable
fun CalendarSection(viewModel: SeedInputViewModel) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "栽培カレンダー",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // ---- 地域別 まきどき / 収穫カレンダー ----
            SeedCalendarGrouped(
                entries = viewModel.packet.calendar ?: emptyList(),
                packetExpirationYear = viewModel.packet.expirationYear,    // ★ 追加
                packetExpirationMonth = viewModel.packet.expirationMonth,  // ★ 追加
                modifier = Modifier.fillMaxWidth(),
                heightDp = 140
            )

            CalendarDetailSection(viewModel)

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
}
