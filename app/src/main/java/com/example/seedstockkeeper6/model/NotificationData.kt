package com.example.seedstockkeeper6.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationData(
    val id: String = "",
    val notificationType: String = "MONTHLY",
    val title: String = "",
    val summary: String = "",
    val farmOwner: String = "",
    val region: String = "",
    val prefecture: String = "",
    val month: Int = 0,
    val thisMonthSeeds: List<SeedInfo> = emptyList(),
    val endingSoonSeeds: List<SeedInfo> = emptyList(),
    val recommendedSeeds: List<SeedInfo> = emptyList(),
    val closingLine: String = "",
    val signature: String = "",
    val sentAt: String = "",
    val userId: String = "",
    val seedCount: Int = 0,
    val priority: String = "DEFAULT",
    val channelId: String = "seed_notifications",
    val documentId: String? = null, // FirestoreのドキュメントID
    val isRead: Int = 0 // 既読フラグ（0: 未読, 1: 既読）
)

@Serializable
data class SeedInfo(
    val name: String = "",
    val variety: String = "",
    val description: String = "",
    val expirationYear: Int = 0,
    val expirationMonth: Int = 0
)
