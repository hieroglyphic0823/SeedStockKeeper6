package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.NumberPicker

// 分割したファイルから関数をインポート
import com.example.seedstockkeeper6.ui.components.CalendarEntryDisplay
import com.example.seedstockkeeper6.ui.components.CalendarEntryEditor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectionDialog(
    showDialog: Boolean,
    regionList: List<String>,
    ocrResult: com.example.seedstockkeeper6.model.SeedPacket?,
    croppedCalendarBitmap: android.graphics.Bitmap?,
    editingCalendarEntry: com.example.seedstockkeeper6.model.CalendarEntry?,
    onRegionSelected: (String) -> Unit,
    onStartEditing: (com.example.seedstockkeeper6.model.CalendarEntry) -> Unit,
    onUpdateEditing: (com.example.seedstockkeeper6.model.CalendarEntry) -> Unit,
    onSaveEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onDismiss: () -> Unit
) {
    android.util.Log.d("RegionSelectionDialog", "RegionSelectionDialog開始: showDialog=$showDialog, regionList=$regionList")
    
    if (showDialog) {
        var selectedRegion by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        var editedEntry by remember { mutableStateOf<com.example.seedstockkeeper6.model.CalendarEntry?>(null) }

        Dialog(
            onDismissRequest = onDismiss
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "地域区分を選択してください",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 切り抜きされたカレンダー画像を表示
                    if (croppedCalendarBitmap != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Image(
                                    bitmap = croppedCalendarBitmap.asImageBitmap(),
                                    contentDescription = "切り抜きされたカレンダー",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

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
                                        android.util.Log.d("RegionSelectionDialog", "地域選択: $region")
                                        
                                        // 地域変更時にOCR結果で期間を上書き
                                        val newRegionEntry = ocrResult?.calendar?.find { it.region == region }
                                        if (newRegionEntry != null) {
                                            onUpdateEditing(newRegionEntry)
                                            editedEntry = newRegionEntry
                                        } else {
                                            // 新しい地域の場合は空のエントリを作成
                                            editedEntry = com.example.seedstockkeeper6.model.CalendarEntry(
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
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 選択された地域のOCR結果と編集項目を表示
                    if (selectedRegion.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val selectedRegionEntry = ocrResult?.calendar?.find { it.region == selectedRegion }
                        val entryToShow = selectedRegionEntry ?: com.example.seedstockkeeper6.model.CalendarEntry(
                            region = selectedRegion,
                            sowing_start = 0,
                            sowing_start_stage = "",
                            sowing_end = 0,
                            sowing_end_stage = "",
                            harvest_start = 0,
                            harvest_start_stage = "",
                            harvest_end = 0,
                            harvest_end_stage = ""
                        )
                        
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
                                if (selectedRegionEntry != null) {
                                    // OCR結果がある場合は表示
                                    Text(
                                        "OCR結果: ${selectedRegion}",
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    // 表示モード
                                    CalendarEntryDisplay(entry = selectedRegionEntry)
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                
                                // 編集項目を表示
                                CalendarEntryEditor(
                                    entry = entryToShow,
                                    onUpdate = { updatedEntry ->
                                        // 編集内容をViewModelに反映
                                        onUpdateEditing(updatedEntry)
                                        // ローカルでも編集された値を保存
                                        editedEntry = updatedEntry
                                    },
                                    onSave = {
                                        // 保存処理
                                        onSaveEditing()
                                    },
                                    onCancel = {
                                        // キャンセル処理
                                        onCancelEditing()
                                    }
                                )
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
                            
                            if (selectedRegion.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Button(
                                    onClick = {
                                        android.util.Log.d("RegionSelectionDialog", "保存ボタンクリック: $selectedRegion")
                                        // 編集された値がある場合は、それを含めて保存
                                        if (editedEntry != null) {
                                            onUpdateEditing(editedEntry!!)
                                            // 編集された値を保存
                                            onSaveEditing()
                                        }
                                        onRegionSelected(selectedRegion)
                                        onDismiss()
                                    }
                                ) {
                                    Text("保存")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
