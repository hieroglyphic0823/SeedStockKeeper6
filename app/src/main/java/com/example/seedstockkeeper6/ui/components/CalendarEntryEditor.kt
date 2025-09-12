package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.utils.DateConversionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarEntryEditor(
    entry: CalendarEntry,
    onUpdate: (CalendarEntry) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // ローカル状態変数（旬ベースの入力）
    var sowingStartYear by remember(entry) { 
        mutableStateOf(DateConversionUtils.getYearFromDate(entry.sowing_start_date).toString()) 
    }
    var sowingStart by remember(entry) { 
        mutableStateOf(DateConversionUtils.getMonthFromDate(entry.sowing_start_date).toString()) 
    }
    var sowingStartStage by remember(entry) { 
        mutableStateOf(DateConversionUtils.convertDateToStage(entry.sowing_start_date)) 
    }
    var sowingEndYear by remember(entry) { 
        mutableStateOf(DateConversionUtils.getYearFromDate(entry.sowing_end_date).toString()) 
    }
    var sowingEnd by remember(entry) { 
        mutableStateOf(DateConversionUtils.getMonthFromDate(entry.sowing_end_date).toString()) 
    }
    var sowingEndStage by remember(entry) { 
        mutableStateOf(DateConversionUtils.convertDateToStage(entry.sowing_end_date)) 
    }
    var harvestStartYear by remember(entry) { 
        mutableStateOf(DateConversionUtils.getYearFromDate(entry.harvest_start_date).toString()) 
    }
    var harvestStart by remember(entry) { 
        mutableStateOf(DateConversionUtils.getMonthFromDate(entry.harvest_start_date).toString()) 
    }
    var harvestStartStage by remember(entry) { 
        mutableStateOf(DateConversionUtils.convertDateToStage(entry.harvest_start_date)) 
    }
    var harvestEndYear by remember(entry) { 
        mutableStateOf(DateConversionUtils.getYearFromDate(entry.harvest_end_date).toString()) 
    }
    var harvestEnd by remember(entry) { 
        mutableStateOf(DateConversionUtils.getMonthFromDate(entry.harvest_end_date).toString()) 
    }
    var harvestEndStage by remember(entry) { 
        mutableStateOf(DateConversionUtils.convertDateToStage(entry.harvest_end_date)) 
    }
    
    // Expanded state variables for dropdowns
    var sowingStartExpanded by remember { mutableStateOf(false) }
    var sowingEndExpanded by remember { mutableStateOf(false) }
    var harvestStartExpanded by remember { mutableStateOf(false) }
    var harvestEndExpanded by remember { mutableStateOf(false) }
    
    // 年選択オプション
    val currentYear = java.time.LocalDate.now().year
    val yearOptions = (currentYear - 1..currentYear + 2).map { it.toString() }

    // 日付変換と更新処理
    fun updateSowingStart(year: String, month: String, stage: String) {
        val yearInt = year.toIntOrNull() ?: 0
        val monthInt = month.toIntOrNull() ?: 0
        if (yearInt > 0 && monthInt > 0 && stage.isNotEmpty()) {
            val startDate = DateConversionUtils.convertStageToStartDate(yearInt, monthInt, stage)
            val endDate = entry.sowing_end_date
            onUpdate(entry.copy(sowing_start_date = startDate))
        }
    }
    
    fun updateSowingEnd(year: String, month: String, stage: String) {
        val yearInt = year.toIntOrNull() ?: 0
        val monthInt = month.toIntOrNull() ?: 0
        if (yearInt > 0 && monthInt > 0 && stage.isNotEmpty()) {
            val endDate = DateConversionUtils.convertStageToEndDate(yearInt, monthInt, stage)
            onUpdate(entry.copy(sowing_end_date = endDate))
        }
    }
    
    fun updateHarvestStart(year: String, month: String, stage: String) {
        val yearInt = year.toIntOrNull() ?: 0
        val monthInt = month.toIntOrNull() ?: 0
        if (yearInt > 0 && monthInt > 0 && stage.isNotEmpty()) {
            val startDate = DateConversionUtils.convertStageToStartDate(yearInt, monthInt, stage)
            onUpdate(entry.copy(harvest_start_date = startDate))
        }
    }
    
    fun updateHarvestEnd(year: String, month: String, stage: String) {
        val yearInt = year.toIntOrNull() ?: 0
        val monthInt = month.toIntOrNull() ?: 0
        if (yearInt > 0 && monthInt > 0 && stage.isNotEmpty()) {
            val endDate = DateConversionUtils.convertStageToEndDate(yearInt, monthInt, stage)
            onUpdate(entry.copy(harvest_end_date = endDate))
        }
    }

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
        
        // 播種開始期間
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { sowingStartExpanded = true },
                modifier = Modifier.width(200.dp)
            ) {
                Text(
                    text = if (sowingStart == "0" && sowingStartStage.isEmpty() && sowingStartYear == "0") {
                        "播種開始期間を選択"
                    } else {
                        "${if (sowingStartYear == "0") "" else "${sowingStartYear}年"}${if (sowingStart == "0") "不明" else sowingStart}月${if (sowingStartStage.isEmpty()) "" else "(${sowingStartStage})"}"
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
                PeriodSelectionBottomSheet(
                    title = "播種開始期間を選択",
                    selectedYear = sowingStartYear,
                    selectedMonth = sowingStart,
                    selectedStage = sowingStartStage,
                    onYearChange = { sowingStartYear = it },
                    onMonthChange = { sowingStart = it },
                    onStageChange = { sowingStartStage = it },
                    onConfirm = {
                        updateSowingStart(sowingStartYear, sowingStart, sowingStartStage)
                        sowingStartExpanded = false
                    },
                    onCancel = { sowingStartExpanded = false }
                )
            }
        }
        
        Text("～", style = MaterialTheme.typography.bodyLarge)
        
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
                    text = if (sowingEnd == "0" && sowingEndStage.isEmpty() && sowingEndYear == "0") {
                        "播種終了期間を選択"
                    } else {
                        "${if (sowingEndYear == "0") "" else "${sowingEndYear}年"}${if (sowingEnd == "0") "不明" else sowingEnd}月${if (sowingEndStage.isEmpty()) "" else "(${sowingEndStage})"}"
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
                PeriodSelectionBottomSheet(
                    title = "播種終了期間を選択",
                    selectedYear = sowingEndYear,
                    selectedMonth = sowingEnd,
                    selectedStage = sowingEndStage,
                    onYearChange = { sowingEndYear = it },
                    onMonthChange = { sowingEnd = it },
                    onStageChange = { sowingEndStage = it },
                    onConfirm = {
                        updateSowingEnd(sowingEndYear, sowingEnd, sowingEndStage)
                        sowingEndExpanded = false
                    },
                    onCancel = { sowingEndExpanded = false }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "収穫期間",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
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
                    text = if (harvestStart == "0" && harvestStartStage.isEmpty() && harvestStartYear == "0") {
                        "収穫開始期間を選択"
                    } else {
                        "${if (harvestStartYear == "0") "" else "${harvestStartYear}年"}${if (harvestStart == "0") "不明" else harvestStart}月${if (harvestStartStage.isEmpty()) "" else "(${harvestStartStage})"}"
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
                PeriodSelectionBottomSheet(
                    title = "収穫開始期間を選択",
                    selectedYear = harvestStartYear,
                    selectedMonth = harvestStart,
                    selectedStage = harvestStartStage,
                    onYearChange = { harvestStartYear = it },
                    onMonthChange = { harvestStart = it },
                    onStageChange = { harvestStartStage = it },
                    onConfirm = {
                        updateHarvestStart(harvestStartYear, harvestStart, harvestStartStage)
                        harvestStartExpanded = false
                    },
                    onCancel = { harvestStartExpanded = false }
                )
            }
        }
        
        Text("～", style = MaterialTheme.typography.bodyLarge)
        
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
                    text = if (harvestEnd == "0" && harvestEndStage.isEmpty() && harvestEndYear == "0") {
                        "収穫終了期間を選択"
                    } else {
                        "${if (harvestEndYear == "0") "" else "${harvestEndYear}年"}${if (harvestEnd == "0") "不明" else harvestEnd}月${if (harvestEndStage.isEmpty()) "" else "(${harvestEndStage})"}"
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
                PeriodSelectionBottomSheet(
                    title = "収穫終了期間を選択",
                    selectedYear = harvestEndYear,
                    selectedMonth = harvestEnd,
                    selectedStage = harvestEndStage,
                    onYearChange = { harvestEndYear = it },
                    onMonthChange = { harvestEnd = it },
                    onStageChange = { harvestEndStage = it },
                    onConfirm = {
                        updateHarvestEnd(harvestEndYear, harvestEnd, harvestEndStage)
                        harvestEndExpanded = false
                    },
                    onCancel = { harvestEndExpanded = false }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 保存・キャンセルボタン
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f)
            ) {
                Text("保存")
            }
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("キャンセル")
            }
        }
    }
}

@Composable
fun ExpirationSelectionBottomSheet(
    title: String,
    selectedYear: String,
    selectedMonth: String,
    onYearChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val currentYear = java.time.LocalDate.now().year
    val yearOptions = (currentYear..currentYear + 5).map { it.toString() }
    val monthOptions = (1..12).map { it.toString() }
    
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
            title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
        // 年選択
        Text("年", style = MaterialTheme.typography.bodyMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(yearOptions) { year ->
                                        Button(
                    onClick = { onYearChange(year) },
                                            colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedYear == year) 
                            MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                    Text(year)
                }
            }
        }
        
        // 月選択
        Text("月", style = MaterialTheme.typography.bodyMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(monthOptions) { month ->
                                        Button(
                    onClick = { onMonthChange(month) },
                                            colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMonth == month) 
                            MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                    Text("${month}月")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 確認・キャンセルボタン
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        Button(
                onClick = onConfirm,
                                            modifier = Modifier.weight(1f),
                enabled = selectedYear != "0" && selectedMonth != "0"
            ) {
                Text("確認")
            }
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("キャンセル")
            }
        }
    }
}

@Composable
fun PeriodSelectionBottomSheet(
    title: String,
    selectedYear: String,
    selectedMonth: String,
    selectedStage: String,
    onYearChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onStageChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val currentYear = java.time.LocalDate.now().year
    val yearOptions = (currentYear - 1..currentYear + 2).map { it.toString() }
    val monthOptions = (1..12).map { it.toString() }
    val stageOptions = listOf("上旬", "中旬", "下旬")
    
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
            title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
        // 年選択
        Text("年", style = MaterialTheme.typography.bodyMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(yearOptions) { year ->
                                        Button(
                    onClick = { onYearChange(year) },
                                            colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedYear == year) 
                            MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                    Text(year)
                }
            }
        }
        
        // 月選択
        Text("月", style = MaterialTheme.typography.bodyMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(monthOptions) { month ->
                                        Button(
                    onClick = { onMonthChange(month) },
                                            colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMonth == month) 
                            MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                    Text("${month}月")
                }
            }
        }
        
        // 旬選択
        Text("旬", style = MaterialTheme.typography.bodyMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(stageOptions) { stage ->
                                    Button(
                    onClick = { onStageChange(stage) },
                                        colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedStage == stage) 
                            MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                    Text(stage)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 確認・キャンセルボタン
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                    Button(
                onClick = onConfirm,
                                        modifier = Modifier.weight(1f),
                enabled = selectedYear != "0" && selectedMonth != "0" && selectedStage.isNotEmpty()
            ) {
                Text("確認")
            }
                    Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
                    ) {
                Text("キャンセル")
                    }
                }
    }
}
