package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectionDialog(
    showDialog: Boolean,
    regionList: List<String>,
    ocrResult: com.example.seedstockkeeper6.model.SeedPacket?,
    onRegionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        var selectedRegion by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "OCRの読み込み結果から地域区分を選択してください",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 地域選択のコンボボックス
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedRegion,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("地域を選択") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            regionList.forEach { region ->
                                DropdownMenuItem(
                                    text = { Text(region) },
                                    onClick = {
                                        selectedRegion = region
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // 選択された地域のOCR結果を表示
                    if (selectedRegion.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val selectedRegionEntry = ocrResult?.calendar?.find { it.region == selectedRegion }
                        if (selectedRegionEntry != null) {
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
                                    Text(
                                        "OCR結果: ${selectedRegion}",
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    // 播種期間
                                    if (selectedRegionEntry.sowing_start > 0 || selectedRegionEntry.sowing_end > 0) {
                                        Text(
                                            "播種期間: ${selectedRegionEntry.sowing_start}月${if (selectedRegionEntry.sowing_start_stage.isNotEmpty()) "(${selectedRegionEntry.sowing_start_stage})" else ""} ～ ${selectedRegionEntry.sowing_end}月${if (selectedRegionEntry.sowing_end_stage.isNotEmpty()) "(${selectedRegionEntry.sowing_end_stage})" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    // 収穫期間
                                    if (selectedRegionEntry.harvest_start > 0 || selectedRegionEntry.harvest_end > 0) {
                                        Text(
                                            "収穫期間: ${selectedRegionEntry.harvest_start}月${if (selectedRegionEntry.harvest_start_stage.isNotEmpty()) "(${selectedRegionEntry.harvest_start_stage})" else ""} ～ ${selectedRegionEntry.harvest_end}月${if (selectedRegionEntry.harvest_end_stage.isNotEmpty()) "(${selectedRegionEntry.harvest_end_stage})" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    // データがない場合
                                    if ((selectedRegionEntry.sowing_start == 0 && selectedRegionEntry.sowing_end == 0) &&
                                        (selectedRegionEntry.harvest_start == 0 && selectedRegionEntry.harvest_end == 0)) {
                                        Text(
                                            "播種・収穫期間の情報がありません",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ボタン
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text("キャンセル")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                if (selectedRegion.isNotEmpty()) {
                                    onRegionSelected(selectedRegion)
                                    onDismiss()
                                }
                            },
                            enabled = selectedRegion.isNotEmpty()
                        ) {
                            Text("選択")
                        }
                    }
                }
            }
        }
    }
}
