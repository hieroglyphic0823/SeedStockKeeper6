package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarEntryEditor(
    entry: com.example.seedstockkeeper6.model.CalendarEntry,
    onUpdate: (com.example.seedstockkeeper6.model.CalendarEntry) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // ローカル状態変数
    var sowingStart by remember(entry) { mutableStateOf(entry.sowing_start.toString()) }
    var sowingStartStage by remember(entry) { mutableStateOf(entry.sowing_start_stage) }
    var sowingEnd by remember(entry) { mutableStateOf(entry.sowing_end.toString()) }
    var sowingEndStage by remember(entry) { mutableStateOf(entry.sowing_end_stage) }
    var harvestStart by remember(entry) { mutableStateOf(entry.harvest_start.toString()) }
    var harvestStartStage by remember(entry) { mutableStateOf(entry.harvest_start_stage) }
    var harvestEnd by remember(entry) { mutableStateOf(entry.harvest_end.toString()) }
    var harvestEndStage by remember(entry) { mutableStateOf(entry.harvest_end_stage) }
    
    // Expanded state variables for dropdowns
    var sowingStartExpanded by remember { mutableStateOf(false) }
    var sowingEndExpanded by remember { mutableStateOf(false) }
    var sowingEndStageExpanded by remember { mutableStateOf(false) }
    var harvestStartExpanded by remember { mutableStateOf(false) }
    var harvestStartStageExpanded by remember { mutableStateOf(false) }
    var harvestEndExpanded by remember { mutableStateOf(false) }
    var harvestEndStageExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            "播種期間",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
                .padding(8.dp)
        )
        
        // 開始期間
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 開始月と段階を選択するボタン
            Button(
                onClick = { sowingStartExpanded = true },
                modifier = Modifier.width(200.dp)
            ) {
                Text(
                    text = if (sowingStart == "0" && sowingStartStage.isEmpty()) {
                        "播種開始期間を選択"
                    } else {
                        "${if (sowingStart == "0") "不明" else sowingStart}月${if (sowingStartStage.isEmpty()) "" else "(${sowingStartStage})"}"
                    }
                )
            }
        }
        
        // 播種開始期間選択ボトムシート
        if (sowingStartExpanded) {
            ModalBottomSheet(
                onDismissRequest = { sowingStartExpanded = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "播種開始期間を選択",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 月選択
                    Text("月", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                    LazyColumn(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1行目: 1, 2, 3, 4
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("1", "2", "3", "4").forEach { month ->
                                        Button(
                                            onClick = {
                                                sowingStart = month
                                                onUpdate(entry.copy(
                                                    sowing_start = month.toIntOrNull() ?: 0,
                                                    sowing_start_stage = sowingStartStage
                                                ))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (sowingStart == month) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Text(
                                                text = month,
                                                color = if (sowingStart == month) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                
                                // 2行目: 5, 6, 7, 8
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("5", "6", "7", "8").forEach { month ->
                                        Button(
                                            onClick = {
                                                sowingStart = month
                                                onUpdate(entry.copy(
                                                    sowing_start = month.toIntOrNull() ?: 0,
                                                    sowing_start_stage = sowingStartStage
                                                ))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (sowingStart == month) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Text(
                                                text = month,
                                                color = if (sowingStart == month) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                
                                // 3行目: 9, 10, 11, 12, 不明
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("9", "10", "11", "12", "不明").forEach { month ->
                                        Button(
                                            onClick = {
                                                sowingStart = if (month == "不明") "0" else month
                                                onUpdate(entry.copy(
                                                    sowing_start = month.toIntOrNull() ?: 0,
                                                    sowing_start_stage = sowingStartStage
                                                ))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if ((month == "不明" && sowingStart == "0") || 
                                                                   (month != "不明" && sowingStart == month)) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Text(
                                                text = month,
                                                color = if ((month == "不明" && sowingStart == "0") || 
                                                           (month != "不明" && sowingStart == month)) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // 段階選択（ラベルなし）
                    LazyColumn(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("上旬", "中旬", "下旬", "不明").forEach { stage ->
                                    Button(
                                        onClick = {
                                            sowingStartStage = if (stage == "不明") "" else stage
                                            onUpdate(entry.copy(
                                                sowing_start = sowingStart.toIntOrNull() ?: 0,
                                                sowing_start_stage = sowingStartStage
                                            ))
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if ((stage == "不明" && sowingStartStage.isEmpty()) || 
                                                               (stage != "不明" && sowingStartStage == stage)) 
                                                MaterialTheme.colorScheme.primaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Text(
                                            text = stage,
                                            color = if ((stage == "不明" && sowingStartStage.isEmpty()) || 
                                                       (stage != "不明" && sowingStartStage == stage)) 
                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = { sowingStartExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("完了")
                    }
                }
            }
        }
        
        // 播種終了期間
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { sowingEndExpanded = true },
                modifier = Modifier.width(200.dp)
            ) {
                Text(
                    text = if (sowingEnd == "0" && sowingEndStage.isEmpty()) {
                        "播種終了期間を選択"
                    } else {
                        "${if (sowingEnd == "0") "不明" else sowingEnd}月${if (sowingEndStage.isEmpty()) "" else "(${sowingEndStage})"}"
                    }
                )
            }
        }
        
        // 播種終了期間選択ボトムシート
        if (sowingEndExpanded) {
            ModalBottomSheet(
                onDismissRequest = { sowingEndExpanded = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "播種終了期間を選択",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 月選択
                    Text("月", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                    LazyColumn(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1行目: 1, 2, 3, 4
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("1", "2", "3", "4").forEach { month ->
                                        Button(
                                            onClick = {
                                                sowingEnd = month
                                                onUpdate(entry.copy(
                                                    sowing_end = month.toIntOrNull() ?: 0,
                                                    sowing_end_stage = sowingEndStage
                                                ))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (sowingEnd == month) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Text(
                                                text = month,
                                                color = if (sowingEnd == month) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                
                                // 2行目: 5, 6, 7, 8
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("5", "6", "7", "8").forEach { month ->
                                        Button(
                                            onClick = {
                                                sowingEnd = month
                                                onUpdate(entry.copy(
                                                    sowing_end = month.toIntOrNull() ?: 0,
                                                    sowing_end_stage = sowingEndStage
                                                ))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (sowingEnd == month) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Text(
                                                text = month,
                                                color = if (sowingEnd == month) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                
                                // 3行目: 9, 10, 11, 12, 不明
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("9", "10", "11", "12", "不明").forEach { month ->
                                        Button(
                                            onClick = {
                                                sowingEnd = if (month == "不明") "0" else month
                                                onUpdate(entry.copy(
                                                    sowing_end = month.toIntOrNull() ?: 0,
                                                    sowing_end_stage = sowingEndStage
                                                ))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if ((month == "不明" && sowingEnd == "0") || 
                                                                   (month != "不明" && sowingEnd == month)) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Text(
                                                text = month,
                                                color = if ((month == "不明" && sowingEnd == "0") || 
                                                           (month != "不明" && sowingEnd == month)) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // 段階選択（ラベルなし）
                    LazyColumn(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("上旬", "中旬", "下旬", "不明").forEach { stage ->
                                    Button(
                                        onClick = {
                                            sowingEndStage = if (stage == "不明") "" else stage
                                            onUpdate(entry.copy(
                                                sowing_end = sowingEnd.toIntOrNull() ?: 0,
                                                sowing_end_stage = sowingEndStage
                                            ))
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if ((stage == "不明" && sowingEndStage.isEmpty()) || 
                                                               (stage != "不明" && sowingEndStage == stage)) 
                                                MaterialTheme.colorScheme.primaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Text(
                                            text = stage,
                                            color = if ((stage == "不明" && sowingEndStage.isEmpty()) || 
                                                       (stage != "不明" && sowingEndStage == stage)) 
                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = { sowingEndExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("完了")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "収穫期間",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
                .padding(8.dp)
        )
        
        // 収穫開始期間
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { harvestStartExpanded = true },
                modifier = Modifier.width(200.dp)
            ) {
                Text(
                    text = if (harvestStart == "0" && harvestStartStage.isEmpty()) {
                        "収穫開始期間を選択"
                    } else {
                        "${if (harvestStart == "0") "不明" else harvestStart}月${if (harvestStartStage.isEmpty()) "" else "(${harvestStartStage})"}"
                    }
                )
            }
        }
        
        // 収穫開始期間選択ボトムシート
        if (harvestStartExpanded) {
            ModalBottomSheet(
                onDismissRequest = { harvestStartExpanded = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "収穫開始期間を選択",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 月選択
                    Text("月", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                    LazyColumn(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1行目: 1, 2, 3, 4
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("1", "2", "3", "4").forEach { month ->
                                        Button(
                                            onClick = {
                                                harvestStart = month
                                                onUpdate(entry.copy(
                                                    harvest_start = month.toIntOrNull() ?: 0,
                                                    harvest_start_stage = harvestStartStage
                                                ))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (entry.harvest_start.toString() == month) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Text(
                                                text = month,
                                                color = if (entry.harvest_start.toString() == month) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                
                                // 2行目: 5, 6, 7, 8
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("5", "6", "7", "8").forEach { month ->
                                        Button(
                                            onClick = {
                                                harvestStart = month
                                                onUpdate(entry.copy(
                                                    harvest_start = month.toIntOrNull() ?: 0,
                                                    harvest_start_stage = harvestStartStage
                                                ))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (entry.harvest_start.toString() == month) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Text(
                                                text = month,
                                                color = if (entry.harvest_start.toString() == month) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                
                                // 3行目: 9, 10, 11, 12, 不明
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("9", "10", "11", "12", "不明").forEach { month ->
                                        Button(
                                            onClick = {
                                                harvestStart = if (month == "不明") "0" else month
                                                onUpdate(entry.copy(
                                                    harvest_start = month.toIntOrNull() ?: 0,
                                                    harvest_start_stage = harvestStartStage
                                                ))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if ((month == "不明" && harvestStart == "0") || 
                                                                   (month != "不明" && harvestStart == month)) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Text(
                                                text = month,
                                                color = if ((month == "不明" && harvestStart == "0") || 
                                                           (month != "不明" && harvestStart == month)) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // 段階選択（ラベルなし）
                    LazyColumn(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("上旬", "中旬", "下旬", "不明").forEach { stage ->
                                    Button(
                                        onClick = {
                                            harvestStartStage = if (stage == "不明") "" else stage
                                            onUpdate(entry.copy(
                                                harvest_start = harvestStart.toIntOrNull() ?: 0,
                                                harvest_start_stage = harvestStartStage
                                            ))
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if ((stage == "不明" && harvestStartStage.isEmpty()) || 
                                                               (stage != "不明" && harvestStartStage == stage)) 
                                                MaterialTheme.colorScheme.primaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Text(
                                            text = stage,
                                            color = if ((stage == "不明" && harvestStartStage.isEmpty()) || 
                                                       (stage != "不明" && harvestStartStage == stage)) 
                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = { harvestStartExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("完了")
                    }
                }
            }
        }
        
        // 収穫終了期間
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { harvestEndExpanded = true },
                modifier = Modifier.width(200.dp)
            ) {
                Text(
                    text = if (harvestEnd == "0" && harvestEndStage.isEmpty()) {
                        "収穫終了期間を選択"
                    } else {
                        "${if (harvestEnd == "0") "不明" else harvestEnd}月${if (harvestEndStage.isEmpty()) "" else "(${harvestEndStage})"}"
                    }
                )
            }
        }
        
        // 収穫終了期間選択ボトムシート
        if (harvestEndExpanded) {
            ModalBottomSheet(
                onDismissRequest = { harvestEndExpanded = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "収穫終了期間を選択",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 月選択
                    Text("月", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                    LazyColumn(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1行目: 1, 2, 3, 4
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("1", "2", "3", "4").forEach { month ->
                                        Button(
                                            onClick = {
                                                harvestEnd = month
                                                onUpdate(entry.copy(
                                                    harvest_end = month.toIntOrNull() ?: 0,
                                                    harvest_end_stage = harvestEndStage
                                                ))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (entry.harvest_end.toString() == month) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Text(
                                                text = month,
                                                color = if (entry.harvest_end.toString() == month) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                
                                // 2行目: 5, 6, 7, 8
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("5", "6", "7", "8").forEach { month ->
                                        Button(
                                            onClick = {
                                                harvestEnd = month
                                                onUpdate(entry.copy(
                                                    harvest_end = month.toIntOrNull() ?: 0,
                                                    harvest_end_stage = harvestEndStage
                                                ))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (entry.harvest_end.toString() == month) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Text(
                                                text = month,
                                                color = if (entry.harvest_end.toString() == month) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                
                                // 3行目: 9, 10, 11, 12, 不明
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("9", "10", "11", "12", "不明").forEach { month ->
                                        Button(
                                            onClick = {
                                                harvestEnd = if (month == "不明") "0" else month
                                                onUpdate(entry.copy(
                                                    harvest_end = month.toIntOrNull() ?: 0,
                                                    harvest_end_stage = harvestEndStage
                                                ))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if ((month == "不明" && harvestEnd == "0") || 
                                                                   (month != "不明" && harvestEnd == month)) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Text(
                                                text = month,
                                                color = if ((month == "不明" && harvestEnd == "0") || 
                                                           (month != "不明" && harvestEnd == month)) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // 段階選択（ラベルなし）
                    LazyColumn(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("上旬", "中旬", "下旬", "不明").forEach { stage ->
                                    Button(
                                        onClick = {
                                            harvestEndStage = if (stage == "不明") "" else stage
                                            onUpdate(entry.copy(
                                                harvest_end = harvestEnd.toIntOrNull() ?: 0,
                                                harvest_end_stage = harvestEndStage
                                            ))
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if ((stage == "不明" && harvestEndStage.isEmpty()) || 
                                                               (stage != "不明" && harvestEndStage == stage)) 
                                                MaterialTheme.colorScheme.primaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Text(
                                            text = stage,
                                            color = if ((stage == "不明" && harvestEndStage.isEmpty()) || 
                                                       (stage != "不明" && harvestEndStage == stage)) 
                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = { harvestEndExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("完了")
                    }
                }
            }
        }
        
        // 保存とキャンセルボタンは削除（ダイアログのボタンを使用）
    }
}
