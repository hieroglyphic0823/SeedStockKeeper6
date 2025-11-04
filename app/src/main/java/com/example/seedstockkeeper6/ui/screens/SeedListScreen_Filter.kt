package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ViewCozy
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.seedstockkeeper6.R

/**
 * Colorをグレースケールに変換する拡張関数
 */
private fun Color.toGrayscale(): Color {
    // RGB値を取得（0.0-1.0の範囲）
    val red = this.red
    val green = this.green
    val blue = this.blue
    
    // 輝度を計算（0.299*R + 0.587*G + 0.114*B）
    val gray = 0.299f * red + 0.587f * green + 0.114f * blue
    
    // グレースケールのColorを作成（透明度は維持）
    return Color(red = gray, green = gray, blue = gray, alpha = this.alpha)
}

/**
 * フィルターカードコンポーネント
 * 種のフィルター条件と検索ボックス、表示モード切り替えを表示
 */
@Composable
fun SeedListFilterCard(
    showThisMonthSeeds: Boolean,
    onThisMonthSeedsChange: (Boolean) -> Unit,
    showUrgentSeeds: Boolean,
    onUrgentSeedsChange: (Boolean) -> Unit,
    showExpiredSeeds: Boolean,
    onExpiredSeedsChange: (Boolean) -> Unit,
    showFinishedSeeds: Boolean,
    onFinishedSeedsChange: (Boolean) -> Unit,
    showNormalSeeds: Boolean,
    onNormalSeedsChange: (Boolean) -> Unit,
    showSearchBox: Boolean,
    onSearchBoxToggle: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    displayMode: String,
    onDisplayModeChange: (String) -> Unit,
    sortType: SortType,
    onSortTypeChange: (SortType) -> Unit
) {
    // 並べ替えダイアログの表示状態
    var showSortDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 1行目：並べ替え、表示、絞込みアイコン（右揃え）
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 並べ替えアイコンボタン（左）
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { showSortDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapVert,
                        contentDescription = "並べ替え",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "並べ替え",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 表示モード切り替えボタン（中央）
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { 
                        onDisplayModeChange(if (displayMode == "list") "gallery" else "list")
                    }
                ) {
                    Icon(
                        imageVector = if (displayMode == "list") Icons.Filled.List else Icons.Filled.ViewCozy,
                        contentDescription = if (displayMode == "list") "目録" else "絵巻",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (displayMode == "list") "目録" else "絵巻",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 絞り込みアイコンボタン（右端）
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onSearchBoxToggle() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterAlt,
                        contentDescription = "吟味",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "吟味",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 並べ替えダイアログ
            if (showSortDialog) {
                AlertDialog(
                    onDismissRequest = { showSortDialog = false },
                    title = { Text("並べ替え") },
                    text = {
                        Column {
                            SortType.values().forEach { type ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            onSortTypeChange(type)
                                            showSortDialog = false
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = sortType == type,
                                        onClick = { 
                                            onSortTypeChange(type)
                                            showSortDialog = false
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = type.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSortDialog = false }) {
                            Text("閉じる")
                        }
                    }
                )
            }
            
            // フィルター用チェックボックスと検索ボックス（条件付き表示）
            if (showSearchBox) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1行目：「終了間近」「まきどき」「通常」（重要度順）
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 終了間近アイコン+状態名ボタン（重要度1）
                        val urgentContainerColor = if (showUrgentSeeds) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer.toGrayscale()
                        }
                        val urgentContentColor = if (showUrgentSeeds) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer.toGrayscale()
                        }
                        Row(
                            modifier = Modifier
                                .background(
                                    color = urgentContainerColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onUrgentSeedsChange(!showUrgentSeeds) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.warning),
                                contentDescription = "終了間近",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = "終了間近",
                                style = MaterialTheme.typography.bodyMedium,
                                color = urgentContentColor
                            )
                        }
                        
                        // まきどきアイコン+状態名ボタン（重要度2）
                        val thisMonthContainerColor = if (showThisMonthSeeds) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.toGrayscale()
                        }
                        val thisMonthContentColor = if (showThisMonthSeeds) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer.toGrayscale()
                        }
                        Row(
                            modifier = Modifier
                                .background(
                                    color = thisMonthContainerColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onThisMonthSeedsChange(!showThisMonthSeeds) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.seed_bag_enp),
                                contentDescription = "まきどき",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = "まきどき",
                                style = MaterialTheme.typography.bodyMedium,
                                color = thisMonthContentColor
                            )
                        }
                        
                        // 通常アイコン+状態名ボタン（重要度3）
                        val normalContainerColor = if (showNormalSeeds) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer.toGrayscale()
                        }
                        val normalContentColor = if (showNormalSeeds) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onTertiaryContainer.toGrayscale()
                        }
                        Row(
                            modifier = Modifier
                                .background(
                                    color = normalContainerColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onNormalSeedsChange(!showNormalSeeds) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.seed_bag_full),
                                contentDescription = "通常",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = "通常",
                                style = MaterialTheme.typography.bodyMedium,
                                color = normalContentColor
                            )
                        }
                    }
                    
                    // 2行目：「まき終わり」「期限切れ」（重要度順）
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // まき終わりアイコン+状態名ボタン（重要度4）
                        val finishedContainerColor = if (showFinishedSeeds) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer.toGrayscale()
                        }
                        val finishedContentColor = if (showFinishedSeeds) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer.toGrayscale()
                        }
                        Row(
                            modifier = Modifier
                                .background(
                                    color = finishedContainerColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onFinishedSeedsChange(!showFinishedSeeds) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.seed),
                                contentDescription = "まき終わり",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = "まき終わり",
                                style = MaterialTheme.typography.bodyMedium,
                                color = finishedContentColor
                            )
                        }
                        
                        // 期限切れアイコン+状態名ボタン（重要度5）
                        val expiredContainerColor = if (showExpiredSeeds) {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest.toGrayscale()
                        }
                        val expiredContentColor = if (showExpiredSeeds) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.toGrayscale()
                        }
                        Row(
                            modifier = Modifier
                                .background(
                                    color = expiredContainerColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onExpiredSeedsChange(!showExpiredSeeds) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = "期限切れ",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = "期限切れ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = expiredContentColor
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 検索ボックス
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("種を検索") },
                    placeholder = { Text("商品名、品種、科名で検索") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}

