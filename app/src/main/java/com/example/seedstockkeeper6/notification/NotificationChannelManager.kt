package com.example.seedstockkeeper6.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationChannelManager(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "seed_notifications"
        
        // 通知チャンネルの設定
        private const val CHANNEL_NAME = "種まき通知"
        private const val CHANNEL_DESCRIPTION = "種まきのタイミングをお知らせします"
        
        // 通知権限
        private const val POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS"
    }
    
    init {
        createNotificationChannel()
    }
    
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                POST_NOTIFICATIONS_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 12以前は権限不要
        }
    }
    
    fun requestNotificationPermission(activity: android.app.Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(POST_NOTIFICATIONS_PERMISSION),
                1001
            )
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}








