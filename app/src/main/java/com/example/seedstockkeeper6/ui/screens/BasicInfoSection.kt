package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

@Composable
fun BasicInfoSection(viewModel: SeedInputViewModel) {
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
                Icons.Filled.Info,
                contentDescription = "基本情報",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Text(
                "基本情報",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        // 商品名
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.productName,
                onValueChange = viewModel::onProductNameChange,
                label = { Text("商品名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        } else {
            // DisplayMode: 読み取り専用表示
            Text(
                text = "商品名: ${viewModel.packet.productName.ifEmpty { "未設定" }}",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 品種
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            OutlinedTextField(
                value = viewModel.packet.variety,
                onValueChange = viewModel::onVarietyChange,
                label = { Text("品種") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        } else {
            // DisplayMode: 読み取り専用表示
            Text(
                text = "品種: ${viewModel.packet.variety.ifEmpty { "未設定" }}",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 科名
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            FamilySelector(
                value = viewModel.packet.family,
                onValueChange = viewModel::onFamilyChange
            )
        } else {
            // DisplayMode: 読み取り専用表示
            Text(
                text = "科名: ${viewModel.packet.family.ifEmpty { "未設定" }}",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        
        Spacer(modifier = Modifier.height(12.dp))
        
    }
}
