package com.example.seedstockkeeper6.notification

import android.content.Context
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.model.NotificationData
import com.example.seedstockkeeper6.service.GeminiNotificationService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationManager(
    private val context: Context,
    private val onRefreshUnreadCount: () -> Unit = {}
) {
    
    private val channelManager = NotificationChannelManager(context)
    private val contentBuilder = NotificationContentBuilder()
    private val contentExtractor = NotificationContentExtractor()
    private val sender = NotificationSender(context, contentBuilder, contentExtractor, onRefreshUnreadCount)
    private val geminiService = GeminiNotificationService()
    private val dataConverter = NotificationDataConverter()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val auth = FirebaseAuth.getInstance()
    
    companion object {
        const val CHANNEL_ID = NotificationChannelManager.CHANNEL_ID
        const val MONTHLY_NOTIFICATION_ID = NotificationSender.MONTHLY_NOTIFICATION_ID
        const val WEEKLY_NOTIFICATION_ID = NotificationSender.WEEKLY_NOTIFICATION_ID
    }
    
    /**
     * 通知権限が許可されているかチェック
     */
    fun hasNotificationPermission(): Boolean {
        return channelManager.hasNotificationPermission()
    }
    
    /**
     * 通知権限をリクエスト
     */
    fun requestNotificationPermission(activity: android.app.Activity) {
        channelManager.requestNotificationPermission(activity)
    }
    
    /**
     * 月1回のおすすめ通知を送信
     */
    fun sendMonthlyRecommendationNotification(
        title: String,
        seedsThisMonth: List<SeedPacket>,
        seedsEndingSoon: List<SeedPacket>,
        recommendedSeeds: List<SeedPacket>,
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int
    ) {
        sender.sendMonthlyRecommendationNotification(
            title, seedsThisMonth, seedsEndingSoon, recommendedSeeds,
            farmOwner, region, prefecture, month
        )
    }
    
    /**
     * 週1回のリマインダー通知を送信
     */
    fun sendWeeklyReminderNotification(title: String, seedsEndingSoon: List<SeedPacket>) {
        sender.sendWeeklyReminderNotification(title, seedsEndingSoon)
    }
    
    /**
     * Gemini AIで生成したコンテンツで月次通知を送信（新しいJSON形式）
     */
    fun sendMonthlyRecommendationNotificationWithContent(
        title: String,
        content: String,
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int,
        seedCount: Int,
        userId: String = ""
    ) {
        
        // テキスト内容をNotificationDataに変換
        val notificationData = dataConverter.convertTextToNotificationData(
            title = title,
            content = content,
            farmOwner = farmOwner,
            region = region,
            prefecture = prefecture,
            month = month,
            notificationType = "MONTHLY",
            userId = userId
        )
        
        
        // 新しいJSON形式で通知を送信
        sender.sendNotificationFromData(notificationData)
        
    }
    
    /**
     * Gemini AIで生成したコンテンツで週次通知を送信（新しいJSON形式）
     */
    fun sendWeeklyReminderNotificationWithContent(
        title: String,
        content: String,
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int,
        seedCount: Int,
        userId: String = ""
    ) {
        android.util.Log.d("NotificationManager", "sendWeeklyReminderNotificationWithContent開始")
        android.util.Log.d("NotificationManager", "タイトル: $title")
        android.util.Log.d("NotificationManager", "コンテンツ: $content")
        android.util.Log.d("NotificationManager", "農園主: $farmOwner, 地域: $region, 月: $month")
        
        // テキスト内容をNotificationDataに変換
        val notificationData = dataConverter.convertTextToNotificationData(
            title = title,
            content = content,
            farmOwner = farmOwner,
            region = region,
            prefecture = prefecture,
            month = month,
            notificationType = "WEEKLY",
            userId = userId
        )
        
        android.util.Log.d("NotificationManager", "NotificationData変換完了")
        android.util.Log.d("NotificationManager", "変換後のNotificationData: タイトル=${notificationData.title}, 今月まきどき=${notificationData.thisMonthSeeds.size}, 期限間近=${notificationData.endingSoonSeeds.size}, おすすめ=${notificationData.recommendedSeeds.size}")
        
        // 新しいJSON形式で通知を送信
        sender.sendNotificationFromData(notificationData)
    }
    
    /**
     * 通知をキャンセル
     */
    fun cancelNotification(notificationId: Int) {
        sender.cancelNotification(notificationId)
    }
    
    /**
     * すべての通知をキャンセル
     */
    fun cancelAllNotifications() {
        sender.cancelAllNotifications()
    }
    
    /**
     * テスト用月次通知を送信
     */
    fun sendTestMonthlyNotification() {
        coroutineScope.launch {
            try {
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                val title = geminiService.generateMonthlyNotificationTitle(
                    region = "暖地",
                    prefecture = "福岡県",
                    seedInfoUrl = "https://sakata-netshop.com/shop/pages/sowingcalendar.aspx",
                    userSeeds = emptyList(),
                    currentMonth = currentMonth,
                    farmOwner = "水戸黄門"
                )
                val content = geminiService.generateMonthlyNotificationContent(
                    region = "暖地",
                    prefecture = "福岡県",
                    seedInfoUrl = "https://sakata-netshop.com/shop/pages/sowingcalendar.aspx",
                    userSeeds = emptyList(),
                    currentMonth = currentMonth,
                    farmOwner = "水戸黄門"
                )
                
                sendMonthlyRecommendationNotificationWithContent(
                    title = title,
                    content = content,
                    farmOwner = "水戸黄門",
                    region = "暖地",
                    prefecture = "福岡県",
                    month = currentMonth,
                    seedCount = 0,
                    userId = auth.currentUser?.uid ?: "test_user"
                )
            } catch (e: Exception) {
            }
        }
    }
    
    /**
     * テスト用週次通知を送信
     */
    fun sendTestWeeklyNotification() {
        coroutineScope.launch {
            try {
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                val title = geminiService.generateWeeklyNotificationTitle(
                    userSeeds = emptyList(),
                    farmOwner = "水戸黄門"
                )
                val content = geminiService.generateWeeklyNotificationContent(
                    userSeeds = emptyList(),
                    farmOwner = "水戸黄門"
                )
                
                sendWeeklyReminderNotificationWithContent(
                    title = title,
                    content = content,
                    farmOwner = "水戸黄門",
                    region = "暖地",
                    prefecture = "福岡県",
                    month = currentMonth,
                    seedCount = 0,
                    userId = auth.currentUser?.uid ?: "test_user"
                )
            } catch (e: Exception) {
            }
        }
    }
}
