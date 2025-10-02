package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.NumberPicker
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.utils.DateConversionUtils
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt

@Composable
fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var offsetY by remember { mutableStateOf(0f) }
    val itemHeight = with(density) { 40.dp.toPx() }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        val index = (-offsetY / itemHeight).roundToInt()
                        val newValue = (value + index).coerceIn(range.first, range.last)
                        if (newValue != value) {
                            onValueChange(newValue)
                        }
                        offsetY = 0f
                    }
                ) { _, dragAmount ->
                    offsetY += dragAmount.y
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            // 上側の項目
            if (value > range.first) {
                Text(
                    text = (value - 1).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
            
            // 中央の選択項目
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .wrapContentHeight(Alignment.CenterVertically)
            )
            
            // 下側の項目
            if (value < range.last) {
                Text(
                    text = (value + 1).toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarEntryEditor(
    entry: CalendarEntry,
    onUpdate: (CalendarEntry) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    hideYearSelection: Boolean = false
) {
    // 現在の編集状態を保持するローカル状態
    var currentEntry by remember(entry) { mutableStateOf(entry) }
    
    // 現在の日付を取得
    val currentDate = java.time.LocalDate.now()
    val currentYear = currentDate.year
    val currentMonth = currentDate.monthValue
    
    // ローカル状態変数（旬ベースの入力）
    var sowingStartYear by remember(entry) { 
        val year = DateConversionUtils.getYearFromDate(entry.sowing_start_date)
        mutableStateOf(if (year > 0) year.toString() else currentYear.toString()) 
    }
    var sowingStart by remember(entry) { 
        val month = DateConversionUtils.getMonthFromDate(entry.sowing_start_date)
        mutableStateOf(if (month > 0) month.toString() else currentMonth.toString()) 
    }
    var sowingStartStage by remember(entry) { 
        val stage = DateConversionUtils.convertDateToStage(entry.sowing_start_date)
        mutableStateOf(if (stage.isNotEmpty()) stage else "上旬") 
    }
    var sowingEndYear by remember(entry) { 
        val year = DateConversionUtils.getYearFromDate(entry.sowing_end_date)
        mutableStateOf(if (year > 0) year.toString() else currentYear.toString()) 
    }
    var sowingEnd by remember(entry) { 
        val month = DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
        mutableStateOf(if (month > 0) month.toString() else currentMonth.toString()) 
    }
    var sowingEndStage by remember(entry) { 
        val stage = DateConversionUtils.convertDateToStage(entry.sowing_end_date)
        mutableStateOf(if (stage.isNotEmpty()) stage else "下旬") 
    }
    var harvestStartYear by remember(entry) { 
        val year = DateConversionUtils.getYearFromDate(entry.harvest_start_date)
        mutableStateOf(if (year > 0) year.toString() else currentYear.toString()) 
    }
    var harvestStart by remember(entry) { 
        val month = DateConversionUtils.getMonthFromDate(entry.harvest_start_date)
        mutableStateOf(if (month > 0) month.toString() else currentMonth.toString()) 
    }
    var harvestStartStage by remember(entry) { 
        val stage = DateConversionUtils.convertDateToStage(entry.harvest_start_date)
        mutableStateOf(if (stage.isNotEmpty()) stage else "上旬") 
    }
    var harvestEndYear by remember(entry) { 
        val year = DateConversionUtils.getYearFromDate(entry.harvest_end_date)
        mutableStateOf(if (year > 0) year.toString() else currentYear.toString()) 
    }
    var harvestEnd by remember(entry) { 
        val month = DateConversionUtils.getMonthFromDate(entry.harvest_end_date)
        mutableStateOf(if (month > 0) month.toString() else currentMonth.toString()) 
    }
    var harvestEndStage by remember(entry) { 
        val stage = DateConversionUtils.convertDateToStage(entry.harvest_end_date)
        mutableStateOf(if (stage.isNotEmpty()) stage else "下旬") 
    }
    
    // Expanded state variables for dropdowns
    var sowingStartExpanded by remember { mutableStateOf(false) }
    var sowingEndExpanded by remember { mutableStateOf(false) }
    var harvestStartExpanded by remember { mutableStateOf(false) }
    var harvestEndExpanded by remember { mutableStateOf(false) }
    
    // 年選択オプション
    val yearOptions = (currentYear - 1..currentYear + 2).map { it.toString() }

    // 日付変換と更新処理
    fun updateSowingStart(year: String, month: String, stage: String) {
        android.util.Log.d("Calendar", "updateSowingStart呼び出し: year=$year, month=$month, stage=$stage")
        android.util.Log.d("Calendar", "hideYearSelection: $hideYearSelection")
        val yearInt = if (hideYearSelection) 0 else (year.toIntOrNull() ?: 0)
        val monthInt = month.toIntOrNull() ?: 0
        android.util.Log.d("Calendar", "変換後: yearInt=$yearInt, monthInt=$monthInt")
        
        if (monthInt > 0 && stage.isNotEmpty()) {
            val startDate = if (hideYearSelection) {
                // 年選択が無効の場合は月と旬のみで日付を構築（年は後で計算）
                val monthStr = monthInt.toString().padStart(2, '0')
                val dayStr = when (stage) {
                    "上旬" -> "01"
                    "中旬" -> "15"
                    "下旬" -> "28"
                    else -> "01"
                }
                "0000-$monthStr-$dayStr" // 年は後で計算される
            } else {
                DateConversionUtils.convertStageToStartDate(yearInt, monthInt, stage)
            }
            android.util.Log.d("Calendar", "生成されたstartDate: $startDate")
            val updatedEntry = currentEntry.copy(sowing_start_date = startDate)
            android.util.Log.d("Calendar", "更新前のentry: $currentEntry")
            android.util.Log.d("Calendar", "更新後のentry: $updatedEntry")
            currentEntry = updatedEntry
            onUpdate(updatedEntry)
            android.util.Log.d("Calendar", "onUpdate呼び出し完了")
        } else {
            android.util.Log.w("Calendar", "updateSowingStart: 条件を満たさない - monthInt=$monthInt, stage=$stage")
        }
    }
    
    fun updateSowingEnd(year: String, month: String, stage: String) {
        android.util.Log.d("Calendar", "updateSowingEnd呼び出し: year=$year, month=$month, stage=$stage")
        android.util.Log.d("Calendar", "hideYearSelection: $hideYearSelection")
        val yearInt = if (hideYearSelection) 0 else (year.toIntOrNull() ?: 0)
        val monthInt = month.toIntOrNull() ?: 0
        android.util.Log.d("Calendar", "変換後: yearInt=$yearInt, monthInt=$monthInt")
        
        if (monthInt > 0 && stage.isNotEmpty()) {
            val endDate = if (hideYearSelection) {
                // 年選択が無効の場合は月と旬のみで日付を構築（年は後で計算）
                val monthStr = monthInt.toString().padStart(2, '0')
                val dayStr = when (stage) {
                    "上旬" -> "10"
                    "中旬" -> "20"
                    "下旬" -> "31"
                    else -> "10"
                }
                "0000-$monthStr-$dayStr" // 年は後で計算される
            } else {
                DateConversionUtils.convertStageToEndDate(yearInt, monthInt, stage)
            }
            android.util.Log.d("Calendar", "生成されたendDate: $endDate")
            val updatedEntry = currentEntry.copy(sowing_end_date = endDate)
            android.util.Log.d("Calendar", "更新前のentry: $currentEntry")
            android.util.Log.d("Calendar", "更新後のentry: $updatedEntry")
            currentEntry = updatedEntry
            onUpdate(updatedEntry)
            android.util.Log.d("Calendar", "onUpdate呼び出し完了")
        } else {
            android.util.Log.w("Calendar", "updateSowingEnd: 条件を満たさない - monthInt=$monthInt, stage=$stage")
        }
    }
    
    fun updateHarvestStart(year: String, month: String, stage: String) {
        android.util.Log.d("Calendar", "updateHarvestStart呼び出し: year=$year, month=$month, stage=$stage")
        val yearInt = if (hideYearSelection) 0 else (year.toIntOrNull() ?: 0)
        val monthInt = month.toIntOrNull() ?: 0
        android.util.Log.d("Calendar", "変換後: yearInt=$yearInt, monthInt=$monthInt")
        
        if (monthInt > 0 && stage.isNotEmpty()) {
            val startDate = if (hideYearSelection) {
                // 年選択が無効の場合は月と旬のみで日付を構築（年は後で計算）
                val monthStr = monthInt.toString().padStart(2, '0')
                val dayStr = when (stage) {
                    "上旬" -> "01"
                    "中旬" -> "15"
                    "下旬" -> "28"
                    else -> "01"
                }
                "0000-$monthStr-$dayStr" // 年は後で計算される
            } else {
                DateConversionUtils.convertStageToStartDate(yearInt, monthInt, stage)
            }
            android.util.Log.d("Calendar", "生成されたstartDate: $startDate")
            val updatedEntry = currentEntry.copy(harvest_start_date = startDate)
            android.util.Log.d("Calendar", "更新前のentry: $currentEntry")
            android.util.Log.d("Calendar", "更新後のentry: $updatedEntry")
            currentEntry = updatedEntry
            onUpdate(updatedEntry)
            android.util.Log.d("Calendar", "onUpdate呼び出し完了")
        } else {
            android.util.Log.w("Calendar", "updateHarvestStart: 条件を満たさない - monthInt=$monthInt, stage=$stage")
        }
    }
    
    fun updateHarvestEnd(year: String, month: String, stage: String) {
        android.util.Log.d("Calendar", "updateHarvestEnd呼び出し: year=$year, month=$month, stage=$stage")
        val yearInt = if (hideYearSelection) 0 else (year.toIntOrNull() ?: 0)
        val monthInt = month.toIntOrNull() ?: 0
        android.util.Log.d("Calendar", "変換後: yearInt=$yearInt, monthInt=$monthInt")
        
        if (monthInt > 0 && stage.isNotEmpty()) {
            val endDate = if (hideYearSelection) {
                // 年選択が無効の場合は月と旬のみで日付を構築（年は後で計算）
                val monthStr = monthInt.toString().padStart(2, '0')
                val dayStr = when (stage) {
                    "上旬" -> "10"
                    "中旬" -> "20"
                    "下旬" -> "31"
                    else -> "10"
                }
                "0000-$monthStr-$dayStr" // 年は後で計算される
            } else {
                DateConversionUtils.convertStageToEndDate(yearInt, monthInt, stage)
            }
            android.util.Log.d("Calendar", "生成されたendDate: $endDate")
            val updatedEntry = currentEntry.copy(harvest_end_date = endDate)
            android.util.Log.d("Calendar", "更新前のentry: $currentEntry")
            android.util.Log.d("Calendar", "更新後のentry: $updatedEntry")
            currentEntry = updatedEntry
            onUpdate(updatedEntry)
            android.util.Log.d("Calendar", "onUpdate呼び出し完了")
        } else {
            android.util.Log.w("Calendar", "updateHarvestEnd: 条件を満たさない - monthInt=$monthInt, stage=$stage")
        }
    }

    Column(
        modifier = Modifier.padding(top = 8.dp)
    ) {
        // 播種期間ラベル（アイコン付き）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            // 播種期間アイコン（背景付き）
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.grain),
                    contentDescription = "播種期間",
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                "播種期間",
                style = MaterialTheme.typography.titleSmall
            )
        }
        
        // 播種期間（開始・終了を横並び）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 播種開始
            Button(
                onClick = { sowingStartExpanded = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    text = if (sowingStart == "0" && sowingStartStage.isEmpty()) {
                        "播種開始"
                    } else {
                        "${if (sowingStart == "0") "不明" else sowingStart}月${if (sowingStartStage.isEmpty()) "" else "(${sowingStartStage})"}"
                    }
                )
            }
            
            Text("～", style = MaterialTheme.typography.bodyLarge)
            
            // 播種終了
            Button(
                onClick = { sowingEndExpanded = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    text = if (sowingEnd == "0" && sowingEndStage.isEmpty()) {
                        "播種終了"
                    } else {
                        "${if (sowingEnd == "0") "不明" else sowingEnd}月${if (sowingEndStage.isEmpty()) "" else "(${sowingEndStage})"}"
                    }
                )
            }
        }
        
        // 播種開始選択ボトムシート
        if (sowingStartExpanded) {
            ModalBottomSheet(
                onDismissRequest = { sowingStartExpanded = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                MonthStageSelectionBottomSheet(
                    title = "播種開始",
                    selectedMonth = sowingStart.toIntOrNull() ?: 0,
                    selectedStage = sowingStartStage,
                    onMonthChange = { sowingStart = it.toString() },
                    onStageChange = { sowingStartStage = it },
                    onConfirm = {
                        updateSowingStart("0", sowingStart, sowingStartStage)
                        sowingStartExpanded = false
                    },
                    onCancel = { sowingStartExpanded = false }
                )
            }
        }
        
        // 播種終了選択ボトムシート
        if (sowingEndExpanded) {
            ModalBottomSheet(
                onDismissRequest = { sowingEndExpanded = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                MonthStageSelectionBottomSheet(
                    title = "播種終了",
                    selectedMonth = sowingEnd.toIntOrNull() ?: 0,
                    selectedStage = sowingEndStage,
                    onMonthChange = { sowingEnd = it.toString() },
                    onStageChange = { sowingEndStage = it },
                    onConfirm = {
                        updateSowingEnd("0", sowingEnd, sowingEndStage)
                        sowingEndExpanded = false
                    },
                    onCancel = { sowingEndExpanded = false }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 収穫期間ラベル（アイコン付き）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            // 収穫期間アイコン（背景付き）
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.harvest),
                    contentDescription = "収穫期間",
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                "収穫期間",
                style = MaterialTheme.typography.titleSmall
            )
        }
        
        // 収穫期間（開始・終了を横並び）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 収穫開始
            Button(
                onClick = { harvestStartExpanded = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    text = if (harvestStart == "0" && harvestStartStage.isEmpty()) {
                        "収穫開始"
                    } else {
                        "${if (harvestStart == "0") "不明" else harvestStart}月${if (harvestStartStage.isEmpty()) "" else "(${harvestStartStage})"}"
                    }
                )
            }
            
            Text("～", style = MaterialTheme.typography.bodyLarge)
            
            // 収穫終了
            Button(
                onClick = { harvestEndExpanded = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    text = if (harvestEnd == "0" && harvestEndStage.isEmpty()) {
                        "収穫終了"
                    } else {
                        "${if (harvestEnd == "0") "不明" else harvestEnd}月${if (harvestEndStage.isEmpty()) "" else "(${harvestEndStage})"}"
                    }
                )
            }
        }
        
        // 収穫開始選択ボトムシート
        if (harvestStartExpanded) {
            ModalBottomSheet(
                onDismissRequest = { harvestStartExpanded = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                MonthStageSelectionBottomSheet(
                    title = "収穫開始",
                    selectedMonth = harvestStart.toIntOrNull() ?: 0,
                    selectedStage = harvestStartStage,
                    onMonthChange = { harvestStart = it.toString() },
                    onStageChange = { harvestStartStage = it },
                    onConfirm = {
                        updateHarvestStart("0", harvestStart, harvestStartStage)
                        harvestStartExpanded = false
                    },
                    onCancel = { harvestStartExpanded = false },
                    isHarvestPeriod = true // 収穫期間のボトムシート
                )
            }
        }
        
        // 収穫終了選択ボトムシート
        if (harvestEndExpanded) {
            ModalBottomSheet(
                onDismissRequest = { harvestEndExpanded = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                MonthStageSelectionBottomSheet(
                    title = "収穫終了",
                    selectedMonth = harvestEnd.toIntOrNull() ?: 0,
                    selectedStage = harvestEndStage,
                    onMonthChange = { harvestEnd = it.toString() },
                    onStageChange = { harvestEndStage = it },
                    onConfirm = {
                        updateHarvestEnd("0", harvestEnd, harvestEndStage)
                        harvestEndExpanded = false
                    },
                    onCancel = { harvestEndExpanded = false },
                    isHarvestPeriod = true // 収穫期間のボトムシート
                )
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
                    
        // 年と月を横並びで表示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 年選択（NumberPicker）
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AndroidView(
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = currentYear
                            maxValue = currentYear + 5
                            val yearOptions = (currentYear..(currentYear + 5)).map { "${it}年" }.toTypedArray()
                            setDisplayedValues(yearOptions)
                            value = selectedYear.toIntOrNull() ?: currentYear
                            setOnValueChangedListener { _, _, newVal ->
                                onYearChange((currentYear + newVal - currentYear).toString())
                            }
                            // 中央揃えの設定
                            try {
                                val field = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
                                field.isAccessible = true
                                val paint = field.get(this) as android.graphics.Paint
                                paint.textAlign = android.graphics.Paint.Align.CENTER
                            } catch (e: Exception) {
                                // リフレクションが失敗した場合は無視
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
            
            // 月選択（NumberPicker）
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AndroidView(
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = 1
                            maxValue = 12
                            val monthOptions = (1..12).map { "${it}月" }.toTypedArray()
                            setDisplayedValues(monthOptions)
                            value = selectedMonth.toIntOrNull() ?: 1
                            setOnValueChangedListener { _, _, newVal ->
                                onMonthChange(newVal.toString())
                            }
                            // 中央揃えの設定
                            try {
                                val field = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
                                field.isAccessible = true
                                val paint = field.get(this) as android.graphics.Paint
                                paint.textAlign = android.graphics.Paint.Align.CENTER
                            } catch (e: Exception) {
                                // リフレクションが失敗した場合は無視
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 確認・キャンセルボタン
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("キャンセル")
            }
                                        Button(
                onClick = onConfirm,
                                            modifier = Modifier.weight(1f),
                enabled = selectedYear != "0" && selectedMonth != "0",
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
    onCancel: () -> Unit,
    confirmButtonColor: androidx.compose.ui.graphics.Color? = null,
    hideYearSelection: Boolean = false,
    isHarvestPeriod: Boolean = false // 収穫期間かどうかを判別
) {
    val currentYear = java.time.LocalDate.now().year
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
                    
        // 年と月を横並びで表示（年選択が無効の場合は月のみ）
        if (hideYearSelection) {
            // 月選択のみ
            NumberPicker(
                value = selectedMonth.toIntOrNull() ?: 1,
                range = 1..12,
                onValueChange = { onMonthChange(it.toString()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        } else {
            // 年と月を横並びで表示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 年選択（NumberPicker）
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    NumberPicker(
                        value = selectedYear.toIntOrNull() ?: currentYear,
                        range = (currentYear - 1)..(currentYear + 2),
                        onValueChange = { onYearChange(it.toString()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
                
                // 月選択（NumberPicker）
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    NumberPicker(
                        value = selectedMonth.toIntOrNull() ?: 1,
                        range = 1..12,
                        onValueChange = { onMonthChange(it.toString()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            }
        }
        
        // 旬選択
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            stageOptions.forEach { stage ->
                Button(
                    onClick = { onStageChange(stage) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedStage == stage) {
                            if (isHarvestPeriod) 
                                MaterialTheme.colorScheme.secondaryContainer 
                            else 
                                MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Text(
                        stage,
                        color = if (selectedStage == stage) {
                            if (isHarvestPeriod) 
                                MaterialTheme.colorScheme.onSecondaryContainer 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
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
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                    ) {
                Text("キャンセル")
                    }
                                    Button(
                onClick = onConfirm,
                                        modifier = Modifier.weight(1f),
                enabled = if (hideYearSelection) {
                    selectedMonth != "0" && selectedStage.isNotEmpty()
                } else {
                    selectedYear != "0" && selectedMonth != "0" && selectedStage.isNotEmpty()
                },
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

@Composable
fun MonthStageSelectionBottomSheet(
    title: String,
    selectedMonth: Int,
    selectedStage: String,
    onMonthChange: (Int) -> Unit,
    onStageChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isHarvestPeriod: Boolean = false // 収穫期間かどうかを判別
) {
    var currentMonth by remember { mutableStateOf(selectedMonth) }
    var currentStage by remember { mutableStateOf(selectedStage) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 月選択（NumberPicker）
        Text(
            text = "月",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Material3のテーマカラーを取得
        val colorScheme = MaterialTheme.colorScheme
        
        AndroidView(
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = 0
                    maxValue = 12
                    setDisplayedValues(arrayOf("不明", "1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"))
                    value = currentMonth
                    setOnValueChangedListener { _, _, newVal ->
                        currentMonth = newVal
                        onMonthChange(newVal)
                    }
                    
                    // NumberPickerの色をMaterial3のテーマカラーに設定
                    try {
                        // テキスト色を設定
                        setTextColor(colorScheme.onSurface.toArgb())
                        // 背景色を設定
                        setBackgroundColor(colorScheme.surface.toArgb())
                        
                        android.util.Log.d("RegionDialogNumberPicker", "=== NumberPicker色設定完了 ===")
                        android.util.Log.d("RegionDialogNumberPicker", "テキスト色: #${Integer.toHexString(colorScheme.onSurface.toArgb())}")
                        android.util.Log.d("RegionDialogNumberPicker", "背景色: #${Integer.toHexString(colorScheme.surface.toArgb())}")
                        android.util.Log.d("RegionDialogNumberPicker", "現在の値: $currentMonth")
                        android.util.Log.d("RegionDialogNumberPicker", "表示値: ${arrayOf("不明", "1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")[currentMonth]}")
                    } catch (e: Exception) {
                        android.util.Log.e("RegionDialogNumberPicker", "色設定エラー: ${e.message}")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 期間選択（上旬、中旬、下旬）
        Text(
            text = "期間",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("上旬", "中旬", "下旬").forEach { stage ->
                Button(
                    onClick = { 
                        currentStage = stage
                        onStageChange(stage)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentStage == stage) {
                            if (isHarvestPeriod) 
                                MaterialTheme.colorScheme.secondaryContainer 
                            else 
                                MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        contentColor = if (currentStage == stage) {
                            if (isHarvestPeriod) 
                                MaterialTheme.colorScheme.onSecondaryContainer 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                ) {
                    Text(stage)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // ボタン（OKを右側に配置）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("キャンセル")
            }
            
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("OK")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
