package com.example.seedstockkeeper6.service

import com.example.seedstockkeeper6.model.NotificationHistory
import com.example.seedstockkeeper6.model.NotificationType
import com.example.seedstockkeeper6.model.NotificationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class NotificationHistoryService {
    
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * 通知履歴をFirebaseに保存
     */
    suspend fun saveNotificationHistory(
        type: NotificationType,
        title: String,
        summary: String = "", // 要点を追加
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int = 0,
        seedCount: Int = 0,
        thisMonthSeeds: List<String> = emptyList(),
        endingSoonSeeds: List<String> = emptyList(),
        recommendedSeeds: List<String> = emptyList(),
        thisMonthDetails: List<com.example.seedstockkeeper6.model.SeedDetail> = emptyList(),
        endingSoonDetails: List<com.example.seedstockkeeper6.model.SeedDetail> = emptyList(),
        recommendedDetails: List<com.example.seedstockkeeper6.model.SeedDetail> = emptyList(),
        closingLine: String = ""
    ): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return false
            }
            
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())
            
            val history = NotificationHistory(
                type = type,
                title = title,
                summary = summary, // 要点を追加
                sentAt = now,
                userId = currentUser.uid,
                farmOwner = farmOwner,
                region = region,
                prefecture = prefecture,
                month = month,
                seedCount = seedCount,
                thisMonthSeeds = thisMonthSeeds,
                endingSoonSeeds = endingSoonSeeds,
                recommendedSeeds = recommendedSeeds,
                thisMonthDetails = thisMonthDetails,
                endingSoonDetails = endingSoonDetails,
                recommendedDetails = recommendedDetails,
                closingLine = closingLine
            )
            
            
            val docRef = db.collection("notificationHistory")
                .add(history)
                .await()
            
            true
            
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * ユーザーの通知履歴を取得
     */
    suspend fun getUserNotificationHistory(limit: Int = 50): List<NotificationHistory> {
        return try {
            val currentUser = auth.currentUser
            
            if (currentUser == null) {
                return emptyList()
            }
            
            // 一時的にorderByを削除してインデックス不足を回避
            val snapshot = db.collection("notificationHistory")
                .whereEqualTo("userId", currentUser.uid)
                .limit(limit.toLong())
                .get()
                .await()
            
            
            // ドキュメントの生データをログ出力
            snapshot.documents.forEach { doc ->
            }
            
            val histories = snapshot.documents.mapNotNull { doc ->
                try {
                    val history = doc.toObject(NotificationHistory::class.java)
                    history?.copy(documentId = doc.id)
                } catch (e: Exception) {
                    null
                }
            }
            
            // クライアント側で日時順にソート
            val sortedHistories = histories.sortedByDescending { history ->
                try {
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    format.parse(history.sentAt)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
            
            sortedHistories.forEach { history ->
            }
            sortedHistories
            
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * JSON形式の通知データを保存
     */
    suspend fun saveNotificationData(notificationData: NotificationData): Boolean {
        return try {
            val currentUser = auth.currentUser
            
            if (currentUser == null) {
                return false
            }
            
            // 通知データをそのままFirebaseに保存
            val docRef = db.collection("notificationData")
                .add(notificationData)
                .await()
            
            true
            
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * ユーザーのJSON形式通知データを取得
     */
    suspend fun getUserNotificationData(limit: Int = 50): List<NotificationData> {
        return try {
            val currentUser = auth.currentUser
            
            if (currentUser == null) {
                return emptyList()
            }
            
            val snapshot = db.collection("notificationData")
                .whereEqualTo("userId", currentUser.uid)
                .limit(limit.toLong())
                .get()
                .await()
            
            
            val notificationDataList = snapshot.documents.mapNotNull { doc ->
                try {
                    val notificationData = doc.toObject(NotificationData::class.java)
                    if (notificationData != null) {
                        // FirestoreのドキュメントIDを設定
                        val updatedNotificationData = notificationData.copy(documentId = doc.id)
                        updatedNotificationData
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            
            // 日時順にソート
            val sortedData = notificationDataList.sortedByDescending { data ->
                try {
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    format.parse(data.sentAt)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
            
            sortedData
            
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 通知履歴を削除
     */
    suspend fun deleteNotificationHistory(documentId: String): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return false
            }
            
            // 自分の通知履歴のみ削除可能
            val doc = db.collection("notificationHistory")
                .document(documentId)
                .get()
                .await()
            
            val history = doc.toObject(NotificationHistory::class.java)
            if (history?.userId != currentUser.uid) {
                return false
            }
            
            db.collection("notificationHistory")
                .document(documentId)
                .delete()
                .await()
            
            true
            
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 通知データを削除
     */
    suspend fun deleteNotificationData(documentId: String): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return false
            }
            
            // 自分の通知データのみ削除可能
            val doc = db.collection("notificationData")
                .document(documentId)
                .get()
                .await()
            
            val notificationData = doc.toObject(NotificationData::class.java)
            if (notificationData?.userId != currentUser.uid) {
                return false
            }
            
            db.collection("notificationData")
                .document(documentId)
                .delete()
                .await()
            
            true
            
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 通知データの既読フラグを更新
     */
    suspend fun markNotificationAsRead(documentId: String): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return false
            }
            
            // 自分の通知データのみ更新可能
            val doc = db.collection("notificationData")
                .document(documentId)
                .get()
                .await()
            
            val notificationData = doc.toObject(NotificationData::class.java)
            if (notificationData?.userId != currentUser.uid) {
                return false
            }
            
            // 既読フラグを1に更新
            db.collection("notificationData")
                .document(documentId)
                .update("isRead", 1)
                .await()
            
            true
            
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 未読通知数を取得
     */
    suspend fun getUnreadNotificationCount(): Int {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return 0
            }
            
            val snapshot = db.collection("notificationData")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("isRead", 0)
                .get()
                .await()
            
            val unreadCount = snapshot.documents.size
            unreadCount
            
        } catch (e: Exception) {
            0
        }
    }
}
