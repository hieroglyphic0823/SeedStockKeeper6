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
import android.graphics.Matrix
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlin.math.max
import kotlin.math.min
import android.graphics.Rect
import com.example.seedstockkeeper6.util.drawRectOverlay

class SeedInputViewModel : ViewModel() {

    var packet by mutableStateOf(SeedPacket())
        private set
    var imageUris = mutableStateListOf<Uri>()

    var ocrTargetIndex by mutableStateOf(-1)
        private set
    var showSnackbar by mutableStateOf<String?>(null)
    var showAIDiffDialog by mutableStateOf(false)
    var aiDiffList = mutableStateListOf<Triple<String, String, String>>()
        private set

    var isLoading by mutableStateOf(false)

    // 画像置換の確認用
    var showCropConfirmDialog by mutableStateOf(false)
    var pendingCropOverlay by mutableStateOf<Bitmap?>(null)
    var pendingCropBitmap by mutableStateOf<Bitmap?>(null)

    fun setSeed(seed: SeedPacket?) {
        packet = seed ?: SeedPacket()
        val localUris = imageUris.filter { it.scheme == "file" || it.scheme == "content" }
        imageUris.clear()
        seed?.imageUrls?.forEach { url ->
            imageUris.add(Uri.parse(url))
        }
        // 画面上で追加されたローカル画像も残す
        imageUris.addAll(localUris)
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
    var selectedImageUri: Uri? by mutableStateOf(null)
        private set
    var selectedImageUrl by mutableStateOf<String?>(null)
        private set
    var selectedImageBitmap by mutableStateOf<Bitmap?>(null)
        private set
    fun selectImage(context: Context, uri: Uri) {
        selectedImageUri = uri  // ★ ここで選択したURIを保存
        selectedImageBitmap = uriToBitmap(context, uri)

        viewModelScope.launch {
            if (uri.toString().startsWith("seed_images/")) {
                val ref = Firebase.storage.reference.child(uri.toString())
                try {
                    val stream = ref.getBytes(5 * 1024 * 1024).await()
                    val bitmap = BitmapFactory.decodeByteArray(stream, 0, stream.size)
                    selectedImageBitmap = bitmap
                } catch (e: Exception) {
                    selectedImageBitmap = null
                }
            } else {
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    selectedImageBitmap = bitmap
                } catch (e: Exception) {
                    selectedImageBitmap = null
                }
            }
        }
    }


    fun clearSelectedImage() {
        selectedImageUrl = null
        selectedImageBitmap = null
    }
    fun moveImage(fromIndex: Int, toIndex: Int) {
        if (toIndex in imageUris.indices) {
            val item = imageUris.removeAt(fromIndex)
            imageUris.add(toIndex, item)
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

    fun rotateAndReplaceImage(context: Context, uri: Uri, degrees: Float) {
        viewModelScope.launch {
            try {
                val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                    if (uri.toString().startsWith("seed_images/")) {
                        val storageRef = Firebase.storage.reference.child(uri.toString())
                        val downloadUrl = storageRef.downloadUrl.await().toString()
                        val urlConnection = URL(downloadUrl).openConnection() as HttpURLConnection
                        urlConnection.connectTimeout = 10000
                        urlConnection.readTimeout = 10000
                        urlConnection.doInput = true
                        urlConnection.connect()
                        BitmapFactory.decodeStream(urlConnection.inputStream)
                    } else {
                        val input = context.contentResolver.openInputStream(uri)
                        BitmapFactory.decodeStream(input)
                    }
                }

                if (bitmap == null) throw Exception("Bitmap取得失敗")

                val matrix = Matrix().apply { postRotate(degrees) }
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                // 一時ファイルに保存
                val file = File(context.cacheDir, "rotated_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use {
                    rotated.compress(Bitmap.CompressFormat.JPEG, 90, it)
                }
                val rotatedUri = Uri.fromFile(file)

                // imageUrisの該当箇所を置き換え
                val index = imageUris.indexOf(uri)
                if (index != -1) {
                    imageUris[index] = rotatedUri
                }

                // ダイアログ表示用にも反映
                selectedImageBitmap = rotated
                selectedImageUri = rotatedUri

            } catch (e: Exception) {
                Log.e("ImageRotate", "回転失敗: $uri", e)
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
                    "file" -> BitmapFactory.decodeFile(uri.path) // ← 追加
                    null, "" -> {
                        // Firebase Storageパス
                        val path = uri.toString()
                        val downloadUrl = getDownloadUrlFromPath(path)
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
        val currentImageUris = imageUris.toList()
        val newDiffs = mutableListOf<Triple<String, String, String>>()

        if (packet.productName.isEmpty() && packet.variety.isEmpty() && packet.family.isEmpty()) {
            packet = parsed
            imageUris.clear()
            imageUris.addAll(currentImageUris)
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
        // カレンダー切り抜きは毎回実行
        try {
            tryAddCroppedCalendarImage(context, bmp)
        } catch (e: Exception) {
            Log.e("MLCrop", "カレンダー切り抜き失敗", e)
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
        if (packet.productName.isBlank()) {
            showSnackbar = "商品名を入力してください"
            onComplete(Result.failure(IllegalArgumentException("商品名が空です")))
            return
        }
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
                    val uriString = uri.toString()

                    if (uriString.startsWith("seed_images/")) {
                        // 既存の Firebase Storage 画像はそのまま採用
                        existingImagePaths.add(uriString)
                    } else {
                        // ローカル（content://, file://）や必要なら http(s):// をアップロード
                        val bitmap = try {
                            when (uri.scheme) {
                                "content" -> uriToBitmap(context, uri)
                                "file"    -> BitmapFactory.decodeFile(uri.path)
                                "https", "http" -> {
                                    (URL(uriString).openConnection() as HttpURLConnection).run {
                                        connectTimeout = 15000; readTimeout = 15000; doInput = true
                                        connect()
                                        if (responseCode == HttpURLConnection.HTTP_OK) {
                                            BitmapFactory.decodeStream(inputStream).also { inputStream.close() }
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

                            // ※ indexベースだと将来の並べ替えで衝突する可能性があるためUUID推奨
                            val imagePath = "seed_images/${id}_${UUID.randomUUID()}.jpg"

                            try {
                                storageRef.child(imagePath).putBytes(bytes).await()
                                uploadedPaths.add(imagePath)
                                Log.d("SeedInputVM", "アップロード成功: $imagePath")
                            } catch (e: Exception) {
                                Log.e("SeedInputVM", "アップロード失敗: $imagePath", e)
                            }
                        }
                    }
                }
            }

            // --- 表示順で保存順を再構築 ---
            val allPathsMap = (uploadedPaths + existingImagePaths).associateBy { it }
            val finalOrderedPaths = imageUris.mapNotNull { uri ->
                // Firebase Storage画像はパス、ローカル画像はアップロード後のパス
                if (uri.toString().startsWith("seed_images/")) {
                    allPathsMap[uri.toString()]
                } else {
                    // ローカル画像はアップロード後のパス（uploadedPathsから取得）
                    uploadedPaths.find { it.contains(id) && it.endsWith(".jpg") }
                }
            }

            Log.d("SeedInputVM", "保存順序付きパス: $finalOrderedPaths")

            val updatedPacket = packet.copy(
                documentId = id,
                imageUrls = finalOrderedPaths
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
                Log.d("MLCrop", "切り抜き画像サイズ: ${croppedBitmap.width}x${croppedBitmap.height}")
                Log.d("MLCrop", "切り抜き画像ファイルパス: ${file.absolutePath}")
                Log.d("MLCrop", "imageUrisに追加: ${Uri.fromFile(file)}")
                Log.d("MLCrop", "現在のimageUris: $imageUris")
                Log.d("MLCrop", "新規登録で切り抜き追加成功")
            } else {
                Log.w("MLCrop", "新規登録：有効なカレンダー検出なし")
            }
        } catch (e: Exception) {
            Log.e("MLCrop", "新規登録中の切り抜き失敗", e)
        }
    }
    private val seedOuterDetector: ObjectDetector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()   // 複数検出して最大面積を選ぶ
            .build()
        ObjectDetection.getClient(options)
    }
    private suspend fun detectSeedPacketOuterAndAddCrop(
        context: Context,
        sourceBitmap: Bitmap,
        insertAfterIndex: Int,
        minAreaRatio: Float = 0.10f,
        centerBias: Float = 0.15f,
        marginRatio: Float = 0.05f,        // ← 余白 5%
        enableAspectFilter: Boolean = true,// ← アスペクト比フィルタ
        aspectMin: Float = 0.5f,
        aspectMax: Float = 2.0f
    ) {
        val input = InputImage.fromBitmap(sourceBitmap, 0)
        val objects = runCatching { seedOuterDetector.process(input).await() }
            .onFailure { Log.e("MLKit", "seed outer detect failed", it) }
            .getOrNull() ?: return

        if (objects.isEmpty()) {
            Log.d("MLKit", "no objects for seed outer")
            return
        }

        val w = sourceBitmap.width
        val h = sourceBitmap.height
        val imgArea = w.toFloat() * h

        fun scoreRect(l: Int, t: Int, r: Int, b: Int): Double {
            val cw = (l + r) / 2f
            val ch = (t + b) / 2f
            val dx = (cw / w - 0.5f)
            val dy = (ch / h - 0.5f)
            val raw = 1.0 - (kotlin.math.hypot(dx.toDouble(), dy.toDouble()) * 2.0 * centerBias)
            val centerPenalty = raw.coerceIn(0.0, 1.0) // ← 負値や>1を抑制
            val area = (r - l).toFloat() * (b - t)
            return area * centerPenalty
        }

        val best = objects
            .map { it.boundingBox }
            .map { bb ->
                // 画像内にまずクリップ
                val l = bb.left.coerceIn(0, w - 1)
                val t = bb.top.coerceIn(0, h - 1)
                val r = bb.right.coerceIn(l + 1, w)
                val b = bb.bottom.coerceIn(t + 1, h)
                android.graphics.Rect(l, t, r, b)
            }
            .filter { rect ->
                val area = (rect.width() * rect.height()).toFloat()
                if (area / imgArea < minAreaRatio) return@filter false
                if (!enableAspectFilter) return@filter true
                val ar = rect.width().toFloat() / rect.height().toFloat()
                ar in aspectMin..aspectMax
            }
            .maxByOrNull { r -> scoreRect(r.left, r.top, r.right, r.bottom) }
            ?: return

        // 余白を付与（Rect → RectF → 余白 → Intに戻す）
        val inflated = RectF(best).addMargin(w, h, marginRatio)
        val l = inflated.left.toInt().coerceIn(0, w - 1)
        val t = inflated.top.toInt().coerceIn(0, h - 1)
        val r = inflated.right.toInt().coerceIn(l + 1, w)
        val b = inflated.bottom.toInt().coerceIn(t + 1, h)

        val crop = runCatching {
            Bitmap.createBitmap(sourceBitmap, l, t, r - l, b - t)
        }.getOrNull() ?: return

        val file = File(context.cacheDir, "seed_outer_${System.currentTimeMillis()}.jpg")
        runCatching {
            FileOutputStream(file).use { out ->
                crop.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        }.onFailure {
            Log.e("MLKit", "save outer crop failed", it)
            return
        }

        val insertPos = insertAfterIndex.coerceIn(0, imageUris.size)
        imageUris.add(insertPos + 1, Uri.fromFile(file))
        Log.d("MLKit", "outer crop added after $insertAfterIndex : ${file.absolutePath}")
    }

    fun RectF.addMargin(width: Int, height: Int, marginRatio: Float): RectF {
        val marginX = width * marginRatio
        val marginY = height * marginRatio
        return RectF(
            max(0f, left - marginX),
            max(0f, top - marginY),
            min(width.toFloat(), right + marginX),
            min(height.toFloat(), bottom + marginY)
        )
    }
    fun cropSeedOuterAtOcrTarget(context: Context) {
        viewModelScope.launch {
            if (isLoading) return@launch
            if (ocrTargetIndex !in imageUris.indices) { showSnackbar = "対象の画像がありません"; return@launch }
            isLoading = true
            try {
                val targetUri = imageUris[ocrTargetIndex]

                // 画像ロード（content/file/http(s)/Firebase Storage パス対応）
                val bmp = withContext(Dispatchers.IO) {
                    when (targetUri.scheme) {
                        "content" -> uriToBitmap(context, targetUri)
                        "file"    -> BitmapFactory.decodeFile(targetUri.path)
                        "http", "https" -> (URL(targetUri.toString()).openConnection() as HttpURLConnection).run {
                            connectTimeout = 15000; readTimeout = 15000; doInput = true; connect()
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                BitmapFactory.decodeStream(inputStream).also { inputStream.close() }
                            } else null
                        }
                        null, "" -> { // Firebase Storage パス
                            val downloadUrl = getDownloadUrlFromPath(targetUri.toString())
                            if (downloadUrl != null) (URL(downloadUrl).openConnection() as HttpURLConnection).run {
                                connectTimeout = 15000; readTimeout = 15000; doInput = true; connect()
                                if (responseCode == HttpURLConnection.HTTP_OK) {
                                    BitmapFactory.decodeStream(inputStream).also { inputStream.close() }
                                } else null
                            } else null
                        }
                        else -> null
                    }
                } ?: run { showSnackbar = "画像の読み込みに失敗しました"; return@launch }

                if (bmp == null) {
                    Log.e("MLKitInput", "bmp is NULL before creating InputImage.")
                    showSnackbar = "画像データが不正です (bmp is null)"
                    return@launch // または適切なエラー処理
                } else if (bmp.isRecycled) {
                    Log.e("MLKitInput", "bmp is RECYCLED before creating InputImage.")
                    showSnackbar = "画像データが不正です (bmp is recycled)"
                    return@launch // または適切なエラー処理
                } else if (bmp.width == 0 || bmp.height == 0) {
                    Log.e("MLKitInput", "bmp has zero width or height. Width: ${bmp.width}, Height: ${bmp.height}")
                    showSnackbar = "画像データが空です (zero dimensions)"
                    return@launch // または適切なエラー処理
                } else {
                    Log.d("MLKitInput", "bmp is valid before creating InputImage. Width: ${bmp.width}, Height: ${bmp.height}")
                }
                // ---- ML Kit 検出 → 1位選出（面積×中心×ラベル係数） ----
                val input = InputImage.fromBitmap(bmp, 0)
                val objects = runCatching { seedOuterDetector.process(input).await() }
                    .onFailure { Log.e("MLKit", "seed outer detect failed", it) }
                    .getOrNull().orEmpty()

                val w = bmp.width; val h = bmp.height
                val imgArea = w.toFloat() * h
                val minAreaRatio = 0.20f
                val arMin = 1.0f; val arMax = 2.5f
                val centerBias = 0.20f
                val marginRatio = 0.06f

                val best = objects.mapNotNull { obj ->
                    val bb = obj.boundingBox
                    val r = android.graphics.Rect(
                        bb.left.coerceIn(0, w - 1),
                        bb.top.coerceIn(0, h - 1),
                        bb.right.coerceIn(1, w),
                        bb.bottom.coerceIn(1, h)
                    )
                    val area = r.width() * r.height()
                    if (area / imgArea < minAreaRatio) return@mapNotNull null

                    val arSym = maxOf(r.width(), r.height()).toFloat() / minOf(r.width(), r.height()).toFloat()
                    if (arSym < arMin || arSym > arMax) return@mapNotNull null

                    val cx = (r.left + r.right) / 2f
                    val cy = (r.top + r.bottom) / 2f
                    val dx = cx / w - 0.5f
                    val dy = cy / h - 0.5f
                    val centerPenalty = (1.0 - (kotlin.math.hypot(dx.toDouble(), dy.toDouble()) * 2.0 * centerBias))
                        .coerceIn(0.0, 1.0)

                    val score = area * centerPenalty * labelFactor(obj.labels)
                    r to score
                }.maxByOrNull { it.second }?.first

                if (best == null) { showSnackbar = "外側を検出できませんでした"; return@launch }

                // margin を付け、さらに“袋のエッジ”に吸着させてタイト化
                val pre = Rect(
                    (best.left   - (w * marginRatio)).toInt().coerceAtLeast(0),
                    (best.top    - (h * marginRatio)).toInt().coerceAtLeast(0),
                    (best.right  + (w * marginRatio)).toInt().coerceAtMost(w),
                    (best.bottom + (h * marginRatio)).toInt().coerceAtMost(h)
                )

                Log.d("TightenDebug", "Before calling tightenRectToEdges:")
                Log.d("TightenDebug", "bmp is null: ${bmp == null}")
                if (bmp != null) {
                    Log.d("TightenDebug", "bmp Width: ${bmp.width}, Height: ${bmp.height}, Config: ${bmp.config}, Recycled: ${bmp.isRecycled}")
                }

                Log.d("TightenDebug", "pre is null: ${pre == null}")
                if (pre != null) {
                    Log.d("TightenDebug", "pre Left: ${pre.left}, Top: ${pre.top}, Right: ${pre.right}, Bottom: ${pre.bottom}")
                    Log.d("TightenDebug", "pre Width: ${pre.width()}, Height: ${pre.height()}")
                }

                val tight = com.example.seedstockkeeper6.util.tightenRectToEdges(
                    src = bmp,
                    rect = pre,
                    scanFrac = 0.10f,        // ← 0.06f → 0.08~0.12 に上げると深く探索
                    percentile = 0.88f,      // ← 0.88 → 0.92~0.96 に上げると内側に寄りやすい
                    smoothWin = 5,
                    maxInsetRatioX = 0.15f,  // 横方向の上限（必要に応じて）
                    maxInsetRatioY = 0.30f   // ← 縦の上限を 0.15 → 0.20~0.25 に上げる
                )
                Log.d("drawRectOverlayDebug", "pre is null: ${pre == null}")
                if (pre != null) {
                    Log.d("drawRectOverlayDebug", "pre Left: ${pre.left}, Top: ${pre.top}, Right: ${pre.right}, Bottom: ${pre.bottom}")
                    Log.d("drawRectOverlayDebug", "pre Width: ${pre.width()}, Height: ${pre.height()}")
                }
                // プレビュー作成＆確認ダイアログへ
//                val overlay = com.example.seedstockkeeper6.util.drawRectOverlay(bmp, tight)
//                val crop = Bitmap.createBitmap(bmp, tight.left, tight.top, tight.width(), tight.height())
                val overlay = drawRectOverlay(bmp, tight)
                Log.d(
                    "CropDebug",
                    "overlay created. overlay is null: ${overlay == null}"
                ) // overlay の状態もログに出す

                val crop: Bitmap?
                try {
                    Log.d(
                        "CropDebug",
                        "Attempting Bitmap.createBitmap with tight: Left=${tight.left}, Top=${tight.top}, Width=${tight.width()}, Height=${tight.height()}"
                    )
                    Log.d("CropDebug", "bmp dimensions: Width=${bmp.width}, Height=${bmp.height}")

                    // ここで条件チェックを明示的に行うのも有効
                    if (tight.width() <= 0 || tight.height() <= 0) {
                        Log.e(
                            "CropDebug",
                            "Bitmap.createBitmap error: tight width or height is zero or negative. Width: ${tight.width()}, Height: ${tight.height()}"
                        )
                        showSnackbar = "切り抜き領域のサイズが不正です。"
                        return@launch
                    }
                    if (tight.left < 0 || tight.top < 0 || tight.left + tight.width() > bmp.width || tight.top + tight.height() > bmp.height) {
                        Log.e(
                            "CropDebug",
                            "Bitmap.createBitmap error: tight coordinates are out of bmp bounds."
                        )
                        Log.e(
                            "CropDebug",
                            "tight: L${tight.left}, T${tight.top}, R${tight.left + tight.width()}, B${tight.top + tight.height()}"
                        )
                        Log.e("CropDebug", "bmp: W${bmp.width}, H${bmp.height}")
                        showSnackbar = "切り抜き領域が画像範囲外です。"
                        return@launch
                    }

                    crop = Bitmap.createBitmap(
                        bmp,
                        tight.left,
                        tight.top,
                        tight.width(),
                        tight.height()
                    )
                    Log.d("CropDebug", "crop created successfully. crop is null: ${crop == null}")

                    pendingCropOverlay = overlay
                    pendingCropBitmap = crop
                    showCropConfirmDialog = true

                } catch (e: IllegalArgumentException) {
                    Log.e(
                        "CropDebug",
                        "IllegalArgumentException during Bitmap.createBitmap: ${e.message}",
                        e
                    )
                    showSnackbar = "画像の切り抜きに失敗 (引数エラー): ${e.message}"
                    return@launch // または適切なエラー処理
                } catch (e: Exception) {
                    Log.e(
                        "CropDebug",
                        "Exception during Bitmap.createBitmap or subsequent assignments: ${e.message}",
                        e
                    )
                    showSnackbar = "画像の切り抜き処理中に予期せぬエラーが発生しました。"
                    return@launch // または適切なエラー処理
                }

                pendingCropOverlay = overlay
                pendingCropBitmap = crop
                showCropConfirmDialog = true

            } finally {
                isLoading = false
            }
        }
    }




    fun confirmCropReplace(context: Context) {
        val crop = pendingCropBitmap ?: run { showCropConfirmDialog = false; return }
        val idx = ocrTargetIndex
        if (idx !in imageUris.indices) { showCropConfirmDialog = false; return }

        // 一時ファイル保存 → imageUris の対象を置換
        val file = File(context.cacheDir, "seed_outer_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { crop.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        imageUris[idx] = Uri.fromFile(file)

        pendingCropOverlay = null
        pendingCropBitmap = null
        showCropConfirmDialog = false
        showSnackbar = "外側クロップで差し替えました"
    }

    fun cancelCropReplace() {
        pendingCropOverlay = null
        pendingCropBitmap = null
        showCropConfirmDialog = false
    }
    private suspend fun createSeedOuterCrop(
        context: Context,
        sourceBitmap: Bitmap,
        minAreaRatio: Float = 0.10f,
        centerBias: Float = 0.15f,
        marginRatio: Float = 0.05f,
        aspectMin: Float = 0.5f,
        aspectMax: Float = 2.0f
    ): Bitmap? {
        val input = InputImage.fromBitmap(sourceBitmap, 0)
        val objects = runCatching { seedOuterDetector.process(input).await() }
            .onFailure { Log.e("MLKit", "seed outer detect failed", it) }
            .getOrNull() ?: return null

        if (objects.isEmpty()) return null

        val w = sourceBitmap.width
        val h = sourceBitmap.height
        val imgArea = w.toFloat() * h

        fun scoreRect(l: Int, t: Int, r: Int, b: Int): Double {
            val cw = (l + r) / 2f
            val ch = (t + b) / 2f
            val dx = (cw / w - 0.5f)
            val dy = (ch / h - 0.5f)
            val centerPenalty = (1.0 - (kotlin.math.hypot(dx.toDouble(), dy.toDouble()) * 2.0 * centerBias))
                .coerceIn(0.0, 1.0)
            val area = (r - l).toFloat() * (b - t)
            return area * centerPenalty
        }

        val best = objects.map { it.boundingBox }
            .map { bb ->
                val l = bb.left.coerceIn(0, w - 1)
                val t = bb.top.coerceIn(0, h - 1)
                val r = bb.right.coerceIn(l + 1, w)
                val b = bb.bottom.coerceIn(t + 1, h)
                android.graphics.Rect(l, t, r, b)
            }
            .filter { rect ->
                val area = (rect.width() * rect.height()).toFloat()
                if (area / imgArea < minAreaRatio) return@filter false
                val ar = rect.width().toFloat() / rect.height().toFloat()
                ar in aspectMin..aspectMax
            }
            .maxByOrNull { r -> scoreRect(r.left, r.top, r.right, r.bottom) }
            ?: return null

        val inflated = RectF(best).addMargin(w, h, marginRatio)
        val l = inflated.left.toInt().coerceIn(0, w - 1)
        val t = inflated.top.toInt().coerceIn(0, h - 1)
        val r = inflated.right.toInt().coerceIn(l + 1, w)
        val b = inflated.bottom.toInt().coerceIn(t + 1, h)

        return runCatching { Bitmap.createBitmap(sourceBitmap, l, t, r - l, b - t) }.getOrNull()
    }
    private fun labelFactor(labels: List<com.google.mlkit.vision.objects.DetectedObject.Label>): Double {
        if (labels.isEmpty()) return 1.0
        val weights = mapOf("food" to 1.25, "home goods" to 1.15, "fashion goods" to 0.95, "place" to 0.85)
        var f = 1.0
        labels.forEach { l -> f += ((weights[l.text.lowercase()] ?: 1.0) - 1.0) * l.confidence }
        return f.coerceIn(0.7, 1.6)
    }

}
