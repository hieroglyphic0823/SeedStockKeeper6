package com.example.seedstockkeeper6.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.seedstockkeeper6.ui.components.SeedCalendarGrouped
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

@Composable
fun CalendarSection(viewModel: SeedInputViewModel) {
    var showRegionBottomSheet by remember { mutableStateOf(false) }
    
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
        
        // 編集モードの場合は編集画面を表示
        val isEditMode = viewModel.isCalendarEditMode
        val hasCalendar = viewModel.packet.calendar?.isNotEmpty() == true
        val selectedRegion = viewModel.selectedRegion
        Log.d("CalendarSection", "編集モード: $isEditMode, カレンダーあり: $hasCalendar, 選択地域: '$selectedRegion'")
        
        if (isEditMode && hasCalendar) {
            CalendarEditMode(viewModel)
        } else {
            // 通常の表示モード
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
            
            // 地域表示は栽培カレンダー内で行うため削除
            
            // ---- まきどき / 収穫カレンダー ----
            SeedCalendarGrouped(
                entries = calendarEntries,
                packetExpirationYear = viewModel.packet.expirationYear,    // ★ 追加
                packetExpirationMonth = viewModel.packet.expirationMonth,  // ★ 追加
                modifier = Modifier.fillMaxWidth(),
                heightDp = 140 // 地域ラベル分の高さを追加
            )

            Spacer(modifier = Modifier.height(16.dp))

            CalendarDetailSection(viewModel)

            // DisplayModeの時は地域追加ボタンを非表示
            if (viewModel.isEditMode || !viewModel.hasExistingData) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showRegionBottomSheet = true }, 
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
    
    // 地域選択ボトムシート
    if (showRegionBottomSheet) {
        RegionSelectionBottomSheet(
            selectedRegion = viewModel.selectedRegion,
            onRegionSelected = { 
                viewModel.addCalendarEntryWithRegion(it)
                showRegionBottomSheet = false
            },
            onDismiss = { showRegionBottomSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarEditMode(viewModel: SeedInputViewModel) {
    val currentEntry = viewModel.packet.calendar.firstOrNull() ?: return
    
    var sowingStart by remember { mutableStateOf(currentEntry.sowing_start.toString()) }
    var sowingStartStage by remember { mutableStateOf(currentEntry.sowing_start_stage) }
    var sowingEnd by remember { mutableStateOf(currentEntry.sowing_end.toString()) }
    var sowingEndStage by remember { mutableStateOf(currentEntry.sowing_end_stage) }
    var harvestStart by remember { mutableStateOf(currentEntry.harvest_start.toString()) }
    var harvestStartStage by remember { mutableStateOf(currentEntry.harvest_start_stage) }
    var harvestEnd by remember { mutableStateOf(currentEntry.harvest_end.toString()) }
    var harvestEndStage by remember { mutableStateOf(currentEntry.harvest_end_stage) }

    // ドロップダウンの展開状態
    var sowingStartExpanded by remember { mutableStateOf(false) }
    var sowingStartStageExpanded by remember { mutableStateOf(false) }
    var sowingEndExpanded by remember { mutableStateOf(false) }
    var sowingEndStageExpanded by remember { mutableStateOf(false) }
    var harvestStartExpanded by remember { mutableStateOf(false) }
    var harvestStartStageExpanded by remember { mutableStateOf(false) }
    var harvestEndExpanded by remember { mutableStateOf(false) }
    var harvestEndStageExpanded by remember { mutableStateOf(false) }

    val monthOptions = (1..12).map { it.toString() }
    val stageOptions = listOf("", "初旬", "中旬", "下旬")

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "編集: ${currentEntry.region}",
                    style = MaterialTheme.typography.titleSmall
                )
                
                TextButton(
                    onClick = { viewModel.exitCalendarEditMode() }
                ) {
                    Text("完了")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "播種期間",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 播種開始月
                ExposedDropdownMenuBox(
                    expanded = sowingStartExpanded,
                    onExpandedChange = { sowingStartExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = if (sowingStart == "0") "" else sowingStart,
                        onValueChange = { },
                        label = { Text("開始月") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sowingStartExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = sowingStartExpanded,
                        onDismissRequest = { sowingStartExpanded = false }
                    ) {
                        monthOptions.forEach { month ->
                            DropdownMenuItem(
                                text = { Text("${month}月") },
                                onClick = {
                                    sowingStart = month
                                    sowingStartExpanded = false
                                    updateCalendarEntry(viewModel, currentEntry.copy(
                                        sowing_start = month.toIntOrNull() ?: 0,
                                        sowing_start_stage = sowingStartStage
                                    ))
                                }
                            )
                        }
                    }
                }
                
                // 播種開始段階
                ExposedDropdownMenuBox(
                    expanded = sowingStartStageExpanded,
                    onExpandedChange = { sowingStartStageExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = sowingStartStage,
                        onValueChange = { },
                        label = { Text("開始段階") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sowingStartStageExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = sowingStartStageExpanded,
                        onDismissRequest = { sowingStartStageExpanded = false }
                    ) {
                        stageOptions.forEach { stage ->
                            DropdownMenuItem(
                                text = { Text(if (stage.isEmpty()) "なし" else stage) },
                                onClick = {
                                    sowingStartStage = stage
                                    sowingStartStageExpanded = false
                                    updateCalendarEntry(viewModel, currentEntry.copy(
                                        sowing_start = sowingStart.toIntOrNull() ?: 0,
                                        sowing_start_stage = stage
                                    ))
                                }
                            )
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 播種終了月
                ExposedDropdownMenuBox(
                    expanded = sowingEndExpanded,
                    onExpandedChange = { sowingEndExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = if (sowingEnd == "0") "" else sowingEnd,
                        onValueChange = { },
                        label = { Text("終了月") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sowingEndExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = sowingEndExpanded,
                        onDismissRequest = { sowingEndExpanded = false }
                    ) {
                        monthOptions.forEach { month ->
                            DropdownMenuItem(
                                text = { Text("${month}月") },
                                onClick = {
                                    sowingEnd = month
                                    sowingEndExpanded = false
                                    updateCalendarEntry(viewModel, currentEntry.copy(
                                        sowing_end = month.toIntOrNull() ?: 0,
                                        sowing_end_stage = sowingEndStage
                                    ))
                                }
                            )
                        }
                    }
                }
                
                // 播種終了段階
                ExposedDropdownMenuBox(
                    expanded = sowingEndStageExpanded,
                    onExpandedChange = { sowingEndStageExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = sowingEndStage,
                        onValueChange = { },
                        label = { Text("終了段階") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sowingEndStageExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = sowingEndStageExpanded,
                        onDismissRequest = { sowingEndStageExpanded = false }
                    ) {
                        stageOptions.forEach { stage ->
                            DropdownMenuItem(
                                text = { Text(if (stage.isEmpty()) "なし" else stage) },
                                onClick = {
                                    sowingEndStage = stage
                                    sowingEndStageExpanded = false
                                    updateCalendarEntry(viewModel, currentEntry.copy(
                                        sowing_end = sowingEnd.toIntOrNull() ?: 0,
                                        sowing_end_stage = stage
                                    ))
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "収穫期間",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 収穫開始月
                ExposedDropdownMenuBox(
                    expanded = harvestStartExpanded,
                    onExpandedChange = { harvestStartExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = if (harvestStart == "0") "" else harvestStart,
                        onValueChange = { },
                        label = { Text("開始月") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = harvestStartExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = harvestStartExpanded,
                        onDismissRequest = { harvestStartExpanded = false }
                    ) {
                        monthOptions.forEach { month ->
                            DropdownMenuItem(
                                text = { Text("${month}月") },
                                onClick = {
                                    harvestStart = month
                                    harvestStartExpanded = false
                                    updateCalendarEntry(viewModel, currentEntry.copy(
                                        harvest_start = month.toIntOrNull() ?: 0,
                                        harvest_start_stage = harvestStartStage
                                    ))
                                }
                            )
                        }
                    }
                }
                
                // 収穫開始段階
                ExposedDropdownMenuBox(
                    expanded = harvestStartStageExpanded,
                    onExpandedChange = { harvestStartStageExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = harvestStartStage,
                        onValueChange = { },
                        label = { Text("開始段階") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = harvestStartStageExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = harvestStartStageExpanded,
                        onDismissRequest = { harvestStartStageExpanded = false }
                    ) {
                        stageOptions.forEach { stage ->
                            DropdownMenuItem(
                                text = { Text(if (stage.isEmpty()) "なし" else stage) },
                                onClick = {
                                    harvestStartStage = stage
                                    harvestStartStageExpanded = false
                                    updateCalendarEntry(viewModel, currentEntry.copy(
                                        harvest_start = harvestStart.toIntOrNull() ?: 0,
                                        harvest_start_stage = stage
                                    ))
                                }
                            )
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 収穫終了月
                ExposedDropdownMenuBox(
                    expanded = harvestEndExpanded,
                    onExpandedChange = { harvestEndExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = if (harvestEnd == "0") "" else harvestEnd,
                        onValueChange = { },
                        label = { Text("終了月") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = harvestEndExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = harvestEndExpanded,
                        onDismissRequest = { harvestEndExpanded = false }
                    ) {
                        monthOptions.forEach { month ->
                            DropdownMenuItem(
                                text = { Text("${month}月") },
                                onClick = {
                                    harvestEnd = month
                                    harvestEndExpanded = false
                                    updateCalendarEntry(viewModel, currentEntry.copy(
                                        harvest_end = month.toIntOrNull() ?: 0,
                                        harvest_end_stage = harvestEndStage
                                    ))
                                }
                            )
                        }
                    }
                }
                
                // 収穫終了段階
                ExposedDropdownMenuBox(
                    expanded = harvestEndStageExpanded,
                    onExpandedChange = { harvestEndStageExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = harvestEndStage,
                        onValueChange = { },
                        label = { Text("終了段階") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = harvestEndStageExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = harvestEndStageExpanded,
                        onDismissRequest = { harvestEndStageExpanded = false }
                    ) {
                        stageOptions.forEach { stage ->
                            DropdownMenuItem(
                                text = { Text(if (stage.isEmpty()) "なし" else stage) },
                                onClick = {
                                    harvestEndStage = stage
                                    harvestEndStageExpanded = false
                                    updateCalendarEntry(viewModel, currentEntry.copy(
                                        harvest_end = harvestEnd.toIntOrNull() ?: 0,
                                        harvest_end_stage = stage
                                    ))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun updateCalendarEntry(viewModel: SeedInputViewModel, updatedEntry: com.example.seedstockkeeper6.model.CalendarEntry) {
    // ViewModelのpacketプロパティを直接更新する代わりに、適切なメソッドを使用
    viewModel.updateCalendarEntry(
        index = 0,
        sowing_start = updatedEntry.sowing_start,
        sowing_start_stage = updatedEntry.sowing_start_stage,
        sowing_end = updatedEntry.sowing_end,
        sowing_end_stage = updatedEntry.sowing_end_stage,
        harvest_start = updatedEntry.harvest_start,
        harvest_start_stage = updatedEntry.harvest_start_stage,
        harvest_end = updatedEntry.harvest_end,
        harvest_end_stage = updatedEntry.harvest_end_stage
    )
}

// 地域ごとの色定義
private fun getRegionColor(region: String): Color {
    return when (region) {
        "寒地" -> Color(0xFF1A237E) // 紺
        "寒冷地" -> Color(0xFF1976D2) // 青
        "温暖地" -> Color(0xFFFF9800) // オレンジ
        "暖地" -> Color(0xFFE91E63) // ピンク
        else -> Color(0xFF9E9E9E) // グレー（未設定時）
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionSelectionBottomSheet(
    selectedRegion: String,
    onRegionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val regions = listOf(
        "寒地", "寒冷地", "温暖地", "暖地"
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Icon(
                    Icons.Filled.Public,
                    contentDescription = "地域選択",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "地域を選択",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            regions.forEach { region ->
                Button(
                    onClick = { onRegionSelected(region) },
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
                        BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                    } else null
                ) {
                    Text(
                        text = region,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (region == selectedRegion) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
