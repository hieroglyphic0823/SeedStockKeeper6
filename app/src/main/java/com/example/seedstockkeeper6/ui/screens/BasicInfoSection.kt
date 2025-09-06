package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

@Composable
fun BasicInfoSection(viewModel: SeedInputViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "基本情報",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 商品名
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.productName,
                onValueChange = viewModel::onProductNameChange,
                label = { Text("商品名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        } else {
            // DisplayMode: 読み取り専用表示
            Text(
                text = "商品名: ${viewModel.packet.productName.ifEmpty { "未設定" }}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 品種
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.variety,
                onValueChange = viewModel::onVarietyChange,
                label = { Text("品種") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        } else {
            // DisplayMode: 読み取り専用表示
            Text(
                text = "品種: ${viewModel.packet.variety.ifEmpty { "未設定" }}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 科名
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            FamilySelector(
                value = viewModel.packet.family,
                onValueChange = viewModel::onFamilyChange
            )
        } else {
            // DisplayMode: 読み取り専用表示
            Text(
                text = "科名: ${viewModel.packet.family.ifEmpty { "未設定" }}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 地域設定
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            RegionSelector(
                value = viewModel.packet.calendar?.firstOrNull()?.region ?: "",
                onValueChange = { region ->
                    // 地域を更新
                    val currentCalendar = viewModel.packet.calendar?.firstOrNull()
                    if (currentCalendar != null) {
                        val updatedCalendar = currentCalendar.copy(region = region)
                        viewModel.updateCalendarEntry(0, updatedCalendar)
                    } else {
                        // カレンダーエントリがない場合は新規作成
                        val newCalendar = com.example.seedstockkeeper6.model.CalendarEntry(
                            region = region,
                            sowing_start = 0,
                            sowing_start_stage = "",
                            sowing_end = 0,
                            sowing_end_stage = "",
                            harvest_start = 0,
                            harvest_start_stage = "",
                            harvest_end = 0,
                            harvest_end_stage = ""
                        )
                        viewModel.updateCalendarEntry(0, newCalendar)
                    }
                }
            )
        } else {
            // DisplayMode: 読み取り専用表示
            val region = viewModel.packet.calendar?.firstOrNull()?.region ?: "未設定"
            val regionColor = getRegionColor(region)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = regionColor.copy(alpha = 0.1f),
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
                                color = regionColor,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    Text(
                        text = region,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (region == "未設定") 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 有効期限
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // 2つの間隔
            ) {
                OutlinedTextField(
                    value = viewModel.packet.expirationYear.toString(),
                    onValueChange = viewModel::onExpirationYearChange,
                    label = { Text("有効期限(年)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                OutlinedTextField(
                    value = viewModel.packet.expirationMonth.toString(),
                    onValueChange = viewModel::onExpirationMonthChange,
                    label = { Text("有効期限(月)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        } else {
            // DisplayMode: 読み取り専用表示
            Text(
                text = "有効期限: ${viewModel.packet.expirationYear}年 ${viewModel.packet.expirationMonth}月",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelector(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRegionBottomSheet by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Public,
                contentDescription = "地域設定",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "地域設定",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // 編集モード時は色付きボタン
        Button(
            onClick = { showRegionBottomSheet = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = getRegionColor(value),
                contentColor = Color.White
            ),
            shape = MaterialTheme.shapes.large,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = value.ifEmpty { "地域を選択" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
    
    // 地域選択ボトムシート
    if (showRegionBottomSheet) {
        RegionSelectionBottomSheet(
            selectedRegion = value,
            onRegionSelected = { region ->
                onValueChange(region)
                showRegionBottomSheet = false
            },
            onDismiss = { showRegionBottomSheet = false }
        )
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
