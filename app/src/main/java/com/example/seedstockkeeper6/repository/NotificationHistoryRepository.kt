package com.example.seedstockkeeper6.repository

import com.example.seedstockkeeper6.model.NotificationData
import com.example.seedstockkeeper6.service.NotificationHistoryService

/**
 * 通知履歴のデータアクセス層
 * NotificationHistoryServiceのラッパーとして機能
 */
class NotificationHistoryRepository {
    private val historyService = NotificationHistoryService()
    
    /**
     * ユーザーの通知データを取得
     */
    suspend fun getUserNotificationData(): List<NotificationData> {
        return historyService.getUserNotificationData()
    }
    
    /**
     * 通知を既読にマーク
     */
    suspend fun markNotificationAsRead(documentId: String): Boolean {
        return historyService.markNotificationAsRead(documentId)
    }
    
    /**
     * 通知データを削除
     */
    suspend fun deleteNotificationData(documentId: String): Boolean {
        return historyService.deleteNotificationData(documentId)
    }
}
