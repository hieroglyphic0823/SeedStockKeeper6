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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.seedstockkeeper6.model.Cultivation
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.model.SoilPrep
import com.example.seedstockkeeper6.data.runGeminiOcr
import com.example.seedstockkeeper6.data.uriToBitmap
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*

fun deleteSeedPacketWithImages(documentId: String) {
    val db = Firebase.firestore
    val storage = Firebase.storage

    db.collection("seeds").document(documentId).get().addOnSuccessListener { doc ->
        val imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList()

        imageUrls.forEach { url ->
            val path = Uri.decode(url).substringAfter("/o/").substringBefore("?")
            if (path.isNotEmpty()) {
                storage.reference.child(path).delete()
            }
        }

        db.collection("seeds").document(documentId).delete()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedInputScreen(
    navController: NavController,
    packet: SeedPacket?,
    viewModel: SeedInputViewModel = viewModel()
) {
    LaunchedEffect(packet) {
        viewModel.setSeed(packet)
    }
    val seed = viewModel.packet
    val context = LocalContext.current
    val scroll = rememberScrollState()

    var imageUri by remember { mutableStateOf<Uri?>(packet?.imageUrls?.firstOrNull()?.let { Uri.parse(it) }) }
    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    var isLoadingOcr by remember { mutableStateOf(false) }
    var parsed by remember { mutableStateOf(false) }

    val labelMod = Modifier.fillMaxWidth()
    val id = packet?.id ?: Firebase.firestore.collection("seeds").document().id
    var productName by remember { mutableStateOf(packet?.productName ?: "") }
    var variety by remember { mutableStateOf(packet?.variety ?: "") }
    var family by remember { mutableStateOf(packet?.family ?: "") }
    var productNumber by remember { mutableStateOf(packet?.productNumber ?: "") }
    var company by remember { mutableStateOf(packet?.company ?: "") }
    var originCountry by remember { mutableStateOf(packet?.originCountry ?: "") }
    var expirationYear by remember { mutableStateOf(packet?.expirationDate?.take(4) ?: "") }
    var expirationMonth by remember { mutableStateOf(packet?.expirationDate?.drop(5)?.replace("月", "") ?: "") }
    var contents by remember { mutableStateOf(packet?.contents ?: "") }
    var germinationRate by remember { mutableStateOf(packet?.germinationRate ?: "") }
    var seedTreatment by remember { mutableStateOf(packet?.seedTreatment ?: "") }
    var spacing_cm_row_min by remember { mutableStateOf(packet?.cultivation?.spacing_cm_row_min?.toString() ?: "") }
    var spacing_cm_row_max by remember { mutableStateOf(packet?.cultivation?.spacing_cm_row_max?.toString() ?: "") }
    var spacing_cm_plant_min by remember { mutableStateOf(packet?.cultivation?.spacing_cm_plant_min?.toString() ?: "") }
    var spacing_cm_plant_max by remember { mutableStateOf(packet?.cultivation?.spacing_cm_plant_max?.toString() ?: "") }
    var germTemp by remember { mutableStateOf(packet?.cultivation?.germinationTemp_c ?: "") }
    var growTemp by remember { mutableStateOf(packet?.cultivation?.growingTemp_c ?: "") }
    var compost by remember { mutableStateOf(packet?.cultivation?.soilPrep_per_sqm?.compost_kg?.toString() ?: "") }
    var lime by remember { mutableStateOf(packet?.cultivation?.soilPrep_per_sqm?.dolomite_lime_g?.toString() ?: "") }
    var fertilizer by remember { mutableStateOf(packet?.cultivation?.soilPrep_per_sqm?.chemical_fertilizer_g?.toString() ?: "") }
    var notes by remember { mutableStateOf(packet?.cultivation?.notes ?: "") }
    var harvesting by remember { mutableStateOf(packet?.cultivation?.harvesting ?: "") }

    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp).verticalScroll(scroll)) {
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
        } ?: packet?.imageUrls?.firstOrNull()?.let {
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

        ImagePickerScreen(onImagePicked = { uri ->
            imageUri = uri
            coroutineScope.launch {
                isLoadingOcr = true
                val bmp = withContext(Dispatchers.IO) {
                    uriToBitmap(context, uri)
                }
                bitmap = bmp
                if (!parsed && bmp != null) {
                    val result = runGeminiOcr(context, bmp)
                    var cleaned = result.trim()
                    cleaned = cleaned.removePrefix("\"").removeSuffix("\"")
                    cleaned = cleaned.replace(Regex("^```json\\s*"), "")
                        .replace(Regex("```\\s*$"), "")
                        .replace("\\\"", "\"").replace("\\n", "\n")
                    try {
                        val parsedPacket = Gson().fromJson(cleaned, SeedPacket::class.java)
                        productName = parsedPacket.productName
                        variety = parsedPacket.variety
                        productNumber = parsedPacket.productNumber
                        family = parsedPacket.family
                        company = parsedPacket.company
                        originCountry = parsedPacket.originCountry
                        expirationYear = parsedPacket.expirationDate.take(4)
                        expirationMonth = Regex("\\d{1,2}月").find(parsedPacket.expirationDate)?.value?.replace("月", "") ?: ""
                        contents = parsedPacket.contents
                        germinationRate = parsedPacket.germinationRate
                        seedTreatment = parsedPacket.seedTreatment
                        spacing_cm_row_min = parsedPacket.cultivation.spacing_cm_row_min.toString()
                        spacing_cm_row_max = parsedPacket.cultivation.spacing_cm_row_max.toString()
                        spacing_cm_plant_min = parsedPacket.cultivation.spacing_cm_plant_min.toString()
                        spacing_cm_plant_max = parsedPacket.cultivation.spacing_cm_plant_max.toString()
                        germTemp = parsedPacket.cultivation.germinationTemp_c
                        growTemp = parsedPacket.cultivation.growingTemp_c
                        compost = parsedPacket.cultivation.soilPrep_per_sqm.compost_kg.toString()
                        lime = parsedPacket.cultivation.soilPrep_per_sqm.dolomite_lime_g.toString()
                        fertilizer = parsedPacket.cultivation.soilPrep_per_sqm.chemical_fertilizer_g.toString()
                        notes = parsedPacket.cultivation.notes
                        harvesting = parsedPacket.cultivation.harvesting
                        parsed = true
                    } catch (e: JsonSyntaxException) {
                        Log.e("PARSE", "Gsonパース失敗: ${e.message}")
                    }
                }
                isLoadingOcr = false
            }
        })

        OutlinedTextField(productName, { productName = it }, label = { Text("商品名") }, modifier = labelMod)
        OutlinedTextField(variety, { variety = it }, label = { Text("品種") }, modifier = labelMod)
        OutlinedTextField(family, { family = it }, label = { Text("科名") }, modifier = labelMod)
        OutlinedTextField(productNumber, { productNumber = it }, label = { Text("商品番号") }, modifier = labelMod)
        OutlinedTextField(company, { company = it }, label = { Text("会社") }, modifier = labelMod)
        OutlinedTextField(originCountry, { originCountry = it }, label = { Text("原産国") }, modifier = labelMod)
        OutlinedTextField(expirationYear, { expirationYear = it }, label = { Text("有効期限(年)") }, modifier = labelMod)
        OutlinedTextField(expirationMonth, { expirationMonth = it }, label = { Text("有効期限(月)") }, modifier = labelMod)
        OutlinedTextField(contents, { contents = it }, label = { Text("内容量") }, modifier = labelMod)
        OutlinedTextField(germinationRate, { germinationRate = it }, label = { Text("発芽率") }, modifier = labelMod)
        OutlinedTextField(seedTreatment, { seedTreatment = it }, label = { Text("種子処理") }, modifier = labelMod)
        OutlinedTextField(spacing_cm_row_min, { spacing_cm_row_min = it }, label = { Text("条間最小(cm)") }, modifier = labelMod)
        OutlinedTextField(spacing_cm_row_max, { spacing_cm_row_max = it }, label = { Text("条間最大(cm)") }, modifier = labelMod)
        OutlinedTextField(spacing_cm_plant_min, { spacing_cm_plant_min = it }, label = { Text("株間最小(cm)") }, modifier = labelMod)
        OutlinedTextField(spacing_cm_plant_max, { spacing_cm_plant_max = it }, label = { Text("株間最大(cm)") }, modifier = labelMod)
        OutlinedTextField(germTemp, { germTemp = it }, label = { Text("発芽温度") }, modifier = labelMod)
        OutlinedTextField(growTemp, { growTemp = it }, label = { Text("生育温度") }, modifier = labelMod)
        OutlinedTextField(compost, { compost = it }, label = { Text("堆肥(kg/㎡)") }, modifier = labelMod)
        OutlinedTextField(lime, { lime = it }, label = { Text("苦土石灰(g/㎡)") }, modifier = labelMod)
        OutlinedTextField(fertilizer, { fertilizer = it }, label = { Text("化成肥料(g/㎡)") }, modifier = labelMod)
        OutlinedTextField(notes, { notes = it }, label = { Text("栽培メモ") }, modifier = labelMod)
        OutlinedTextField(harvesting, { harvesting = it }, label = { Text("収穫") }, modifier = labelMod)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val expirationDate = buildString {
                if (expirationYear.isNotBlank()) append(expirationYear).append("年")
                if (expirationMonth.isNotBlank()) append(expirationMonth).append("月")
            }

            val packetToSave = SeedPacket(
                id, productName, variety, family, productNumber, company, originCountry,
                expirationDate, contents, germinationRate, seedTreatment,
                imageUrls = listOfNotNull(imageUri?.toString()),
                cultivation = Cultivation(
                    spacing_cm_row_min = spacing_cm_row_min.toIntOrNull() ?: 0,
                    spacing_cm_row_max = spacing_cm_row_max.toIntOrNull() ?: 0,
                    spacing_cm_plant_min = spacing_cm_plant_min.toIntOrNull() ?: 0,
                    spacing_cm_plant_max = spacing_cm_plant_max.toIntOrNull() ?: 0,
                    germinationTemp_c = germTemp,
                    growingTemp_c = growTemp,
                    soilPrep_per_sqm = SoilPrep(
                        compost.toIntOrNull() ?: 0,
                        lime.toIntOrNull() ?: 0,
                        fertilizer.toIntOrNull() ?: 0
                    ),
                    notes = notes,
                    harvesting = harvesting
                )
            )

            val db = Firebase.firestore
            val storage = Firebase.storage

            val docRef = db.collection("seeds").document()
            val uploadTasks = mutableListOf<Task<*>>()

            bitmap?.let {
                val baos = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val data = baos.toByteArray()
                val uuid = UUID.randomUUID().toString()
                val path = "seed_images/$uuid.jpg"
                val uploadTask = storage.reference.child(path).putBytes(data)
                uploadTasks.add(uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                    storage.reference.child(path).downloadUrl
                }.addOnSuccessListener { uri ->
                    docRef.set(packetToSave.copy(imageUrls = listOf(uri.toString()))).addOnSuccessListener {
                        navController.popBackStack()
                    }
                })
            } ?: run {
                docRef.set(packetToSave).addOnSuccessListener {
                    navController.popBackStack()
                }
            }
        }) {
            Text("保存")
        }
    }
}
