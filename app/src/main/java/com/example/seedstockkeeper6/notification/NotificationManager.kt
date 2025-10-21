package com.example.seedstockkeeper6.notification

import android.content.Context
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.service.GeminiNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationManager(private val context: Context) {
    
    private val channelManager = NotificationChannelManager(context)
    private val contentBuilder = NotificationContentBuilder()
    private val contentExtractor = NotificationContentExtractor()
    private val sender = NotificationSender(context, contentBuilder, contentExtractor)
    private val geminiService = GeminiNotificationService()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
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
        seedsThisMonth: List<SeedPacket>,
        seedsEndingSoon: List<SeedPacket>,
        recommendedSeeds: List<SeedPacket>,
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int
    ) {
        sender.sendMonthlyRecommendationNotification(
            seedsThisMonth, seedsEndingSoon, recommendedSeeds,
            farmOwner, region, prefecture, month
        )
    }
    
    /**
     * 週1回のリマインダー通知を送信
     */
    fun sendWeeklyReminderNotification(seedsEndingSoon: List<SeedPacket>) {
        sender.sendWeeklyReminderNotification(seedsEndingSoon)
    }
    
    /**
     * Gemini AIで生成したコンテンツで月次通知を送信
     */
    fun sendMonthlyRecommendationNotificationWithContent(
        title: String,
        content: String,
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int,
        seedCount: Int
    ) {
        sender.sendMonthlyRecommendationNotificationWithContent(
            title, content, farmOwner, region, prefecture, month, seedCount
        )
    }
    
    /**
     * Gemini AIで生成したコンテンツで週次通知を送信
     */
    fun sendWeeklyReminderNotificationWithContent(
        title: String,
        content: String,
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int,
        seedCount: Int
    ) {
        sender.sendWeeklyReminderNotificationWithContent(
            title, content, farmOwner, region, prefecture, month, seedCount
        )
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
                
                android.util.Log.d("NotificationManager", "テスト月次通知送信開始")
                sendMonthlyRecommendationNotificationWithContent(
                    title = title,
                    content = content,
                    farmOwner = "水戸黄門",
                    region = "暖地",
                    prefecture = "福岡県",
                    month = currentMonth,
                    seedCount = 0
                )
                android.util.Log.d("NotificationManager", "テスト月次通知送信完了")
            } catch (e: Exception) {
                android.util.Log.e("NotificationManager", "テスト月次通知送信エラー", e)
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
                
                android.util.Log.d("NotificationManager", "テスト週次通知送信開始")
                sendWeeklyReminderNotificationWithContent(
                    title = title,
                    content = content,
                    farmOwner = "水戸黄門",
                    region = "暖地",
                    prefecture = "福岡県",
                    month = currentMonth,
                    seedCount = 0
                )
                android.util.Log.d("NotificationManager", "テスト週次通知送信完了")
            } catch (e: Exception) {
                android.util.Log.e("NotificationManager", "テスト週次通知送信エラー", e)
            }
        }
    }
}