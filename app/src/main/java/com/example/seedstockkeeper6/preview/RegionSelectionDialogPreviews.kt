package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
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
                    text = "地域区分",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 地域選択ボタン
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("寒地", "寒冷地", "温暖地", "暖地").forEach { region ->
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (region == "寒地") 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = region,
                                color = if (region == "寒地") 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // 選択された地域の栽培期間表示
                val selectedRegionEntry = demoOcrResult.calendar?.find { it.region == "寒地" }
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
                                text = "寒地の栽培期間",
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
                    text = "地域区分",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 地域選択ボタン
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("寒地", "寒冷地", "温暖地", "暖地").forEach { region ->
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (region == "寒地") 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = region,
                                color = if (region == "寒地") 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // 選択された地域の栽培期間表示
                val selectedRegionEntry = demoOcrResult.calendar?.find { it.region == "寒地" }
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
                                text = "寒地の栽培期間",
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "OCR結果地域選択ダイアログ（編集モード）", heightDp = 1000)
@Composable
fun OcrRegionSelectionDialogPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        // OCR結果のデモデータ（複数地域）
        val demoOcrResult = com.example.seedstockkeeper6.model.SeedPacket(
            id = "demo-ocr-result",
            productName = "恋むすめ",
            variety = "ニンジン",
            family = "せり科",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "寒地",
                    sowing_start_date = "2025-06-01",
                    sowing_end_date = "2025-07-01",
                    harvest_start_date = "2025-08-01",
                    harvest_end_date = "2025-10-31"
                ),
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "寒冷地",
                    sowing_start_date = "2025-07-01",
                    sowing_end_date = "2025-08-01",
                    harvest_start_date = "2025-09-01",
                    harvest_end_date = "2025-11-30"
                ),
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "温暖地",
                    sowing_start_date = "2025-08-20",
                    sowing_end_date = "2025-09-20",
                    harvest_start_date = "2025-10-15",
                    harvest_end_date = "2026-01-15"
                ),
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "暖地",
                    sowing_start_date = "2025-08-15",
                    sowing_end_date = "2025-09-15",
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
                    text = "地域区分",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 切り抜きされたカレンダー画像の表示（実装に合わせて追加）
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // プレビュー用のカレンダー画像（実際の実装ではcroppedCalendarBitmapを使用）
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = "切り抜きされたカレンダー",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // 地域選択ドロップダウン
                var expanded by remember { mutableStateOf(false) }
                var selectedRegion by remember { mutableStateOf("寒地") }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedRegion,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("地域") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("寒地", "寒冷地", "温暖地", "暖地").forEach { region ->
                            DropdownMenuItem(
                                text = { Text(region) },
                                onClick = {
                                    selectedRegion = region
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // 選択された地域のOCR結果と編集項目を表示（実装に合わせて修正）
                if (selectedRegion.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val selectedRegionEntry = demoOcrResult.calendar?.find { it.region == selectedRegion }
                    val entryToShow = selectedRegionEntry ?: com.example.seedstockkeeper6.model.CalendarEntry(
                        region = selectedRegion,
                        sowing_start_date = "",
                        sowing_end_date = "",
                        harvest_start_date = "",
                        harvest_end_date = ""
                    )
                    
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
                            if (selectedRegionEntry != null) {
                                // OCR結果がある場合は表示
                                Text(
                                    "OCR結果: $selectedRegion",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                // 表示モード
                                com.example.seedstockkeeper6.ui.components.CalendarEntryDisplay(entry = selectedRegionEntry)
                                
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            
                            // 編集項目を表示
                            com.example.seedstockkeeper6.ui.components.CalendarEntryEditor(
                                entry = entryToShow,
                                onUpdate = { },
                                onSave = { },
                                onCancel = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "OCR結果地域選択ダイアログ（複数地域）", heightDp = 800)
@Composable
fun OcrRegionSelectionDialogMultipleRegionsPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        // OCR結果のデモデータ（複数地域）
        val demoOcrResult = com.example.seedstockkeeper6.model.SeedPacket(
            id = "demo-ocr-result",
            productName = "恋むすめ",
            variety = "ニンジン",
            family = "せり科",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "寒地",
                    sowing_start_date = "2025-06-01",
                    sowing_end_date = "2025-07-01",
                    harvest_start_date = "2025-08-01",
                    harvest_end_date = "2025-10-31"
                ),
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "寒冷地",
                    sowing_start_date = "2025-07-01",
                    sowing_end_date = "2025-08-01",
                    harvest_start_date = "2025-09-01",
                    harvest_end_date = "2025-11-30"
                ),
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "温暖地",
                    sowing_start_date = "2025-08-20",
                    sowing_end_date = "2025-09-20",
                    harvest_start_date = "2025-10-15",
                    harvest_end_date = "2026-01-15"
                ),
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "暖地",
                    sowing_start_date = "2025-08-15",
                    sowing_end_date = "2025-09-15",
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
                    text = "地域区分",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 地域選択ボタン（複数地域）
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(120.dp)
                ) {
                    items(listOf("寒地", "寒冷地", "温暖地", "暖地")) { region ->
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (region == "寒地") 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = region,
                                color = if (region == "寒地") 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // 選択された地域のOCR結果表示
                val selectedRegionEntry = demoOcrResult.calendar?.find { it.region == "寒地" }
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
                                text = "寒地の栽培期間",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // OCR結果の表示
                            com.example.seedstockkeeper6.ui.components.CalendarEntryDisplay(entry)
                        }
                    }
                }
            }
        }
    }
}
