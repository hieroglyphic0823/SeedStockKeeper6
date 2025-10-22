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
        notificationManager.notify(MONTHLY_NOTIFICATION_ID, notification)
        
        // 通知履歴を保存
        coroutineScope.launch {
            historyService.saveNotificationHistory(
                type = NotificationType.MONTHLY,
                title = "今月の種まき情報",
                content = content,
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
        notificationManager.notify(WEEKLY_NOTIFICATION_ID, notification)
        
        // 通知履歴を保存
        coroutineScope.launch {
            historyService.saveNotificationHistory(
                type = NotificationType.WEEKLY,
                title = "種まき期限のお知らせ",
                content = content,
                farmOwner = "",
                region = "",
                prefecture = "",
                seedCount = seedsEndingSoon.size,
                endingSoonSeeds = seedsEndingSoon.map { it.productName }
            )
        }
    }
    
    fun sendMonthlyRecommendationNotificationWithContent(
        title: String,
        content: String,
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int,
        seedCount: Int
    ) {
        // JSONブロックを除去した表示用コンテンツ
        val displayContent = contentBuilder.removeJsonCodeBlock(content)
        val summary = displayContent.lines().firstOrNull { it.isNotBlank() } ?: ""
        
        // 通知用の簡潔なコンテンツを作成
        val condensedContent = contentBuilder.buildCondensedContent(displayContent)
        
        // 通知履歴用の情報を抽出
        val extracted = contentExtractor.extractNamesForHistory(content)
        val details = contentExtractor.extractDetailsForHistory(content)
        val closingLine = contentExtractor.extractClosingLine(content)
        
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tanesuke_white)
            .setContentTitle(title)
            .setContentText(summary)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(condensedContent.toString())
                .setSummaryText("月次通知"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createPendingIntent())
            .setAutoCancel(true)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(MONTHLY_NOTIFICATION_ID, notification)
        
        // 通知履歴を保存（元の完全なコンテンツを保存）
        android.util.Log.d("NotificationManager", "保存する元の完全なコンテンツ:")
        android.util.Log.d("NotificationManager", "==========================================")
        android.util.Log.d("NotificationManager", content)
        android.util.Log.d("NotificationManager", "==========================================")
        android.util.Log.d("NotificationManager", "抽出されたclosingLine: '$closingLine'")
        
        coroutineScope.launch {
            historyService.saveNotificationHistory(
                type = NotificationType.MONTHLY,
                title = title,
                content = content, // 元の完全なコンテンツを保存
                summary = summary,
                farmOwner = farmOwner,
                region = region,
                prefecture = prefecture,
                month = month,
                seedCount = seedCount,
                thisMonthSeeds = extracted.first,
                endingSoonSeeds = extracted.second,
                recommendedSeeds = extracted.third,
                thisMonthDetails = details.first,
                endingSoonDetails = details.second,
                recommendedDetails = details.third,
                closingLine = closingLine
            )
        }
    }
    
    fun sendWeeklyReminderNotificationWithContent(
        title: String,
        content: String,
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int,
        seedCount: Int
    ) {
        // JSONブロックを除去した表示用コンテンツ
        val displayContent = contentBuilder.removeJsonCodeBlock(content)
        val summary = displayContent.lines().firstOrNull { it.isNotBlank() } ?: ""
        
        // 通知用の簡潔なコンテンツを作成
        val condensedContent = contentBuilder.buildCondensedContent(displayContent)
        
        // 通知履歴用の情報を抽出
        val extracted = contentExtractor.extractNamesForHistory(content)
        val details = contentExtractor.extractDetailsForHistory(content)
        val closingLine = contentExtractor.extractClosingLine(content)
        
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tanesuke_white)
            .setContentTitle(title)
            .setContentText(summary)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(condensedContent.toString())
                .setSummaryText("週次通知"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createPendingIntent())
            .setAutoCancel(true)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(WEEKLY_NOTIFICATION_ID, notification)
        
        // 通知履歴を保存（元の完全なコンテンツを保存）
        android.util.Log.d("NotificationManager", "保存する元の完全なコンテンツ:")
        android.util.Log.d("NotificationManager", "==========================================")
        android.util.Log.d("NotificationManager", content)
        android.util.Log.d("NotificationManager", "==========================================")
        android.util.Log.d("NotificationManager", "抽出されたclosingLine: '$closingLine'")
        
        coroutineScope.launch {
            historyService.saveNotificationHistory(
                type = NotificationType.WEEKLY,
                title = title,
                content = content, // 元の完全なコンテンツを保存
                summary = summary,
                farmOwner = farmOwner,
                region = region,
                prefecture = prefecture,
                month = month,
                seedCount = seedCount,
                thisMonthSeeds = extracted.first,
                endingSoonSeeds = extracted.second,
                recommendedSeeds = extracted.third,
                thisMonthDetails = details.first,
                endingSoonDetails = details.second,
                recommendedDetails = details.third,
                closingLine = closingLine
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
}

