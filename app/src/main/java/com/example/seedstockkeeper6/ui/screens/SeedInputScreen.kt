// SeedInputScreen.kt

package com.example.seedstockkeeper6.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.seedstockkeeper6.ui.components.AIDiffDialog
import com.example.seedstockkeeper6.ui.components.CompanionEffectIcon
import com.example.seedstockkeeper6.ui.components.SeedCalendar
import com.example.seedstockkeeper6.ui.components.SeedCalendarFromEntries
import com.example.seedstockkeeper6.ui.components.buildBands
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SeedInputScreen(
    navController: NavController,
    viewModel: SeedInputViewModel
) {
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val cs = rememberCoroutineScope()

    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        Log.d("ImagePicker", "URIs received: $uris")
        viewModel.addImages(uris)
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(scroll)
                .padding(horizontal = 2.dp, vertical = 16.dp)
                .fillMaxWidth()
        ) {
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
                        // 画像削除ボタン
                        IconButton(onClick = {
                            cs.launch {
                                val path = viewModel.imageUris[index].toString()
                                if (path.startsWith("seed_images/")) {
                                    try {
                                        Firebase.storage.reference.child(path).delete().await()
                                        Log.d("SeedInputScreen", "削除成功: $path")
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
                        val context = LocalContext.current

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
                item {
                    IconButton(onClick = { pickImagesLauncher.launch("image/*") }) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "追加")
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
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
                    modifier = Modifier.weight(1f) // 必要なら横幅を取りたい時
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
            OutlinedTextField(
                viewModel.packet.productName,
                viewModel::onProductNameChange,
                label = { Text("商品名") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.variety,
                viewModel::onVarietyChange,
                label = { Text("品種") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.family,
                viewModel::onFamilyChange,
                label = { Text("科名") },
                modifier = Modifier.fillMaxWidth()
            )
            // ---- 地域別 まきどき / 収穫カレンダー ----
            val regionColors = mapOf(
                "関東" to Color(0xFF42A5F5),
                "関西" to Color(0xFF66BB6A)
            )
            SeedCalendarFromEntries(
                entries = viewModel.packet.calendar ?: emptyList(),
                regionColors = regionColors,
                modifier = Modifier.fillMaxWidth(),
                heightDp = 140
            )

            Text("栽培カレンダー", style = MaterialTheme.typography.titleMedium)

            viewModel.packet.calendar.forEachIndexed { index, entry ->
                Text("地域 ${index + 1}", style = MaterialTheme.typography.bodyLarge)

                OutlinedTextField(
                    value = entry.region,
                    onValueChange = { viewModel.updateCalendarRegion(index, it) },
                    label = { Text("地域") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = entry.sowing_start?.toString() ?: "",
                    onValueChange = { viewModel.updateCalendarSowingStart(index, it.toIntOrNull() ?: 0) },
                    label = { Text("播種開始（月）") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = entry.sowing_start_stage ?: "",
                    onValueChange = { viewModel.updateCalendarSowingStartStage(index, it) },
                    label = { Text("播種開始（旬）") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = entry.sowing_end?.toString() ?: "",
                    onValueChange = { viewModel.updateCalendarSowingEnd(index, it.toIntOrNull() ?: 0) },
                    label = { Text("播種終了（月）") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = entry.sowing_end_stage ?: "",
                    onValueChange = { viewModel.updateCalendarSowingEndStage(index, it) },
                    label = { Text("播種終了（旬）") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = entry.harvest_start?.toString() ?: "",
                    onValueChange = { viewModel.updateCalendarHarvestStart(index, it.toIntOrNull() ?: 0) },
                    label = { Text("収穫開始（月）") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = entry.harvest_start_stage ?: "",
                    onValueChange = { viewModel.updateCalendarHarvestStartStage(index, it) },
                    label = { Text("収穫開始（旬）") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = entry.harvest_end?.toString() ?: "",
                    onValueChange = { viewModel.updateCalendarHarvestEnd(index, it.toIntOrNull() ?: 0) },
                    label = { Text("収穫終了（月）") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = entry.harvest_end_stage ?: "",
                    onValueChange = { viewModel.updateCalendarHarvestEndStage(index, it) },
                    label = { Text("収穫終了（旬）") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(onClick = { viewModel.addCalendarEntry() }, modifier = Modifier.fillMaxWidth()) {
                Text("行を追加")
            }

            OutlinedTextField(
                viewModel.packet.productNumber,
                viewModel::onProductNumberChange,
                label = { Text("商品番号") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.company,
                viewModel::onCompanyChange,
                label = { Text("会社") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.originCountry,
                viewModel::onOriginCountryChange,
                label = { Text("原産国") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.expirationDate,
                viewModel::onExpirationDateChange,
                label = { Text("有効期限") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.contents,
                viewModel::onContentsChange,
                label = { Text("内容量") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.germinationRate,
                viewModel::onGerminationRateChange,
                label = { Text("発芽率") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.seedTreatment,
                viewModel::onSeedTreatmentChange,
                label = { Text("種子処理") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                viewModel.packet.cultivation.spacing_cm_row_min.toString(),
                viewModel::onSpacingRowMinChange,
                label = { Text("条間最小 (cm)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.cultivation.spacing_cm_row_max.toString(),
                viewModel::onSpacingRowMaxChange,
                label = { Text("条間最大 (cm)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.cultivation.spacing_cm_plant_min.toString(),
                viewModel::onSpacingPlantMinChange,
                label = { Text("株間最小 (cm)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.cultivation.spacing_cm_plant_max.toString(),
                viewModel::onSpacingPlantMaxChange,
                label = { Text("株間最大 (cm)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                viewModel.packet.cultivation.germinationTemp_c,
                viewModel::onGermTempChange,
                label = { Text("発芽温度") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.cultivation.growingTemp_c,
                viewModel::onGrowTempChange,
                label = { Text("生育温度") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                viewModel.packet.cultivation.soilPrep_per_sqm.compost_kg.toString(),
                viewModel::onCompostChange,
                label = { Text("堆肥 (kg/㎡)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.cultivation.soilPrep_per_sqm.dolomite_lime_g.toString(),
                viewModel::onLimeChange,
                label = { Text("苦土石灰 (g/㎡)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.cultivation.soilPrep_per_sqm.chemical_fertilizer_g.toString(),
                viewModel::onFertilizerChange,
                label = { Text("化成肥料 (g/㎡)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                viewModel.packet.cultivation.notes,
                viewModel::onNotesChange,
                label = { Text("栽培メモ") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                viewModel.packet.cultivation.harvesting,
                viewModel::onHarvestingChange,
                label = { Text("収穫") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- コンパニオンプランツ表示＆追加部 ---
            Text("コンパニオンプランツと効果", style = MaterialTheme.typography.titleMedium)
            viewModel.packet.companionPlants.forEachIndexed { i, companion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "植物: ${companion.plant} ",
                            Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.size(8.dp)) // アイコンとの間隔
                        CompanionEffectIcon(companion.effect) // ← アイコン表示
                    }
                }
            }
            var cpPlant by remember { mutableStateOf("") }
            var cpEffect by remember { mutableStateOf("") }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    cpPlant,
                    { cpPlant = it },
                    label = { Text("植物名") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    cpEffect,
                    { cpEffect = it },
                    label = { Text("効果") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (cpPlant.isNotBlank() || cpEffect.isNotBlank()) {
                        viewModel.addCompanionPlant(
                            com.example.seedstockkeeper6.model.CompanionPlant(
                                cpPlant,
                                cpEffect
                            )
                        )
                        cpPlant = ""
                        cpEffect = ""
                    }
                }) {
                    Text("追加")
                }
            }
            // --- ここまでコンパニオンプランツ部 ---

        }
        if (viewModel.showCropConfirmDialog) {
            CropConfirmDialog(viewModel = viewModel)
        }
        if (viewModel.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(999f),
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation()
            }
        }
    }

    AIDiffDialog(
        showDialog = viewModel.showAIDiffDialog,
        diffList = viewModel.aiDiffList,
        onConfirm = { viewModel.applyAIDiffResult() },
        onDismiss = { viewModel.onAIDiffDialogDismiss() }
    )
    if (viewModel.selectedImageBitmap != null) {
        Dialog(onDismissRequest = { viewModel.clearSelectedImage() }) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            val context = LocalContext.current

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            offset += pan
                        }
                    }
            ) {
                Image(
                    bitmap = viewModel.selectedImageBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .align(Alignment.Center)
                        .widthIn(max = 300.dp)
                        .heightIn(max = 400.dp)
                )

                IconButton(
                    onClick = {
                        Log.d("SeedInputScreen", "回転アイコンがクリックされた")
                        viewModel.selectedImageUri?.let { uri ->
                            viewModel.rotateAndReplaceImage(context, uri, 90f)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "回転",
                        tint = Color.White
                    )
                }
            }
        }
    }


}

@Composable
fun CropConfirmDialog(viewModel: SeedInputViewModel) {
    val ctx = LocalContext.current
    if (!viewModel.showCropConfirmDialog) return
    val preview = viewModel.pendingCropBitmap

    AlertDialog(
        onDismissRequest = { viewModel.cancelCropReplace() },
        title = { Text("画像を差し替えますか？") },
        text = {
            if (preview != null) {
                Image(
                    bitmap = preview.asImageBitmap(),
                    contentDescription = "切り抜きプレビュー",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 360.dp)
                )
            } else {
                Text("プレビューを表示できませんでした")
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.confirmCropReplace(ctx) }) {
                Text("はい")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.cancelCropReplace() }) {
                Text("いいえ")
            }
        }
    )
}


@Composable
fun LoadingAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("AI_network.json"))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress = { progress }
    )

}
