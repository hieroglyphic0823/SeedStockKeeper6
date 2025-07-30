package com.example.seedstockkeeper6.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.seedstockkeeper6.data.runGeminiOcr
import com.example.seedstockkeeper6.data.uriToBitmap
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedInputScreen(
    navController: NavController,
    viewModel: SeedInputViewModel = viewModel()
) {
    val scroll = rememberScrollState()
    val context = LocalContext.current
    val packet = viewModel.packet

    Column(modifier = Modifier.padding(16.dp).verticalScroll(scroll)) {
        viewModel.imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
        } ?: Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = "デフォルト画像",
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(32.dp)
        )

        val coroutineScope = rememberCoroutineScope()
        ImagePickerScreen(onImagePicked = { uri ->
            viewModel.onImageSelected(uri)
            coroutineScope.launch(Dispatchers.IO) {
                val bmp = uriToBitmap(context, uri)
                viewModel.setBitmap(bmp)
                (context as? ComponentActivity)?.lifecycleScope?.launch {
                    val result = runGeminiOcr(context, bmp)
                    var cleaned = result.trim()
                    cleaned = cleaned.removePrefix("\"").removeSuffix("\"")
                        .replace(Regex("^```json\\s*"), "")
                        .replace(Regex("```\\s*$"), "")
                        .replace("\\\"", "\"").replace("\\n", "\n")
                    try {
                        val parsed = Gson().fromJson(cleaned, SeedPacket::class.java)
                        viewModel.applyOcrResult(parsed)
                    } catch (e: JsonSyntaxException) {
                        Log.e("PARSE", "Gsonパース失敗: \${e.message}")
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
        OutlinedTextField(packet.cultivation.notes, viewModel::onNotesChange, label = { Text("栽培メモ") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(packet.cultivation.harvesting, viewModel::onHarvestingChange, label = { Text("収穫") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))
    }
}
