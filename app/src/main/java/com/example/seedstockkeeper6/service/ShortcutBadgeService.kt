package com.example.seedstockkeeper6.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.seedstockkeeper6.MainActivity
import com.example.seedstockkeeper6.R

class ShortcutBadgeService(private val context: Context) {
    
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    companion object {
        private const val CHANNEL_ID = "badge_channel"
        private const val NOTIFICATION_ID = 1 // バッジ用の固定通知ID
    }
    
    /**
     * アプリアイコンのバッジに未読通知数を設定
     */
    fun setBadgeCount(count: Int) {
        // Android 8.0 (Oreo) 以降でのみ公式にサポートされている方法
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel() // チャンネルがなければ作成
            if (count > 0) {
                // 件数付きの通知を作成してバッジを表示
                val notification = createBadgeNotification(count)
                notificationManager.notify(NOTIFICATION_ID, notification)
            } else {
                // バッジを消すために通知をキャンセル
                clearBadge()
            }
        } else {
            // Android 8.0未満では、ランチャー依存の非公式な方法しかないため、
            // ここでは何もしないか、setLegacyBadgeのような別の実装を試すことになります。
            // ただし、setLegacyBadgeも確実ではありません。
        }
    }
    
    /**
     * Android 8.0以降で通知チャンネルを作成
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // ユーザーに表示されない、バッジ表示専用のチャンネルを作成
            val name = "アプリアイコンバッジ"
            val descriptionText = "未読件数をアプリアイコンに表示します。"
            // IMPORTANCE_DEFAULT以上でないとバッジが表示されない場合がある
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // このチャンネルの通知でバッジを表示する設定
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * バッジ表示用の通知を作成
     */
    private fun createBadgeNotification(count: Int): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // この通知はユーザーに表示する必要がないため、最小限の設定にする
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tanesuke_white) // 通知用の小さなアイコン(必須)
            .setContentTitle("たねすけ")
            .setContentText("未読の通知が $count 件あります")
            .setNumber(count) // これがバッジの数字になる
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }
    
    /**
     * バッジをクリア
     */
    fun clearBadge() {
        // バッジを表示している通知をキャンセルすることで、バッジも消える
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * バッジがサポートされているかチェック
     * Android 8.0以降の公式サポートを基準とする
     */
    fun isBadgeSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
}
