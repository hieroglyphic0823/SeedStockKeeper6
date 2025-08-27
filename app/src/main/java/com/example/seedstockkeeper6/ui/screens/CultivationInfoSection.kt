package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

@Composable
fun CultivationInfoSection(viewModel: SeedInputViewModel) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "栽培情報",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            OutlinedTextField(
                value = viewModel.packet.productNumber,
                onValueChange = viewModel::onProductNumberChange,
                label = { Text("商品番号") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            
            OutlinedTextField(
                value = viewModel.packet.company,
                onValueChange = viewModel::onCompanyChange,
                label = { Text("会社") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            
            OutlinedTextField(
                value = viewModel.packet.originCountry,
                onValueChange = viewModel::onOriginCountryChange,
                label = { Text("原産国") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedTextField(
                value = viewModel.packet.contents,
                onValueChange = viewModel::onContentsChange,
                label = { Text("内容量") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            
            OutlinedTextField(
                value = viewModel.packet.germinationRate,
                onValueChange = viewModel::onGerminationRateChange,
                label = { Text("発芽率") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            
            OutlinedTextField(
                value = viewModel.packet.seedTreatment,
                onValueChange = viewModel::onSeedTreatmentChange,
                label = { Text("種子処理") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // 条間
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.packet.cultivation.spacing_cm_row_min.toString(),
                    onValueChange = viewModel::onSpacingRowMinChange,
                    label = { Text("条間最小") },
                    modifier = Modifier.width(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text("～", style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(
                    value = viewModel.packet.cultivation.spacing_cm_row_max.toString(),
                    onValueChange = viewModel::onSpacingRowMaxChange,
                    label = { Text("条間最大") },
                    modifier = Modifier.width(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text("(cm)", style = MaterialTheme.typography.bodyMedium)
            }
            
            // 株間
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.packet.cultivation.spacing_cm_plant_min.toString(),
                    onValueChange = viewModel::onSpacingPlantMinChange,
                    label = { Text("株間最小") },
                    modifier = Modifier.width(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text("～", style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(
                    value = viewModel.packet.cultivation.spacing_cm_plant_max.toString(),
                    onValueChange = viewModel::onSpacingPlantMaxChange,
                    label = { Text("株間最大") },
                    modifier = Modifier.width(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text("(cm)", style = MaterialTheme.typography.bodyMedium)
            }

            OutlinedTextField(
                value = viewModel.packet.cultivation.germinationTemp_c,
                onValueChange = viewModel::onGermTempChange,
                label = { Text("発芽温度") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            
            OutlinedTextField(
                value = viewModel.packet.cultivation.growingTemp_c,
                onValueChange = viewModel::onGrowTempChange,
                label = { Text("生育温度") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedTextField(
                value = viewModel.packet.cultivation.soilPrep_per_sqm.compost_kg.toString(),
                onValueChange = viewModel::onCompostChange,
                label = { Text("堆肥 (kg/㎡)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            
            OutlinedTextField(
                value = viewModel.packet.cultivation.soilPrep_per_sqm.dolomite_lime_g.toString(),
                onValueChange = viewModel::onLimeChange,
                label = { Text("苦土石灰 (g/㎡)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            
            OutlinedTextField(
                value = viewModel.packet.cultivation.soilPrep_per_sqm.chemical_fertilizer_g.toString(),
                onValueChange = viewModel::onFertilizerChange,
                label = { Text("化成肥料 (g/㎡)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedTextField(
                value = viewModel.packet.cultivation.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("栽培メモ") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            
            OutlinedTextField(
                value = viewModel.packet.cultivation.harvesting,
                onValueChange = viewModel::onHarvestingChange,
                label = { Text("収穫") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
