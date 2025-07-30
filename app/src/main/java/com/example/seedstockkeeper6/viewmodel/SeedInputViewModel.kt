package com.example.seedstockkeeper6.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seedstockkeeper6.model.SeedPacket
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class SeedInputViewModel : ViewModel() {
    // 編集対象の種データ
    var packet by mutableStateOf(SeedPacket())
        private set

    // ユーザーがローカルから選択した画像（nullならFirebase画像を使用）
    private val _imageUri = mutableStateOf<Uri?>(null)
    val imageUri: State<Uri?> = _imageUri

    // OCR処理と画像アップロード用のBitmap
    private var bitmap: Bitmap? = null

    /** Firebaseから渡されたデータで初期化 */
    fun setSeed(seed: SeedPacket?) {
        packet = seed ?: SeedPacket()
    }

    /** ユーザーが画像を選択したときに呼ばれる */
    fun onImageSelected(uri: Uri) {
        _imageUri.value = uri
        packet = packet.copy(imageUrls = listOf(uri.toString())) // 一時的に画像URLにもセット
    }

    fun setBitmap(bmp: Bitmap?) {
        bitmap = bmp
    }

    /** OCR結果を反映 */
    fun applyOcrResult(parsed: SeedPacket) {
        packet = parsed.copy(
            id = packet.id,
            imageUrls = packet.imageUrls,
            cultivation = parsed.cultivation
        )
    }

    // フィールド更新メソッド（略さずにそのまま）
    fun onProductNameChange(v: String) = update { it.copy(productName = v) }
    fun onVarietyChange(v: String) = update { it.copy(variety = v) }
    fun onFamilyChange(v: String) = update { it.copy(family = v) }
    fun onProductNumberChange(v: String) = update { it.copy(productNumber = v) }
    fun onCompanyChange(v: String) = update { it.copy(company = v) }
    fun onOriginCountryChange(v: String) = update { it.copy(originCountry = v) }
    fun onExpirationDateChange(v: String) = update { it.copy(expirationDate = v) }
    fun onContentsChange(v: String) = update { it.copy(contents = v) }
    fun onGerminationRateChange(v: String) = update { it.copy(germinationRate = v) }
    fun onSeedTreatmentChange(v: String) = update { it.copy(seedTreatment = v) }

    fun onSpacingRowMinChange(v: String) = update {
        it.copy(cultivation = it.cultivation.copy(spacing_cm_row_min = v.toIntOrNull() ?: 0))
    }

    fun onSpacingRowMaxChange(v: String) = update {
        it.copy(cultivation = it.cultivation.copy(spacing_cm_row_max = v.toIntOrNull() ?: 0))
    }

    fun onSpacingPlantMinChange(v: String) = update {
        it.copy(cultivation = it.cultivation.copy(spacing_cm_plant_min = v.toIntOrNull() ?: 0))
    }

    fun onSpacingPlantMaxChange(v: String) = update {
        it.copy(cultivation = it.cultivation.copy(spacing_cm_plant_max = v.toIntOrNull() ?: 0))
    }

    fun onGermTempChange(v: String) = update {
        it.copy(cultivation = it.cultivation.copy(germinationTemp_c = v))
    }

    fun onGrowTempChange(v: String) = update {
        it.copy(cultivation = it.cultivation.copy(growingTemp_c = v))
    }

    fun onCompostChange(v: String) = update {
        it.copy(cultivation = it.cultivation.copy(
            soilPrep_per_sqm = it.cultivation.soilPrep_per_sqm.copy(compost_kg = v.toIntOrNull() ?: 0)
        ))
    }

    fun onLimeChange(v: String) = update {
        it.copy(cultivation = it.cultivation.copy(
            soilPrep_per_sqm = it.cultivation.soilPrep_per_sqm.copy(dolomite_lime_g = v.toIntOrNull() ?: 0)
        ))
    }

    fun onFertilizerChange(v: String) = update {
        it.copy(cultivation = it.cultivation.copy(
            soilPrep_per_sqm = it.cultivation.soilPrep_per_sqm.copy(chemical_fertilizer_g = v.toIntOrNull() ?: 0)
        ))
    }

    fun onNotesChange(v: String) = update {
        it.copy(cultivation = it.cultivation.copy(notes = v))
    }

    fun onHarvestingChange(v: String) = update {
        it.copy(cultivation = it.cultivation.copy(harvesting = v))
    }

    private fun update(transform: (SeedPacket) -> SeedPacket) {
        packet = transform(packet)
    }

    /** FirestoreとStorageにデータ保存 */
    fun saveSeed(onComplete: () -> Unit) {
        viewModelScope.launch {
            val db = Firebase.firestore
            val storageRoot = Firebase.storage.reference
            val ref = db.collection("seeds")
            val id = packet.id ?: ref.document().id
            var final = packet.copy(id = id)

            bitmap?.let { bmp ->
                try {
                    val baos = ByteArrayOutputStream().apply {
                        bmp.compress(Bitmap.CompressFormat.JPEG, 80, this)
                    }
                    val path = "seed_images/$id.jpg"
                    val imageRef = storageRoot.child(path)
                    imageRef.putBytes(baos.toByteArray()).await()
                    val url = imageRef.downloadUrl.await().toString()
                    final = final.copy(imageUrls = listOf(url))
                } catch (e: Exception) {
                    Log.e("ViewModel", "Upload failed", e)
                }
            }

            try {
                db.collection("seeds").document(id).set(final).await()
                packet = final
                onComplete()
            } catch (e: Exception) {
                Log.e("ViewModel", "Firestore save failed", e)
            }
        }
    }
}
