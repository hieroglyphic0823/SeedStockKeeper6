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
    onDismiss: () -> Unit,
    onUpdateExpiration: (com.example.seedstockkeeper6.model.CalendarEntry) -> Unit = {} // 有効期限更新のコールバック
) {
    
    // ウィンドウサイズとダイアログサイズをLog出力
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val windowWidthDp = configuration.screenWidthDp
    val windowHeightDp = configuration.screenHeightDp
    val dialogHeightDp = 700f.dp // 高さをさらに増加
    
    // ダイアログの実際の横幅を計算
    val dialogPadding = 16.dp // Cardのpadding
    val dialogContentPadding = 20.dp // Columnのpadding
    val totalDialogPadding = dialogPadding + dialogContentPadding
    val dialogActualWidthDp = windowWidthDp - totalDialogPadding.value
    
    LaunchedEffect(showDialog) {
        android.util.Log.d("RegionSelectionDialog", "LaunchedEffect: showDialog=$showDialog")
        if (showDialog) {
            android.util.Log.d("RegionSelectionDialog", "RegionSelectionDialog: dialog is showing, regionList=$regionList, ocrResult=${ocrResult != null}")
            // OCR結果の有効期限情報をログに表示
            ocrResult?.let { result ->
                result.calendar?.forEach { entry ->
                } ?: run { /* カレンダー情報なし */ }
            } ?: run { /* OCR結果なし */ }
            
            // ダイアログ要素の高さ情報をログに表示
        }
    }
    
    android.util.Log.d("RegionSelectionDialog", "RegionSelectionDialog composable: showDialog=$showDialog")
    if (showDialog) {
        // defaultRegionをキーにして、ダイアログが表示される度に初期化（農園設定の地域を優先）
        var selectedRegion by remember(showDialog, defaultRegion) { 
            mutableStateOf(defaultRegion) 
        }
        var editedEntry by remember { mutableStateOf<com.example.seedstockkeeper6.model.CalendarEntry?>(null) }

        Dialog(
            onDismissRequest = onDismiss
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight() // 要素に合わせて高さを調整
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight() // 要素に合わせて高さを調整
                        .padding(16.dp)
                ) {
                    // 固定ヘッダー部分
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = "種暦",
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = "種暦",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // 説明コメント
                    Text(
                        text = "AI読取結果を確認してください。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // スクロール可能なコンテンツエリア
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp), // 高さをさらに増加
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

                        // AI読み取り結果カードは非表示

                        // 編集項目を表示
                        if (selectedRegion.isNotEmpty()) {
                            item {
                                val entryToShow = ocrResult?.calendar?.find { it.region == selectedRegion } ?: run {
                                    // OCR結果に該当地域がない場合は、OCR結果の有効期限情報を使用してエントリを作成
                                    val currentDate = java.time.LocalDate.now()
                                    val expirationYear = ocrResult?.expirationYear ?: (currentDate.year + 1)
                                    val expirationMonth = ocrResult?.expirationMonth ?: currentDate.monthValue
                                    // OCR結果から該当地域のエントリを取得
                                    val ocrEntry = ocrResult?.calendar?.find { it.region == selectedRegion }
                                    val newEntry = com.example.seedstockkeeper6.model.CalendarEntry(
                                        region = selectedRegion,
                                        sowing_start_date = ocrEntry?.sowing_start_date ?: "",
                                        sowing_end_date = ocrEntry?.sowing_end_date ?: "",
                                        harvest_start_date = ocrEntry?.harvest_start_date ?: "",
                                        harvest_end_date = ocrEntry?.harvest_end_date ?: "",
                                        expirationYear = expirationYear,
                                        expirationMonth = expirationMonth
                                    )
                                    newEntry
                                }
                                
                                
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
                                            // 地域タイトルと地域ボタンを横並びで表示
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                // 地域タイトル
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    androidx.compose.material3.Icon(
                                                        androidx.compose.material.icons.Icons.Filled.Public,
                                                        contentDescription = "地域設定",
                                                        tint = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Text(
                                                        text = "地域",
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                
                                                // 地域選択ボタン（農園情報画面と同じスタイル）
                                                Button(
                                                    onClick = { showRegionBottomSheet = true },
                                                    modifier = Modifier.weight(1f),
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
                                                                    // 有効期限情報を種登録画面に反映
                                                                    onUpdateExpiration(newRegionEntry)
                                                                } else {
                                                                    // 新しい地域の場合は空のエントリを作成（OCR結果の有効期限情報を使用）
                                                                    val currentDate = java.time.LocalDate.now()
                                                                    val expirationYear = ocrResult?.expirationYear ?: (currentDate.year + 1)
                                                                    val expirationMonth = ocrResult?.expirationMonth ?: currentDate.monthValue
                                                                    // OCR結果から該当地域のエントリを取得
                                                                    val ocrEntry = ocrResult?.calendar?.find { it.region == region }
                                                                    val newEntry = com.example.seedstockkeeper6.model.CalendarEntry(
                                                                        region = region,
                                                                        sowing_start_date = ocrEntry?.sowing_start_date ?: "",
                                                                        sowing_end_date = ocrEntry?.sowing_end_date ?: "",
                                                                        harvest_start_date = ocrEntry?.harvest_start_date ?: "",
                                                                        harvest_end_date = ocrEntry?.harvest_end_date ?: "",
                                                                        expirationYear = expirationYear,
                                                                        expirationMonth = expirationMonth
                                                                    )
                                                                    editedEntry = newEntry
                                                                    onUpdateEditing(newEntry)
                                                                }
                                                            },
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 6.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = getRegionColor(region),
                                                                contentColor = Color.White
                                                            ),
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
                                        
                                        // editedEntryがnullの場合は初期化
                                        if (editedEntry == null) {
                                            val regionEntry = ocrResult?.calendar?.find { it.region == selectedRegion }
                                            if (regionEntry != null) {
                                                val expirationYear = ocrResult?.expirationYear ?: 0
                                                val expirationMonth = ocrResult?.expirationMonth ?: 0
                                                val newRegionEntry = regionEntry.copy(
                                                    expirationYear = expirationYear,
                                                    expirationMonth = expirationMonth
                                                )
                                                editedEntry = newRegionEntry
                                                onUpdateEditing(newRegionEntry)
                                            } else {
                                                val expirationYear = ocrResult?.expirationYear ?: 0
                                                val expirationMonth = ocrResult?.expirationMonth ?: 0
                                                val newEntry = com.example.seedstockkeeper6.model.CalendarEntry(
                                                    region = selectedRegion,
                                                    sowing_start_date = "",
                                                    sowing_end_date = "",
                                                    harvest_start_date = "",
                                                    harvest_end_date = "",
                                                    expirationYear = expirationYear,
                                                    expirationMonth = expirationMonth
                                                )
                                                editedEntry = newEntry
                                                onUpdateEditing(newEntry)
                                            }
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
                                            onSave = { },
                                            onCancel = { },
                                            onUpdateExpiration = { updatedEntry ->
                                                // 有効期限情報を種登録画面に反映
                                                onUpdateExpiration(updatedEntry)
                                            },
                                            ocrResult = ocrResult // OCR結果を渡す
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // 固定フッター部分（OK・キャンセルボタン）- Column内に配置
                    LaunchedEffect(Unit) {
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("キャンセル")
                        }
                        
                        Button(
                            onClick = {
                                // 編集された値がある場合は、それを含めて保存
                                if (editedEntry != null) {
                                    
                                    onUpdateEditing(editedEntry!!)
                                    
                                    // 編集された値を保存
                                    onSaveEditing()
                                    
                                    // 有効期限情報を種登録画面に反映
                                    onUpdateExpiration(editedEntry!!)
                                    
                                } else {
                                }
                                onRegionSelected(selectedRegion)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
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
        }
    }
}
