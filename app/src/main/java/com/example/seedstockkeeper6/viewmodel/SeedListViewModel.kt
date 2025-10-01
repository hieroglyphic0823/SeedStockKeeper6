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
import com.example.seedstockkeeper6.service.StatisticsService
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class SeedListViewModel : ViewModel() {
    private val _seeds = mutableStateOf<List<SeedPacket>>(emptyList())
    val seeds: State<List<SeedPacket>> = _seeds
    
    // 集計サービス
    private val statisticsService = StatisticsService()
    
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
                
                // 集計データを更新
                try {
                    updateStatisticsAfterSeedChange()
                } catch (e: Exception) {
                    Log.w("SeedListVM", "集計更新に失敗しましたが、削除は成功", e)
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("SeedListVM", "Error deleting packet $documentId", e)
                Result.failure(e)
            }
        }
    
    /**
     * 種データ変更後の集計更新処理
     */
    private suspend fun updateStatisticsAfterSeedChange() {
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid ?: return
            
            // 現在のユーザーの全種データを取得
            val db = Firebase.firestore
            val seedsSnapshot = db.collection("seeds")
                .whereEqualTo("ownerUid", uid)
                .get().await()
            
            val seeds = seedsSnapshot.documents.mapNotNull { doc ->
                try {
                    val seed = doc.toObject(SeedPacket::class.java)
                    seed?.copy(id = doc.id, documentId = doc.id)
                } catch (e: Exception) {
                    Log.w("StatisticsUpdate", "種データ解析エラー: ${doc.id}", e)
                    null
                }
            }
            
            // 集計データを更新
            val result = statisticsService.updateStatisticsOnSeedChange(uid, seeds)
            if (result.success) {
                Log.d("StatisticsUpdate", "集計データ更新完了")
            } else {
                Log.w("StatisticsUpdate", "集計データ更新失敗: ${result.message}")
            }
        } catch (e: Exception) {
            Log.e("StatisticsUpdate", "集計更新処理エラー", e)
        }
    }
}
