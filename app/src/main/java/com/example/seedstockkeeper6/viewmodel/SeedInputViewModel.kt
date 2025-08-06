package com.example.seedstockkeeper6.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.data.runGeminiOcr
import com.example.seedstockkeeper6.data.uriToBitmap
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import org.tensorflow.lite.support.image.TensorImage
import com.example.seedstockkeeper6.ml.CalendarDetector
import java.io.FileOutputStream

class SeedInputViewModel : ViewModel() {

    var packet by mutableStateOf(SeedPacket())
        private set

    val imageUris = mutableStateListOf<Uri>()
    var ocrTargetIndex by mutableStateOf(-1)
        private set

    var showSnackbar by mutableStateOf<String?>(null)
    var showAIDiffDialog by mutableStateOf(false)
    var aiDiffList = mutableStateListOf<Triple<String, String, String>>()
        private set

    var isLoading by mutableStateOf(false)

    fun setSeed(seed: SeedPacket?) {
        packet = seed ?: SeedPacket()
        imageUris.clear()
        seed?.imageUrls?.forEach { url ->
            imageUris.add(Uri.parse(url))
        }
        ocrTargetIndex = if (imageUris.isNotEmpty()) 0 else -1
    }

    fun addImages(uris: List<Uri>) {
        imageUris.addAll(uris)
        if (ocrTargetIndex == -1 && imageUris.isNotEmpty()) {
            ocrTargetIndex = 0
        }
    }

    fun setOcrTarget(index: Int) {
        if (index in imageUris.indices) {
            ocrTargetIndex = index
        }
    }

    fun removeImage(index: Int) {
        if (index !in imageUris.indices) return

        // URI を取り出す（ログ出力にも使用）
        val uri = imageUris[index]
        Log.d("SeedInputVM", "画面上から画像削除: $uri")

        // 表示用 URI リストから削除
        imageUris.removeAt(index)

        // OCR ターゲット調整
        if (ocrTargetIndex == index) {
            ocrTargetIndex = if (imageUris.isNotEmpty()) 0 else -1
        } else if (ocrTargetIndex > index) {
            ocrTargetIndex--
        }

        // ※ Firebase Storage 削除はここでは行わない
    }



    suspend fun performOcr(context: Context) {
        if (ocrTargetIndex !in imageUris.indices) {
            showSnackbar = "対象の画像がありません。"
            return
        }

        val uri = imageUris[ocrTargetIndex]
        val bmp = try {
            withContext(Dispatchers.IO) {
                when (uri.scheme) {
                    "content" -> uriToBitmap(context, uri)
                    "https", "http" -> {
                        val urlConnection = URL(uri.toString()).openConnection() as HttpURLConnection
                        urlConnection.connectTimeout = 15000
                        urlConnection.readTimeout = 15000
                        urlConnection.doInput = true
                        urlConnection.connect()
                        if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                            val bitmap = BitmapFactory.decodeStream(urlConnection.inputStream)
                            urlConnection.inputStream.close()
                            bitmap
                        } else {
                            Log.e("OCR_Download", "Failed response: ${urlConnection.responseCode}")
                            null
                        }
                    }
                    null, "" -> {
                        // ここでFirebase StorageパスだったらdownloadUrlを取得してダウンロードする
                        val path = uri.toString()
                        val downloadUrl =
                            getDownloadUrlFromPath(path) // 既存のsuspend関数
                        if (downloadUrl != null) {
                            val urlConnection = URL(downloadUrl).openConnection() as HttpURLConnection
                            urlConnection.connectTimeout = 15000
                            urlConnection.readTimeout = 15000
                            urlConnection.doInput = true
                            urlConnection.connect()
                            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                                val bitmap = BitmapFactory.decodeStream(urlConnection.inputStream)
                                urlConnection.inputStream.close()
                                bitmap
                            } else null
                        } else null
                    }
                    else -> null
                }
            }
        } catch (e: Exception) {
            Log.e("OCR", "Error loading image: $uri", e)
            showSnackbar = "画像の読み込みに失敗しました。"
            return
        }

        if (bmp == null) {
            showSnackbar = "画像が読み込めませんでした。"
            return
        }

        val jsonText = try {
            runGeminiOcr(context, bmp)
        } catch (e: Exception) {
            Log.e("OCR_Gemini", "解析失敗", e)
            showSnackbar = "AI解析中にエラーが発生しました"
            return
        }

        val parsed = try {
            val cleanedJson = jsonText.removePrefix("```json").removeSuffix("```" ).trim()
            kotlinx.serialization.json.Json.decodeFromString<SeedPacket>(cleanedJson)
        } catch (e: Exception) {
            Log.e("OCR_Parse", "解析結果のJSON変換失敗", e)
            showSnackbar = "解析結果の読み取りに失敗しました"
            return
        }

        val newDiffs = mutableListOf<Triple<String, String, String>>()

        if (packet.productName.isEmpty() && packet.variety.isEmpty() && packet.family.isEmpty()) {
            packet = parsed
            showSnackbar = "AI解析結果を反映しました"
            try {
                tryAddCroppedCalendarImage(context, bmp)
            } catch (e: Exception) {
                Log.e("MLCrop", "performOcr内の切り抜き失敗", e)
            }
            return
        }

        if (packet.productName != parsed.productName) newDiffs.add(Triple("商品名", packet.productName, parsed.productName))
        if (packet.variety != parsed.variety) newDiffs.add(Triple("品種", packet.variety, parsed.variety))
        if (packet.family != parsed.family) newDiffs.add(Triple("科名", packet.family, parsed.family))
        if (packet.company != parsed.company) newDiffs.add(Triple("会社", packet.company, parsed.company))
        if (packet.expirationDate != parsed.expirationDate) newDiffs.add(Triple("有効期限", packet.expirationDate, parsed.expirationDate))
        if (packet.contents != parsed.contents) newDiffs.add(Triple("内容量", packet.contents, parsed.contents))

        aiDiffList.clear()
        aiDiffList.addAll(newDiffs)
        if (newDiffs.isNotEmpty()) {
            showAIDiffDialog = true
        } else {
            showSnackbar = "差異はありませんでした"
        }
    }

    fun applyAIDiffResult() {
        if (aiDiffList.isNotEmpty()) {
            aiDiffList.forEach { (label, _, aiValue) ->
                when (label) {
                    "商品名" -> onProductNameChange(aiValue)
                    "品種" -> onVarietyChange(aiValue)
                    "科名" -> onFamilyChange(aiValue)
                    "会社" -> onCompanyChange(aiValue)
                    "有効期限" -> onExpirationDateChange(aiValue)
                    "内容量" -> onContentsChange(aiValue)
                }
            }
            showSnackbar = "AI解析結果を反映しました"
        }
        showAIDiffDialog = false
    }

    fun onAIDiffDialogDismiss() {
        showAIDiffDialog = false
    }

    fun onProductNameChange(value: String) { packet = packet.copy(productName = value) }
    fun onVarietyChange(value: String) { packet = packet.copy(variety = value) }
    fun onFamilyChange(value: String) { packet = packet.copy(family = value) }
    fun onProductNumberChange(value: String) { packet = packet.copy(productNumber = value) }
    fun onCompanyChange(value: String) { packet = packet.copy(company = value) }
    fun onOriginCountryChange(value: String) { packet = packet.copy(originCountry = value) }
    fun onExpirationDateChange(value: String) { packet = packet.copy(expirationDate = value) }
    fun onContentsChange(value: String) { packet = packet.copy(contents = value) }
    fun onGerminationRateChange(value: String) { packet = packet.copy(germinationRate = value) }
    fun onSeedTreatmentChange(value: String) { packet = packet.copy(seedTreatment = value) }

    fun onSpacingRowMinChange(value: String) {
        val new = packet.cultivation.copy(spacing_cm_row_min = value.toIntOrNull() ?: 0)
        packet = packet.copy(cultivation = new)
    }
    fun onSpacingRowMaxChange(value: String) {
        val new = packet.cultivation.copy(spacing_cm_row_max = value.toIntOrNull() ?: 0)
        packet = packet.copy(cultivation = new)
    }
    fun onSpacingPlantMinChange(value: String) {
        val new = packet.cultivation.copy(spacing_cm_plant_min = value.toIntOrNull() ?: 0)
        packet = packet.copy(cultivation = new)
    }
    fun onSpacingPlantMaxChange(value: String) {
        val new = packet.cultivation.copy(spacing_cm_plant_max = value.toIntOrNull() ?: 0)
        packet = packet.copy(cultivation = new)
    }
    fun onGermTempChange(value: String) {
        val new = packet.cultivation.copy(germinationTemp_c = value)
        packet = packet.copy(cultivation = new)
    }
    fun onGrowTempChange(value: String) {
        val new = packet.cultivation.copy(growingTemp_c = value)
        packet = packet.copy(cultivation = new)
    }
    fun onCompostChange(value: String) {
        val newSoil = packet.cultivation.soilPrep_per_sqm.copy(compost_kg = value.toIntOrNull() ?: 0)
        val new = packet.cultivation.copy(soilPrep_per_sqm = newSoil)
        packet = packet.copy(cultivation = new)
    }
    fun onLimeChange(value: String) {
        val newSoil = packet.cultivation.soilPrep_per_sqm.copy(dolomite_lime_g = value.toIntOrNull() ?: 0)
        val new = packet.cultivation.copy(soilPrep_per_sqm = newSoil)
        packet = packet.copy(cultivation = new)
    }
    fun onFertilizerChange(value: String) {
        val newSoil = packet.cultivation.soilPrep_per_sqm.copy(chemical_fertilizer_g = value.toIntOrNull() ?: 0)
        val new = packet.cultivation.copy(soilPrep_per_sqm = newSoil)
        packet = packet.copy(cultivation = new)
    }
    fun onNotesChange(value: String) {
        val new = packet.cultivation.copy(notes = value)
        packet = packet.copy(cultivation = new)
    }
    fun onHarvestingChange(value: String) {
        val new = packet.cultivation.copy(harvesting = value)
        packet = packet.copy(cultivation = new)
    }

    fun onCompanionPlantsChange(value: List<com.example.seedstockkeeper6.model.CompanionPlant>) {
        packet = packet.copy(companionPlants = value)
    }

    fun addCompanionPlant(plant: com.example.seedstockkeeper6.model.CompanionPlant) {
        val newList = packet.companionPlants + plant
        packet = packet.copy(companionPlants = newList)
    }

    fun removeCompanionPlant(index: Int) {
        if (index !in packet.companionPlants.indices) return
        val newList = packet.companionPlants.toMutableList().apply { removeAt(index) }
        packet = packet.copy(companionPlants = newList)
    }

    fun saveSeed(context: Context, onComplete: (Result<Unit>) -> Unit) {
        val db = Firebase.firestore
        val storageRef = Firebase.storage.reference
        val target = packet.documentId?.let {
            db.collection("seeds").document(it)
        } ?: db.collection("seeds").document(UUID.randomUUID().toString())

        val id = target.id

        viewModelScope.launch(Dispatchers.Main) {
            val uploadedPaths = mutableListOf<String>()

            // ストレージからの削除候補を判定
            val currentStrings = imageUris.map { it.toString().trimEnd('/') }
            val toDelete = packet.imageUrls.map { it.trimEnd('/') }
                .filter { it !in currentStrings }

            withContext(Dispatchers.IO) {
                toDelete.forEach { pathUrl ->
                    try {
                        val raw = pathUrl.substringAfter("/o/").substringBefore("?")
                        val decodedPath = raw.replace("%2F", "/")
                        storageRef.child(decodedPath).delete().await()
                        Log.d("SeedInputVM", "Deleted: $decodedPath")
                    } catch (e: Exception) {
                        Log.e("SeedInputVM", "Delete failure: $pathUrl", e)
                    }
                }
            }

            // 画面上で残っている画像をアップロードする
            val existingImagePaths = mutableListOf<String>()
            packet.imageUrls.forEach { existingPath ->
                val uriString = existingPath
                if (imageUris.any { it.toString() == uriString }) {
                    existingImagePaths.add(existingPath)
                }
            }

            withContext(Dispatchers.IO) {
                imageUris.forEachIndexed { index, uri ->
                    val scheme = uri.scheme
                    val bitmap = try {
                        when (scheme) {
                            "content" -> uriToBitmap(context, uri)
                            "https", "http" -> {
                                (URL(uri.toString()).openConnection() as HttpURLConnection).run {
                                    connectTimeout = 15000; readTimeout = 15000; doInput = true
                                    connect()
                                    if (responseCode == HttpURLConnection.HTTP_OK) {
                                        BitmapFactory.decodeStream(inputStream)
                                    } else null
                                }
                            }
                            else -> null
                        }
                    } catch (e: Exception) {
                        Log.e("SeedInputVM", "Bitmap load failed: $uri", e)
                        null
                    }

                    if (bitmap != null) {
                        val baos = ByteArrayOutputStream().apply {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, this)
                        }
                        val bytes = baos.toByteArray()
                        val imagePath = "seed_images/${id}_${index}.jpg"
                        try {
                            storageRef.child(imagePath).putBytes(bytes).await()
                            uploadedPaths.add(imagePath)
                            Log.d("SeedInputVM", "Uploaded path: $imagePath")
                        } catch (e: Exception) {
                            Log.e("SeedInputVM", "Upload fail: $imagePath", e)
                        }
                    }
                }
            }

            uploadedPaths.addAll(existingImagePaths)

            val updatedPacket = packet.copy(
                documentId = id,
                imageUrls = uploadedPaths
            )

            target.set(updatedPacket)
                .addOnSuccessListener {
                    packet = updatedPacket
                    showSnackbar = "保存が完了しました（画像: ${uploadedPaths.size}）"
                    onComplete(Result.success(Unit))
                }
                .addOnFailureListener {
                    showSnackbar =
                        "保存に失敗しました: ${it.localizedMessage ?: "不明なエラー"}"
                    onComplete(Result.failure(it))
                }
        }
    }
    suspend fun getDownloadUrlFromPath(path: String): String? {
        return try {
            Firebase.storage.reference.child(path).downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("Image", "URL取得失敗: $path", e)
            null
        }
    }
    private suspend fun tryAddCroppedCalendarImage(context: Context, bmp: Bitmap) {
        try {
            val model = CalendarDetector.newInstance(context)
            val outputs = model.process(TensorImage.fromBitmap(bmp))
            model.close()

            val locations = outputs.locationsAsTensorBuffer.floatArray
            val scores = outputs.scoresAsTensorBuffer.floatArray
            val numDetections = outputs.numberOfDetectionsAsTensorBuffer.floatArray[0].toInt()

            if (numDetections > 0 && scores[0] > 0.5) {
                val top = (locations[0] * bmp.height).toInt()
                val left = (locations[1] * bmp.width).toInt()
                val bottom = (locations[2] * bmp.height).toInt()
                val right = (locations[3] * bmp.width).toInt()
                val width = right - left
                val height = bottom - top

                val croppedBitmap = Bitmap.createBitmap(bmp, left, top, width, height)
                val file = File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use {
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
                }
                imageUris.add(Uri.fromFile(file))

                Log.d("MLCrop", "新規登録で切り抜き追加成功")
            } else {
                Log.w("MLCrop", "新規登録：有効なカレンダー検出なし")
            }
        } catch (e: Exception) {
            Log.e("MLCrop", "新規登録中の切り抜き失敗", e)
        }
    }

}
