package com.example.seedstockkeeper6.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seedstockkeeper6.data.runGeminiOcr
import com.example.seedstockkeeper6.data.uriToBitmap
import com.example.seedstockkeeper6.ml.CalendarDetector
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.util.drawRectOverlay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.TensorImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

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

    // 地域選択関連
    var showRegionSelectionDialog by mutableStateOf(false)
    var detectedRegions = mutableStateListOf<String>()
        private set
    var selectedRegion by mutableStateOf("")
        private set
    var ocrResult by mutableStateOf<SeedPacket?>(null)
        private set
    var croppedCalendarBitmap by mutableStateOf<Bitmap?>(null)
        private set

    // 地域選択ダイアログの編集用状態
    var editingCalendarEntry by mutableStateOf<CalendarEntry?>(null)
        private set
    
    // 地域選択後の編集モード状態
    var isCalendarEditMode by mutableStateOf(false)
        private set

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
        
        // OCR結果を保存
        ocrResult = parsed
        Log.d("RegionSelection", "OCR結果保存: ocrResult=$parsed")
        
        // 地域名を検出して地域選択ダイアログを表示
        val detectedRegions = extractRegionsFromOcrResult(parsed)
        Log.d("RegionSelection", "地域検出結果: detectedRegions=$detectedRegions, isNotEmpty=${detectedRegions.isNotEmpty()}")
        
        // 地域が検出されていない場合はデフォルト地域リストを使用
        val regionsToShow = if (detectedRegions.isNotEmpty()) {
            detectedRegions
        } else {
            Log.d("RegionSelection", "地域が検出されませんでした。デフォルト地域リストを使用します")
            listOf("北海道", "東北", "関東", "中部", "関西", "中国", "四国", "九州", "沖縄")
        }
        
        showRegionSelectionDialog(regionsToShow)
        
        // カレンダー切り抜きは毎回実行
        try {
            tryAddCroppedCalendarImage(context, bmp)
        } catch (e: Exception) {
            Log.e("MLCrop", "カレンダー切り抜き失敗", e)
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
        if (packet.expirationYear != parsed.expirationYear) {
            newDiffs.add(
                Triple(
                    "有効期限",
                    packet.expirationYear.toString(), // Int を String に変換
                    parsed.expirationYear.toString()  // Int を String に変換
                )
            )
        }
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
                    "有効期限" -> onExpirationYearChange(aiValue)
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
    fun onExpirationYearChange(value: String) { packet = packet.copy(expirationYear = value.toIntOrNull() ?: 0) }
    fun onExpirationMonthChange(value: String) { packet = packet.copy(expirationMonth = value.toIntOrNull() ?: 0) }
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

    fun saveSeed(context: android.content.Context, onComplete: (Result<Unit>) -> Unit) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        if (uid == null) {
            showSnackbar = "保存にはサインインが必要です"
            onComplete(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        if (packet.productName.isBlank()) {
            showSnackbar = "商品名を入力してください"
            onComplete(Result.failure(IllegalArgumentException("商品名が空です")))
            return
        }

        val db = com.google.firebase.ktx.Firebase.firestore
        val storageRef = com.google.firebase.ktx.Firebase.storage.reference

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            isLoading = true
            try {
                // 1) docId を確定（既存なら流用、無ければ新規発番）
                val target = packet.documentId?.let { db.collection("seeds").document(it) }
                    ?: db.collection("seeds").document()
                val id = target.id

                // 2) 所有者を確定（ここで create/update を“先に”確定させる）
                ensureSeedOwnershipOrFail(id, uid)

                // 3) 画像アップロード（命名: seed_images/{docId}_{UUID}.jpg）
                val pathsByIndex = MutableList(imageUris.size) { null as String? }
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    imageUris.forEachIndexed { index, uri ->
                        val s = uri.toString()
                        val scheme = uri.scheme ?: ""

                        // 既に Storage パスならそのまま採用
                        if (s.startsWith("seed_images/") || scheme == "seed_images") {
                            pathsByIndex[index] = s
                            android.util.Log.d("SaveSeed", "keep @ $index : $s")
                            return@forEachIndexed
                        }

                        // Bitmap 化
                        val bitmap = try {
                            when (scheme) {
                                "content" -> com.example.seedstockkeeper6.data.uriToBitmap(context, uri)
                                "file"    -> android.graphics.BitmapFactory.decodeFile(uri.path)
                                "http", "https" -> {
                                    (java.net.URL(s).openConnection() as java.net.HttpURLConnection).run {
                                        connectTimeout = 15000
                                        readTimeout = 15000
                                        doInput = true
                                        connect()
                                        if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                                            android.graphics.BitmapFactory.decodeStream(inputStream).also {
                                                kotlin.runCatching { inputStream.close() }
                                            }
                                        } else null
                                    }
                                }
                                else -> null
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("SaveSeed", "Bitmap load failed @ $index : $uri", e)
                            null
                        }

                        if (bitmap != null) {
                            val baos = java.io.ByteArrayOutputStream().apply {
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, this)
                            }
                            val bytes = baos.toByteArray()
                            val imagePath = "seed_images/${id}_${java.util.UUID.randomUUID()}.jpg"
                            try {
                                storageRef.child(imagePath).putBytes(bytes).await()
                                pathsByIndex[index] = imagePath
                                android.util.Log.d("SaveSeed", "uploaded @ $index : $imagePath (from $uri)")
                            } catch (e: Exception) {
                                android.util.Log.e("SaveSeed", "upload failed @ $index : $imagePath", e)
                            }
                        } else {
                            android.util.Log.e("SaveSeed", "skip @ $index : bitmap null ($uri)")
                        }
                    }
                }

                // 4) 旧ストレージ画像の削除
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val oldSet = packet.imageUrls.toSet()
                    val newSet = pathsByIndex.mapNotNull { it }.toSet()
                    val toDelete = oldSet - newSet
                    toDelete.forEach { path ->
                        runCatching {
                            storageRef.child(path).delete().await()
                            android.util.Log.d("SaveSeed", "deleted: $path")
                        }.onFailure {
                            android.util.Log.e("SaveSeed", "delete failed: $path", it)
                        }
                    }
                }

                // 5) Firestore を最終更新（ownerUid を上書きしない merge）
                val finalOrderedPaths = pathsByIndex.mapNotNull { it }
                android.util.Log.d("SaveSeed", "finalOrderedPaths(size=${finalOrderedPaths.size}): $finalOrderedPaths")

                val updatedPacket = packet.copy(
                    documentId = id,
                    imageUrls = finalOrderedPaths
                )

                // 失敗している行の置き換え版
// target.set(updatedPacket, SetOptions.merge()).await()

                val json = com.google.gson.Gson().toJson(updatedPacket)
                val type = object : com.google.gson.reflect.TypeToken<MutableMap<String, Any?>>() {}.type
                val map: MutableMap<String, Any?> = com.google.gson.Gson().fromJson(json, type)

                // ここがポイント：同じ ownerUid を必ず送る（ルールの ownerUnchanged を満たす）
                map["ownerUid"] = uid

                target.set(map, com.google.firebase.firestore.SetOptions.merge()).await()

                // ViewModel の状態を更新
                packet = updatedPacket
                showSnackbar = "保存が完了しました（画像: ${finalOrderedPaths.size}）"
                onComplete(Result.success(Unit))
            } catch (e: SecurityException) {
                android.util.Log.e("SaveSeed", "ownership error", e)
                showSnackbar = "このデータの所有者ではありません"
                onComplete(Result.failure(e))
            } catch (e: Exception) {
                android.util.Log.e("SaveSeed", "failed", e)
                showSnackbar = "保存に失敗しました: ${e.localizedMessage ?: "不明なエラー"}"
                onComplete(Result.failure(e))
            } finally {
                isLoading = false
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
                
                // 切り抜きされたカレンダー画像を保存
                croppedCalendarBitmap = croppedBitmap
                
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
                croppedCalendarBitmap = null
            }
        } catch (e: Exception) {
            Log.e("MLCrop", "新規登録中の切り抜き失敗", e)
            croppedCalendarBitmap = null
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

    fun addCalendarEntry() {
        val list = (packet.calendar ?: emptyList()) + com.example.seedstockkeeper6.model.CalendarEntry()
        packet = packet.copy(calendar = list)
    }

    fun updateCalendarEntry(
        index: Int,
        region: String? = null,
        sowing_start: Int? = null,
        sowing_start_stage: String? = null,
        sowing_end: Int? = null,
        sowing_end_stage: String? = null,
        harvest_start: Int? = null,
        harvest_start_stage: String? = null,
        harvest_end: Int? = null,
        harvest_end_stage: String? = null
    ) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val newItem = old.copy(
            region = region ?: old.region,
            sowing_start = sowing_start ?: old.sowing_start,
            sowing_start_stage = sowing_start_stage ?: old.sowing_start_stage,
            sowing_end = sowing_end ?: old.sowing_end,
            sowing_end_stage = sowing_end_stage ?: old.sowing_end_stage,
            harvest_start = harvest_start ?: old.harvest_start,
            harvest_start_stage = harvest_start_stage ?: old.harvest_start_stage,
            harvest_end = harvest_end ?: old.harvest_end,
            harvest_end_stage = harvest_end_stage ?: old.harvest_end_stage
        )
        val next = cur.toMutableList().apply { set(index, newItem) }
        packet = packet.copy(calendar = next)
    }


    fun removeCalendarEntry(index: Int) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val next = cur.toMutableList().apply { removeAt(index) }
        packet = packet.copy(calendar = next)
    }


    fun updateCalendarRegion(index: Int, value: String) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val updated = old.copy(region = value)
        val next = cur.toMutableList().apply { set(index, updated) }
        packet = packet.copy(calendar = next)
    }
    fun updateCalendarSowingStart(index: Int, month: Int) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val updated = old.copy(sowing_start = month)
        val next = cur.toMutableList().apply { set(index, updated) }
        packet = packet.copy(calendar = next)
    }

    fun updateCalendarSowingStartStage(index: Int, stage: String) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val updated = old.copy(sowing_start_stage = stage)
        val next = cur.toMutableList().apply { set(index, updated) }
        packet = packet.copy(calendar = next)
    }
    fun updateCalendarSowingEnd(index: Int, month: Int) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val updated = old.copy(sowing_end = month)
        val next = cur.toMutableList().apply { set(index, updated) }
        packet = packet.copy(calendar = next)
    }
    fun updateCalendarSowingEndStage(index: Int, stage: String) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val updated = old.copy(sowing_end_stage = stage)
        val next = cur.toMutableList().apply { set(index, updated) }
        packet = packet.copy(calendar = next)
    }
    fun updateCalendarHarvestStart(index: Int, month: Int) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val updated = old.copy(harvest_start = month)
        val next = cur.toMutableList().apply { set(index, updated) }
        packet = packet.copy(calendar = next)
    }
    fun updateCalendarHarvestStartStage(index: Int, stage: String) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val updated = old.copy(harvest_start_stage = stage)
        val next = cur.toMutableList().apply { set(index, updated) }
        packet = packet.copy(calendar = next)
    }

    fun updateCalendarHarvestEnd(index: Int, month: Int) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val updated = old.copy(harvest_end = month)
        val next = cur.toMutableList().apply { set(index, updated) }
        packet = packet.copy(calendar = next)
    }
    fun updateCalendarHarvestEndStage(index: Int, stage: String) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val updated = old.copy(harvest_end_stage = stage)
        val next = cur.toMutableList().apply { set(index, updated) }
        packet = packet.copy(calendar = next)
    }
    fun removeCalendarEntryAtIndex(index: Int) {
        // 1. 安全チェック: インデックスがカレンダーリストの有効な範囲内にあるか確認します。
        if (index >= 0 && index < packet.calendar.size) {
            // 2. 現在のカレンダーリストのミュータブルなコピーを作成します。
            //    元の packet.calendar は不変(Immutable)なので、直接変更できません。
            val updatedCalendarList = packet.calendar.toMutableList()

            // 3. ミュータブルなリストから、指定されたインデックスの要素を削除します。
            updatedCalendarList.removeAt(index)

            // 4. packet ステートを更新します。
            //    data class の copy() メソッドを使用して、他のプロパティはそのままに、
            //    calendar プロパティだけを更新されたリストで置き換えた新しい Packet オブジェクトを作成します。
            //    updatedCalendarList.toList() で、再度不変なリストに戻しています（推奨）。
            packet = packet.copy(calendar = updatedCalendarList.toList())

            // これで、packet の状態が変わり、Compose UI がこの変更を検知して
            // 関連する部分を再コンポジション（再描画）します。

        } else {
            // 5. インデックスが無効だった場合の処理（エラーハンドリング）。
            //    ここではログに警告を出力しています。必要に応じて他の処理も追加できます。
            Log.w("YourViewModel", "Attempted to remove calendar entry at invalid index: $index. List size: ${packet.calendar.size}")
        }
    }


    private suspend fun ensureSeedOwnershipOrFail(seedId: String, uid: String) {
        val ref = com.google.firebase.ktx.Firebase.firestore
            .collection("seeds")
            .document(seedId)

        // 読み取りをせず、ownerUid だけを merge 書き込み（既存フィールドは上書きしない）
        ref.set(
            mapOf("ownerUid" to uid),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    // 地域選択関連のメソッド
    fun showRegionSelectionDialog(regions: List<String>) {
        Log.d("RegionSelection", "showRegionSelectionDialog呼び出し: regions=$regions")
        detectedRegions.clear()
        detectedRegions.addAll(regions)
        showRegionSelectionDialog = true
        Log.d("RegionSelection", "showRegionSelectionDialog設定完了: showRegionSelectionDialog=$showRegionSelectionDialog")
    }

    fun onRegionSelected(region: String) {
        Log.d("RegionSelection", "onRegionSelected開始: $region")
        selectedRegion = region
        showRegionSelectionDialog = false
        
        // 編集された値がある場合は、それを優先して使用
        if (editingCalendarEntry != null && editingCalendarEntry!!.region == region) {
            Log.d("RegionSelection", "編集された値を使用: $editingCalendarEntry")
            packet = packet.copy(calendar = listOf(editingCalendarEntry!!))
        } else {
            // 選択された地域でカレンダーエントリを更新
            updateCalendarWithSelectedRegion(region)
        }
        
        // 編集モードを有効にする
        isCalendarEditMode = true
        Log.d("RegionSelection", "地域選択完了: $region, 編集モード: $isCalendarEditMode, カレンダーサイズ: ${packet.calendar?.size}")
    }

    fun onRegionSelectionDismiss() {
        showRegionSelectionDialog = false
    }

    private fun updateCalendarWithSelectedRegion(region: String) {
        // OCR結果から選択された地域の情報を取得
        val selectedRegionEntry = ocrResult?.calendar?.find { it.region == region }
        
        if (selectedRegionEntry != null) {
            // OCR結果から該当地域の情報を適用
            packet = packet.copy(calendar = listOf(selectedRegionEntry))
            Log.d("RegionSelection", "OCR結果から地域情報を適用: $region")
        } else {
            // OCR結果に該当地域がない場合は空のエントリを作成
            val newCalendarEntry = CalendarEntry(
                region = region,
                sowing_start = 0,
                sowing_start_stage = "",
                sowing_end = 0,
                sowing_end_stage = "",
                harvest_start = 0,
                harvest_start_stage = "",
                harvest_end = 0,
                harvest_end_stage = ""
            )
            packet = packet.copy(calendar = listOf(newCalendarEntry))
            Log.d("RegionSelection", "新規カレンダーエントリを作成: $region")
        }
    }

    private fun extractRegionsFromOcrResult(parsed: SeedPacket): List<String> {
        val regions = mutableListOf<String>()
        
        Log.d("RegionSelection", "extractRegionsFromOcrResult開始: parsed.calendar=${parsed.calendar}")
        
        // カレンダーエントリから地域名を抽出
        parsed.calendar?.forEach { entry ->
            Log.d("RegionSelection", "カレンダーエントリ確認: region='${entry.region}', isNotEmpty=${entry.region.isNotEmpty()}")
            if (entry.region.isNotEmpty()) {
                regions.add(entry.region)
            }
        }
        
        // 重複を除去して返す
        val distinctRegions = regions.distinct()
        Log.d("RegionSelection", "extractRegionsFromOcrResult完了: regions=$distinctRegions")
        return distinctRegions
    }

    // 地域選択ダイアログの編集用メソッド
    fun startEditingCalendarEntry(entry: CalendarEntry) {
        editingCalendarEntry = entry.copy()
    }

    fun updateEditingCalendarEntry(updatedEntry: CalendarEntry) {
        Log.d("RegionSelection", "updateEditingCalendarEntry: $updatedEntry")
        editingCalendarEntry = updatedEntry
    }

    fun saveEditingCalendarEntry() {
        editingCalendarEntry?.let { entry ->
            Log.d("RegionSelection", "saveEditingCalendarEntry: $entry")
            
            // OCR結果を更新
            ocrResult?.let { result ->
                val updatedCalendar = result.calendar.map { 
                    if (it.region == entry.region) entry else it 
                }
                ocrResult = result.copy(calendar = updatedCalendar)
                Log.d("RegionSelection", "OCR結果更新完了: $updatedCalendar")
            }
            
            // パケットのカレンダーも更新
            packet = packet.copy(calendar = listOf(entry))
            Log.d("RegionSelection", "パケット更新完了: ${packet.calendar}")
            
            // 編集状態をクリア
            editingCalendarEntry = null
        }
    }

    fun cancelEditingCalendarEntry() {
        editingCalendarEntry = null
    }

    fun exitCalendarEditMode() {
        isCalendarEditMode = false
    }

}
