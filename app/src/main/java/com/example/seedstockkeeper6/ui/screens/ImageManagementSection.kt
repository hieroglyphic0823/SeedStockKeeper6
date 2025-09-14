package com.example.seedstockkeeper6.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import com.example.seedstockkeeper6.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 画像管理タイトル
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.album),
                contentDescription = "画像管理",
                modifier = Modifier.size(24.dp)
            )
            Text(
                "画像管理",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
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
                    .then(
                        if (viewModel.ocrTargetIndex == index) {
                            Modifier.border(
                                width = 8.dp,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
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
                        Image(
                            painter = painterResource(id = R.drawable.delete_button),
                            contentDescription = "削除",
                            modifier = Modifier.size(24.dp)
                        )
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
                    Image(
                        painter = painterResource(id = R.drawable.glass_pr),
                        contentDescription = "拡大表示",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        // DisplayModeの時は画像追加ボタンを非表示
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            item {
                IconButton(onClick = { pickImagesLauncher.launch("image/*") }) {
                    Image(
                        painter = painterResource(id = R.drawable.add_pr),
                        contentDescription = "追加",
                        modifier = Modifier.size(24.dp)
                    )
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
        
        // DisplayModeの時は操作ボタンを非表示
        if (viewModel.isEditMode || !viewModel.hasExistingData) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    Text(
                        text = "AIで解析",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.star_opc),
                        contentDescription = "OCR",
                        modifier = Modifier.size(24.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            if (viewModel.imageUris.isNotEmpty()) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    )
                }

                IconButton(
                    onClick = { viewModel.cropSeedOuterAtOcrTarget(context) },
                    enabled = viewModel.ocrTargetIndex in viewModel.imageUris.indices && !viewModel.isLoading
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.crop),
                        contentDescription = "外側を切り抜く",
                        modifier = Modifier.size(24.dp),
                        colorFilter = if (viewModel.ocrTargetIndex in viewModel.imageUris.indices && !viewModel.isLoading) {
                            null
                        } else {
                            androidx.compose.ui.graphics.ColorFilter.tint(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    )
                }
            }
        }
        }
    }
    
    // 拡大表示のモーダルダイアログ
    viewModel.selectedImageUri?.let { uri ->
        Dialog(
            onDismissRequest = { viewModel.clearSelectedImage() },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
            ) {
                // 拡大表示する画像（横幅90%）
                AsyncImage(
                    model = uri.toString(),
                    contentDescription = "拡大表示",
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.9f)
                        .align(Alignment.Center)
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                
                // ×ボタン（右上）- 常に表示
                IconButton(
                    onClick = { viewModel.clearSelectedImage() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "閉じる",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
