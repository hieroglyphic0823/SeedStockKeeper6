package com.example.seedstockkeeper6.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seedstockkeeper6.data.runGeminiOcr
import com.example.seedstockkeeper6.data.uriToBitmap
import com.example.seedstockkeeper6.ml.CalendarDetector
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.service.StatisticsService
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
    
    // 集計サービス
    private val statisticsService = StatisticsService()

    var ocrTargetIndex by mutableStateOf(-1)
        private set
    var showSnackbar by mutableStateOf<String?>(null)
    var showAIDiffDialog by mutableStateOf(false)
    var aiDiffList = mutableStateListOf<Triple<String, String, String>>()
        private set

    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)

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
    
    // 種情報登録画面の編集モード状態
    var isEditMode by mutableStateOf(false)
        private set
    
    // 既存データがあるかどうか
    var hasExistingData by mutableStateOf(false)
    
    // 農園情報の地域（地域選択ダイアログの初期値として使用）
    var farmDefaultRegion by mutableStateOf("")

    fun setSeed(seed: SeedPacket?) {
        packet = seed ?: SeedPacket()
        // idまたはdocumentIdのいずれかが存在すれば既存データとみなす
        hasExistingData = (seed?.id?.isNotEmpty() == true) || (seed?.documentId?.isNotEmpty() == true)
        isEditMode = false // 初期状態は表示モード
        
        
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
        selectedImageUri = null
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
            }
        }
    }

    suspend fun performOcr(context: Context) {
        if (ocrTargetIndex !in imageUris.indices) {
            showSnackbar = "対象の画像がありません。"
            return
        }
        
        // アプリ起動直後は少し待機
        kotlinx.coroutines.delay(500L)

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
            showSnackbar = "画像の読み込みに失敗しました。"
            return
        }

        if (bmp == null) {
            showSnackbar = "画像が読み込めませんでした。"
            return
        }

        val jsonText = try {
            // 初回失敗時は少し待ってからリトライ
            var attempt = 0
            var result: String? = null
            
            while (attempt < 2 && result == null) {
                try {
                    if (attempt > 0) {
                        // リトライ前に少し待機
                        kotlinx.coroutines.delay(1000L)
                    }
                    result = runGeminiOcr(context, bmp)
                } catch (e: Exception) {
                    if (attempt == 0) {
                        // 初回失敗時はリトライ
                        attempt++
                        continue
                    } else {
                        // リトライ後も失敗
                        throw e
                    }
                }
            }
            
            result ?: throw Exception("解析に失敗しました")
        } catch (e: Exception) {
            showSnackbar = "解析失敗"
            return
        }

        val parsed = try {
            val cleanedJson = jsonText.removePrefix("```json").removeSuffix("```" ).trim()
            kotlinx.serialization.json.Json.decodeFromString<SeedPacket>(cleanedJson)
        } catch (e: Exception) {
            showSnackbar = "解析失敗"
            return
        }
        
        // OCR結果を保存
        ocrResult = parsed
        
        // OCR結果の有効期限情報をログに表示
        parsed.calendar?.forEach { entry ->
        } ?: run { /* カレンダー情報なし */ }
        
        // 地域名を検出して地域選択ダイアログを表示
        val detectedRegions = extractRegionsFromOcrResult(parsed)
        
        // 地域が検出されていない場合はデフォルト地域リストを使用
        val regionsToShow = if (detectedRegions.isNotEmpty()) {
            detectedRegions
        } else {
            listOf("北海道", "東北", "関東", "中部", "関西", "中国", "四国", "九州", "沖縄")
        }
        
        showRegionSelectionDialog(regionsToShow)
        
        // カレンダー切り抜きは毎回実行
        try {
            tryAddCroppedCalendarImage(context, bmp)
        } catch (e: Exception) {
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
            
            // 地域確認ダイアログで編集されたカレンダー情報をパケットに反映
            // OCR結果ではなく、地域確認ダイアログで編集された値を優先
            if (packet.calendar != null && packet.calendar.isNotEmpty()) {
                // 既にpacket.calendarに反映されているので、そのまま使用
            } else {
                // パケットにカレンダー情報がない場合は、OCR結果を使用
                ocrResult?.let { result ->
                    packet = packet.copy(calendar = result.calendar)
                } ?: run {
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
    fun onExpirationYearChange(value: String) { 
        val newExpirationYear = value.toIntOrNull() ?: 0
        packet = packet.copy(expirationYear = newExpirationYear)
        // カレンダーエントリの有効期限も更新
        updateCalendarEntriesExpiration(newExpirationYear, packet.expirationMonth)
    }
    fun onExpirationMonthChange(value: String) { 
        val newExpirationMonth = value.toIntOrNull() ?: 0
        packet = packet.copy(expirationMonth = newExpirationMonth)
        // カレンダーエントリの有効期限も更新
        updateCalendarEntriesExpiration(packet.expirationYear, newExpirationMonth)
    }
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
            isSaving = true
            try {
                // 1) docId を確定（既存なら流用、無ければ新規発番）
                val target = packet.documentId?.let { db.collection("seeds").document(it) }
                    ?: db.collection("seeds").document()
                val id = target.id

                // 2) 所有者を確定（ここで create/update を"先に"確定させる）
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
                            null
                        }

                        if (bitmap != null) {
                            val baos = java.io.ByteArrayOutputStream().apply {
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, this)
                            }
                            val bytes = baos.toByteArray()
                            val imagePath = "seed_images/${id}_${java.util.UUID.randomUUID()}.jpg"
                            try {
                                // アップロード処理（リトライ機能付き）
                                var uploadSuccess = false
                                var retryCount = 0
                                val maxRetries = 3
                                
                                while (!uploadSuccess && retryCount < maxRetries) {
                                    try {
                                        storageRef.child(imagePath).putBytes(bytes).await()
                                        uploadSuccess = true
                                        pathsByIndex[index] = imagePath
                                    } catch (e: kotlinx.coroutines.CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        retryCount++
                                        if (retryCount >= maxRetries) {
                                        } else {
                                            // リトライ前に少し待機
                                            kotlinx.coroutines.delay(1000L * retryCount)
                                        }
                                    }
                                }
                            } catch (e: kotlinx.coroutines.CancellationException) {
                                throw e
                            } catch (e: Exception) {
                            }
                        } else {
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
                        }.onFailure {
                        }
                    }
                }

                // 5) アップロード結果のチェック
                val finalOrderedPaths = pathsByIndex.mapNotNull { it }
                val failedUploads = pathsByIndex.count { it == null }
                
                
                // アップロード失敗がある場合の処理
                if (failedUploads > 0) {
                    // 一部の画像アップロードが失敗した場合でも、成功した画像は保存する
                }

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
                
                // 集計データを更新
                try {
                    updateStatisticsAfterSeedChange(uid)
                } catch (e: Exception) {
                }
                
                showSnackbar = "保存が完了しました（画像: ${finalOrderedPaths.size}）"
                onComplete(Result.success(Unit))
            } catch (e: SecurityException) {
                showSnackbar = "このデータの所有者ではありません"
                onComplete(Result.failure(e))
            } catch (e: Exception) {
                showSnackbar = "保存に失敗しました: ${e.localizedMessage ?: "不明なエラー"}"
                onComplete(Result.failure(e))
            } finally {
                isLoading = false
                isSaving = false
            }
        }
    }


    suspend fun getDownloadUrlFromPath(path: String): String? {
        return try {
            Firebase.storage.reference.child(path).downloadUrl.await().toString()
        } catch (e: Exception) {
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
            } else {
                croppedCalendarBitmap = null
            }
        } catch (e: Exception) {
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
            .onFailure { /* MLKit seed outer detect failed */ }
            .getOrNull() ?: return

        if (objects.isEmpty()) {
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
            return
        }

        val insertPos = insertAfterIndex.coerceIn(0, imageUris.size)
        imageUris.add(insertPos + 1, Uri.fromFile(file))
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
                    showSnackbar = "画像データが不正です (bmp is null)"
                    return@launch // または適切なエラー処理
                } else if (bmp.isRecycled) {
                    showSnackbar = "画像データが不正です (bmp is recycled)"
                    return@launch // または適切なエラー処理
                } else if (bmp.width == 0 || bmp.height == 0) {
                    showSnackbar = "画像データが空です (zero dimensions)"
                    return@launch // または適切なエラー処理
                } else {
                }
                // ---- ML Kit 検出 → 1位選出（面積×中心×ラベル係数） ----
                val input = InputImage.fromBitmap(bmp, 0)
                val objects = runCatching { seedOuterDetector.process(input).await() }
                    .onFailure { /* MLKit seed outer detect failed */ }
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

                if (best == null) { showSnackbar = "解析失敗"; return@launch }

                // margin を付け、さらに“袋のエッジ”に吸着させてタイト化
                val pre = Rect(
                    (best.left   - (w * marginRatio)).toInt().coerceAtLeast(0),
                    (best.top    - (h * marginRatio)).toInt().coerceAtLeast(0),
                    (best.right  + (w * marginRatio)).toInt().coerceAtMost(w),
                    (best.bottom + (h * marginRatio)).toInt().coerceAtMost(h)
                )

                if (bmp != null) {
                }

                if (pre != null) {
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
                if (pre != null) {
                }
                // プレビュー作成＆確認ダイアログへ
//                val overlay = com.example.seedstockkeeper6.util.drawRectOverlay(bmp, tight)
//                val crop = Bitmap.createBitmap(bmp, tight.left, tight.top, tight.width(), tight.height())
                val overlay = drawRectOverlay(bmp, tight)

                val crop: Bitmap?
                try {
                    // ここで条件チェックを明示的に行うのも有効
                    if (tight.width() <= 0 || tight.height() <= 0) {
                        
                        showSnackbar = "切り抜き領域のサイズが不正です。"
                        return@launch
                    }
                    if (tight.left < 0 || tight.top < 0 || tight.left + tight.width() > bmp.width || tight.top + tight.height() > bmp.height) {
                        
                        
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

                    pendingCropOverlay = overlay
                    pendingCropBitmap = crop
                    showCropConfirmDialog = true

                } catch (e: IllegalArgumentException) {
                     
                    showSnackbar = "画像の切り抜きに失敗 (引数エラー): ${e.message}"
                    return@launch // または適切なエラー処理
                } catch (e: Exception) {
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
            .onFailure { /* MLKit seed outer detect failed */ }
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

    fun addCalendarEntryWithRegion(region: String) {
        val newEntry = com.example.seedstockkeeper6.model.CalendarEntry(
            region = region,
            sowing_start_date = "",
            sowing_end_date = "",
            harvest_start_date = "",
            harvest_end_date = ""
        )
        val list = (packet.calendar ?: emptyList()) + newEntry
        packet = packet.copy(calendar = list)
    }

    fun updateCalendarEntry(
        index: Int,
        region: String? = null,
        sowing_start_date: String? = null,
        sowing_end_date: String? = null,
        harvest_start_date: String? = null,
        harvest_end_date: String? = null
    ) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val newItem = old.copy(
            region = region ?: old.region,
            sowing_start_date = sowing_start_date ?: old.sowing_start_date,
            sowing_end_date = sowing_end_date ?: old.sowing_end_date,
            harvest_start_date = harvest_start_date ?: old.harvest_start_date,
            harvest_end_date = harvest_end_date ?: old.harvest_end_date
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
    // 日付ベースの更新関数
    fun updateCalendarSowingStartDate(index: Int, date: String) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val updated = old.copy(sowing_start_date = date)
        val next = cur.toMutableList().apply { set(index, updated) }
        packet = packet.copy(calendar = next)
    }

    fun updateCalendarSowingEndDate(index: Int, date: String) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val updated = old.copy(sowing_end_date = date)
        val next = cur.toMutableList().apply { set(index, updated) }
        packet = packet.copy(calendar = next)
    }

    fun updateCalendarHarvestStartDate(index: Int, date: String) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val updated = old.copy(harvest_start_date = date)
        val next = cur.toMutableList().apply { set(index, updated) }
        packet = packet.copy(calendar = next)
    }

    fun updateCalendarHarvestEndDate(index: Int, date: String) {
        val cur = packet.calendar ?: return
        if (index !in cur.indices) return
        val old = cur[index]
        val updated = old.copy(harvest_end_date = date)
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
        detectedRegions.clear()
        detectedRegions.addAll(regions)
        showRegionSelectionDialog = true
    }

    fun onRegionSelected(region: String) {
        selectedRegion = region
        showRegionSelectionDialog = false
        
        // 編集された値がある場合は、それを優先して使用
        if (editingCalendarEntry != null && editingCalendarEntry!!.region == region) {
            packet = packet.copy(calendar = listOf(editingCalendarEntry!!))
            
            // 有効期限が設定されている場合は、パケットの有効期限も更新
            if (editingCalendarEntry!!.expirationYear > 0 && editingCalendarEntry!!.expirationMonth > 0) {
                packet = packet.copy(
                    expirationYear = editingCalendarEntry!!.expirationYear,
                    expirationMonth = editingCalendarEntry!!.expirationMonth
                )
            }
        } else {
            // 選択された地域でカレンダーエントリを更新
            updateCalendarWithSelectedRegion(region)
        }
        
        // 編集モードを有効にする
        isCalendarEditMode = true
    }

    fun onRegionSelectionDismiss() {
        showRegionSelectionDialog = false
    }

    private fun updateCalendarWithSelectedRegion(region: String) {
        // OCR結果から選択された地域の情報を取得
        val selectedRegionEntry = ocrResult?.calendar?.find { it.region == region }
        
        if (selectedRegionEntry != null) {
            // OCR結果から該当地域の情報を適用
            // 有効期限の年を設定（OCR結果にない場合は現在年+1）
            val currentDate = java.time.LocalDate.now()
            val expirationYear = if (selectedRegionEntry.expirationYear > 0) {
                selectedRegionEntry.expirationYear
            } else if (packet.expirationYear > 0) {
                packet.expirationYear
            } else {
                currentDate.year + 1 // 一年後の年
            }
            
            val expirationMonth = if (selectedRegionEntry.expirationMonth > 0) {
                selectedRegionEntry.expirationMonth
            } else if (packet.expirationMonth > 0) {
                packet.expirationMonth
            } else {
                currentDate.monthValue
            }
            
            val updatedEntry = selectedRegionEntry.copy(
                expirationYear = expirationYear,
                expirationMonth = expirationMonth,
                // 播種期間と収穫期間の年を適切に設定
                sowing_start_date = calculateDateWithAppropriateYear(selectedRegionEntry.sowing_start_date, expirationYear, currentDate),
                sowing_end_date = calculateDateWithAppropriateYear(selectedRegionEntry.sowing_end_date, expirationYear, currentDate),
                harvest_start_date = calculateDateWithAppropriateYear(selectedRegionEntry.harvest_start_date, expirationYear, currentDate),
                harvest_end_date = calculateDateWithAppropriateYear(selectedRegionEntry.harvest_end_date, expirationYear, currentDate)
            )
            
            packet = packet.copy(calendar = listOf(updatedEntry))
        } else {
            // OCR結果に該当地域がない場合は空のエントリを作成
            // 有効期限の年を取得（OCR結果から、または現在年+1）
            val currentDate = java.time.LocalDate.now()
            val expirationYear = if (packet.expirationYear > 0) {
                packet.expirationYear
            } else {
                currentDate.year + 1 // 一年後の年
            }
            
            val newCalendarEntry = CalendarEntry(
                region = region,
                sowing_start_date = "",
                sowing_end_date = "",
                harvest_start_date = "",
                harvest_end_date = "",
                expirationYear = expirationYear,
                expirationMonth = if (packet.expirationMonth > 0) packet.expirationMonth else currentDate.monthValue
            )
            packet = packet.copy(calendar = listOf(newCalendarEntry))
        }
    }

    private fun extractRegionsFromOcrResult(parsed: SeedPacket): List<String> {
        val regions = mutableListOf<String>()
        
        
        // カレンダーエントリから地域名を抽出
        parsed.calendar?.forEach { entry ->
            if (entry.region.isNotEmpty()) {
                regions.add(entry.region)
            }
        }
        
        // 重複を除去して返す
        val distinctRegions = regions.distinct()
        return distinctRegions
    }

    // 地域選択ダイアログの編集用メソッド
    fun startEditingCalendarEntry(entry: CalendarEntry) {
        editingCalendarEntry = entry.copy()
    }

    fun updateEditingCalendarEntry(updatedEntry: CalendarEntry) {
        editingCalendarEntry = updatedEntry
    }

    fun saveEditingCalendarEntry() {
        editingCalendarEntry?.let { entry ->
            
            // 年を有効期限から計算して設定
            val expirationYear = packet.expirationYear
            val calculatedEntry = if (expirationYear > 0) {
                // 有効期限の年を使用して日付を再構築
                val updatedEntry = entry.copy(
                    sowing_start_date = calculateDateWithYear(entry.sowing_start_date, expirationYear),
                    sowing_end_date = calculateDateWithYear(entry.sowing_end_date, expirationYear),
                    harvest_start_date = calculateDateWithYear(entry.harvest_start_date, expirationYear),
                    harvest_end_date = calculateDateWithYear(entry.harvest_end_date, expirationYear)
                )
                updatedEntry
            } else {
                entry
            }
            
            // OCR結果を更新
            ocrResult?.let { result ->
                val updatedCalendar = result.calendar.map { 
                    if (it.region == calculatedEntry.region) calculatedEntry else it 
                }
                ocrResult = result.copy(calendar = updatedCalendar)
            } ?: run {
            }
            
            // パケットのカレンダーも更新
            packet = packet.copy(calendar = listOf(calculatedEntry))
            
            // 編集状態をクリア
            editingCalendarEntry = null
        } ?: run {
        }
    }
    
    // 地域確認ダイアログで有効期限が変更された際に種登録画面の有効期限フィールドに反映する
    fun updateExpirationFromCalendarEntry(entry: CalendarEntry) {
        
        // カレンダーエントリの有効期限情報を種登録画面の有効期限フィールドに反映
        if (entry.expirationYear > 0) {
            packet = packet.copy(expirationYear = entry.expirationYear)
        }
        
        if (entry.expirationMonth > 0) {
            packet = packet.copy(expirationMonth = entry.expirationMonth)
        }
        
    }
    
    // 種の有効期限が変更された際にカレンダーエントリの有効期限も更新する
    private fun updateCalendarEntriesExpiration(expirationYear: Int, expirationMonth: Int) {
        if (expirationYear > 0 && expirationMonth > 0) {
            packet.calendar?.let { calendar ->
                val updatedCalendar = calendar.map { entry ->
                    entry.copy(
                        expirationYear = expirationYear,
                        expirationMonth = expirationMonth
                    )
                }
                packet = packet.copy(calendar = updatedCalendar)
            }
            
            // OCR結果のカレンダーエントリも更新
            ocrResult?.let { result ->
                val updatedOcrCalendar = result.calendar.map { entry ->
                    entry.copy(
                        expirationYear = expirationYear,
                        expirationMonth = expirationMonth
                    )
                }
                ocrResult = result.copy(calendar = updatedOcrCalendar)
            }
        }
    }
    
    // 月と旬から年を設定して日付を構築するヘルパー関数
    private fun calculateDateWithYear(dateString: String, year: Int): String {
        if (dateString.isEmpty()) return ""
        
        // 既存の日付から月と日を抽出
        val parts = dateString.split("-")
        if (parts.size >= 2) {
            val month = parts[1]
            val day = if (parts.size >= 3) parts[2] else "01"
            return "$year-$month-$day"
        }
        
        return dateString
    }
    
    // 月が現在以降なら現在の年、月が現在以前なら有効期限の年を設定するヘルパー関数
    private fun calculateDateWithAppropriateYear(dateString: String, expirationYear: Int, currentDate: java.time.LocalDate): String {
        if (dateString.isEmpty()) return ""
        
        // 既存の日付から月と日を抽出
        val parts = dateString.split("-")
        if (parts.size >= 2) {
            val month = parts[1].toIntOrNull() ?: return dateString
            val day = if (parts.size >= 3) parts[2] else "01"
            
            // 月が現在以降なら現在の年、月が現在以前なら有効期限の年
            val targetYear = if (month >= currentDate.monthValue) {
                currentDate.year
            } else {
                expirationYear
            }
            
            return "$targetYear-$month-$day"
        }
        
        return dateString
    }

    fun cancelEditingCalendarEntry() {
        editingCalendarEntry = null
    }

    fun enterCalendarEditMode() {
        isCalendarEditMode = true
    }
    
    fun exitCalendarEditMode() {
        isCalendarEditMode = false
    }
    
    // 種情報登録画面の編集モード制御
    fun enterEditMode() {
        isEditMode = true
    }
    
    fun exitEditMode() {
        isEditMode = false
    }
    
    fun markAsExistingData() {
        hasExistingData = true
    }
    
    fun saveSeedData(context: android.content.Context, onComplete: (Result<Unit>) -> Unit) {
        // 種情報の保存処理
        // 既存のsaveSeedメソッドを呼び出す
        saveSeed(context, onComplete)
    }
    
    /**
     * 種データ変更後の集計更新処理
     */
    private suspend fun updateStatisticsAfterSeedChange(ownerUid: String) {
        try {
            
            // 現在のユーザーの全種データを取得
            val db = Firebase.firestore
            val seedsSnapshot = db.collection("seeds")
                .whereEqualTo("ownerUid", ownerUid)
                .get().await()
            
            
            val seeds = seedsSnapshot.documents.mapNotNull { doc ->
                try {
                    val seed = doc.toObject(SeedPacket::class.java)
                    seed?.copy(id = doc.id, documentId = doc.id)
                } catch (e: Exception) {
                    null
                }
            }
            
            // 集計データを更新
            val result = statisticsService.updateStatisticsOnSeedChange(ownerUid, seeds)
            
            if (result.success) {
            } else {
            }
        } catch (e: Exception) {
        }
    }

}
