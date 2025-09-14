package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme

// 地域ごとの色定義
private fun getRegionColor(region: String): Color {
    return when (region) {
        "寒地" -> Color(0xFF1A237E) // 紺
        "寒冷地" -> Color(0xFF1976D2) // 青
        "温暖地" -> Color(0xFFFF9800) // オレンジ
        "暖地" -> Color(0xFFE91E63) // ピンク
        else -> Color(0xFF757575) // グレー（デフォルト）
    }
}

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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("寒地", "寒冷地", "温暖地", "暖地").forEach { region ->
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = getRegionColor(region),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = region,
                                style = MaterialTheme.typography.bodyMedium
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
                                containerColor = getRegionColor(region),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = region,
                                style = MaterialTheme.typography.bodyMedium
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
@Preview(showBackground = true, name = "OCR結果地域選択ダイアログ（編集モード）", heightDp = 1500)
@Composable
fun OcrRegionSelectionDialogPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        var selectedRegion by remember { mutableStateOf("寒地") }
        
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
                    text = "地域確認",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // 説明コメント
                Text(
                    text = "AIで読み取った地域区分、播種期間、収穫期間を確認してください。間違っていた場合は修正してください。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

                // OCR結果表示カード（写真と地域選択の間）
                var selectedRegion by remember { mutableStateOf("寒地") }
                val selectedRegionEntry = demoOcrResult.calendar?.find { it.region == selectedRegion }
                selectedRegionEntry?.let { entry ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // アイコン + AI読み取り結果をtitleLargeで表示
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = R.drawable.star_opc),
                                    contentDescription = "AI読み取り結果",
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    "AI読み取り結果",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            
                            // 地域情報を表示（農園情報のDisplayModeと同じスタイル）
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = getRegionColor(selectedRegion).copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                color = getRegionColor(selectedRegion),
                                                shape = CircleShape
                                            )
                                    )
                                    Text(
                                        text = selectedRegion.ifEmpty { "未設定" },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (selectedRegion.isEmpty()) 
                                            MaterialTheme.colorScheme.onSurfaceVariant 
                                        else 
                                            MaterialTheme.colorScheme.onSurface,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                    )
                                }
                            }
                            
                            // 表示モード（OCR結果では年を表示しない）
                            com.example.seedstockkeeper6.ui.components.CalendarEntryDisplay(entry = entry, showYear = false)
                        }
                    }
                }



                // 編集項目を表示
                if (selectedRegion.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val entryToShow = demoOcrResult.calendar?.find { it.region == selectedRegion } ?: com.example.seedstockkeeper6.model.CalendarEntry(
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
                            // 地域設定セクション（農園情報画面と同じスタイル）
                            var showRegionBottomSheet by remember { mutableStateOf(false) }
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    androidx.compose.material3.Icon(
                                        androidx.compose.material.icons.Icons.Filled.Public,
                                        contentDescription = "地域設定",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "地域",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                // 地域選択ボタン（農園情報画面と同じスタイル）
                                Button(
                                    onClick = { showRegionBottomSheet = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = getRegionColor(selectedRegion),
                                        contentColor = Color.White
                                    ),
                                    shape = MaterialTheme.shapes.large,
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Text(
                                        text = selectedRegion.ifEmpty { "地域" },
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                    )
                                }
                            }
                            
                            // 地域選択ボトムシート（農園情報画面と同じ）
                            if (showRegionBottomSheet) {
                                androidx.compose.material3.ModalBottomSheet(
                                    onDismissRequest = { showRegionBottomSheet = false }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "地域を選択",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                        
                                        listOf("寒地", "寒冷地", "温暖地", "暖地").forEach { region ->
                                            Button(
                                                onClick = {
                                                    selectedRegion = region
                                                    showRegionBottomSheet = false
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = getRegionColor(region),
                                                    contentColor = Color.White
                                                ),
                                                shape = MaterialTheme.shapes.large,
                                                elevation = ButtonDefaults.buttonElevation(
                                                    defaultElevation = if (region == selectedRegion) 4.dp else 2.dp
                                                ),
                                                border = if (region == selectedRegion) {
                                                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                                                } else null
                                            ) {
                                                Text(
                                                    text = region,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = if (region == selectedRegion) 
                                                        androidx.compose.ui.text.font.FontWeight.Bold 
                                                    else 
                                                        androidx.compose.ui.text.font.FontWeight.Medium
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
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
                
                // 保存・キャンセルボタン（カードの外）
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Text("OK")
                    }
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Text("キャンセル")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "OCR結果地域選択ダイアログ（複数地域）", heightDp = 1200)
@Composable
fun OcrRegionSelectionDialogMultipleRegionsPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        var selectedRegion by remember { mutableStateOf("寒地") }
        
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
                val selectedRegionEntry = demoOcrResult.calendar?.find { it.region == selectedRegion }
                selectedRegionEntry?.let { entry ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // 地域設定セクション（農園情報画面と同じスタイル）
                            var showRegionBottomSheet by remember { mutableStateOf(false) }
                            var cardSelectedRegion by remember { mutableStateOf<String>(selectedRegion) }
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    androidx.compose.material3.Icon(
                                        androidx.compose.material.icons.Icons.Filled.Public,
                                        contentDescription = "地域設定",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "地域",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                
                                // 地域選択ボタン（農園情報画面と同じスタイル）
                                Button(
                                    onClick = { showRegionBottomSheet = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = getRegionColor(cardSelectedRegion),
                                        contentColor = Color.White
                                    ),
                                    shape = MaterialTheme.shapes.large,
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Text(
                                        text = cardSelectedRegion.ifEmpty { "地域" },
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                    )
                                }
                            }
                            
                            // 地域選択ボトムシート（農園情報画面と同じ）
                            if (showRegionBottomSheet) {
                                androidx.compose.material3.ModalBottomSheet(
                                    onDismissRequest = { showRegionBottomSheet = false }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "地域を選択",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                        
                                        listOf("寒地", "寒冷地", "温暖地", "暖地").forEach { region ->
                                            Button(
                                                onClick = {
                                                    cardSelectedRegion = region
                                                    showRegionBottomSheet = false
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = getRegionColor(region),
                                                    contentColor = Color.White
                                                ),
                                                shape = MaterialTheme.shapes.large,
                                                elevation = ButtonDefaults.buttonElevation(
                                                    defaultElevation = if (region == cardSelectedRegion) 4.dp else 2.dp
                                                ),
                                                border = if (region == cardSelectedRegion) {
                                                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                                                } else null
                                            ) {
                                                Text(
                                                    text = region,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = if (region == cardSelectedRegion) 
                                                        androidx.compose.ui.text.font.FontWeight.Bold 
                                                    else 
                                                        androidx.compose.ui.text.font.FontWeight.Medium
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "寒地の栽培期間",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // OCR結果の表示（年を表示しない）
                            com.example.seedstockkeeper6.ui.components.CalendarEntryDisplay(entry, showYear = false)
                        }
                    }
                }
            }
        }
    }
}
