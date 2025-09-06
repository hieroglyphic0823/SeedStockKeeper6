package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

@Composable
fun CultivationInfoSection(viewModel: SeedInputViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                Icons.Filled.LocalFlorist,
                contentDescription = "栽培情報",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Text(
                "栽培情報",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        // 商品番号
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.productNumber,
                onValueChange = viewModel::onProductNumberChange,
                label = { Text("商品番号") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "商品番号: ${viewModel.packet.productNumber.ifEmpty { "未設定" }}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 会社
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.company,
                onValueChange = viewModel::onCompanyChange,
                label = { Text("会社") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "会社: ${viewModel.packet.company.ifEmpty { "未設定" }}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 原産国
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.originCountry,
                onValueChange = viewModel::onOriginCountryChange,
                label = { Text("原産国") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "原産国: ${viewModel.packet.originCountry.ifEmpty { "未設定" }}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 内容量
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.contents,
                onValueChange = viewModel::onContentsChange,
                label = { Text("内容量") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "内容量: ${viewModel.packet.contents.ifEmpty { "未設定" }}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 発芽率
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.germinationRate,
                onValueChange = viewModel::onGerminationRateChange,
                label = { Text("発芽率") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "発芽率: ${viewModel.packet.germinationRate.ifEmpty { "未設定" }}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 種子処理
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.seedTreatment,
                onValueChange = viewModel::onSeedTreatmentChange,
                label = { Text("種子処理") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "種子処理: ${viewModel.packet.seedTreatment.ifEmpty { "未設定" }}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 条間
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.packet.cultivation.spacing_cm_row_min.toString(),
                    onValueChange = viewModel::onSpacingRowMinChange,
                    label = { Text("条間最小") },
                    modifier = Modifier.width(80.dp)
                )
                Text("～", style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(
                    value = viewModel.packet.cultivation.spacing_cm_row_max.toString(),
                    onValueChange = viewModel::onSpacingRowMaxChange,
                    label = { Text("条間最大") },
                    modifier = Modifier.width(80.dp)
                )
                Text("(cm)", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Text(
                text = "条間: ${viewModel.packet.cultivation.spacing_cm_row_min}～${viewModel.packet.cultivation.spacing_cm_row_max}cm",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 株間
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.packet.cultivation.spacing_cm_plant_min.toString(),
                    onValueChange = viewModel::onSpacingPlantMinChange,
                    label = { Text("株間最小") },
                    modifier = Modifier.width(80.dp)
                )
                Text("～", style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(
                    value = viewModel.packet.cultivation.spacing_cm_plant_max.toString(),
                    onValueChange = viewModel::onSpacingPlantMaxChange,
                    label = { Text("株間最大") },
                    modifier = Modifier.width(80.dp)
                )
                Text("(cm)", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Text(
                text = "株間: ${viewModel.packet.cultivation.spacing_cm_plant_min}～${viewModel.packet.cultivation.spacing_cm_plant_max}cm",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 発芽温度
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.cultivation.germinationTemp_c,
                onValueChange = viewModel::onGermTempChange,
                label = { Text("発芽温度") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "発芽温度: ${viewModel.packet.cultivation.germinationTemp_c.ifEmpty { "未設定" }}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 生育温度
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.cultivation.growingTemp_c,
                onValueChange = viewModel::onGrowTempChange,
                label = { Text("生育温度") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "生育温度: ${viewModel.packet.cultivation.growingTemp_c.ifEmpty { "未設定" }}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 堆肥
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.cultivation.soilPrep_per_sqm.compost_kg.toString(),
                onValueChange = viewModel::onCompostChange,
                label = { Text("堆肥 (kg/㎡)") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "堆肥: ${viewModel.packet.cultivation.soilPrep_per_sqm.compost_kg}kg/㎡",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 苦土石灰
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.cultivation.soilPrep_per_sqm.dolomite_lime_g.toString(),
                onValueChange = viewModel::onLimeChange,
                label = { Text("苦土石灰 (g/㎡)") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "苦土石灰: ${viewModel.packet.cultivation.soilPrep_per_sqm.dolomite_lime_g}g/㎡",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 化成肥料
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.cultivation.soilPrep_per_sqm.chemical_fertilizer_g.toString(),
                onValueChange = viewModel::onFertilizerChange,
                label = { Text("化成肥料 (g/㎡)") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "化成肥料: ${viewModel.packet.cultivation.soilPrep_per_sqm.chemical_fertilizer_g}g/㎡",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 栽培メモ
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.cultivation.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("栽培メモ") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "栽培メモ: ${viewModel.packet.cultivation.notes.ifEmpty { "未設定" }}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 収穫方法
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.cultivation.harvesting,
                onValueChange = viewModel::onHarvestingChange,
                label = { Text("収穫") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "収穫: ${viewModel.packet.cultivation.harvesting.ifEmpty { "未設定" }}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
