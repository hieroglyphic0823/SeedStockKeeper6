package com.example.seedstockkeeper6.service

import android.util.Log
import com.example.seedstockkeeper6.model.NotificationHistory
import com.example.seedstockkeeper6.model.NotificationType
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
        content: String,
        summary: String = "", // 要点を追加
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int = 0,
        seedCount: Int = 0
    ): Boolean {
        return try {
            val currentUser = auth.currentUser
            Log.d("NotificationHistoryService", "通知履歴保存開始 - currentUser: ${currentUser?.uid}")
            if (currentUser == null) {
                Log.w("NotificationHistoryService", "ユーザーが認証されていません")
                return false
            }
            
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())
            
            val history = NotificationHistory(
                type = type,
                title = title,
                content = content,
                summary = summary, // 要点を追加
                sentAt = now,
                userId = currentUser.uid,
                farmOwner = farmOwner,
                region = region,
                prefecture = prefecture,
                month = month,
                seedCount = seedCount
            )
            
            Log.d("NotificationHistoryService", "保存する通知履歴: $history")
            
            val docRef = db.collection("notificationHistory")
                .add(history)
                .await()
            
            Log.d("NotificationHistoryService", "通知履歴を保存しました: ${docRef.id}")
            true
            
        } catch (e: Exception) {
            Log.e("NotificationHistoryService", "通知履歴の保存に失敗", e)
            false
        }
    }
    
    /**
     * ユーザーの通知履歴を取得
     */
    suspend fun getUserNotificationHistory(limit: Int = 50): List<NotificationHistory> {
        return try {
            val currentUser = auth.currentUser
            Log.d("NotificationHistoryService", "通知履歴取得開始 - currentUser: ${currentUser?.uid}")
            Log.d("NotificationHistoryService", "認証状態: ${auth.currentUser != null}")
            Log.d("NotificationHistoryService", "ユーザー情報: ${currentUser?.email}, ${currentUser?.displayName}")
            
            if (currentUser == null) {
                Log.w("NotificationHistoryService", "ユーザーが認証されていません")
                return emptyList()
            }
            
            Log.d("NotificationHistoryService", "Firebaseクエリ実行開始 - userId: ${currentUser.uid}")
            // 一時的にorderByを削除してインデックス不足を回避
            val snapshot = db.collection("notificationHistory")
                .whereEqualTo("userId", currentUser.uid)
                .limit(limit.toLong())
                .get()
                .await()
            
            Log.d("NotificationHistoryService", "Firebaseクエリ完了 - 取得ドキュメント数: ${snapshot.documents.size}")
            
            // ドキュメントの生データをログ出力
            snapshot.documents.forEach { doc ->
                Log.d("NotificationHistoryService", "ドキュメント生データ: ${doc.id} = ${doc.data}")
            }
            
            val histories = snapshot.documents.mapNotNull { doc ->
                try {
                    Log.d("NotificationHistoryService", "ドキュメント解析中: ${doc.id}")
                    val history = doc.toObject(NotificationHistory::class.java)
                    Log.d("NotificationHistoryService", "解析結果: $history")
                    history?.copy(documentId = doc.id)
                } catch (e: Exception) {
                    Log.w("NotificationHistoryService", "通知履歴の解析に失敗: ${doc.id}", e)
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
            
            Log.d("NotificationHistoryService", "通知履歴を取得しました: ${sortedHistories.size}件")
            sortedHistories.forEach { history ->
                Log.d("NotificationHistoryService", "履歴詳細: id=${history.id}, title=${history.title}, sentAt=${history.sentAt}")
            }
            sortedHistories
            
        } catch (e: Exception) {
            Log.e("NotificationHistoryService", "通知履歴の取得に失敗", e)
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
                Log.w("NotificationHistoryService", "ユーザーが認証されていません")
                return false
            }
            
            // 自分の通知履歴のみ削除可能
            val doc = db.collection("notificationHistory")
                .document(documentId)
                .get()
                .await()
            
            val history = doc.toObject(NotificationHistory::class.java)
            if (history?.userId != currentUser.uid) {
                Log.w("NotificationHistoryService", "他のユーザーの通知履歴は削除できません")
                return false
            }
            
            db.collection("notificationHistory")
                .document(documentId)
                .delete()
                .await()
            
            Log.d("NotificationHistoryService", "通知履歴を削除しました: $documentId")
            true
            
        } catch (e: Exception) {
            Log.e("NotificationHistoryService", "通知履歴の削除に失敗", e)
            false
        }
    }
}
