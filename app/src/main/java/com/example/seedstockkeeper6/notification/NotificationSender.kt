package com.example.seedstockkeeper6.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.seedstockkeeper6.MainActivity
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.NotificationType
import com.example.seedstockkeeper6.model.SeedDetail
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.model.NotificationData
import com.example.seedstockkeeper6.service.NotificationHistoryService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationSender(
    private val context: Context,
    private val contentBuilder: NotificationContentBuilder,
    private val contentExtractor: NotificationContentExtractor
) {
    
    private val historyService = NotificationHistoryService()
    private val contentGenerator = NotificationContentGenerator()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        const val MONTHLY_NOTIFICATION_ID = 1001
        const val WEEKLY_NOTIFICATION_ID = 1002
    }
    
    fun sendMonthlyRecommendationNotification(
        seedsThisMonth: List<SeedPacket>,
        seedsEndingSoon: List<SeedPacket>,
        recommendedSeeds: List<SeedPacket>,
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int
    ) {
        val content = contentBuilder.buildMonthlyNotificationContent(
            seedsThisMonth, seedsEndingSoon, recommendedSeeds
        )
        
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tanesuke_white)
            .setContentTitle("今月の種まき情報")
            .setContentText("種まきのタイミングをお知らせします")
            .setStyle(contentBuilder.createInboxStyle(content, "今月の種まき情報"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createPendingIntent())
            .setAutoCancel(true)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(MONTHLY_NOTIFICATION_ID, notification)
        } else {
            android.util.Log.w("NotificationSender", "通知が無効になっています")
        }
        
        // 通知履歴を保存
        coroutineScope.launch {
            historyService.saveNotificationHistory(
                type = NotificationType.MONTHLY,
                title = "今月の種まき情報",
                summary = content.lines().firstOrNull { it.isNotBlank() } ?: "",
                farmOwner = farmOwner,
                region = region,
                prefecture = prefecture,
                month = month,
                seedCount = seedsThisMonth.size + seedsEndingSoon.size + recommendedSeeds.size,
                thisMonthSeeds = seedsThisMonth.map { it.productName },
                endingSoonSeeds = seedsEndingSoon.map { it.productName },
                recommendedSeeds = recommendedSeeds.map { it.productName }
            )
        }
    }
    
    fun sendWeeklyReminderNotification(seedsEndingSoon: List<SeedPacket>) {
        val content = contentBuilder.buildWeeklyNotificationContent(seedsEndingSoon)
        
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tanesuke_white)
            .setContentTitle("種まき期限のお知らせ")
            .setContentText("まき時終了間近の種があります")
            .setStyle(contentBuilder.createInboxStyle(content, "種まき期限のお知らせ"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createPendingIntent())
            .setAutoCancel(true)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(WEEKLY_NOTIFICATION_ID, notification)
        } else {
            android.util.Log.w("NotificationSender", "通知が無効になっています")
        }
        
        // 通知履歴を保存
        coroutineScope.launch {
            historyService.saveNotificationHistory(
                type = NotificationType.WEEKLY,
                title = "種まき期限のお知らせ",
                summary = content.lines().firstOrNull { it.isNotBlank() } ?: "",
                farmOwner = "",
                region = "",
                prefecture = "",
                seedCount = seedsEndingSoon.size,
                endingSoonSeeds = seedsEndingSoon.map { it.productName }
            )
        }
    }
    
    
    
    private fun createPendingIntent(): PendingIntent {
        // Deep Link で通知履歴画面を直接開く
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://seedstockkeeper6/notification_history"), context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    fun cancelNotification(notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
    }
    
    fun cancelAllNotifications() {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancelAll()
    }
    
    /**
     * JSON形式の通知データから通知を送信
     */
    fun sendNotificationFromData(notificationData: NotificationData) {
        android.util.Log.d("NotificationSender", "sendNotificationFromData開始")
        android.util.Log.d("NotificationSender", "NotificationData - id: ${notificationData.id}, userId: ${notificationData.userId}, title: ${notificationData.title}")
        
        // 通知文を生成
        val content = contentGenerator.generateContent(notificationData)
        val condensedContent = contentGenerator.generateCondensedContent(notificationData)
        val summary = contentGenerator.generateSummary(notificationData)
        
        android.util.Log.d("NotificationSender", "通知文生成完了 - summary: $summary")
        
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tanesuke_white)
            .setContentTitle(notificationData.title)
            .setContentText(summary)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(condensedContent)
                .setSummaryText(when (notificationData.notificationType) {
                    "MONTHLY" -> "月次通知"
                    "WEEKLY" -> "週次通知"
                    "CUSTOM" -> "カスタム通知"
                    else -> "通知"
                }))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createPendingIntent())
            .setAutoCancel(true)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        if (notificationManager.areNotificationsEnabled()) {
            val notificationId = when (notificationData.notificationType) {
                "MONTHLY" -> MONTHLY_NOTIFICATION_ID
                "WEEKLY" -> WEEKLY_NOTIFICATION_ID
                else -> MONTHLY_NOTIFICATION_ID
            }
            notificationManager.notify(notificationId, notification)
            android.util.Log.d("NotificationSender", "通知表示完了 - notificationId: $notificationId")
        } else {
            android.util.Log.w("NotificationSender", "通知が無効になっています")
        }
        
        // JSON形式の通知データをFirebaseに保存
        coroutineScope.launch {
            android.util.Log.d("NotificationSender", "Firebase保存開始")
            val success = historyService.saveNotificationData(notificationData)
            android.util.Log.d("NotificationSender", "JSON通知データ保存結果: $success")
            if (!success) {
                android.util.Log.e("NotificationSender", "Firebase保存に失敗しました")
            }
        }
        
        android.util.Log.d("NotificationSender", "sendNotificationFromData完了")
    }
}

