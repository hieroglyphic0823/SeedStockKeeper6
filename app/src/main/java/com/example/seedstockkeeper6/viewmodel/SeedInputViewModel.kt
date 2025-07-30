package com.example.seedstockkeeper6.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seedstockkeeper6.model.SeedPacket
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class SeedInputViewModel : ViewModel() {
    // UI にバインドする主要な packet
    var packet by mutableStateOf(SeedPacket())
        private set

    // packet.imageUrls の先頭を Uri? 型で取得
    val imageUri: Uri?
        get() = packet.imageUrls.firstOrNull()?.toUri()

    private var bitmap: Bitmap? = null

    fun setSeed(seed: SeedPacket?) {
        packet = seed ?: SeedPacket()
    }

    fun onImageSelected(uri: Uri) {
        packet = packet.copy(imageUrls = listOf(uri.toString()))
    }

    fun setBitmap(bmp: Bitmap?) {
        bitmap = bmp
    }

    fun applyOcrResult(parsed: SeedPacket) {
        packet = parsed.copy(id = packet.id, imageUrls = packet.imageUrls)
    }

    // 各フィールド更新用の公開メソッド
    fun onProductNameChange(newName: String) = update { copy(productName = newName) }
    fun onVarietyChange(newVar: String) = update { copy(variety = newVar) }
    fun onFamilyChange(newFamily: String) = update { copy(family = newFamily) }
    fun onProductNumberChange(newPN: String) = update { copy(productNumber = newPN) }
    fun onCompanyChange(newCo: String) = update { copy(company = newCo) }
    fun onOriginCountryChange(newO: String) = update { copy(originCountry = newO) }
    fun onExpirationDateChange(newExp: String) = update { copy(expirationDate = newExp) }
    fun onContentsChange(newC: String) = update { copy(contents = newC) }
    fun onGerminationRateChange(newG: String) = update { copy(germinationRate = newG) }
    fun onSeedTreatmentChange(newST: String) = update { copy(seedTreatment = newST) }
    fun onNotesChange(newNotes: String) = update {
        copy(cultivation = cultivation.copy(notes = newNotes))
    }
    fun onHarvestingChange(newHarvest: String) = update {
        copy(cultivation = cultivation.copy(harvesting = newHarvest))
    }

    // 共通の更新ヘルパー
    private fun update(transform: SeedPacket.() -> SeedPacket) {
        packet = packet.transform()
    }

    fun saveSeed(onComplete: () -> Unit) {
        viewModelScope.launch {
            val db = Firebase.firestore
            val ref = db.collection("seeds")
            val id = packet.id ?: ref.document().id
            val storageRef = Firebase.storage.reference.child("seed_images/$id.jpg")

            val finalPacket = if (bitmap != null) {
                // bitmap をアップロード
                val baos = ByteArrayOutputStream().apply {
                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, this)
                }
                val uploadTask = storageRef.putBytes(baos.toByteArray()).await()
                val url = storageRef.downloadUrl.await().toString()
                packet.copy(id = id, imageUrls = listOf(url))
            } else {
                packet.copy(id = id)
            }

            db.collection("seeds").document(id).set(finalPacket).await()
            packet = finalPacket
            onComplete()
        }
    }
}
