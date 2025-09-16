package com.example.seedstockkeeper6.notification

import android.content.Context
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        const val MONTHLY_WORK_NAME = "monthly_notification"
        const val WEEKLY_WORK_NAME = "weekly_notification"
        
        // 通知時刻の設定
        private const val NOTIFICATION_HOUR = 8 // 朝8時
        private const val NOTIFICATION_MINUTE = 0
    }
    
    /**
     * 月次通知をスケジュール
     */
    fun scheduleMonthlyNotification() {
        // 既存の月次通知をキャンセル
        workManager.cancelUniqueWork(MONTHLY_WORK_NAME)
        
        // 月初（1日）の朝8時に通知を送信
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, NOTIFICATION_HOUR)
            set(Calendar.MINUTE, NOTIFICATION_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // 今月の1日が過ぎている場合は来月の1日に設定
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.MONTH, 1)
            }
        }
        
        val delay = calendar.timeInMillis - System.currentTimeMillis()
        
        val monthlyWorkRequest = OneTimeWorkRequestBuilder<MonthlyNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("notification")
            .addTag("monthly")
            .build()
        
        workManager.enqueueUniqueWork(
            MONTHLY_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            monthlyWorkRequest
        )
        
        android.util.Log.d("NotificationScheduler", "月次通知をスケジュール: ${calendar.time}")
    }
    
    /**
     * 週次通知をスケジュール
     */
    fun scheduleWeeklyNotification(selectedWeekday: String) {
        // 既存の週次通知をキャンセル
        workManager.cancelUniqueWork(WEEKLY_WORK_NAME)
        
        // 指定された曜日の朝8時に通知を送信
        val weekday = getWeekdayFromString(selectedWeekday)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, weekday)
            set(Calendar.HOUR_OF_DAY, NOTIFICATION_HOUR)
            set(Calendar.MINUTE, NOTIFICATION_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // 今週の指定曜日が過ぎている場合は来週の同じ曜日に設定
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        
        val delay = calendar.timeInMillis - System.currentTimeMillis()
        
        val weeklyWorkRequest = OneTimeWorkRequestBuilder<WeeklyNotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("notification")
            .addTag("weekly")
            .build()
        
        workManager.enqueueUniqueWork(
            WEEKLY_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            weeklyWorkRequest
        )
        
        android.util.Log.d("NotificationScheduler", "週次通知をスケジュール: ${calendar.time} (${selectedWeekday})")
    }
    
    /**
     * 通知をキャンセル
     */
    fun cancelNotifications() {
        workManager.cancelUniqueWork(MONTHLY_WORK_NAME)
        workManager.cancelUniqueWork(WEEKLY_WORK_NAME)
        android.util.Log.d("NotificationScheduler", "すべての通知をキャンセル")
    }
    
    /**
     * 月次通知のみをキャンセル
     */
    fun cancelMonthlyNotification() {
        workManager.cancelUniqueWork(MONTHLY_WORK_NAME)
        android.util.Log.d("NotificationScheduler", "月次通知をキャンセル")
    }
    
    /**
     * 週次通知のみをキャンセル
     */
    fun cancelWeeklyNotification() {
        workManager.cancelUniqueWork(WEEKLY_WORK_NAME)
        android.util.Log.d("NotificationScheduler", "週次通知をキャンセル")
    }
    
    /**
     * 通知設定に基づいてスケジュールを更新
     */
    fun updateNotificationSchedule(notificationFrequency: String, selectedWeekday: String = "月曜日") {
        when (notificationFrequency) {
            "月一回" -> {
                scheduleMonthlyNotification()
                cancelWeeklyNotification()
            }
            "週１回" -> {
                scheduleWeeklyNotification(selectedWeekday)
                cancelMonthlyNotification()
            }
            "なし" -> {
                cancelNotifications()
            }
        }
    }
    
    /**
     * 文字列から曜日の定数を取得
     */
    private fun getWeekdayFromString(weekday: String): Int {
        return when (weekday) {
            "日曜日" -> Calendar.SUNDAY
            "月曜日" -> Calendar.MONDAY
            "火曜日" -> Calendar.TUESDAY
            "水曜日" -> Calendar.WEDNESDAY
            "木曜日" -> Calendar.THURSDAY
            "金曜日" -> Calendar.FRIDAY
            "土曜日" -> Calendar.SATURDAY
            else -> Calendar.MONDAY // デフォルトは月曜日
        }
    }
    
    /**
     * 現在の通知スケジュール状態を取得
     */
    fun getNotificationStatus(): Map<String, Boolean> {
        val monthlyStatus = workManager.getWorkInfosForUniqueWork(MONTHLY_WORK_NAME).get().isNotEmpty()
        val weeklyStatus = workManager.getWorkInfosForUniqueWork(WEEKLY_WORK_NAME).get().isNotEmpty()
        
        return mapOf(
            "monthly" to monthlyStatus,
            "weekly" to weeklyStatus
        )
    }
}
