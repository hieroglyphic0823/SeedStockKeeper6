package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "地域選択ダイアログ（実際のDialog）", heightDp = 800)
@Composable
fun RegionSelectionDialogPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        // プレビュー用のデモデータ（extractRegionsFromOcrResultで検出される地域）
        val demoOcrResult = com.example.seedstockkeeper6.model.SeedPacket(
            id = "demo-ocr-result",
            productName = "恋むすめ",
            variety = "ニンジン",
            family = "せり科",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "暖地",
                    sowing_start_date = "2025-08-15",
                    sowing_end_date = "2025-09-31",
                    harvest_start_date = "2025-10-01",
                    harvest_end_date = "2025-12-31"
                ),
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "温暖地",
                    sowing_start_date = "2025-08-15",
                    sowing_end_date = "2025-09-31",
                    harvest_start_date = "2025-10-01",
                    harvest_end_date = "2025-12-31"
                )
            )
        )
        
        // Dialogの内容を直接表示（プレビュー用）
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "地域区分を選択してください",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 地域選択ボタン
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("暖地", "温暖地").forEach { region ->
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (region == "暖地") 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = region,
                                color = if (region == "暖地") 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // 選択された地域の栽培期間表示
                val selectedRegionEntry = demoOcrResult.calendar?.find { it.region == "暖地" }
                selectedRegionEntry?.let { entry ->
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
                            Text(
                                text = "暖地の栽培期間",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // 播種期間と収穫期間の表示
                            com.example.seedstockkeeper6.ui.components.CalendarEntryDisplay(entry)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "地域選択ダイアログ（内容表示）", heightDp = 800)
@Composable
fun RegionSelectionDialogContentPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        // プレビュー用のデモデータ
        val demoOcrResult = com.example.seedstockkeeper6.model.SeedPacket(
            id = "demo-ocr-result",
            productName = "恋むすめ",
            variety = "ニンジン",
            family = "せり科",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "暖地",
                    sowing_start_date = "2025-08-15",
                    sowing_end_date = "2025-09-31",
                    harvest_start_date = "2025-10-01",
                    harvest_end_date = "2025-12-31"
                ),
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "温暖地",
                    sowing_start_date = "2025-08-15",
                    sowing_end_date = "2025-09-31",
                    harvest_start_date = "2025-10-01",
                    harvest_end_date = "2025-12-31"
                )
            )
        )
        
        // Dialogの内容を直接表示（プレビュー用）
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "地域区分を選択してください",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 地域選択ボタン
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("暖地", "温暖地").forEach { region ->
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (region == "暖地") 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = region,
                                color = if (region == "暖地") 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // 選択された地域の栽培期間表示
                val selectedRegionEntry = demoOcrResult.calendar?.find { it.region == "暖地" }
                selectedRegionEntry?.let { entry ->
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
                            Text(
                                text = "暖地の栽培期間",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // 播種期間と収穫期間の表示（色変更が反映される）
                            com.example.seedstockkeeper6.ui.components.CalendarEntryDisplay(entry)
                        }
                    }
                }
            }
        }
    }
}
