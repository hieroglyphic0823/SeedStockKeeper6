package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.ui.screens.SeedListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "種一覧画面", heightDp = 800)
@Composable
fun SeedListScreenPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        // プレビュー用のデモデータ
        val demoSeeds = listOf(
            com.example.seedstockkeeper6.model.SeedPacket(
                id = "demo-seed-1",
                productName = "恋むすめ",
                variety = "ニンジン",
                family = "せり科",
                expirationYear = 2026,
                expirationMonth = 10,
                companionPlants = listOf(
                    com.example.seedstockkeeper6.model.CompanionPlant(
                        plant = "ネギ",
                        effects = listOf("害虫忌避", "土壌改善")
                    )
                )
            ),
            com.example.seedstockkeeper6.model.SeedPacket(
                id = "demo-seed-2",
                productName = "サラダ菜",
                variety = "レタス",
                family = "キク科",
                expirationYear = 2025,
                expirationMonth = 12,
                companionPlants = listOf(
                    com.example.seedstockkeeper6.model.CompanionPlant(
                        plant = "マリーゴールド",
                        effects = listOf("害虫忌避")
                    )
                )
            ),
            com.example.seedstockkeeper6.model.SeedPacket(
                id = "demo-seed-3",
                productName = "二十日大根",
                variety = "ラディッシュ",
                family = "アブラナ科",
                expirationYear = 2026,
                expirationMonth = 3,
                companionPlants = listOf(
                    com.example.seedstockkeeper6.model.CompanionPlant(
                        plant = "バジル",
                        effects = listOf("害虫忌避", "風味向上")
                    )
                )
            )
        )
        
        val selectedIds = remember { mutableStateOf(listOf("demo-seed-1")) } // 1つ選択状態でプレビュー
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "種リスト",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    actions = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 削除ボタン（選択されている場合のみ表示）
                            if (selectedIds.value.isNotEmpty()) {
                                IconButton(onClick = { /* プレビュー用 */ }) {
                                    Image(
                                        painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.delete),
                                        contentDescription = "削除",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            // 設定ボタン
                            IconButton(onClick = { /* プレビュー用 */ }) {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = "設定",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp), // アイテム間の間隔を0dpに設定
                contentPadding = PaddingValues(vertical = 8.dp) // 上下に8dpのパディングを追加
            ) {
                items(demoSeeds) { seed ->
                    val checked = selectedIds.value.contains(seed.id)
                    
                    // リストアイテム（実装と同じ形式）
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // FamilyIcon（色変更が反映される）
                        com.example.seedstockkeeper6.ui.components.FamilyIcon(
                            family = seed.family,
                            size = 48.dp,
                            cornerRadius = 8.dp,
                            rotationLabel = "3", // 連作バッジのデモ（年を削除）
                            badgeProtrusion = 4.dp,
                            showCircleBorder = true
                        )
                        
                        // 商品情報Column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            // 品種名の上の余白（Material3ルール: 8dp）
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 商品名と品種名を横並び
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    seed.productName, 
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal)
                                )
                                Text(
                                    "（${seed.variety}）", 
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Light)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(2.dp)) // 4dp → 2dpに縮小
                            
                            // 有効期限のみ表示（Checkbox一時削除でテスト）
                            Text(
                                "有効期限: ${seed.expirationYear}年 ${seed.expirationMonth}月", 
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Light,
                                    lineHeight = MaterialTheme.typography.bodyLarge.fontSize // フォントサイズと同じlineHeight
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp) // 上下の余白を4dpに制限（Checkbox削除テスト）
                            )
                            
                            // コンパニオンプランツの表示（実装と同じ）
                            if (seed.companionPlants.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp)) // 4dp → 2dpに縮小
                                val companionPlantNames = seed.companionPlants
                                    .filter { it.plant.isNotBlank() }
                                    .map { it.plant }
                                    .take(3) // 最大3つまで表示
                                
                                if (companionPlantNames.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Cを丸で囲ったアイコン（Material3準拠）
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                                    shape = androidx.compose.foundation.shape.CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "C",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                            )
                                        }
                                        
                                        // コンパニオンプランツ名（1行のみ表示、折り返さない）
                                        Text(
                                            "${companionPlantNames.joinToString(", ")}${if (seed.companionPlants.size > 3) "..." else ""}",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Light),
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            
                            // コンパニオンプランツの下の余白（Material3ルール: 8dp）
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    // 区切り線（最後のアイテム以外）
                    if (demoSeeds.indexOf(seed) < demoSeeds.size - 1) {
                        androidx.compose.material3.HorizontalDivider(
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
