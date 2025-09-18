package com.example.seedstockkeeper6.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class NotificationHistory(
    val id: String = UUID.randomUUID().toString(),
    val type: NotificationType = NotificationType.MONTHLY,
    val title: String = "",
    val content: String = "",
    val sentAt: String = "", // ISO形式の日時文字列
    val userId: String = "", // 送信先ユーザーのUID
    val farmOwner: String = "",
    val region: String = "",
    val prefecture: String = "",
    val month: Int = 0, // 月次通知の場合の対象月
    val seedCount: Int = 0, // 対象となった種の数
    val documentId: String? = null // FirestoreのドキュメントID
)

@Serializable
enum class NotificationType(val displayName: String) {
    MONTHLY("月次通知"),
    WEEKLY("週次通知"),
    CUSTOM("カスタム通知")
}
