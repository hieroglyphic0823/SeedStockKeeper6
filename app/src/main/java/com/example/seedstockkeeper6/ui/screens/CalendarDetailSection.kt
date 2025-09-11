package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.ui.screens.StageSelector
import com.example.seedstockkeeper6.ui.components.CalendarEntryDisplay

@Composable
fun CalendarDetailSection(viewModel: SeedInputViewModel) {
    viewModel.packet.calendar.forEachIndexed { index, entry ->
        key(entry.id) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                
                // --- 播種期間 ---
                if (viewModel.isEditMode || !viewModel.hasExistingData) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = entry.sowing_start?.toString() ?: "",
                            onValueChange = {
                                viewModel.updateCalendarSowingStart(
                                    index,
                                    it.toIntOrNull() ?: 0
                                )
                            },
                            label = { Text("開始月") },
                            modifier = Modifier.width(70.dp)
                        )
                        StageSelector(
                            label = "開始旬",
                            value = entry.sowing_start_stage ?: "",
                            onValueChange = { newStage ->
                                viewModel.updateCalendarSowingStartStage(index, newStage)
                            },
                            modifier = Modifier.width(70.dp)
                        )
                        Text("～", style = MaterialTheme.typography.bodyLarge)
                        OutlinedTextField(
                            value = entry.sowing_end?.toString() ?: "",
                            onValueChange = {
                                viewModel.updateCalendarSowingEnd(
                                    index,
                                    it.toIntOrNull() ?: 0
                                )
                            },
                            label = { Text("終了月") },
                            modifier = Modifier.width(70.dp)
                        )
                        StageSelector(
                            label = "終了旬",
                            value = entry.sowing_end_stage ?: "",
                            onValueChange = { newStage ->
                                viewModel.updateCalendarSowingEndStage(index, newStage)
                            },
                            modifier = Modifier.width(70.dp)
                        )
                    }
                } else {
                    // DisplayMode: アイコン付きタイトルと期間を1行で表示
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.grain),
                                contentDescription = "播種期間",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            "播種期間: ${if (entry.sowing_start_year > 0) "${entry.sowing_start_year}年" else ""}${entry.sowing_start}月${if (entry.sowing_start_stage.isNotEmpty()) "(${entry.sowing_start_stage})" else ""} ～ ${if (entry.sowing_end_year > 0) "${entry.sowing_end_year}年" else ""}${entry.sowing_end}月${if (entry.sowing_end_stage.isNotEmpty()) "(${entry.sowing_end_stage})" else ""}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // --- 収穫期間 ---
                if (viewModel.isEditMode || !viewModel.hasExistingData) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = entry.harvest_start?.toString() ?: "",
                            onValueChange = {
                                viewModel.updateCalendarHarvestStart(
                                    index,
                                    it.toIntOrNull() ?: 0
                                )
                            },
                            label = { Text("開始月") },
                            modifier = Modifier.width(70.dp)
                        )
                        StageSelector(
                            label = "開始旬",
                            value = entry.harvest_start_stage ?: "",
                            onValueChange = { newStage ->
                                viewModel.updateCalendarHarvestStartStage(index, newStage)
                            },
                            modifier = Modifier.width(70.dp)
                        )
                        Text("～", style = MaterialTheme.typography.bodyLarge)
                        OutlinedTextField(
                            value = entry.harvest_end?.toString() ?: "",
                            onValueChange = {
                                viewModel.updateCalendarHarvestEnd(
                                    index,
                                    it.toIntOrNull() ?: 0
                                )
                            },
                            label = { Text("終了月") },
                            modifier = Modifier.width(70.dp)
                        )
                        StageSelector(
                            label = "終了旬",
                            value = entry.harvest_end_stage ?: "",
                            onValueChange = { newStage ->
                                viewModel.updateCalendarHarvestEndStage(index, newStage)
                            },
                            modifier = Modifier.width(70.dp)
                        )
                    }
                } else {
                    // DisplayMode: アイコン付きタイトルと期間を1行で表示
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.harvest),
                                contentDescription = "収穫期間",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            "収穫期間: ${if (entry.harvest_start_year > 0) "${entry.harvest_start_year}年" else ""}${entry.harvest_start}月${if (entry.harvest_start_stage.isNotEmpty()) "(${entry.harvest_start_stage})" else ""} ～ ${if (entry.harvest_end_year > 0) "${entry.harvest_end_year}年" else ""}${entry.harvest_end}月${if (entry.harvest_end_stage.isNotEmpty()) "(${entry.harvest_end_stage})" else ""}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarDetailSectionPreview() {
    SeedStockKeeper6Theme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 播種期間と収穫期間の両方がある場合
            CalendarDetailSection(
                viewModel = object : SeedInputViewModel() {
                    override val packet = SeedPacket(
                        calendar = listOf(
                            CalendarEntry(
                                region = "温暖地",
                                sowing_start_year = 2025,
                                sowing_start = 3,
                                sowing_start_stage = "上旬",
                                sowing_end_year = 2025,
                                sowing_end = 5,
                                sowing_end_stage = "下旬",
                                harvest_start_year = 2025,
                                harvest_start = 7,
                                harvest_start_stage = "上旬",
                                harvest_end_year = 2025,
                                harvest_end = 9,
                                harvest_end_stage = "下旬"
                            )
                        )
                    )
                    override val isEditMode = false
                    override val hasExistingData = true
                }
            )
            
            // 播種期間のみの場合
            CalendarDetailSection(
                viewModel = object : SeedInputViewModel() {
                    override val packet = SeedPacket(
                        calendar = listOf(
                            CalendarEntry(
                                region = "寒冷地",
                                sowing_start_year = 2025,
                                sowing_start = 4,
                                sowing_start_stage = "中旬",
                                sowing_end_year = 2025,
                                sowing_end = 6,
                                sowing_end_stage = "中旬"
                            )
                        )
                    )
                    override val isEditMode = false
                    override val hasExistingData = true
                }
            )
            
            // 収穫期間のみの場合
            CalendarDetailSection(
                viewModel = object : SeedInputViewModel() {
                    override val packet = SeedPacket(
                        calendar = listOf(
                            CalendarEntry(
                                region = "暖地",
                                harvest_start_year = 2025,
                                harvest_start = 8,
                                harvest_start_stage = "上旬",
                                harvest_end_year = 2025,
                                harvest_end = 10,
                                harvest_end_stage = "下旬"
                            )
                        )
                    )
                    override val isEditMode = false
                    override val hasExistingData = true
                }
            )
        }
    }
}

