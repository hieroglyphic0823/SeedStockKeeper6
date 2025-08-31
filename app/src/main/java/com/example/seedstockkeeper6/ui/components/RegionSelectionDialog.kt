package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.offset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectionDialog(
    showDialog: Boolean,
    regionList: List<String>,
    ocrResult: com.example.seedstockkeeper6.model.SeedPacket?,
    croppedCalendarBitmap: android.graphics.Bitmap?,
    editingCalendarEntry: com.example.seedstockkeeper6.model.CalendarEntry?,
    onRegionSelected: (String) -> Unit,
    onStartEditing: (com.example.seedstockkeeper6.model.CalendarEntry) -> Unit,
    onUpdateEditing: (com.example.seedstockkeeper6.model.CalendarEntry) -> Unit,
    onSaveEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onDismiss: () -> Unit
) {
    android.util.Log.d("RegionSelectionDialog", "RegionSelectionDialog開始: showDialog=$showDialog, regionList=$regionList")
    if (showDialog) {
        var selectedRegion by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = onDismiss) {
                         Card(
                 modifier = Modifier
                     .fillMaxWidth(0.95f)
                     .padding(16.dp),
                 shape = MaterialTheme.shapes.medium,
                 colors = CardDefaults.cardColors(
                     containerColor = MaterialTheme.colorScheme.surface
                 )
             ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = if (regionList.any { it in listOf("北海道", "東北", "関東", "中部", "関西", "中国", "四国", "九州", "沖縄") }) {
                            "地域区分を選択してください"
                        } else {
                            "OCRの読み込み結果から地域区分を選択してください"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 切り抜きされたカレンダー画像を表示
                    if (croppedCalendarBitmap != null) {
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
                                Text(
                                    "検出されたカレンダー部分",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Image(
                                    bitmap = croppedCalendarBitmap.asImageBitmap(),
                                    contentDescription = "切り抜きされたカレンダー",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    // 地域選択のコンボボックス
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedRegion,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("地域を選択") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            regionList.forEach { region ->
                                DropdownMenuItem(
                                    text = { Text(region) },
                                    onClick = {
                                        selectedRegion = region
                                        expanded = false
                                        android.util.Log.d("RegionSelectionDialog", "地域選択: $region")
                                        
                                        // 地域変更時にOCR結果で期間を上書き
                                        val newRegionEntry = ocrResult?.calendar?.find { it.region == region }
                                        if (newRegionEntry != null) {
                                            onUpdateEditing(newRegionEntry)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 選択された地域のOCR結果と編集項目を表示
                    if (selectedRegion.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val selectedRegionEntry = ocrResult?.calendar?.find { it.region == selectedRegion }
                        val entryToShow = selectedRegionEntry ?: com.example.seedstockkeeper6.model.CalendarEntry(
                            region = selectedRegion,
                            sowing_start = 0,
                            sowing_start_stage = "",
                            sowing_end = 0,
                            sowing_end_stage = "",
                            harvest_start = 0,
                            harvest_start_stage = "",
                            harvest_end = 0,
                            harvest_end_stage = ""
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
                                Text(
                                    "地域: ${selectedRegion}",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                if (selectedRegionEntry != null) {
                                    // OCR結果がある場合は表示
                                    Text(
                                        "OCR結果: ${selectedRegion}",
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    // 表示モード
                                    CalendarEntryDisplay(entry = selectedRegionEntry)
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                
                                // 編集項目を表示
                                CalendarEntryEditor(
                                    entry = entryToShow,
                                    onUpdate = { updatedEntry ->
                                        // 編集内容をViewModelに反映
                                        onUpdateEditing(updatedEntry)
                                    },
                                    onSave = {
                                        // 保存処理
                                        onSaveEditing()
                                    },
                                    onCancel = {
                                        // キャンセル処理
                                        onCancelEditing()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ボタン
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text("キャンセル")
                        }
                        
                        if (selectedRegion.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = {
                                    android.util.Log.d("RegionSelectionDialog", "保存ボタンクリック: $selectedRegion")
                                    onRegionSelected(selectedRegion)
                                    onDismiss()
                                }
                            ) {
                                Text("保存")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarEntryDisplay(entry: com.example.seedstockkeeper6.model.CalendarEntry) {
    Column {
        // 播種期間
        if (entry.sowing_start > 0 || entry.sowing_end > 0) {
            Text(
                "播種期間: ${entry.sowing_start}月${if (entry.sowing_start_stage.isNotEmpty()) "(${entry.sowing_start_stage})" else ""} ～ ${entry.sowing_end}月${if (entry.sowing_end_stage.isNotEmpty()) "(${entry.sowing_end_stage})" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        // 収穫期間
        if (entry.harvest_start > 0 || entry.harvest_end > 0) {
            Text(
                "収穫期間: ${entry.harvest_start}月${if (entry.harvest_start_stage.isNotEmpty()) "(${entry.harvest_start_stage})" else ""} ～ ${entry.harvest_end}月${if (entry.harvest_end_stage.isNotEmpty()) "(${entry.harvest_end_stage})" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        // データがない場合
        if ((entry.sowing_start == 0 && entry.sowing_end == 0) &&
            (entry.harvest_start == 0 && entry.harvest_end == 0)) {
            Text(
                "播種・収穫期間の情報がありません",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarEntryEditor(
    entry: com.example.seedstockkeeper6.model.CalendarEntry,
    onUpdate: (com.example.seedstockkeeper6.model.CalendarEntry) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var sowingStart by remember { mutableStateOf(entry.sowing_start.toString()) }
    var sowingStartStage by remember { mutableStateOf(entry.sowing_start_stage) }
    var sowingEnd by remember { mutableStateOf(entry.sowing_end.toString()) }
    var sowingEndStage by remember { mutableStateOf(entry.sowing_end_stage) }
    var harvestStart by remember { mutableStateOf(entry.harvest_start.toString()) }
    var harvestStartStage by remember { mutableStateOf(entry.harvest_start_stage) }
    var harvestEnd by remember { mutableStateOf(entry.harvest_end.toString()) }
    var harvestEndStage by remember { mutableStateOf(entry.harvest_end_stage) }
    
    // ドロップダウンの展開状態
    var sowingStartExpanded by remember { mutableStateOf(false) }
    var sowingStartStageExpanded by remember { mutableStateOf(false) }
    var sowingEndExpanded by remember { mutableStateOf(false) }
    var sowingEndStageExpanded by remember { mutableStateOf(false) }
    var harvestStartExpanded by remember { mutableStateOf(false) }
    var harvestStartStageExpanded by remember { mutableStateOf(false) }
    var harvestEndExpanded by remember { mutableStateOf(false) }
    var harvestEndStageExpanded by remember { mutableStateOf(false) }
    
    // エントリが変更された時に状態を更新
    LaunchedEffect(entry) {
        sowingStart = entry.sowing_start.toString()
        sowingStartStage = entry.sowing_start_stage
        sowingEnd = entry.sowing_end.toString()
        sowingEndStage = entry.sowing_end_stage
        harvestStart = entry.harvest_start.toString()
        harvestStartStage = entry.harvest_start_stage
        harvestEnd = entry.harvest_end.toString()
        harvestEndStage = entry.harvest_end_stage
    }

    Column(
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            "播種期間",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // 開始期間
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("開始:", style = MaterialTheme.typography.bodyMedium)
            
            // 開始月（リスト表示）
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .clickable { sowingStartExpanded = true }
            ) {
                                 OutlinedTextField(
                     value = if (sowingStart == "0") "不明" else sowingStart,
                     onValueChange = {},
                     readOnly = true,
                     label = { Text("") },
                     modifier = Modifier.fillMaxWidth()
                 )
                 
                 // 月ラベルを右下に表示（線にまたがる）
                 Text(
                     text = "月",
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     modifier = Modifier
                         .align(Alignment.BottomEnd)
                         .offset(x = (-4).dp, y = (-4).dp)
                 )
                
                if (sowingStartExpanded) {
                    Card(
                        modifier = Modifier
                            .width(50.dp)
                            .offset(y = 60.dp)
                            .zIndex(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(listOf("不明", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")) { month ->
                                Text(
                                    text = month,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            sowingStart = if (month == "不明") "0" else month
                                            sowingStartExpanded = false
                                            onUpdate(entry.copy(
                                                sowing_start = sowingStart.toIntOrNull() ?: 0,
                                                sowing_start_stage = sowingStartStage
                                            ))
                                        }
                                        .padding(8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // 開始段階（リスト表示）
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .clickable { sowingStartStageExpanded = true }
            ) {
                OutlinedTextField(
                    value = sowingStartStage.ifEmpty { "不明" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                                 // 旬ラベルを右下に表示（線にまたがる）
                 Text(
                     text = "旬",
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     modifier = Modifier
                         .align(Alignment.BottomEnd)
                         .offset(x = (-4).dp, y = (-4).dp)
                 )
                 
                 if (sowingStartStageExpanded) {
                    Card(
                        modifier = Modifier
                            .width(50.dp)
                            .offset(y = 60.dp)
                            .zIndex(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                                                         items(listOf("不明", "上", "中", "下")) { stage ->
                                 Text(
                                     text = stage,
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .clickable {
                                             sowingStartStage = if (stage == "不明") "" else stage
                                             sowingStartStageExpanded = false
                                             onUpdate(entry.copy(
                                                 sowing_start = sowingStart.toIntOrNull() ?: 0,
                                                 sowing_start_stage = sowingStartStage
                                             ))
                                         }
                                         .padding(8.dp),
                                     style = MaterialTheme.typography.bodyMedium
                                 )
                             }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 終了期間
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("終了:", style = MaterialTheme.typography.bodyMedium)
            
            // 終了月（リスト表示）
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .clickable { sowingEndExpanded = true }
            ) {
                                 OutlinedTextField(
                     value = if (sowingEnd == "0") "不明" else sowingEnd,
                     onValueChange = {},
                     readOnly = true,
                     label = { Text("") },
                     modifier = Modifier.fillMaxWidth()
                 )
                 
                 // 月ラベルを右下に表示（線にまたがる）
                 Text(
                     text = "月",
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     modifier = Modifier
                         .align(Alignment.BottomEnd)
                         .offset(x = (-4).dp, y = (-4).dp)
                 )
                
                if (sowingEndExpanded) {
                    Card(
                        modifier = Modifier
                            .width(50.dp)
                            .offset(y = 60.dp)
                            .zIndex(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(listOf("不明", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")) { month ->
                                Text(
                                    text = month,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            sowingEnd = if (month == "不明") "0" else month
                                            sowingEndExpanded = false
                                            onUpdate(entry.copy(
                                                sowing_end = sowingEnd.toIntOrNull() ?: 0,
                                                sowing_end_stage = sowingEndStage
                                            ))
                                        }
                                        .padding(8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // 終了段階（リスト表示）
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .clickable { sowingEndStageExpanded = true }
            ) {
                OutlinedTextField(
                    value = sowingEndStage.ifEmpty { "不明" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                                 // 旬ラベルを右下に表示（線にまたがる）
                 Text(
                     text = "旬",
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     modifier = Modifier
                         .align(Alignment.BottomEnd)
                         .offset(x = (-4).dp, y = (-4).dp)
                 )
                 
                 if (sowingEndStageExpanded) {
                    Card(
                        modifier = Modifier
                            .width(50.dp)
                            .offset(y = 60.dp)
                            .zIndex(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                                                         items(listOf("不明", "上", "中", "下")) { stage ->
                                 Text(
                                     text = stage,
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .clickable {
                                             sowingEndStage = if (stage == "不明") "" else stage
                                             sowingEndStageExpanded = false
                                             onUpdate(entry.copy(
                                                 sowing_end = sowingEnd.toIntOrNull() ?: 0,
                                                 sowing_end_stage = sowingEndStage
                                             ))
                                         }
                                         .padding(8.dp),
                                     style = MaterialTheme.typography.bodyMedium
                                 )
                             }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "収穫期間",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // 開始期間
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("開始:", style = MaterialTheme.typography.bodyMedium)
            
            // 開始月（リスト表示）
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .clickable { harvestStartExpanded = true }
            ) {
                                 OutlinedTextField(
                     value = if (harvestStart == "0") "不明" else harvestStart,
                     onValueChange = {},
                     readOnly = true,
                     label = { Text("") },
                     modifier = Modifier.fillMaxWidth()
                 )
                 
                 // 月ラベルを右下に表示（線にまたがる）
                 Text(
                     text = "月",
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     modifier = Modifier
                         .align(Alignment.BottomEnd)
                         .offset(x = (-4).dp, y = (-4).dp)
                 )
                
                if (harvestStartExpanded) {
                    Card(
                        modifier = Modifier
                            .width(50.dp)
                            .offset(y = 60.dp)
                            .zIndex(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(listOf("不明", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")) { month ->
                                Text(
                                    text = month,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            harvestStart = if (month == "不明") "0" else month
                                            harvestStartExpanded = false
                                            onUpdate(entry.copy(
                                                harvest_start = harvestStart.toIntOrNull() ?: 0,
                                                harvest_start_stage = harvestStartStage
                                            ))
                                        }
                                        .padding(8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // 開始段階（リスト表示）
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .clickable { harvestStartStageExpanded = true }
            ) {
                OutlinedTextField(
                    value = harvestStartStage.ifEmpty { "不明" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                                 // 旬ラベルを右下に表示（線にまたがる）
                 Text(
                     text = "旬",
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     modifier = Modifier
                         .align(Alignment.BottomEnd)
                         .offset(x = (-4).dp, y = (-4).dp)
                 )
                 
                 if (harvestStartStageExpanded) {
                    Card(
                        modifier = Modifier
                            .width(50.dp)
                            .offset(y = 60.dp)
                            .zIndex(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                                                         items(listOf("不明", "上", "中", "下")) { stage ->
                                 Text(
                                     text = stage,
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .clickable {
                                             harvestStartStage = if (stage == "不明") "" else stage
                                             harvestStartStageExpanded = false
                                             onUpdate(entry.copy(
                                                 harvest_start = harvestStart.toIntOrNull() ?: 0,
                                                 harvest_start_stage = harvestStartStage
                                             ))
                                         }
                                         .padding(8.dp),
                                     style = MaterialTheme.typography.bodyMedium
                                 )
                             }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 終了期間
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("終了:", style = MaterialTheme.typography.bodyMedium)
            
            // 終了月（リスト表示）
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .clickable { harvestEndExpanded = true }
            ) {
                                 OutlinedTextField(
                     value = if (harvestEnd == "0") "不明" else harvestEnd,
                     onValueChange = {},
                     readOnly = true,
                     label = { Text("") },
                     modifier = Modifier.fillMaxWidth()
                 )
                 
                 // 月ラベルを右下に表示（線にまたがる）
                 Text(
                     text = "月",
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     modifier = Modifier
                         .align(Alignment.BottomEnd)
                         .offset(x = (-4).dp, y = (-4).dp)
                 )
                
                if (harvestEndExpanded) {
                    Card(
                        modifier = Modifier
                            .width(50.dp)
                            .offset(y = 60.dp)
                            .zIndex(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(listOf("不明", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")) { month ->
                                Text(
                                    text = month,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            harvestEnd = if (month == "不明") "0" else month
                                            harvestEndExpanded = false
                                            onUpdate(entry.copy(
                                                harvest_end = harvestEnd.toIntOrNull() ?: 0,
                                                harvest_end_stage = harvestEndStage
                                            ))
                                        }
                                        .padding(8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // 終了段階（リスト表示）
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .clickable { harvestEndStageExpanded = true }
            ) {
                OutlinedTextField(
                    value = harvestEndStage.ifEmpty { "不明" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                                 // 旬ラベルを右下に表示（線にまたがる）
                 Text(
                     text = "旬",
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     modifier = Modifier
                         .align(Alignment.BottomEnd)
                         .offset(x = (-4).dp, y = (-4).dp)
                 )
                 
                 if (harvestEndStageExpanded) {
                    Card(
                        modifier = Modifier
                            .width(50.dp)
                            .offset(y = 60.dp)
                            .zIndex(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                                                         items(listOf("不明", "上", "中", "下")) { stage ->
                                 Text(
                                     text = stage,
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .clickable {
                                             harvestEndStage = if (stage == "不明") "" else stage
                                             harvestEndStageExpanded = false
                                             onUpdate(entry.copy(
                                                 harvest_end = harvestEnd.toIntOrNull() ?: 0,
                                                 harvest_end_stage = harvestEndStage
                                             ))
                                         }
                                         .padding(8.dp),
                                     style = MaterialTheme.typography.bodyMedium
                                 )
                             }
                        }
                    }
                }
            }
        }
        
        // 保存とキャンセルボタンは削除（ダイアログのボタンを使用）
    }
}
