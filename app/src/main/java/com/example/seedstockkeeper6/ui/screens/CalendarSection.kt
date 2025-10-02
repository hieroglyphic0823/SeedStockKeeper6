package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.ui.components.SeedCalendarGrouped
import com.example.seedstockkeeper6.ui.components.ExpirationSelectionBottomSheet
import com.example.seedstockkeeper6.ui.components.PeriodSelectionBottomSheet
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.utils.DateConversionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSection(viewModel: SeedInputViewModel) {
    // ボトムシートの表示状態
    var showSowingStartBottomSheet by remember { mutableStateOf(false) }
    var showSowingEndBottomSheet by remember { mutableStateOf(false) }
    var showHarvestStartBottomSheet by remember { mutableStateOf(false) }
    var showHarvestEndBottomSheet by remember { mutableStateOf(false) }
    var showExpirationBottomSheet by remember { mutableStateOf(false) }
    
    // 現在のカレンダーエントリを取得
    val currentEntry = viewModel.packet.calendar?.firstOrNull() ?: CalendarEntry()
    
    // カレンダーエントリが変更された時に再計算
    LaunchedEffect(currentEntry) {
        // カレンダーエントリが変更された時の処理
        android.util.Log.d("Calendar", "カレンダーエントリ変更検出: $currentEntry")
        android.util.Log.d("Calendar", "播種開始日: ${currentEntry.sowing_start_date}")
        android.util.Log.d("Calendar", "播種終了日: ${currentEntry.sowing_end_date}")
        android.util.Log.d("Calendar", "収穫開始日: ${currentEntry.harvest_start_date}")
        android.util.Log.d("Calendar", "収穫終了日: ${currentEntry.harvest_end_date}")
    }
    
    // 現在の日付を取得
    val currentDate = java.time.LocalDate.now()
    val currentYear = currentDate.year
    val currentMonth = currentDate.monthValue
    
    // 期間選択の状態変数（初期値を直接設定）
    var sowingStartYear by remember(currentEntry.sowing_start_date) { 
        val year = DateConversionUtils.getYearFromDate(currentEntry.sowing_start_date)
        mutableStateOf(if (year > 0) year.toString() else currentYear.toString()) 
    }
    var sowingStartMonth by remember(currentEntry.sowing_start_date) { 
        val month = DateConversionUtils.getMonthFromDate(currentEntry.sowing_start_date)
        mutableStateOf(if (month > 0) month.toString() else currentMonth.toString()) 
    }
    var sowingStartStage by remember(currentEntry.sowing_start_date) { 
        val stage = DateConversionUtils.convertDateToStage(currentEntry.sowing_start_date)
        mutableStateOf(if (stage.isNotEmpty()) stage else "上旬") 
    }
    var sowingEndYear by remember(currentEntry.sowing_end_date) { 
        val year = DateConversionUtils.getYearFromDate(currentEntry.sowing_end_date)
        mutableStateOf(if (year > 0) year.toString() else currentYear.toString()) 
    }
    var sowingEndMonth by remember(currentEntry.sowing_end_date) { 
        val month = DateConversionUtils.getMonthFromDate(currentEntry.sowing_end_date)
        mutableStateOf(if (month > 0) month.toString() else currentMonth.toString()) 
    }
    var sowingEndStage by remember(currentEntry.sowing_end_date) { 
        val stage = DateConversionUtils.convertDateToStage(currentEntry.sowing_end_date)
        mutableStateOf(if (stage.isNotEmpty()) stage else "下旬") 
    }
    var harvestStartYear by remember(currentEntry.harvest_start_date) { 
        val year = DateConversionUtils.getYearFromDate(currentEntry.harvest_start_date)
        mutableStateOf(if (year > 0) year.toString() else currentYear.toString()) 
    }
    var harvestStartMonth by remember(currentEntry.harvest_start_date) { 
        val month = DateConversionUtils.getMonthFromDate(currentEntry.harvest_start_date)
        mutableStateOf(if (month > 0) month.toString() else currentMonth.toString()) 
    }
    var harvestStartStage by remember(currentEntry.harvest_start_date) { 
        val stage = DateConversionUtils.convertDateToStage(currentEntry.harvest_start_date)
        mutableStateOf(if (stage.isNotEmpty()) stage else "上旬") 
    }
    var harvestEndYear by remember(currentEntry.harvest_end_date) { 
        val year = DateConversionUtils.getYearFromDate(currentEntry.harvest_end_date)
        mutableStateOf(if (year > 0) year.toString() else currentYear.toString()) 
    }
    var harvestEndMonth by remember(currentEntry.harvest_end_date) { 
        val month = DateConversionUtils.getMonthFromDate(currentEntry.harvest_end_date)
        mutableStateOf(if (month > 0) month.toString() else currentMonth.toString()) 
    }
    var harvestEndStage by remember(currentEntry.harvest_end_date) { 
        val stage = DateConversionUtils.convertDateToStage(currentEntry.harvest_end_date)
        mutableStateOf(if (stage.isNotEmpty()) stage else "下旬") 
    }
    var expirationYear by remember(viewModel.packet.expirationYear) { 
        mutableStateOf(viewModel.packet.expirationYear.toString()) 
    }
    var expirationMonth by remember(viewModel.packet.expirationMonth) { 
        mutableStateOf(viewModel.packet.expirationMonth.toString()) 
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.calendar),
                contentDescription = "栽培カレンダー",
                modifier = Modifier.size(24.dp)
            )
            Text(
                "栽培カレンダー",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        // 地域設定（栽培カレンダーのタイトルとカレンダー図の間）
        val currentRegion = viewModel.packet.calendar?.firstOrNull()?.region ?: ""
        
            // 地域が選択されていない場合でも、空のカレンダーエントリを作成して表示
            val calendarEntries = if (viewModel.selectedRegion.isEmpty() && (viewModel.packet.calendar?.isEmpty() != false)) {
                // 地域が選択されていない場合は空のカレンダーエントリを作成
                listOf(
                CalendarEntry(
                        region = "",
                    sowing_start_date = "",
                    sowing_end_date = "",
                    harvest_start_date = "",
                    harvest_end_date = ""
                    )
                )
            } else {
                viewModel.packet.calendar ?: emptyList()
            }
            
        // カレンダー表示
        if (calendarEntries.isNotEmpty()) {
            SeedCalendarGrouped(
                entries = calendarEntries,
                packetExpirationYear = viewModel.packet.expirationYear,
                packetExpirationMonth = viewModel.packet.expirationMonth,
                modifier = Modifier.fillMaxWidth(),
                heightDp = 114,
                previewDate = null // 通常の実行時は現在の日付を使用
            )

            // 播種期間と収穫期間の表示
            val calendarEntry = calendarEntries.firstOrNull()
            if (calendarEntry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            
                // 播種期間
                val sowingPeriod = formatDateRange(calendarEntry.sowing_start_date, calendarEntry.sowing_end_date)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    // 播種期間アイコンとラベル（縦並び）
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(40.dp)
                    ) {
                        // 播種期間アイコン（背景付き）
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.germination),
                                contentDescription = "播種期間",
                                modifier = Modifier.size(18.dp),
                                contentScale = ContentScale.Fit,
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                            )
                        }
                        
                        // 播種期間ラベル
                        Text(
                            text = "播種",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    if (viewModel.isEditMode || !viewModel.hasExistingData) {
                        // 編集モード: ボタン表示
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showSowingStartBottomSheet = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (sowingStartYear == "0" && sowingStartMonth == "0" && sowingStartStage.isEmpty()) {
                                        "不明"
                                    } else {
                                        val year = if (sowingStartYear == "0") "" else "${sowingStartYear}年"
                                        val month = if (sowingStartMonth == "0") "不明" else "${sowingStartMonth}月"
                                        val stage = if (sowingStartStage.isNotEmpty()) {
                                            "(${sowingStartStage})"
                                        } else {
                                            "(${sowingStartStage.ifEmpty { "上旬" }})"
                                        }
                                        
                                        // 1行で表示
                                        "$year$month$stage"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            Text("～", style = MaterialTheme.typography.bodyLarge)
                            Button(
                                onClick = { showSowingEndBottomSheet = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (sowingEndYear == "0" && sowingEndMonth == "0" && sowingEndStage.isEmpty()) {
                                        "不明"
                                    } else {
                                        val year = if (sowingEndYear == "0") "" else "${sowingEndYear}年"
                                        val month = if (sowingEndMonth == "0") "不明" else "${sowingEndMonth}月"
                                        val stage = if (sowingEndStage.isNotEmpty()) {
                                            "(${sowingEndStage})"
                                        } else {
                                            "(${sowingEndStage.ifEmpty { "下旬" }})"
                                        }
                                        
                                        // 1行で表示
                                        "$year$month$stage"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // 表示モード: テキスト表示
                        Text(
                            text = sowingPeriod,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 収穫期間
                val harvestPeriod = formatDateRange(calendarEntry.harvest_start_date, calendarEntry.harvest_end_date)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    // 収穫期間アイコンとラベル（縦並び）
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(40.dp)
                    ) {
                        // 収穫期間アイコン（背景付き）
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.harvest),
                                contentDescription = "収穫期間",
                                modifier = Modifier.size(18.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        
                        // 収穫期間ラベル
                        Text(
                            text = "収穫",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    if (viewModel.isEditMode || !viewModel.hasExistingData) {
                        // 編集モード: ボタン表示
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showHarvestStartBottomSheet = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (harvestStartYear == "0" && harvestStartMonth == "0" && harvestStartStage.isEmpty()) {
                                        "不明"
                                    } else {
                                        val year = if (harvestStartYear == "0") "" else "${harvestStartYear}年"
                                        val month = if (harvestStartMonth == "0") "不明" else "${harvestStartMonth}月"
                                        val stage = if (harvestStartStage.isNotEmpty()) {
                                            "(${harvestStartStage})"
                                        } else {
                                            "(${harvestStartStage.ifEmpty { "上旬" }})"
                                        }
                                        
                                        // 1行で表示
                                        "$year$month$stage"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            Text("～", style = MaterialTheme.typography.bodyMedium)
                            Button(
                                onClick = { showHarvestEndBottomSheet = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (harvestEndYear == "0" && harvestEndMonth == "0" && harvestEndStage.isEmpty()) {
                                        "不明"
                                    } else {
                                        val year = if (harvestEndYear == "0") "" else "${harvestEndYear}年"
                                        val month = if (harvestEndMonth == "0") "不明" else "${harvestEndMonth}月"
                                        val stage = if (harvestEndStage.isNotEmpty()) {
                                            "(${harvestEndStage})"
                                        } else {
                                            "(${harvestEndStage.ifEmpty { "下旬" }})"
                                        }
                                        
                                        // 1行で表示
                                        "$year$month$stage"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // 表示モード: テキスト表示
                        Text(
                            text = harvestPeriod,
                            modifier = Modifier.padding(vertical = 4.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // 有効期限表示
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                // 有効期限アイコンとラベル（縦並び）
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(40.dp)
                ) {
                    // 有効期限アイコン（背景付き）
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = "有効期限",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    // 有効期限ラベル
                    Text(
                        text = "有効\n期限",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                if (viewModel.isEditMode || !viewModel.hasExistingData) {
                    // 編集モード: ボタン表示
                    Button(
                        onClick = { showExpirationBottomSheet = true },
                        modifier = Modifier.width(160.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (expirationYear == "0" && expirationMonth == "0") {
                                "有効期限"
                            } else {
                                "${expirationYear}年${expirationMonth}月"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1
                        )
                    }
                } else {
                    // 表示モード: テキスト表示
                    Text(
                        text = if (viewModel.packet.expirationYear > 0 && viewModel.packet.expirationMonth > 0) "${viewModel.packet.expirationYear}年${viewModel.packet.expirationMonth}月" else "未設定",
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // ボトムシートの表示
    if (showSowingStartBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSowingStartBottomSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            PeriodSelectionBottomSheet(
                title = "播種開始",
                selectedYear = sowingStartYear,
                selectedMonth = sowingStartMonth,
                selectedStage = sowingStartStage,
                onYearChange = { sowingStartYear = it },
                onMonthChange = { sowingStartMonth = it },
                onStageChange = { sowingStartStage = it },
                onConfirm = {
                    val yearInt = sowingStartYear.toIntOrNull() ?: 0
                    val monthInt = sowingStartMonth.toIntOrNull() ?: 0
                    if (yearInt > 0 && monthInt > 0 && sowingStartStage.isNotEmpty()) {
                        val startDate = DateConversionUtils.convertStageToStartDate(yearInt, monthInt, sowingStartStage)
                        viewModel.updateCalendarSowingStartDate(0, startDate)
                    }
                    showSowingStartBottomSheet = false
                },
                onCancel = { showSowingStartBottomSheet = false }
            )
        }
    }
    
    if (showSowingEndBottomSheet) {
    ModalBottomSheet(
            onDismissRequest = { showSowingEndBottomSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            PeriodSelectionBottomSheet(
                title = "播種終了",
                selectedYear = sowingEndYear,
                selectedMonth = sowingEndMonth,
                selectedStage = sowingEndStage,
                onYearChange = { sowingEndYear = it },
                onMonthChange = { sowingEndMonth = it },
                onStageChange = { sowingEndStage = it },
                onConfirm = {
                    val yearInt = sowingEndYear.toIntOrNull() ?: 0
                    val monthInt = sowingEndMonth.toIntOrNull() ?: 0
                    if (yearInt > 0 && monthInt > 0 && sowingEndStage.isNotEmpty()) {
                        val endDate = DateConversionUtils.convertStageToEndDate(yearInt, monthInt, sowingEndStage)
                        viewModel.updateCalendarSowingEndDate(0, endDate)
                    }
                    showSowingEndBottomSheet = false
                },
                onCancel = { showSowingEndBottomSheet = false }
            )
        }
    }
    
    if (showHarvestStartBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHarvestStartBottomSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            PeriodSelectionBottomSheet(
                title = "収穫開始",
                selectedYear = harvestStartYear,
                selectedMonth = harvestStartMonth,
                selectedStage = harvestStartStage,
                onYearChange = { harvestStartYear = it },
                onMonthChange = { harvestStartMonth = it },
                onStageChange = { harvestStartStage = it },
                onConfirm = {
                    val yearInt = harvestStartYear.toIntOrNull() ?: 0
                    val monthInt = harvestStartMonth.toIntOrNull() ?: 0
                    if (yearInt > 0 && monthInt > 0 && harvestStartStage.isNotEmpty()) {
                        val startDate = DateConversionUtils.convertStageToStartDate(yearInt, monthInt, harvestStartStage)
                        viewModel.updateCalendarHarvestStartDate(0, startDate)
                    }
                    showHarvestStartBottomSheet = false
                },
                onCancel = { showHarvestStartBottomSheet = false },
                isHarvestPeriod = true // 収穫期間のボトムシート
            )
        }
    }
    
    if (showHarvestEndBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHarvestEndBottomSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            PeriodSelectionBottomSheet(
                title = "収穫終了",
                selectedYear = harvestEndYear,
                selectedMonth = harvestEndMonth,
                selectedStage = harvestEndStage,
                onYearChange = { harvestEndYear = it },
                onMonthChange = { harvestEndMonth = it },
                onStageChange = { harvestEndStage = it },
                onConfirm = {
                    val yearInt = harvestEndYear.toIntOrNull() ?: 0
                    val monthInt = harvestEndMonth.toIntOrNull() ?: 0
                    if (yearInt > 0 && monthInt > 0 && harvestEndStage.isNotEmpty()) {
                        val endDate = DateConversionUtils.convertStageToEndDate(yearInt, monthInt, harvestEndStage)
                        viewModel.updateCalendarHarvestEndDate(0, endDate)
                    }
                    showHarvestEndBottomSheet = false
                },
                onCancel = { showHarvestEndBottomSheet = false },
                isHarvestPeriod = true // 収穫期間のボトムシート
            )
        }
    }
    
    if (showExpirationBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExpirationBottomSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            ExpirationSelectionBottomSheet(
                title = "有効期限",
                selectedYear = expirationYear,
                selectedMonth = expirationMonth,
                onYearChange = { expirationYear = it },
                onMonthChange = { expirationMonth = it },
                onConfirm = {
                    val yearInt = expirationYear.toIntOrNull() ?: 0
                    val monthInt = expirationMonth.toIntOrNull() ?: 0
                    if (yearInt > 0 && monthInt > 0) {
                        viewModel.onExpirationYearChange(yearInt.toString())
                        viewModel.onExpirationMonthChange(monthInt.toString())
                    }
                    showExpirationBottomSheet = false
                },
                onCancel = { showExpirationBottomSheet = false }
            )
        }
    }
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

// 日付範囲を旬形式でフォーマットするヘルパー関数
private fun formatDateRange(startDate: String, endDate: String): String {
    if (startDate.isEmpty() && endDate.isEmpty()) {
        return "未設定"
    }
    
    val startFormatted = if (startDate.isNotEmpty()) {
        val year = DateConversionUtils.getYearFromDate(startDate)
        val month = DateConversionUtils.getMonthFromDate(startDate)
        val stage = DateConversionUtils.convertDateToStage(startDate)
        "${year}年${month}月(${stage})"
    } else {
        "未設定"
    }
    
    val endFormatted = if (endDate.isNotEmpty()) {
        val year = DateConversionUtils.getYearFromDate(endDate)
        val month = DateConversionUtils.getMonthFromDate(endDate)
        val stage = DateConversionUtils.convertDateToStage(endDate)
        "${year}年${month}月(${stage})"
    } else {
        "未設定"
    }
    
    return if (startDate.isEmpty() || endDate.isEmpty()) {
        if (startDate.isEmpty()) endFormatted else startFormatted
    } else {
        "$startFormatted ～ $endFormatted"
    }
}
