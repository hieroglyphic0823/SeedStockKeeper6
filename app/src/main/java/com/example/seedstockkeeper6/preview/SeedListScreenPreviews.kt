package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
        
        val mockUser = com.example.seedstockkeeper6.preview.createMockFirebaseUser()
        val seedListViewModel = com.example.seedstockkeeper6.preview.createPreviewSeedListViewModel()
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "種一覧",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(demoSeeds) { seed ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // FamilyIcon（色変更が反映される）
                            com.example.seedstockkeeper6.ui.components.FamilyIcon(
                                family = seed.family,
                                size = 50.dp,
                                cornerRadius = 8.dp,
                                rotationLabel = "3年", // 連作バッジのデモ
                                badgeProtrusion = 4.dp
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = seed.productName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${seed.variety} (${seed.family})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "有効期限: ${seed.expirationYear}年${seed.expirationMonth}月",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
