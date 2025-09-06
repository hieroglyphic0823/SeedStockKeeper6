package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.ui.screens.StageSelector

@Composable
fun CalendarDetailSection(viewModel: SeedInputViewModel) {
    viewModel.packet.calendar.forEachIndexed { index, entry ->
        key(entry.id) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                // --- 地域情報 ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 左側: 地域名テキストと入力フィールド
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (viewModel.isEditMode || !viewModel.hasExistingData) {
                            OutlinedTextField(
                                value = entry.region ?: "",
                                onValueChange = { viewModel.updateCalendarRegion(index, it) },
                                label = { Text("地域名") },
                                modifier = Modifier.weight(2f), // 横幅を2倍に変更
                            )
                        } else {
                            Text(
                                text = entry.region ?: "未設定",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // DisplayModeの時は削除ボタンを非表示
                    if (viewModel.isEditMode || !viewModel.hasExistingData) {
                        IconButton(
                            onClick = {
                                viewModel.removeCalendarEntryAtIndex(index)
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "地域情報を削除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // --- 播種期間 ---
                Text("播種期間", style = MaterialTheme.typography.titleMedium)
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
                Text("収穫期間", style = MaterialTheme.typography.titleMedium)
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
            // DisplayModeの時は仕切り線を削除
            if (viewModel.isEditMode || !viewModel.hasExistingData) {
                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
