package com.example.seedstockkeeper6.viewmodel

import android.net.Uri
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
    }
    
    // プレビュー用のデモデータ設定メソッド
    fun setDemoSeeds(demoSeeds: List<SeedPacket>) {
        demoSeeds.forEach { seed ->
        }
        _seeds.value = demoSeeds
    }

    fun deleteSeedPacketWithImages(documentId: String, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = deleteSeedPacketWithImagesInternal(documentId)
            onComplete(result)
        }
    }
    
    fun updateFinishedFlag(documentId: String, isFinished: Boolean, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = updateFinishedFlagInternal(documentId, isFinished)
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
                    return@withContext Result.failure(NoSuchElementException("Document $documentId not found"))
                }

                val imageUrls = documentSnapshot.get("imageUrls") as? List<String> ?: emptyList()

                imageUrls.forEach { url ->
                    if (url.isNotBlank()) {
                        try {
                            val path = Uri.decode(url).substringAfter("/o/").substringBefore("?")
                            if (path.isNotEmpty()) {
                                storage.reference.child(path).delete().await()
                            }
                        } catch (e: Exception) {
                        }
                    }
                }

                docRef.delete().await()
                
                // 集計データを更新
                try {
                    updateStatisticsAfterSeedChange()
                } catch (e: Exception) {
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    suspend fun updateFinishedFlagInternal(documentId: String, isFinished: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            val db = Firebase.firestore
            
            try {
                val docRef = db.collection("seeds").document(documentId)
                docRef.update("isFinished", isFinished).await()
                
                // 集計データを更新
                try {
                    updateStatisticsAfterSeedChange()
                } catch (e: Exception) {
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 種データ変更後の集計更新処理
     */
    private suspend fun updateStatisticsAfterSeedChange() {
        try {
            
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid
            
            if (uid == null) {
                return
            }
            
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
                    null
                }
            }
            
            // 集計データを更新
            val result = statisticsService.updateStatisticsOnSeedChange(uid, seeds)
            
            if (result.success) {
            } else {
            }
        } catch (e: Exception) {
        }
    }
}
