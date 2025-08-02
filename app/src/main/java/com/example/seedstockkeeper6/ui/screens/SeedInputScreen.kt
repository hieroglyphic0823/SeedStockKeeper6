// SeedInputScreen.kt

package com.example.seedstockkeeper6.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.seedstockkeeper6.ui.components.AIDiffDialog
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

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

    Column(modifier = Modifier.verticalScroll(scroll).padding(16.dp)) {
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
                    viewModel.performOcr(context)
                }
            },
            enabled = viewModel.imageUris.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
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
    }

    AIDiffDialog(
        showDialog = viewModel.showAIDiffDialog,
        diffList = viewModel.aiDiffList,
        onConfirm = { viewModel.applyAIDiffResult() },
        onDismiss = { viewModel.onAIDiffDialogDismiss() }
    )
}
