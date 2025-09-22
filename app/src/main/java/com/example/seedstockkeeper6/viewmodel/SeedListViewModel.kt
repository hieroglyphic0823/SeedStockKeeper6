package com.example.seedstockkeeper6.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.example.seedstockkeeper6.model.SeedPacket
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class SeedListViewModel : ViewModel() {
    private val _seeds = mutableStateOf<List<SeedPacket>>(emptyList())
    val seeds: State<List<SeedPacket>> = _seeds
    
    init {
        Log.d("BootTrace", "SeedListViewModel init")
        android.util.Log.d("SeedListViewModel", "初期化時: seeds.value.size = ${_seeds.value.size}")
    }
    
    // プレビュー用のデモデータ設定メソッド
    fun setDemoSeeds(demoSeeds: List<SeedPacket>) {
        android.util.Log.d("SeedListViewModel", "setDemoSeeds呼び出し: ${demoSeeds.size}件")
        demoSeeds.forEach { seed ->
            android.util.Log.d("SeedListViewModel", "設定する商品: ${seed.productName}")
        }
        _seeds.value = demoSeeds
        android.util.Log.d("SeedListViewModel", "seeds.value更新完了: ${_seeds.value.size}件")
    }

    fun deleteSeedPacketWithImages(documentId: String, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = deleteSeedPacketWithImagesInternal(documentId)
            onComplete(result)
        }
    }

    // ★ public に変更
    suspend fun deleteSeedPacketWithImagesInternal(documentId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val db = Firebase.firestore
            val storage = Firebase.storage

            try {
                val docRef = db.collection("seeds").document(documentId)
                val documentSnapshot = docRef.get().await()

                if (!documentSnapshot.exists()) {
                    Log.w("SeedListVM", "Document $documentId does not exist.")
                    return@withContext Result.failure(NoSuchElementException("Document $documentId not found"))
                }

                val imageUrls = documentSnapshot.get("imageUrls") as? List<String> ?: emptyList()

                imageUrls.forEach { url ->
                    if (url.isNotBlank()) {
                        try {
                            val path = Uri.decode(url).substringAfter("/o/").substringBefore("?")
                            if (path.isNotEmpty()) {
                                storage.reference.child(path).delete().await()
                                Log.d("SeedListVM", "Deleted image: $path")
                            }
                        } catch (e: Exception) {
                            Log.e("SeedListVM", "Failed to delete image: $url", e)
                        }
                    }
                }

                docRef.delete().await()
                Log.d("SeedListVM", "Deleted document: $documentId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("SeedListVM", "Error deleting packet $documentId", e)
                Result.failure(e)
            }
        }
}
