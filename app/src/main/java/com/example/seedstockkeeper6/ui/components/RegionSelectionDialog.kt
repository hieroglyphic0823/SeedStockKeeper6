package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import com.example.seedstockkeeper6.R

// 分割したファイルから関数をインポート
import com.example.seedstockkeeper6.ui.components.CalendarEntryDisplay
import com.example.seedstockkeeper6.ui.components.CalendarEntryEditor

// 地域ごとの色定義
private fun getRegionColor(region: String): Color {
    return when (region) {
        "寒地" -> Color(0xFF1A237E) // 紺
        "寒冷地" -> Color(0xFF1976D2) // 青
        "温暖地" -> Color(0xFFFF9800) // オレンジ
        "暖地" -> Color(0xFFE91E63) // ピンク
        else -> Color(0xFF757575) // グレー（デフォルト）
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectionDialog(
    showDialog: Boolean,
    regionList: List<String>,
    ocrResult: com.example.seedstockkeeper6.model.SeedPacket?,
    croppedCalendarBitmap: android.graphics.Bitmap?,
    editingCalendarEntry: com.example.seedstockkeeper6.model.CalendarEntry?,
    defaultRegion: String = "", // 農園情報の地域を初期値として使用
    onRegionSelected: (String) -> Unit,
    onStartEditing: (com.example.seedstockkeeper6.model.CalendarEntry) -> Unit,
    onUpdateEditing: (com.example.seedstockkeeper6.model.CalendarEntry) -> Unit,
    onSaveEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onDismiss: () -> Unit
) {
    android.util.Log.d("RegionSelectionDialog", "RegionSelectionDialog開始: showDialog=$showDialog, regionList=$regionList")
    
    // ウィンドウサイズとダイアログサイズをLog出力
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val windowWidthDp = configuration.screenWidthDp
    val windowHeightDp = configuration.screenHeightDp
    val dialogHeightDp = windowHeightDp * 0.9f
    
    // ダイアログの実際の横幅を計算
    val dialogPadding = 16.dp // Cardのpadding
    val dialogContentPadding = 20.dp // Columnのpadding
    val totalDialogPadding = dialogPadding + dialogContentPadding
    val dialogActualWidthDp = windowWidthDp - totalDialogPadding.value
    
    LaunchedEffect(showDialog) {
        if (showDialog) {
            android.util.Log.d("RegionSelectionDialog", "=== ダイアログサイズ計算 ===")
            android.util.Log.d("RegionSelectionDialog", "ウィンドウ幅: ${windowWidthDp}dp")
            android.util.Log.d("RegionSelectionDialog", "ウィンドウ高: ${windowHeightDp}dp")
            android.util.Log.d("RegionSelectionDialog", "ダイアログ高(90%): ${dialogHeightDp}dp")
            android.util.Log.d("RegionSelectionDialog", "ダイアログ高(px): ${density.run { dialogHeightDp.dp.toPx() }}px")
            android.util.Log.d("RegionSelectionDialog", "Card padding: ${dialogPadding.value}dp")
            android.util.Log.d("RegionSelectionDialog", "Column padding: ${dialogContentPadding.value}dp")
            android.util.Log.d("RegionSelectionDialog", "合計padding: ${totalDialogPadding.value}dp")
            android.util.Log.d("RegionSelectionDialog", "ダイアログ実際の幅: ${dialogActualWidthDp}dp")
            android.util.Log.d("RegionSelectionDialog", "ダイアログ実際の幅(px): ${density.run { dialogActualWidthDp.dp.toPx() }}px")
            android.util.Log.d("RegionSelectionDialog", "ダイアログ幅の割合: ${(dialogActualWidthDp / windowWidthDp * 100).toInt()}%")
        }
    }
    
    if (showDialog) {
        var selectedRegion by remember { mutableStateOf(defaultRegion) }
        var editedEntry by remember { mutableStateOf<com.example.seedstockkeeper6.model.CalendarEntry?>(null) }

        Dialog(
            onDismissRequest = onDismiss
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f) // 画面の90%の高さに制限
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(20.dp)
                ) {
                    // 固定ヘッダー部分
                    Text(
                        text = "地域確認",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // 説明コメント
                    Text(
                        text = "AIで読み取った期間を確認してください。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // スクロール可能なコンテンツエリア
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // 残りのスペースを使用
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 切り抜きされたカレンダー画像を表示
                        if (croppedCalendarBitmap != null) {
                            item {
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
                                        Image(
                                            bitmap = croppedCalendarBitmap.asImageBitmap(),
                                            contentDescription = "切り抜きされたカレンダー",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.FillWidth
                                        )
                                    }
                                }
                            }
                        }

                        // OCR結果表示カード（写真と地域選択の間）
                        item {
                            val selectedRegionEntry = ocrResult?.calendar?.find { it.region == selectedRegion }
                            selectedRegionEntry?.let { entry ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        // アイコン + AI読み取り結果をtitleLargeで表示
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.star_opc),
                                                contentDescription = "AI読み取り結果",
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                "AI読み取り結果",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                        
                                        // 地域情報を表示（農園情報のDisplayModeと同じスタイル）
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = getRegionColor(selectedRegion).copy(alpha = 0.1f),
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
                                                            color = getRegionColor(selectedRegion),
                                                            shape = CircleShape
                                                        )
                                                )
                    Text(
                                                    text = selectedRegion.ifEmpty { "未設定" },
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = if (selectedRegion.isEmpty()) 
                                                        MaterialTheme.colorScheme.onSurfaceVariant 
                                                    else 
                                                        MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                                )
                                            }
                                        }
                                        
                                        // 表示モード（OCR結果では年を表示しない）
                                        CalendarEntryDisplay(entry = entry, showYear = false)
                                    }
                                }
                            }
                        }

                        // 編集項目を表示
                        if (selectedRegion.isNotEmpty()) {
                            item {
                                val entryToShow = ocrResult?.calendar?.find { it.region == selectedRegion } ?: com.example.seedstockkeeper6.model.CalendarEntry(
                                    region = selectedRegion,
                                    sowing_start_date = "",
                                    sowing_end_date = "",
                                    harvest_start_date = "",
                                    harvest_end_date = ""
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
                                        // 地域設定セクション（農園情報画面と同じスタイル）
                                        var showRegionBottomSheet by remember { mutableStateOf(false) }
                                        
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                androidx.compose.material3.Icon(
                                                    androidx.compose.material.icons.Icons.Filled.Public,
                                                    contentDescription = "地域設定",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text(
                                                    text = "地域",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            
                                            // 地域選択ボタン（農園情報画面と同じスタイル）
                                            Button(
                                                onClick = { showRegionBottomSheet = true },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = getRegionColor(selectedRegion),
                                                    contentColor = Color.White
                                                ),
                                                shape = MaterialTheme.shapes.large,
                                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                            ) {
                                                Text(
                                                    text = selectedRegion.ifEmpty { "地域" },
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                                )
                                            }
                                        }
                                        
                                        // 地域選択ボトムシート（農園情報画面と同じ）
                                        if (showRegionBottomSheet) {
                                            androidx.compose.material3.ModalBottomSheet(
                                                onDismissRequest = { showRegionBottomSheet = false }
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp)
                                                ) {
                                                    Text(
                                                        text = "地域を選択",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        modifier = Modifier.padding(bottom = 16.dp)
                                                    )
                                                    
                                                    listOf("寒地", "寒冷地", "温暖地", "暖地").forEach { region ->
                                                        Button(
                                                            onClick = {
                                                                selectedRegion = region
                                                                showRegionBottomSheet = false
                                                                
                                                                // 地域変更時にOCR結果で期間を上書き
                                                                val newRegionEntry = ocrResult?.calendar?.find { it.region == region }
                                                                if (newRegionEntry != null) {
                                                                    onUpdateEditing(newRegionEntry)
                                                                    editedEntry = newRegionEntry
                                                                } else {
                                                                    // 新しい地域の場合は空のエントリを作成
                                                                    editedEntry = com.example.seedstockkeeper6.model.CalendarEntry(
                                                                        region = region,
                                                                        sowing_start_date = "",
                                                                        sowing_end_date = "",
                                                                        harvest_start_date = "",
                                                                        harvest_end_date = ""
                                                                    )
                                                                }
                                                            },
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
                                                                androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                                                            } else null
                                                        ) {
                                                            Text(
                                                                text = region,
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                fontWeight = if (region == selectedRegion) 
                                                                    androidx.compose.ui.text.font.FontWeight.Bold 
                                                                else 
                                                                    androidx.compose.ui.text.font.FontWeight.Medium
                                                            )
                                                        }
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                }
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        // 編集項目を表示
                                        CalendarEntryEditor(
                                            entry = entryToShow,
                                            onUpdate = { updatedEntry ->
                                                android.util.Log.d("RegionSelectionDialog", "CalendarEntryEditor onUpdate: $updatedEntry")
                                                // 編集内容をViewModelに反映
                                                onUpdateEditing(updatedEntry)
                                                // ローカルでも編集された値を保存
                                                editedEntry = updatedEntry
                                                android.util.Log.d("RegionSelectionDialog", "editedEntry更新: $editedEntry")
                                            },
                                            onSave = { },
                                            onCancel = { }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // 固定フッター部分（OK・キャンセルボタン）- Column内に配置
                    LaunchedEffect(Unit) {
                        android.util.Log.d("RegionSelectionDialog", "OKボタン表示位置: Column内、Card内")
                        android.util.Log.d("RegionSelectionDialog", "OKボタンパディング: horizontal=0dp, vertical=16dp")
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("キャンセル")
                        }
                        
                        Button(
                            onClick = {
                                android.util.Log.d("RegionSelectionDialog", "OKボタンクリック: $selectedRegion")
                                android.util.Log.d("RegionSelectionDialog", "editedEntry: $editedEntry")
                                // 編集された値がある場合は、それを含めて保存
                                if (editedEntry != null) {
                                    android.util.Log.d("RegionSelectionDialog", "editedEntryを保存: $editedEntry")
                                    onUpdateEditing(editedEntry!!)
                                    // 編集された値を保存
                                    onSaveEditing()
                                } else {
                                    android.util.Log.w("RegionSelectionDialog", "editedEntryがnullです")
                                }
                                onRegionSelected(selectedRegion)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}
