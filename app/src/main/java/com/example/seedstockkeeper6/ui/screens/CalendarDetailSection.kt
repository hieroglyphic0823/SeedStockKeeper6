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
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.ui.screens.StageSelector

@Composable
fun CalendarDetailSection(viewModel: SeedInputViewModel) {
    viewModel.packet.calendar.forEachIndexed { index, entry ->
        key(entry.id) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                
                // --- 播種期間 ---
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
                    Text("播種期間", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // 1行目: 播種開始月、開始旬、終了月、終了旬 (4項目)
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
                    // DisplayMode: 読み取り専用表示
                    Text(
                        text = "播種期間: ${entry.sowing_start}月${entry.sowing_start_stage} ～ ${entry.sowing_end}月${entry.sowing_end_stage}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // --- 収穫期間 ---
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
                    Text("収穫期間", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // 1行目: 収穫開始月、開始旬、終了月、終了旬 (4項目)
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
                    // DisplayMode: 読み取り専用表示
                    Text(
                        text = "収穫期間: ${entry.harvest_start}月${entry.harvest_start_stage} ～ ${entry.harvest_end}月${entry.harvest_end_stage}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

