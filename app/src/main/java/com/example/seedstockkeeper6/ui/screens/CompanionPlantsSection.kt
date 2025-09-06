package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.ui.components.CompanionEffectIcon
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

@Composable
fun CompanionPlantsSection(viewModel: SeedInputViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // --- コンパニオンプランツ表示＆追加部 ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                Icons.Filled.Eco,
                contentDescription = "コンパニオンプランツと効果",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Text(
                "コンパニオンプランツと効果", 
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        viewModel.packet.companionPlants.forEachIndexed { i, companion ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    companion.plant,
                    Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
                )
                Spacer(modifier = Modifier.size(8.dp))
                CompanionEffectIcon(companion.effects)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // DisplayModeの時はコンパニオンプランツ追加セクションを非表示
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            CompanionPlantInputSection(viewModel)
        }
    }
}

@Composable
private fun CompanionPlantInputSection(viewModel: SeedInputViewModel) {
    var cpPlant by remember { mutableStateOf("") }
    var selectedEffects by remember { mutableStateOf(mutableSetOf<com.example.seedstockkeeper6.model.CompanionEffectCode>()) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 植物名入力
        OutlinedTextField(
            value = cpPlant,
            onValueChange = { cpPlant = it },
            label = { Text("植物名") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 効果選択
        Text("効果を選択:", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        
        // 効果の選択肢をグリッドで表示
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.height(120.dp)
        ) {
            items(com.example.seedstockkeeper6.model.CompanionEffectCode.values()) { effectCode ->
                val isSelected = selectedEffects.contains(effectCode)
                FilterChip(
                    onClick = {
                        if (isSelected) {
                            selectedEffects.remove(effectCode)
                        } else {
                            selectedEffects.add(effectCode)
                        }
                    },
                    label = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CompanionEffectIcon(listOf(effectCode.code))
                            Text(effectCode.displayName, style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    selected = isSelected,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 追加ボタン
        Button(
            onClick = {
                if (cpPlant.isNotBlank() && selectedEffects.isNotEmpty()) {
                    val effectCodes = selectedEffects.map { effectCode -> effectCode.code }
                    viewModel.addCompanionPlant(
                        com.example.seedstockkeeper6.model.CompanionPlant(
                            plant = cpPlant,
                            effects = effectCodes
                        )
                    )
                    cpPlant = ""
                    selectedEffects.clear()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("追加")
        }
    }
}
