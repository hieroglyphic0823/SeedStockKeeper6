package com.example.seedstockkeeper6.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.seedstockkeeper6.MainActivity
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.SeedPacket
import java.util.Calendar

class NotificationManager(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "seed_notifications"
        const val MONTHLY_NOTIFICATION_ID = 1001
        const val WEEKLY_NOTIFICATION_ID = 1002
        
        // 通知チャンネルの設定
        private const val CHANNEL_NAME = "種まき通知"
        private const val CHANNEL_DESCRIPTION = "種まきのタイミングをお知らせします"
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 月1回のおすすめ通知を送信
     */
    fun sendMonthlyRecommendationNotification(
        seedsToSowThisMonth: List<SeedPacket>,
        seasonalRecommendations: List<String> = emptyList(),
        seedsEndingThisMonth: List<SeedPacket> = emptyList()
    ) {
        val title = "今月の種まきおすすめ"
        val content = buildMonthlyNotificationContent(seedsToSowThisMonth, seasonalRecommendations, seedsEndingThisMonth)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(MONTHLY_NOTIFICATION_ID, notification)
        }
    }
    
    /**
     * 週1回のリマインダー通知を送信
     */
    fun sendWeeklyReminderNotification(seedsEndingSoon: List<SeedPacket>) {
        val title = "種まきタイミングリマインダー"
        val content = buildWeeklyNotificationContent(seedsEndingSoon)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(WEEKLY_NOTIFICATION_ID, notification)
        }
    }
    
    /**
     * GeminiAPI生成の月次通知を送信
     */
    fun sendMonthlyRecommendationNotificationWithContent(content: String) {
        val title = "今月の種まきおすすめ"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(MONTHLY_NOTIFICATION_ID, notification)
        }
    }
    
    /**
     * GeminiAPI生成の週次通知を送信
     */
    fun sendWeeklyReminderNotificationWithContent(content: String) {
        val title = "種まきタイミングリマインダー"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(WEEKLY_NOTIFICATION_ID, notification)
        }
    }
    
    private fun buildMonthlyNotificationContent(
        seedsToSowThisMonth: List<SeedPacket>,
        seasonalRecommendations: List<String>,
        seedsEndingThisMonth: List<SeedPacket>
    ): String {
        val content = StringBuilder()
        
        // 今月まき時の種リスト
        if (seedsToSowThisMonth.isNotEmpty()) {
            content.append("🌱 今月まき時の種:\n")
            seedsToSowThisMonth.take(5).forEach { seed ->
                content.append("• ${seed.productName} (${seed.variety})\n")
            }
            if (seedsToSowThisMonth.size > 5) {
                content.append("他 ${seedsToSowThisMonth.size - 5} 種類\n")
            }
            content.append("\n")
        }
        
        // 季節のおすすめ品種
        if (seasonalRecommendations.isNotEmpty()) {
            content.append("🌟 季節のおすすめ:\n")
            seasonalRecommendations.take(3).forEach { recommendation ->
                content.append("• $recommendation\n")
            }
            content.append("\n")
        }
        
        // まき時が今月で終わる種への注意
        if (seedsEndingThisMonth.isNotEmpty()) {
            content.append("⚠️ まき時終了間近:\n")
            seedsEndingThisMonth.take(3).forEach { seed ->
                content.append("• ${seed.productName} (${seed.variety})\n")
            }
            if (seedsEndingThisMonth.size > 3) {
                content.append("他 ${seedsEndingThisMonth.size - 3} 種類\n")
            }
        }
        
        return content.toString().trim()
    }
    
    private fun buildWeeklyNotificationContent(seedsEndingSoon: List<SeedPacket>): String {
        val content = StringBuilder()
        
        if (seedsEndingSoon.isNotEmpty()) {
            content.append("⏰ まき時終了の2週間前の種があります:\n\n")
            seedsEndingSoon.take(5).forEach { seed ->
                content.append("• ${seed.productName} (${seed.variety})\n")
                content.append("  土づくりすれば間に合います！\n\n")
            }
            if (seedsEndingSoon.size > 5) {
                content.append("他 ${seedsEndingSoon.size - 5} 種類\n")
            }
        } else {
            content.append("今週は特に注意が必要な種はありません。\n")
            content.append("計画的に種まきを進めましょう！")
        }
        
        return content.toString().trim()
    }
    
    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * 通知をキャンセル
     */
    fun cancelNotification(notificationId: Int) {
        with(NotificationManagerCompat.from(context)) {
            cancel(notificationId)
        }
    }
    
    /**
     * すべての通知をキャンセル
     */
    fun cancelAllNotifications() {
        with(NotificationManagerCompat.from(context)) {
            cancelAll()
        }
    }
    
    /**
     * テスト用の月次通知を送信
     */
    fun sendTestMonthlyNotification() {
        val testSeeds = listOf(
            SeedPacket(
                id = "test1",
                productName = "恋むすめ",
                variety = "ニンジン",
                family = "せり科",
                expirationYear = 2026,
                expirationMonth = 10,
                germinationRate = "85",
                calendar = emptyList(),
                companionPlants = emptyList(),
                imageUrls = emptyList()
            ),
            SeedPacket(
                id = "test2",
                productName = "サラダミックス",
                variety = "レタス",
                family = "きく科",
                expirationYear = 2026,
                expirationMonth = 12,
                germinationRate = "90",
                calendar = emptyList(),
                companionPlants = emptyList(),
                imageUrls = emptyList()
            )
        )
        
        val seasonalRecommendations = listOf(
            "春野菜の種まきシーズンです",
            "トマト、ナス、ピーマンの準備を始めましょう",
            "レタス、キャベツの種まきが最適です"
        )
        
        val seedsEndingSoon = listOf(
            SeedPacket(
                id = "test3",
                productName = "春菊",
                variety = "中葉春菊",
                family = "きく科",
                expirationYear = 2026,
                expirationMonth = 3,
                germinationRate = "80",
                calendar = emptyList(),
                companionPlants = emptyList(),
                imageUrls = emptyList()
            )
        )
        
        sendMonthlyRecommendationNotification(
            seedsToSowThisMonth = testSeeds,
            seasonalRecommendations = seasonalRecommendations,
            seedsEndingThisMonth = seedsEndingSoon
        )
    }
    
    /**
     * テスト用の週次通知を送信
     */
    fun sendTestWeeklyNotification() {
        val testSeeds = listOf(
            SeedPacket(
                id = "test1",
                productName = "恋むすめ",
                variety = "ニンジン",
                family = "せり科",
                expirationYear = 2026,
                expirationMonth = 10,
                germinationRate = "85",
                calendar = emptyList(),
                companionPlants = emptyList(),
                imageUrls = emptyList()
            ),
            SeedPacket(
                id = "test2",
                productName = "大根",
                variety = "青首大根",
                family = "あぶらな科",
                expirationYear = 2026,
                expirationMonth = 11,
                germinationRate = "88",
                calendar = emptyList(),
                companionPlants = emptyList(),
                imageUrls = emptyList()
            )
        )
        
        sendWeeklyReminderNotification(testSeeds)
    }
}
