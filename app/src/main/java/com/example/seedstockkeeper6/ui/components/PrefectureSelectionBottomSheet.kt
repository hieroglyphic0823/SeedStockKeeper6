package com.example.seedstockkeeper6.ui.components

import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrefectureSelectionBottomSheet(
    selectedPrefecture: String,
    onPrefectureSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val prefectures = listOf(
        "北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県",
        "茨城県", "栃木県", "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県",
        "新潟県", "富山県", "石川県", "福井県", "山梨県", "長野県", "岐阜県",
        "静岡県", "愛知県", "三重県", "滋賀県", "京都府", "大阪府", "兵庫県",
        "奈良県", "和歌山県", "鳥取県", "島根県", "岡山県", "広島県", "山口県",
        "徳島県", "香川県", "愛媛県", "高知県", "福岡県", "佐賀県", "長崎県",
        "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県"
    )
    
    var selectedIndex by remember { mutableIntStateOf(prefectures.indexOf(selectedPrefecture).takeIf { it >= 0 } ?: 0) }
    
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
                    Icons.Filled.LocationOn,
                    contentDescription = "県選択",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "県を選択",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // NumberPicker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = 0
                            maxValue = prefectures.size - 1
                            displayedValues = prefectures.toTypedArray()
                            value = selectedIndex
                            
                            // Material3テーマカラーを適用
                            try {
                                val onSurfaceColor = androidx.compose.ui.graphics.Color(0xFF1C1B1F).toArgb() // onSurface相当
                                val surfaceColor = androidx.compose.ui.graphics.Color(0xFFFEF7FF).toArgb() // surface相当
                                setTextColor(onSurfaceColor)
                                setBackgroundColor(surfaceColor)
                            } catch (e: Exception) {
                                // エラーが発生した場合はデフォルトのまま
                                android.util.Log.w("PrefectureSelectionBottomSheet", "NumberPicker色設定に失敗", e)
                            }
                        }
                    },
                    update = { numberPicker ->
                        numberPicker.value = selectedIndex
                        numberPicker.setOnValueChangedListener { _, _, newVal ->
                            selectedIndex = newVal
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // OK/Cancel ボタン
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("キャンセル")
                }
                
                Button(
                    onClick = {
                        onPrefectureSelected(prefectures[selectedIndex])
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
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
