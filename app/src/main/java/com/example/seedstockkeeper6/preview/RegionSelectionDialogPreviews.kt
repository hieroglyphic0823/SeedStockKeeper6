package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.components.RegionSelectionDialog
import com.example.seedstockkeeper6.ui.components.CalendarEntryEditor

@Preview(showBackground = true, showSystemUi = true, heightDp = 600)
@Composable
fun RegionSelectionDialogPreview() {
    MaterialTheme {
        // デモ用のデータ
        val demoCalendarEntry = CalendarEntry(
            id = "demo-id",
            region = "暖地",
            sowing_start_date = "2025-03-01",
            sowing_end_date = "2025-03-31",
            harvest_start_date = "2025-06-01",
            harvest_end_date = "2025-06-30",
            expirationYear = 2026,
            expirationMonth = 10
        )
        
        val demoSeedPacket = SeedPacket(
            id = "demo-packet-id",
            variety = "ニンジン",
            family = "せり科",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(demoCalendarEntry)
        )
        
        // プレビュー用の状態
        var showDialog by remember { mutableStateOf(true) }
        var selectedRegion by remember { mutableStateOf("暖地") }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 地域選択ダイアログ
            RegionSelectionDialog(
                showDialog = showDialog,
                regionList = listOf("寒地", "寒冷地", "温暖地", "暖地"),
                croppedCalendarBitmap = null,
                ocrResult = demoSeedPacket,
                defaultRegion = "暖地",
                onRegionSelected = { region ->
                    selectedRegion = region
                    showDialog = false
                },
                onStartEditing = { },
                onUpdateEditing = { },
                onSaveEditing = { },
                onCancelEditing = { },
                onDismiss = { showDialog = false },
                onUpdateExpiration = { },
                editingCalendarEntry = null
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, heightDp = 500)
@Composable
fun CalendarEntryEditorPreview() {
    MaterialTheme {
        // デモ用のデータ
        val demoCalendarEntry = CalendarEntry(
            id = "demo-id",
            region = "暖地",
            sowing_start_date = "2025-03-01",
            sowing_end_date = "2025-03-31",
            harvest_start_date = "2025-06-01",
            harvest_end_date = "2025-06-30",
            expirationYear = 2026,
            expirationMonth = 10
        )
        
        val demoSeedPacket = SeedPacket(
            id = "demo-packet-id",
            variety = "ニンジン",
            family = "せり科",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(demoCalendarEntry)
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .heightIn(min = 400.dp, max = 600.dp), // 高さの制約を追加
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp)
                ) {
                    // 地域情報表示
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = "種暦",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "種暦",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Text(
                        text = "AI読取結果を確認してください。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 地域情報カード
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
                            // 地域設定セクション
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                androidx.compose.material3.Icon(
                                    Icons.Filled.Public,
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
                            
                            // 地域選択ボタン
                            Button(
                                onClick = { },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE91E63), // 暖地の色
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp
                                ),
                                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(
                                    text = "暖地",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // カレンダーエントリエディター
                    CalendarEntryEditor(
                        entry = demoCalendarEntry,
                        onUpdate = { },
                        onSave = { },
                        onCancel = { },
                        hideYearSelection = false,
                        onUpdateExpiration = { },
                        ocrResult = demoSeedPacket
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // OK・キャンセルボタン
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("キャンセル")
                        }
                        
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}
