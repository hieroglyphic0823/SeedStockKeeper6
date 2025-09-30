package com.example.seedstockkeeper6.model

import com.google.firebase.Timestamp

/**
 * たねすけさんからのメッセージデータクラス
 */
data class SukesanMessage(
    val id: String = "",
    val userId: String = "",
    val message: String = "",
    val messageType: MessageType = MessageType.DAILY,
    val date: String = "", // YYYY-MM-DD形式
    val createdAt: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)

/**
 * メッセージの種類
 */
enum class MessageType {
    DAILY,      // 日次メッセージ
    URGENT,     // 緊急メッセージ（まき時終了間近など）
    WELCOME,    // ウェルカムメッセージ
    REMINDER    // リマインダーメッセージ
}
