package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    // ViewModelから直接参照
    val calendarEntry = viewModel.packet.calendar?.firstOrNull() ?: CalendarEntry()
    val currentRegion = calendarEntry.region
    val expirationYear = viewModel.packet.expirationYear
    val expirationMonth = viewModel.packet.expirationMonth
    
    // ボトムシート表示状態
    var showSowingStartSheet by remember { mutableStateOf(false) }
    var showSowingEndSheet by remember { mutableStateOf(false) }
    var showHarvestStartSheet by remember { mutableStateOf(false) }
    var showHarvestEndSheet by remember { mutableStateOf(false) }
    var showExpirationSheet by remember { mutableStateOf(false) }
    
    // ボトムシート内の一時的な選択値
    val currentDate = java.time.LocalDate.now()
    val currentYear = currentDate.year
    val currentMonth = currentDate.monthValue
    
    var tempSowingStartYear by remember { mutableStateOf("") }
    var tempSowingStartMonth by remember { mutableStateOf("") }
    var tempSowingStartStage by remember { mutableStateOf("") }
    var tempSowingEndYear by remember { mutableStateOf("") }
    var tempSowingEndMonth by remember { mutableStateOf("") }
    var tempSowingEndStage by remember { mutableStateOf("") }
    var tempHarvestStartYear by remember { mutableStateOf("") }
    var tempHarvestStartMonth by remember { mutableStateOf("") }
    var tempHarvestStartStage by remember { mutableStateOf("") }
    var tempHarvestEndYear by remember { mutableStateOf("") }
    var tempHarvestEndMonth by remember { mutableStateOf("") }
    var tempHarvestEndStage by remember { mutableStateOf("") }
    var tempExpirationYear by remember { mutableStateOf("") }
    var tempExpirationMonth by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // タイトル行
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.calendar),
                contentDescription = "種暦",
                modifier = Modifier.size(24.dp)
            )
            Text(
                "種暦",
                style = MaterialTheme.typography.titleLarge
            )
        }

        // 地域表示
        if (currentRegion.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(getRegionColor(currentRegion))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    currentRegion,
                    style = MaterialTheme.typography.titleMedium,
                    color = getRegionColor(currentRegion)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }

        // カレンダー表示本体
        viewModel.packet.calendar?.let { entries ->
            if (entries.isNotEmpty()) {
                SeedCalendarGrouped(
                    entries = entries,
                    packetExpirationYear = expirationYear,
                    packetExpirationMonth = expirationMonth,
                    modifier = Modifier.fillMaxWidth(),
                    heightDp = 114,
                    previewDate = null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 播種期間
        PeriodRow(
            label = "播種",
            color = MaterialTheme.colorScheme.primaryContainer,
            iconRes = R.drawable.grain,
            startText = formatDate(calendarEntry.sowing_start_date),
            endText = formatDate(calendarEntry.sowing_end_date),
            onStartClick = {
                val year = DateConversionUtils.getYearFromDate(calendarEntry.sowing_start_date)
                val month = DateConversionUtils.getMonthFromDate(calendarEntry.sowing_start_date)
                val stage = DateConversionUtils.convertDateToStage(calendarEntry.sowing_start_date)
                tempSowingStartYear = if (year > 0) year.toString() else currentYear.toString()
                tempSowingStartMonth = if (month > 0) month.toString() else currentMonth.toString()
                tempSowingStartStage = if (stage.isNotEmpty()) stage else "上旬"
                showSowingStartSheet = true
            },
            onEndClick = {
                val year = DateConversionUtils.getYearFromDate(calendarEntry.sowing_end_date)
                val month = DateConversionUtils.getMonthFromDate(calendarEntry.sowing_end_date)
                val stage = DateConversionUtils.convertDateToStage(calendarEntry.sowing_end_date)
                tempSowingEndYear = if (year > 0) year.toString() else currentYear.toString()
                tempSowingEndMonth = if (month > 0) month.toString() else currentMonth.toString()
                tempSowingEndStage = if (stage.isNotEmpty()) stage else "下旬"
                showSowingEndSheet = true
            },
            editable = viewModel.isEditMode || !viewModel.hasExistingData
        )

        // 収穫期間
        Spacer(modifier = Modifier.height(12.dp))
        PeriodRow(
            label = "収穫",
            color = MaterialTheme.colorScheme.secondaryContainer,
            iconRes = R.drawable.harvest,
            startText = formatDate(calendarEntry.harvest_start_date),
            endText = formatDate(calendarEntry.harvest_end_date),
            onStartClick = {
                val year = DateConversionUtils.getYearFromDate(calendarEntry.harvest_start_date)
                val month = DateConversionUtils.getMonthFromDate(calendarEntry.harvest_start_date)
                val stage = DateConversionUtils.convertDateToStage(calendarEntry.harvest_start_date)
                tempHarvestStartYear = if (year > 0) year.toString() else currentYear.toString()
                tempHarvestStartMonth = if (month > 0) month.toString() else currentMonth.toString()
                tempHarvestStartStage = if (stage.isNotEmpty()) stage else "上旬"
                showHarvestStartSheet = true
            },
            onEndClick = {
                val year = DateConversionUtils.getYearFromDate(calendarEntry.harvest_end_date)
                val month = DateConversionUtils.getMonthFromDate(calendarEntry.harvest_end_date)
                val stage = DateConversionUtils.convertDateToStage(calendarEntry.harvest_end_date)
                tempHarvestEndYear = if (year > 0) year.toString() else currentYear.toString()
                tempHarvestEndMonth = if (month > 0) month.toString() else currentMonth.toString()
                tempHarvestEndStage = if (stage.isNotEmpty()) stage else "下旬"
                showHarvestEndSheet = true
            },
            editable = viewModel.isEditMode || !viewModel.hasExistingData
        )

        // 有効期限
        Spacer(modifier = Modifier.height(12.dp))
        ExpirationRow(
            expirationYear = expirationYear,
            expirationMonth = expirationMonth,
            onClick = {
                tempExpirationYear = if (expirationYear > 0) expirationYear.toString() else ""
                tempExpirationMonth = if (expirationMonth > 0) expirationMonth.toString() else ""
                showExpirationSheet = true
            },
            editable = viewModel.isEditMode || !viewModel.hasExistingData
        )
    }

    // --- ボトムシート群 ---
    if (showSowingStartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSowingStartSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            PeriodSelectionBottomSheet(
                title = "播種開始",
                selectedYear = tempSowingStartYear,
                selectedMonth = tempSowingStartMonth,
                selectedStage = tempSowingStartStage,
                onYearChange = { tempSowingStartYear = it },
                onMonthChange = { tempSowingStartMonth = it },
                onStageChange = { tempSowingStartStage = it },
                onConfirm = {
                    val yearInt = tempSowingStartYear.toIntOrNull() ?: return@PeriodSelectionBottomSheet
                    val monthInt = tempSowingStartMonth.toIntOrNull() ?: return@PeriodSelectionBottomSheet
                    if (yearInt > 0 && monthInt > 0 && tempSowingStartStage.isNotEmpty()) {
                        val date = DateConversionUtils.convertStageToStartDate(yearInt, monthInt, tempSowingStartStage)
                        viewModel.updateCalendarSowingStartDate(0, date)
                    }
                    showSowingStartSheet = false
                },
                onCancel = { showSowingStartSheet = false }
            )
        }
    }

    if (showSowingEndSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSowingEndSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            PeriodSelectionBottomSheet(
                title = "播種終了",
                selectedYear = tempSowingEndYear,
                selectedMonth = tempSowingEndMonth,
                selectedStage = tempSowingEndStage,
                onYearChange = { tempSowingEndYear = it },
                onMonthChange = { tempSowingEndMonth = it },
                onStageChange = { tempSowingEndStage = it },
                onConfirm = {
                    val yearInt = tempSowingEndYear.toIntOrNull() ?: return@PeriodSelectionBottomSheet
                    val monthInt = tempSowingEndMonth.toIntOrNull() ?: return@PeriodSelectionBottomSheet
                    if (yearInt > 0 && monthInt > 0 && tempSowingEndStage.isNotEmpty()) {
                        val date = DateConversionUtils.convertStageToEndDate(yearInt, monthInt, tempSowingEndStage)
                        viewModel.updateCalendarSowingEndDate(0, date)
                    }
                    showSowingEndSheet = false
                },
                onCancel = { showSowingEndSheet = false }
            )
        }
    }

    if (showHarvestStartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHarvestStartSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            PeriodSelectionBottomSheet(
                title = "収穫開始",
                selectedYear = tempHarvestStartYear,
                selectedMonth = tempHarvestStartMonth,
                selectedStage = tempHarvestStartStage,
                onYearChange = { tempHarvestStartYear = it },
                onMonthChange = { tempHarvestStartMonth = it },
                onStageChange = { tempHarvestStartStage = it },
                onConfirm = {
                    val yearInt = tempHarvestStartYear.toIntOrNull() ?: return@PeriodSelectionBottomSheet
                    val monthInt = tempHarvestStartMonth.toIntOrNull() ?: return@PeriodSelectionBottomSheet
                    if (yearInt > 0 && monthInt > 0 && tempHarvestStartStage.isNotEmpty()) {
                        val date = DateConversionUtils.convertStageToStartDate(yearInt, monthInt, tempHarvestStartStage)
                        viewModel.updateCalendarHarvestStartDate(0, date)
                    }
                    showHarvestStartSheet = false
                },
                onCancel = { showHarvestStartSheet = false },
                isHarvestPeriod = true
            )
        }
    }

    if (showHarvestEndSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHarvestEndSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            PeriodSelectionBottomSheet(
                title = "収穫終了",
                selectedYear = tempHarvestEndYear,
                selectedMonth = tempHarvestEndMonth,
                selectedStage = tempHarvestEndStage,
                onYearChange = { tempHarvestEndYear = it },
                onMonthChange = { tempHarvestEndMonth = it },
                onStageChange = { tempHarvestEndStage = it },
                onConfirm = {
                    val yearInt = tempHarvestEndYear.toIntOrNull() ?: return@PeriodSelectionBottomSheet
                    val monthInt = tempHarvestEndMonth.toIntOrNull() ?: return@PeriodSelectionBottomSheet
                    if (yearInt > 0 && monthInt > 0 && tempHarvestEndStage.isNotEmpty()) {
                        val date = DateConversionUtils.convertStageToEndDate(yearInt, monthInt, tempHarvestEndStage)
                        viewModel.updateCalendarHarvestEndDate(0, date)
                    }
                    showHarvestEndSheet = false
                },
                onCancel = { showHarvestEndSheet = false },
                isHarvestPeriod = true
            )
        }
    }

    if (showExpirationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExpirationSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            ExpirationSelectionBottomSheet(
                title = "有効期限",
                selectedYear = tempExpirationYear,
                selectedMonth = tempExpirationMonth,
                onYearChange = { tempExpirationYear = it },
                onMonthChange = { tempExpirationMonth = it },
                onConfirm = {
                    val yearInt = tempExpirationYear.toIntOrNull() ?: return@ExpirationSelectionBottomSheet
                    val monthInt = tempExpirationMonth.toIntOrNull() ?: return@ExpirationSelectionBottomSheet
                    if (yearInt > 0 && monthInt > 0) {
                        viewModel.onExpirationChanged(yearInt, monthInt)
                    }
                    showExpirationSheet = false
                },
                onCancel = { showExpirationSheet = false }
            )
        }
    }
}

// ---- サブUI ----

@Composable
private fun PeriodRow(
    label: String,
    color: Color,
    iconRes: Int,
    startText: String,
    endText: String,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    editable: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (editable) {
            // Material3のルールに従ったon色を取得
            val onColor = when (color) {
                MaterialTheme.colorScheme.primaryContainer -> MaterialTheme.colorScheme.onPrimaryContainer
                MaterialTheme.colorScheme.secondaryContainer -> MaterialTheme.colorScheme.onSecondaryContainer
                MaterialTheme.colorScheme.tertiaryContainer -> MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            }
            Button(
                onClick = onStartClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color,
                    contentColor = onColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    startText.ifEmpty { "${label}開始" },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text("～", style = MaterialTheme.typography.bodyLarge)
            Button(
                onClick = onEndClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color,
                    contentColor = onColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    endText.ifEmpty { "${label}終了" },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Text(
                if (startText.isNotEmpty() && endText.isNotEmpty()) "$startText ～ $endText"
                else if (startText.isNotEmpty()) startText
                else if (endText.isNotEmpty()) endText
                else "未設定",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ExpirationRow(
    expirationYear: Int,
    expirationMonth: Int,
    onClick: () -> Unit,
    editable: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "期限",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (editable) {
            Button(
                onClick = onClick,
                modifier = Modifier.width(160.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (expirationYear > 0 && expirationMonth > 0) "${expirationYear}年${expirationMonth}月" else "有効期限",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        } else {
            Text(
                if (expirationYear > 0 && expirationMonth > 0) "${expirationYear}年${expirationMonth}月" else "未設定",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---- Utility ----

private fun formatDate(date: String): String {
    if (date.isEmpty()) return ""
    return try {
        val d = java.time.LocalDate.parse(date, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        val stage = DateConversionUtils.convertDayToStage(d.dayOfMonth)
        "${d.year}年${d.monthValue}月(${stage})"
    } catch (e: Exception) {
        date
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
