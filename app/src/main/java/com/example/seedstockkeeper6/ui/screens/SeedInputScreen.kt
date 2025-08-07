// SeedInputScreen.kt

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.seedstockkeeper6.ui.components.AIDiffDialog
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

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
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            LazyRow(verticalAlignment = Alignment.CenterVertically) {
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

                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        downloadUrl?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = "画像$index",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setOcrTarget(index) },
                                contentScale = ContentScale.Crop
                            )
                        }

                        if (viewModel.ocrTargetIndex == index) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "OCR対象",
                                tint = Color.Green,
                                modifier = Modifier.align(Alignment.TopStart)
                            )
                        }
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
                        IconButton(
                            onClick = {
                                viewModel.selectImage(uri)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
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

            Button(
                onClick = {
                    cs.launch {
                        viewModel.isLoading = true
                        viewModel.performOcr(context)
                        viewModel.isLoading = false
                    }
                },
                enabled = viewModel.imageUris.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = "OCR")
                Spacer(Modifier.width(8.dp))
                Text("AIで解析")
            }

            OutlinedTextField(viewModel.packet.productName, viewModel::onProductNameChange, label = { Text("商品名") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.variety, viewModel::onVarietyChange, label = { Text("品種") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.family, viewModel::onFamilyChange, label = { Text("科名") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.productNumber, viewModel::onProductNumberChange, label = { Text("商品番号") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.company, viewModel::onCompanyChange, label = { Text("会社") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.originCountry, viewModel::onOriginCountryChange, label = { Text("原産国") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.expirationDate, viewModel::onExpirationDateChange, label = { Text("有効期限") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.contents, viewModel::onContentsChange, label = { Text("内容量") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.germinationRate, viewModel::onGerminationRateChange, label = { Text("発芽率") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.seedTreatment, viewModel::onSeedTreatmentChange, label = { Text("種子処理") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(viewModel.packet.cultivation.spacing_cm_row_min.toString(), viewModel::onSpacingRowMinChange, label = { Text("条間最小 (cm)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.cultivation.spacing_cm_row_max.toString(), viewModel::onSpacingRowMaxChange, label = { Text("条間最大 (cm)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.cultivation.spacing_cm_plant_min.toString(), viewModel::onSpacingPlantMinChange, label = { Text("株間最小 (cm)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.cultivation.spacing_cm_plant_max.toString(), viewModel::onSpacingPlantMaxChange, label = { Text("株間最大 (cm)") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(viewModel.packet.cultivation.germinationTemp_c, viewModel::onGermTempChange, label = { Text("発芽温度") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.cultivation.growingTemp_c, viewModel::onGrowTempChange, label = { Text("生育温度") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(viewModel.packet.cultivation.soilPrep_per_sqm.compost_kg.toString(), viewModel::onCompostChange, label = { Text("堆肥 (kg/㎡)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.cultivation.soilPrep_per_sqm.dolomite_lime_g.toString(), viewModel::onLimeChange, label = { Text("苦土石灰 (g/㎡)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.cultivation.soilPrep_per_sqm.chemical_fertilizer_g.toString(), viewModel::onFertilizerChange, label = { Text("化成肥料 (g/㎡)") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(viewModel.packet.cultivation.notes, viewModel::onNotesChange, label = { Text("栽培メモ") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(viewModel.packet.cultivation.harvesting, viewModel::onHarvestingChange, label = { Text("収穫") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            // --- コンパニオンプランツ表示＆追加部 ---
            Text("コンパニオンプランツと効果", style = MaterialTheme.typography.titleMedium)
            viewModel.packet.companionPlants.forEachIndexed { i, companion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "植物: ${companion.plant} ／ 効果: ${companion.effect}",
                            Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.removeCompanionPlant(i) }) {
                            Icon(Icons.Default.Delete, contentDescription = "削除")
                        }
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
    if (viewModel.selectedImageUri != null) {
        Dialog(onDismissRequest = { viewModel.clearSelectedImage() }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { viewModel.clearSelectedImage() }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(viewModel.selectedImageUri),
                    Log.d("TAG", "selectedImageUri = ${viewModel.selectedImageUri}"),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun LoadingAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("AI_network.json"))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    LottieAnimation(
        composition = composition,
        progress = { progress }
    )
}
