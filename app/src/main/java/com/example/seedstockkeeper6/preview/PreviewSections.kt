package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme

// プレビュー専用の画像管理セクション（Firebaseの参照を避ける）
@Composable
fun PreviewImageManagementSection(viewModel: com.example.seedstockkeeper6.viewmodel.SeedInputViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val imageSize = (screenWidth - 32.dp) / 3 // 3枚表示に合わせる
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.image),
                contentDescription = "画像管理",
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "画像管理",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(viewModel.imageUris) { index, uri ->
                Box(
                    modifier = Modifier
                        .size(imageSize)
                        .padding(end = 2.dp)
                        .then(
                            if (viewModel.ocrTargetIndex == index) {
                                Modifier.border(
                                    width = 4.dp,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                ) {
                    AsyncImage(
                        model = uri.toString(),
                        contentDescription = "画像$index",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // 削除ボタン
                    IconButton(
                        onClick = { },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "削除",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // 追加ボタン
            if (viewModel.isEditMode || !viewModel.hasExistingData) {
                item {
                    IconButton(onClick = { }) {
                        Image(
                            painter = painterResource(id = R.drawable.plus),
                            contentDescription = "追加",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        // AIで解析ボタン
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            Button(
                onClick = { },
                enabled = viewModel.imageUris.isNotEmpty() && !viewModel.isLoading,
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Text("AIで解析")
                Spacer(Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.star_w),
                    contentDescription = "OCR",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// プレビュー専用の栽培情報セクション（播種期間・収穫期間を確実に表示）
@Composable
fun PreviewCultivationInfoSection(viewModel: com.example.seedstockkeeper6.viewmodel.SeedInputViewModel) {
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
            Image(
                painter = painterResource(id = R.drawable.monitoring),
                contentDescription = "栽培情報",
                modifier = Modifier.size(24.dp)
            )
            Text(
                "栽培情報",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        // 商品番号
        Text(
            text = "商品番号: ${viewModel.packet.productNumber.ifEmpty { "未設定" }}",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        
        // 有効期限
        Text(
            text = "有効期限: ${viewModel.packet.expirationYear}年${viewModel.packet.expirationMonth}月",
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true, name = "プレビュー用画像管理セクション")
@Composable
fun PreviewImageManagementSectionPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val viewModel = com.example.seedstockkeeper6.preview.createPreviewSeedInputViewModel()
        PreviewImageManagementSection(viewModel)
    }
}

@Preview(showBackground = true, name = "プレビュー用栽培情報セクション")
@Composable
fun PreviewCultivationInfoSectionPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val viewModel = com.example.seedstockkeeper6.preview.createPreviewSeedInputViewModel()
        PreviewCultivationInfoSection(viewModel)
    }
}
