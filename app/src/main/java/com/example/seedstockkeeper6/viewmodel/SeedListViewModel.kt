package com.example.seedstockkeeper6.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.auth.FirebaseAuth
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

    fun deleteSeedPacketWithImages(documentId: String, context: android.content.Context?, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = deleteSeedPacketWithImagesInternal(documentId, context)
            onComplete(result)
        }
    }
    
    fun updateFinishedFlag(documentId: String, isFinished: Boolean, context: android.content.Context?, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = updateFinishedFlagInternal(documentId, isFinished, context)
            onComplete(result)
        }
    }

    // ★ public に変更
    suspend fun deleteSeedPacketWithImagesInternal(documentId: String, context: android.content.Context?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val db = Firebase.firestore
            val storage = Firebase.storage

            try {
                val docRef = db.collection("seeds").document(documentId)
                val documentSnapshot = docRef.get().await()

                if (!documentSnapshot.exists()) {
                    return@withContext Result.failure(NoSuchElementException("Document $documentId not found"))
                }
                
                // 削除前にSeedPacketを取得（Googleカレンダー連携用）
                val seedPacket = documentSnapshot.toObject(SeedPacket::class.java)

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
                
                // Googleカレンダーからイベントを削除
                seedPacket?.let { packet ->
                    context?.let { ctx ->
                        try {
                            deleteGoogleCalendarEvents(ctx, packet)
                        } catch (e: Exception) {
                            android.util.Log.e("SeedListViewModel", "Googleカレンダーイベント削除エラー: ${e.message}", e)
                            // エラーが起きてもFirestore削除は続行
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
    
    /**
     * Googleカレンダーからイベントを削除
     */
    private suspend fun deleteGoogleCalendarEvents(context: android.content.Context, packet: SeedPacket) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return
        
        // 農園設定からcalendarIdを取得
        val db = Firebase.firestore
        val settingsDoc = db.collection("users").document(uid).collection("settings").document("general")
        val settingsSnapshot = settingsDoc.get().await()
        
        val calendarId = settingsSnapshot.getString("calendarId")
        if (calendarId.isNullOrBlank()) {
            android.util.Log.d("SeedListViewModel", "GoogleカレンダーIDが設定されていません。スキップします。")
            return
        }
        
        // アクセストークンを取得
        val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
            ?: return // Google Sign-Inされていない場合はスキップ
        
        val accountEmail = account.email ?: return
        val accessToken = withContext(Dispatchers.IO) {
            try {
                com.google.android.gms.auth.GoogleAuthUtil.getToken(
                    context,
                    android.accounts.Account(accountEmail, "com.google"),
                    "oauth2:${com.google.api.services.calendar.CalendarScopes.CALENDAR}"
                )
            } catch (e: Exception) {
                android.util.Log.e("SeedListViewModel", "アクセストークン取得エラー: ${e.message}")
                null
            }
        }
        
        if (accessToken.isNullOrBlank()) {
            android.util.Log.e("SeedListViewModel", "アクセストークンが取得できませんでした")
            return
        }
        
        // GoogleCalendarServiceでイベント削除
        val calendarService = com.example.seedstockkeeper6.service.GoogleCalendarService(context)
        val result = calendarService.deleteEventsForSeedPacket(accessToken, calendarId, packet)
        
        result.onSuccess {
            android.util.Log.d("SeedListViewModel", "Googleカレンダーイベント削除成功")
        }.onFailure { exception ->
            android.util.Log.e("SeedListViewModel", "Googleカレンダーイベント削除失敗: ${exception.message}", exception)
        }
    }
    
    suspend fun updateFinishedFlagInternal(documentId: String, isFinished: Boolean, context: android.content.Context?): Result<Unit> =
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
                        
                        // Googleカレンダー連携（「まいた」イベントの更新/削除）
                        context?.let { ctx ->
                            try {
                                syncPlantedEventWithGoogleCalendar(ctx, seed, documentId, isFinished, newSowingDate)
                            } catch (e: Exception) {
                                android.util.Log.e("SeedListViewModel", "Googleカレンダー連携エラー: ${e.message}", e)
                                // エラーが起きてもFirestore更新は成功しているので、ログだけ出力
                            }
                        }
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
     * 「まいた」イベントをGoogleカレンダーと同期
     * @param isFinished trueの場合は「まいた」イベントを更新/作成、falseの場合は削除
     */
    private suspend fun syncPlantedEventWithGoogleCalendar(
        context: android.content.Context,
        seed: SeedPacket,
        documentId: String,
        isFinished: Boolean,
        newSowingDate: String
    ) {
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid ?: return
            
            // 農園設定からcalendarIdを取得
            val db = Firebase.firestore
            val settingsDoc = db.collection("users").document(uid).collection("settings").document("general")
            val settingsSnapshot = settingsDoc.get().await()
            
            val calendarId = settingsSnapshot.getString("calendarId")
            if (calendarId.isNullOrBlank()) {
                android.util.Log.d("SeedListViewModel", "GoogleカレンダーIDが設定されていません。スキップします。")
                return
            }
            
            val farmName = settingsSnapshot.getString("farmName")
            
            // アクセストークンを取得
            val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
                ?: return // Google Sign-Inされていない場合はスキップ
            
            val accountEmail = account.email ?: return
            val accessToken = withContext(Dispatchers.IO) {
                try {
                    com.google.android.gms.auth.GoogleAuthUtil.getToken(
                        context,
                        android.accounts.Account(accountEmail, "com.google"),
                        "oauth2:${com.google.api.services.calendar.CalendarScopes.CALENDAR}"
                    )
                } catch (e: Exception) {
                    android.util.Log.e("SeedListViewModel", "アクセストークン取得エラー: ${e.message}")
                    null
                }
            }
            
            if (accessToken.isNullOrBlank()) {
                android.util.Log.e("SeedListViewModel", "アクセストークンが取得できませんでした")
                return
            }
            
            val calendarService = com.example.seedstockkeeper6.service.GoogleCalendarService(context)
            
            if (isFinished && newSowingDate.isNotEmpty()) {
                // まきおわりにした場合：「まいた」イベントを更新または作成
                val updatedPacket = seed.copy(
                    sowingDate = newSowingDate,
                    documentId = documentId
                )
                
                // updateEventsForSeedPacketを使用（既存のplantedEventIdがあれば更新、なければ作成）
                val result = calendarService.updateEventsForSeedPacket(accessToken, calendarId, updatedPacket, farmName)
                
                result.onSuccess { (sowingEventId, harvestEventId, plantedEventId) ->
                    // 取得したeventIdをFirestoreに保存（特にplantedEventId）
                    if (plantedEventId != null) {
                        val eventIdUpdates = mutableMapOf<String, Any?>()
                        eventIdUpdates["plantedEventId"] = plantedEventId
                        // 他のeventIdも更新（既に存在する場合）
                        sowingEventId?.let { eventIdUpdates["sowingEventId"] = it }
                        harvestEventId?.let { eventIdUpdates["harvestEventId"] = it }
                        
                        db.collection("seeds").document(documentId)
                            .update(eventIdUpdates)
                            .await()
                        
                        android.util.Log.d("SeedListViewModel", "「まいた」イベント更新成功: $plantedEventId")
                    } else if (seed.plantedEventId.isNotEmpty()) {
                        // 既存のイベントが削除された場合は空文字にクリア
                        db.collection("seeds").document(documentId)
                            .update("plantedEventId", "")
                            .await()
                    }
                }.onFailure { exception ->
                    android.util.Log.e("SeedListViewModel", "「まいた」イベント更新失敗: ${exception.message}", exception)
                }
            } else {
                // まきおわりを解除した場合：「まいた」イベントを削除
                if (seed.plantedEventId.isNotEmpty()) {
                    try {
                        // GoogleCalendarServiceの内部メソッドを使うため、直接APIを呼び出す
                        val credential = com.google.api.client.googleapis.auth.oauth2.GoogleCredential().setAccessToken(accessToken)
                        val transport = com.google.api.client.http.javanet.NetHttpTransport()
                        val jsonFactory = com.google.api.client.json.gson.GsonFactory.getDefaultInstance()
                        val service = com.google.api.services.calendar.Calendar.Builder(
                            transport,
                            jsonFactory,
                            credential
                        )
                            .setApplicationName("SeedStockKeeper")
                            .build()
                        
                        service.events().delete(calendarId, seed.plantedEventId).execute()
                        
                        // FirestoreからplantedEventIdをクリア
                        db.collection("seeds").document(documentId)
                            .update("plantedEventId", "")
                            .await()
                        
                        android.util.Log.d("SeedListViewModel", "「まいた」イベント削除成功: ${seed.plantedEventId}")
                    } catch (e: Exception) {
                        android.util.Log.e("SeedListViewModel", "「まいた」イベント削除失敗: ${e.message}", e)
                        // 404エラー（既に削除済み）は無視
                        val is404Error = e.message?.contains("404", ignoreCase = true) == true
                        if (!is404Error) {
                            throw e
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SeedListViewModel", "Googleカレンダー連携処理エラー: ${e.message}", e)
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
