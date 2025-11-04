package com.example.seedstockkeeper6.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.auth.ktx.auth
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
        loadSeeds()
    }
    
    /**
     * Firebaseから種データを読み込む
     */
    fun loadSeeds() {
        viewModelScope.launch {
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                
                if (currentUser == null) {
                    _seeds.value = emptyList()
                    return@launch
                }
                
                val db = Firebase.firestore
                val snapshot = db.collection("seeds")
                    .whereEqualTo("ownerUid", currentUser.uid)
                    .get()
                    .await()
                
                val seedList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val seed = doc.toObject(SeedPacket::class.java)
                        seed?.copy(documentId = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                
                _seeds.value = seedList
            } catch (e: Exception) {
                // エラーハンドリング
                _seeds.value = emptyList()
            }
        }
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
                
                // まず現在の種データを取得して期限切れフラグをチェック
                val docSnapshot = docRef.get().await()
                if (docSnapshot.exists()) {
                    val seed = docSnapshot.toObject(SeedPacket::class.java)
                    if (seed != null) {
                        val isExpired = com.example.seedstockkeeper6.utils.ExpirationUtils.isSeedExpired(seed)
                        
                        // まき終わりフラグと期限切れフラグ、播種日を同時に更新
                        val updates = mutableMapOf<String, Any>()
                        updates["isFinished"] = isFinished
                        if (seed.isExpired != isExpired) {
                            updates["isExpired"] = isExpired
                            android.util.Log.d("SeedListViewModel", "期限切れフラグを更新: ${seed.productName} ${seed.isExpired} -> $isExpired")
                        }
                        // まき終わりに変更する場合は現在の日付を設定、解除する場合はクリア
                        val currentDate = java.time.LocalDate.now()
                        val newSowingDate = if (isFinished) {
                            currentDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                        } else {
                            ""
                        }
                        updates["sowingDate"] = newSowingDate
                        
                        docRef.update(updates).await()
                    } else {
                        // 種データが取得できない場合はまき終わりフラグと播種日を更新
                        val currentDate = java.time.LocalDate.now()
                        val newSowingDate = if (isFinished) {
                            currentDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                        } else {
                            ""
                        }
                        val updates = hashMapOf<String, Any>(
                            "isFinished" to isFinished,
                            "sowingDate" to newSowingDate
                        )
                        docRef.update(updates).await()
                    }
                } else {
                    // ドキュメントが存在しない場合はまき終わりフラグと播種日を更新
                    val currentDate = java.time.LocalDate.now()
                    val newSowingDate = if (isFinished) {
                        currentDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                    } else {
                        ""
                    }
                    val updates = hashMapOf<String, Any>(
                        "isFinished" to isFinished,
                        "sowingDate" to newSowingDate
                    )
                    docRef.update(updates).await()
                }
                
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
