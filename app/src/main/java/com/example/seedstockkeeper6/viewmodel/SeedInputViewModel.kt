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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.net.URL

class SeedInputViewModel : ViewModel() {
    init {
        Log.d("SeedInputVM_Lifecycle", "SeedInputViewModel instance created: $this")
    }

    var packet by mutableStateOf(SeedPacket())
        private set

    val imageUris = mutableStateListOf<Uri>()
    var ocrTargetIndex by mutableStateOf(-1)
        private set

    fun setSeed(seed: SeedPacket?) {
        Log.w("SET_SEED_CALLED", "setSeed called with: $seed. Current packet BEFORE change: ${this.packet}", Throwable())
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
        if (index in imageUris.indices) {
            imageUris.removeAt(index)
            if (ocrTargetIndex == index) {
                ocrTargetIndex = if (imageUris.isNotEmpty()) 0 else -1
            } else if (ocrTargetIndex > index) {
                ocrTargetIndex--
            }
        }
    }

    suspend fun performOcr(context: Context) {
        if (ocrTargetIndex !in imageUris.indices) return
        val uri = imageUris[ocrTargetIndex]
        val bmp = when {
            uri.scheme == "content" -> uriToBitmap(context, uri)
            uri.scheme == "https" || uri.scheme == "http" -> {
                val stream = URL(uri.toString()).openStream()
                BitmapFactory.decodeStream(stream)
            }
            else -> null
        }
        if (bmp == null){
            Log.e("OCR","Bitmap is null,cannot run OCR")
            return
        }
        val parsedJson = runGeminiOcr(context, bmp)
        val cleaned = parsedJson.removePrefix("```json").removeSuffix("```").trim()
        val parsedPacket = com.google.gson.Gson().fromJson(cleaned, SeedPacket::class.java)
        packet = parsedPacket.copy(
            id = packet.id,
            imageUrls = packet.imageUrls,
            cultivation = parsedPacket.cultivation
        )
    }

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
        val oldPacket = packet
        packet = transform(packet)
        Log.d("VM_PacketUpdate", "Packet updated. Old: $oldPacket, New: $packet")
    }

    fun saveSeed(context: Context, onComplete: () -> Unit) {
        viewModelScope.launch {
            val db = Firebase.firestore
            val storageRoot = Firebase.storage.reference
            val ref = db.collection("seeds")
            val id = packet.id ?: ref.document().id
            val uploadedUrls = mutableListOf<String>()

            imageUris.forEachIndexed { index, uri ->
                if (uri.toString().startsWith("http")) {
                    uploadedUrls.add(uri.toString())
                } else {
                    try {
                        val bmp = uriToBitmap(context, uri)
                        val bytes = ByteArrayOutputStream().apply {
                            bmp.compress(Bitmap.CompressFormat.JPEG, 80, this)
                        }.toByteArray()
                        val path = "seed_images/${id}_$index.jpg"
                        val imageRef = storageRoot.child(path)
                        imageRef.putBytes(bytes).await()
                        val url = imageRef.downloadUrl.await().toString()
                        uploadedUrls.add(url)
                    } catch (e: Exception) {
                        Log.e("SeedInputVM", "Upload failed: $uri", e)
                    }
                }
            }

            val final = packet.copy(id = id, imageUrls = uploadedUrls)

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
