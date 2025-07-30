package com.example.seedstockkeeper6.ui.screens

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.seedstockkeeper6.data.runGeminiOcr
import com.example.seedstockkeeper6.data.uriToBitmap
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedInputScreen(
    navController: NavController,
    viewModel: SeedInputViewModel = viewModel()
) {
    val packet = viewModel.packet
    val context = LocalContext.current
    val scroll = rememberScrollState()

    Column(modifier = Modifier.verticalScroll(scroll).padding(16.dp)) {
        val imageModifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(bottom = 16.dp)

        when {
            viewModel.imageUri.value != null -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(viewModel.imageUri.value)
                        .crossfade(true)
                        .build(),
                    contentDescription = "選択された画像",
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            }
            packet.imageUrls.isNotEmpty() -> {
                AsyncImage(
                    model = packet.imageUrls.first(),
                    contentDescription = packet.productName,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
                Log.d("SeedInputScreen", "Firebase image URL: ${packet.imageUrls.firstOrNull()}")
            }
            else -> {
                Icon(
                    Icons.Default.Eco,
                    contentDescription = "デフォルト画像",
                    modifier = imageModifier.padding(32.dp)
                )
            }
        }

        val cs = rememberCoroutineScope()
        ImagePickerScreen(onImagePicked = { uri ->
            viewModel.onImageSelected(uri)
            cs.launch(Dispatchers.IO) {
                val bmp = uriToBitmap(context, uri)
                viewModel.setBitmap(bmp)
                (context as? ComponentActivity)?.lifecycleScope?.launch {
                    val parsed = runGeminiOcr(context, bmp)
                    val cleanedJson = parsed.removePrefix("```json").removeSuffix("```").trim()
                    parsed?.let {
                        val parsedPacket = Gson().fromJson(cleanedJson, SeedPacket::class.java)
                        viewModel.applyOcrResult(parsedPacket)
                    }
                }
            }
        })

        OutlinedTextField(packet.productName, viewModel::onProductNameChange, label = { Text("商品名") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.variety, viewModel::onVarietyChange, label = { Text("品種") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.family, viewModel::onFamilyChange, label = { Text("科名") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.productNumber, viewModel::onProductNumberChange, label = { Text("商品番号") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.company, viewModel::onCompanyChange, label = { Text("会社") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.originCountry, viewModel::onOriginCountryChange, label = { Text("原産国") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.expirationDate, viewModel::onExpirationDateChange, label = { Text("有効期限") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.contents, viewModel::onContentsChange, label = { Text("内容量") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.germinationRate, viewModel::onGerminationRateChange, label = { Text("発芽率") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.seedTreatment, viewModel::onSeedTreatmentChange, label = { Text("種子処理") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(packet.cultivation.spacing_cm_row_min.toString(), viewModel::onSpacingRowMinChange, label = { Text("条間最小 (cm)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.cultivation.spacing_cm_row_max.toString(), viewModel::onSpacingRowMaxChange, label = { Text("条間最大 (cm)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.cultivation.spacing_cm_plant_min.toString(), viewModel::onSpacingPlantMinChange, label = { Text("株間最小 (cm)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.cultivation.spacing_cm_plant_max.toString(), viewModel::onSpacingPlantMaxChange, label = { Text("株間最大 (cm)") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(packet.cultivation.germinationTemp_c, viewModel::onGermTempChange, label = { Text("発芽温度") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.cultivation.growingTemp_c, viewModel::onGrowTempChange, label = { Text("生育温度") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(packet.cultivation.soilPrep_per_sqm.compost_kg.toString(), viewModel::onCompostChange, label = { Text("堆肥 (kg/㎡)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.cultivation.soilPrep_per_sqm.dolomite_lime_g.toString(), viewModel::onLimeChange, label = { Text("苦土石灰 (g/㎡)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.cultivation.soilPrep_per_sqm.chemical_fertilizer_g.toString(), viewModel::onFertilizerChange, label = { Text("化成肥料 (g/㎡)") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(packet.cultivation.notes, viewModel::onNotesChange, label = { Text("栽培メモ") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.cultivation.harvesting, viewModel::onHarvestingChange, label = { Text("収穫") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))
    }
}
