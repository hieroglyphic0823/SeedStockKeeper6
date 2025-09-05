package com.example.seedstockkeeper6.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ImageManagementSection(viewModel: SeedInputViewModel) {
    val context = LocalContext.current
    val cs = rememberCoroutineScope()

    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        viewModel.addImages(uris)
    }

    // 画面幅から3枚のサイズを計算（縦横どちらでも3枚）
    val conf = LocalConfiguration.current
    val screenWidth = conf.screenWidthDp.dp
    val sidePadding = 0.dp           // 左右の余白
    val spacing = 0.dp               // 各アイテムの間隔（3枚だと2箇所）
    val imageSize = (screenWidth - sidePadding * 2 - spacing * 2) / 3

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = sidePadding),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        itemsIndexed(viewModel.imageUris) { index, uri ->
            var downloadUrl by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(uri) {
                if (uri.toString().startsWith("seed_images/")) {
                    val storageRef = Firebase.storage.reference.child(uri.toString())
                    downloadUrl = try {
                        storageRef.downloadUrl.await().toString()
                    } catch (e: Exception) {
                        Log.e("ImageLoad", "URL取得失敗: $uri", e)
                        null
                    }
                } else {
                    downloadUrl = uri.toString()
                }
            }

            Box(
                modifier = Modifier
                    .size(imageSize) // ← 3枚表示に合わせる
                    .padding(end = 2.dp) // ← 隙間は最小限
            ) {
                downloadUrl?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "画像$index",
                        modifier = Modifier
                            .fillMaxSize() // ← Box全体にフィット
                            .clickable { viewModel.setOcrTarget(index) },
                        contentScale = ContentScale.Crop
                    )
                }
                // OCR対象マーク
                if (viewModel.ocrTargetIndex == index) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "OCR対象",
                        tint = Color.Green,
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                }
                // DisplayModeの時は操作ボタンを非表示
                if (viewModel.isEditMode || !viewModel.hasExistingData) {
                    // 画像削除ボタン
                    IconButton(onClick = {
                        cs.launch {
                            val path = viewModel.imageUris[index].toString()
                            if (path.startsWith("seed_images/")) {
                                try {
                                    Firebase.storage.reference.child(path).delete().await()
                                } catch (e: Exception) {
                                    Log.e("SeedInputScreen", "削除失敗: $path", e)
                                }
                            }
                            viewModel.removeImage(index)
                        }
                    }, modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(Icons.Default.Delete, contentDescription = "削除")
                    }
                    // 左右移動ボタン追加
                    if (viewModel.imageUris.size > 1) {
                        // 左へ
                        IconButton(
                            onClick = { viewModel.moveImage(index, index - 1) },
                            enabled = index > 0,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.ChevronLeft,
                                contentDescription = "左へ",
                                tint = Color.White
                            )
                        }

                        // 右へ
                        IconButton(
                            onClick = { viewModel.moveImage(index, index + 1) },
                            enabled = index < viewModel.imageUris.lastIndex,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = "右へ",
                                tint = Color.White
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.selectImage(context, uri) // ← Contextを渡す
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "拡大表示",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp)
                    )
                }
            }
        }
        // DisplayModeの時は画像追加ボタンを非表示
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            item {
                IconButton(onClick = { pickImagesLauncher.launch("image/*") }) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "追加")
                }
            }
        }
    }

    // 操作ボタンセクション
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "画像管理",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // DisplayModeの時は操作ボタンを非表示
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        cs.launch {
                            viewModel.isLoading = true
                            viewModel.performOcr(context)
                            viewModel.isLoading = false
                        }
                    },
                    enabled = viewModel.imageUris.isNotEmpty(),
                    modifier = Modifier.wrapContentWidth(), // 横幅を詰める
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = "OCR")
                    Spacer(Modifier.width(8.dp))
                    Text("AIで解析")
                }

                IconButton(
                    onClick = { viewModel.cropSeedOuterAtOcrTarget(context) },
                    enabled = viewModel.ocrTargetIndex in viewModel.imageUris.indices && !viewModel.isLoading
                ) {
                    Icon(Icons.Outlined.ContentCut, contentDescription = "外側を切り抜く")
                }
            }
        }
    }
}
