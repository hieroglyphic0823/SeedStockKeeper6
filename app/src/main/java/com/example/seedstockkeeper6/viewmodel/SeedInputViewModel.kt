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

        val uri = imageUris[index]

        // まず画面から削除
        imageUris.removeAt(index)

        // OCR 対象インデックスの更新
        if (ocrTargetIndex == index) {
            ocrTargetIndex = if (imageUris.isNotEmpty()) 0 else -1
        } else if (ocrTargetIndex > index) {
            ocrTargetIndex--
        }

        // Firebase Storage に保存されている画像なら削除
        if (uri.toString().startsWith("https://firebasestorage.googleapis.com")) {
            val path = uri.toString()
                .substringAfter("/o/")
                .substringBefore("?") // 例: seed_images%2Fxxx.jpg
                .replace("%2F", "/")
            viewModelScope.launch {
                try {
                    Firebase.storage.reference.child(path).delete().await()
                    Log.d("SeedInputVM", "Firebase画像削除成功: $path")
                } catch (e: Exception) {
                    Log.e("SeedInputVM", "Firebase画像削除失敗: $path", e)
                }
            }
        }
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

    fun saveSeed(context: Context, onComplete: (Result<Unit>) -> Unit) {
        val db = Firebase.firestore
        val storageRef = Firebase.storage.reference
        val target = packet.documentId?.let { db.collection("seeds").document(it) }
            ?: db.collection("seeds").document(UUID.randomUUID().toString())

        val id = target.id

        viewModelScope.launch(Dispatchers.Main) {
            val uploadedUrls = mutableListOf<String>()

            withContext(Dispatchers.IO) {
                Log.d("Upload", "画像数: ${imageUris.size}")

                imageUris.forEachIndexed { index, uri ->
                    val bitmap = try {
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
                                } else null
                            }
                            else -> null
                        }
                    } catch (e: Exception) {
                        Log.e("Upload", "画像読み込み失敗: $uri", e)
                        null
                    }

                    if (bitmap != null) {
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                        val bytes = baos.toByteArray()
                        val imagePath = "seed_images/${id}_${index}.jpg"
                        val imageRef = storageRef.child(imagePath)
                        try {
                            imageRef.putBytes(bytes).await()
                            val downloadUrl = imageRef.downloadUrl.await().toString()
                            uploadedUrls.add(downloadUrl)
                            Log.d("Upload", "アップロード成功: $downloadUrl")
                        } catch (e: Exception) {
                            Log.e("Upload", "アップロード失敗: $uri", e)
                        }
                    }
                }
            }

            val updatedPacket = packet.copy(
                documentId = id,
                imageUrls = uploadedUrls
            )

            target.set(updatedPacket)
                .addOnSuccessListener {
                    packet = updatedPacket
                    showSnackbar = "保存が完了しました（画像数: ${uploadedUrls.size}）"
                    onComplete(Result.success(Unit))
                }
                .addOnFailureListener {
                    showSnackbar = "保存に失敗しました: ${it.localizedMessage ?: "不明なエラー"}"
                    onComplete(Result.failure(it))
                }
        }
    }

}
