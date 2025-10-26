package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.ui.components.FamilyIcon
import com.example.seedstockkeeper6.ui.components.FamilyIconCircle
import com.example.seedstockkeeper6.util.familyRotationYearsRange
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import kotlinx.coroutines.launch

@Composable
fun BasicInfoSection(
    viewModel: SeedInputViewModel,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.germination),
                    contentDescription = "基本情報",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "基本情報",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            // まき終わりアイコン
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "まき終わり",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = {
                        val isChecked = !viewModel.packet.isFinished
                        viewModel.updateFinishedFlagAndRefresh(isChecked) { result ->
                            scope.launch {
                                if (result.isSuccess) {
                                    val message = if (isChecked) "種をまき終わりました" else "まき終わりを解除しました"
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
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (viewModel.packet.isFinished) R.drawable.checkmark else R.drawable.packet
                        ),
                        contentDescription = if (viewModel.packet.isFinished) "まき終わり済み" else "まき終わり未完了",
                        modifier = Modifier.size(36.dp),
                        tint = Color.Unspecified // tintにColor.Unspecifiedを指定して元の色を使用
                    )
                }
            }
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
            // EditMode: 科名のOutlineTextと横並びで選択した科名に合ったFamilyアイコンを表示
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FamilySelector(
                    value = viewModel.packet.family,
                    onValueChange = viewModel::onFamilyChange,
                    modifier = Modifier.weight(1f)
                )
                // 選択した科名に合ったFamilyアイコン
                com.example.seedstockkeeper6.ui.components.FamilyIcon(
                    family = viewModel.packet.family,
                    size = 24.dp
                )
            }
        } else {
            // DisplayMode: 「科名: せり科（アイコン）」の順に表示
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "科名: ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = viewModel.packet.family.ifEmpty { "未設定" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // コンパニオンプランツと同じスタイルの丸いアイコン
                FamilyIconCircle(
                    family = viewModel.packet.family
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 連作障害年数
        val rotationYears = familyRotationYearsRange(viewModel.packet.family)
        if (rotationYears != null) {
            Text(
                text = "連作障害年数: $rotationYears",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
    }
}
