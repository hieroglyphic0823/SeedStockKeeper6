package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.components.FamilyIcon
import com.example.seedstockkeeper6.ui.components.SwipeToDeleteItem
import com.example.seedstockkeeper6.util.familyRotationMinYearsLabel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch

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
    onDisplayModeChange: (String) -> Unit
) {
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
            // フィルター用チェックボックス（常に表示）
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1行目：「まきどき」「終了間近」「通常」
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // まきどきチェックボックス
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = showThisMonthSeeds,
                            onCheckedChange = onThisMonthSeedsChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                uncheckedColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "まきどき",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    // 終了間近チェックボックス
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = showUrgentSeeds,
                            onCheckedChange = onUrgentSeedsChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.onErrorContainer,
                                uncheckedColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "終了間近",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    
                    // 通常チェックボックス
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = showNormalSeeds,
                            onCheckedChange = onNormalSeedsChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.onSurface,
                                uncheckedColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "通常",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // 表示モード切り替えアイコン（1行目の右端）
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // リスト表示アイコン
                        IconButton(
                            onClick = { onDisplayModeChange("list") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ViewList,
                                contentDescription = "リスト表示",
                                tint = if (displayMode == "list") 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // ギャラリー表示アイコン
                        IconButton(
                            onClick = { onDisplayModeChange("gallery") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ViewModule,
                                contentDescription = "ギャラリー表示",
                                tint = if (displayMode == "gallery") 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                // 2行目：「期限切れ」「まき終わり」と検索ボックス表示切り替えボタン
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 期限切れチェックボックス
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = showExpiredSeeds,
                            onCheckedChange = onExpiredSeedsChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.onSurface,
                                uncheckedColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "期限切れ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // まき終わりチェックボックス
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = showFinishedSeeds,
                            onCheckedChange = onFinishedSeedsChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                uncheckedColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "まき終わり",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    // 検索ボックス表示切り替えボタン（2行目の右端）
                    IconButton(
                        onClick = onSearchBoxToggle
                    ) {
                        Icon(
                            imageVector = if (showSearchBox) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (showSearchBox) "検索ボックスを隠す" else "検索ボックスを表示",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // 検索ボックス（条件付き表示）
            if (showSearchBox) {
                Spacer(modifier = Modifier.height(12.dp))
                
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

/**
 * リストアイテムコンポーネント
 */
@Composable
fun SeedListItem(
    seed: SeedPacket,
    encodedSeed: String,
    navController: NavController,
    viewModel: SeedListViewModel,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    onDelete: () -> Unit,
    isLastItem: Boolean
) {
    // スワイプ可能なリストアイテム
    SwipeToDeleteItem(
        modifier = Modifier
            .heightIn(min = 80.dp),
        onDelete = onDelete
    ) {
        // 種の状態を判定
        val seedStatus = getSeedStatus(seed)
        val backgroundColor = when (seedStatus) {
            "finished" -> MaterialTheme.colorScheme.secondaryContainer  // まき終わり
            "expired" -> MaterialTheme.colorScheme.surfaceContainerHighest  // 有効期限切れ
            "urgent" -> MaterialTheme.colorScheme.errorContainer         // 終了間近
            "thisMonth" -> MaterialTheme.colorScheme.primaryContainer    // 今月まきどき
            else -> MaterialTheme.colorScheme.surfaceContainerLowest                   // 通常
        }
        
        // リストアイテム
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(12.dp)
                .clickable {
                    navController.navigate("input/$encodedSeed")
                },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Familyアイコン
            val rotation = familyRotationMinYearsLabel(seed.family) ?: ""
            FamilyIcon(
                family = seed.family,
                size = 48.dp,
                cornerRadius = 8.dp,
                rotationLabel = rotation,
                badgeProtrusion = 4.dp,
                showCircleBorder = true
            )
            
            // 中央: 縦並びの情報
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // 商品名
                Text(
                    text = seed.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // 品種名
                Text(
                    text = seed.variety,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 有効期限
                Text(
                    text = "有効期限: ${seed.expirationYear}/${seed.expirationMonth}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // コンパニオンプランツ
                if (seed.companionPlants.isNotEmpty()) {
                    val companionPlantNames = seed.companionPlants
                        .filter { it.plant.isNotBlank() }
                        .map { it.plant }
                        .take(3)
                    
                    if (companionPlantNames.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Cを丸で囲ったアイコン
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "C",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // コンパニオンプランツ名
                            Text(
                                text = "${companionPlantNames.joinToString(", ")}${if (seed.companionPlants.size > 3) "..." else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            
            // 右側: まき終わりアイコン
            IconButton(
                onClick = {
                    val isChecked = !seed.isFinished
                    // まき終わりフラグの更新処理
                    val documentId = seed.documentId ?: seed.id
                    if (documentId != null) {
                        viewModel.updateFinishedFlag(documentId, isChecked) { result ->
                            scope.launch {
                                if (result.isSuccess) {
                                    val message = if (isChecked) "まき終わりに設定しました" else "まき終わりを解除しました"
                                    snackbarHostState.showSnackbar(
                                        message = message,
                                        duration = SnackbarDuration.Short
                                    )
                                } else {
                                    snackbarHostState.showSnackbar(
                                        message = "更新に失敗しました",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (seed.isFinished) R.drawable.checkmark else R.drawable.packet
                    ),
                    contentDescription = if (seed.isFinished) "まき終わり済み" else "まき終わり未完了",
                    modifier = Modifier.size(36.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
    
    // 区切り線（最後のアイテム以外）
    if (!isLastItem) {
        HorizontalDivider(
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

/**
 * ギャラリーアイテムコンポーネント
 */
@Composable
fun SeedGalleryItem(
    seed: SeedPacket,
    encodedSeed: String,
    navController: NavController
) {
    val seedStatus = getSeedStatus(seed)
    
    // ギャラリーアイテム
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                navController.navigate("input/$encodedSeed")
            }
    ) {
        // 背景画像（k_goshiki_jin.png）
        Image(
            painter = painterResource(id = R.drawable.k_goshiki_jin),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        
        // 種の写真（中央に表示）
        if (seed.imageUrls.isNotEmpty()) {
            val firstImageUrl = seed.imageUrls.first()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = firstImageUrl,
                    contentDescription = seed.productName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentScale = ContentScale.Fit
                )
            }
        } else {
            // 写真がない場合のフォールバック表示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Familyアイコン
                    val rotation = familyRotationMinYearsLabel(seed.family) ?: ""
                    FamilyIcon(
                        family = seed.family,
                        size = 32.dp,
                        cornerRadius = 6.dp,
                        rotationLabel = rotation,
                        badgeProtrusion = 3.dp,
                        showCircleBorder = true
                    )
                    
                    // 商品名
                    Text(
                        text = seed.productName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        // 状態インジケーター（右上）
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            val indicatorColor = when (seedStatus) {
                "finished" -> MaterialTheme.colorScheme.secondaryContainer
                "expired" -> MaterialTheme.colorScheme.surfaceContainerHighest
                "urgent" -> MaterialTheme.colorScheme.errorContainer
                "thisMonth" -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerLowest
            }
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
        }
    }
}

