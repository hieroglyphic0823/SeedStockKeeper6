package com.example.seedstockkeeper6.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.seedstockkeeper6.MainActivity
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.model.NotificationType
import com.example.seedstockkeeper6.service.NotificationHistoryService
import com.example.seedstockkeeper6.service.GeminiNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationManager(private val context: Context) {
    
    private val historyService = NotificationHistoryService()
    private val geminiService = GeminiNotificationService()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        const val CHANNEL_ID = "seed_notifications"
        const val MONTHLY_NOTIFICATION_ID = 1001
        const val WEEKLY_NOTIFICATION_ID = 1002
        
        // 通知チャンネルの設定
        private const val CHANNEL_NAME = "種まき通知"
        private const val CHANNEL_DESCRIPTION = "種まきのタイミングをお知らせします"
        
        // 通知権限
        private const val POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS"
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * 通知権限が許可されているかチェック
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                POST_NOTIFICATIONS_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12以下では権限は不要
            true
        }
    }
    
    /**
     * 通知権限をリクエスト（Activityから呼び出し）
     */
    fun requestNotificationPermission(activity: android.app.Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(POST_NOTIFICATIONS_PERMISSION),
                1001 // リクエストコード
            )
        }
    }
    
    private fun createNotificationChannel() {
        android.util.Log.d("NotificationManager", "通知チャンネル作成開始")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
                setShowBadge(true) // 通知ドット（バッジ）を有効化
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            android.util.Log.d("NotificationManager", "通知チャンネル作成完了 - ID: $CHANNEL_ID, 名前: $CHANNEL_NAME")
        } else {
            android.util.Log.d("NotificationManager", "Android O未満のため通知チャンネル作成をスキップ")
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
        if (!hasNotificationPermission()) {
            android.util.Log.w("NotificationManager", "通知権限が許可されていません")
            return
        }
        
        val title = "今月の種まきおすすめ"
        val content = buildMonthlyNotificationContent(seedsToSowThisMonth, seasonalRecommendations, seedsEndingThisMonth)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_tanesuke_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL) // 通知ドット（バッジ）を設定
            .setNumber(1) // バッジに表示する数値
            .build()
        
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(MONTHLY_NOTIFICATION_ID, notification)
            }
        } catch (e: SecurityException) {
            android.util.Log.e("NotificationManager", "通知送信に失敗: 権限が不足しています", e)
        }
    }
    
    /**
     * 週1回のリマインダー通知を送信
     */
    fun sendWeeklyReminderNotification(seedsEndingSoon: List<SeedPacket>) {
        if (!hasNotificationPermission()) {
            android.util.Log.w("NotificationManager", "通知権限が許可されていません")
            return
        }
        
        // 動的タイトル生成
        coroutineScope.launch {
            try {
                val title = geminiService.generateWeeklyNotificationTitle(seedsEndingSoon, "お銀")
                val content = buildWeeklyNotificationContent(seedsEndingSoon)
        
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_tanesuke_foreground)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(createPendingIntent())
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL) // 通知ドット（バッジ）を設定
                    .setNumber(1) // バッジに表示する数値
                    .build()
                
                try {
                    with(NotificationManagerCompat.from(context)) {
                        notify(WEEKLY_NOTIFICATION_ID, notification)
                    }
                } catch (e: SecurityException) {
                    android.util.Log.e("NotificationManager", "通知送信に失敗: 権限が不足しています", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationManager", "週次通知タイトル生成に失敗", e)
                // フォールバック: 固定タイトルで通知送信
                val fallbackTitle = "種まきタイミングリマインダー"
                val content = buildWeeklyNotificationContent(seedsEndingSoon)
                
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_tanesuke_foreground)
                    .setContentTitle(fallbackTitle)
                    .setContentText(content)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(createPendingIntent())
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setNumber(1)
                    .build()
                
                try {
                    with(NotificationManagerCompat.from(context)) {
                        notify(WEEKLY_NOTIFICATION_ID, notification)
                    }
                } catch (e: SecurityException) {
                    android.util.Log.e("NotificationManager", "通知送信に失敗: 権限が不足しています", e)
                }
            }
        }
    }
    
    /**
     * GeminiAPI生成の月次通知を送信
     */
    fun sendMonthlyRecommendationNotificationWithContent(
        content: String,
        farmOwner: String = "",
        region: String = "",
        prefecture: String = "",
        farmAddress: String = "",
        month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
        seedCount: Int = 0
    ) {
        if (!hasNotificationPermission()) {
            android.util.Log.w("NotificationManager", "通知権限が許可されていません")
            return
        }
        
        // 和風月名でタイトルを生成（非同期）
        coroutineScope.launch {
            try {
                val title = geminiService.generateMonthlyNotificationTitle(
                    region = region,
                    prefecture = prefecture,
                    seedInfoUrl = "https://example.com/seed-info",
                    userSeeds = emptyList(),
                    currentMonth = month,
                    farmOwner = farmOwner,
                    farmAddress = farmAddress
                )
                // 通知表示用に「文頭＋種名のみの3セクション」に整形
                val displayContent = buildCondensedContent(content)
                val summary = displayContent.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: ""
                
                // 通知スタイルを決定（内容に応じて）
                val notificationStyle = if (displayContent.contains("•") && displayContent.split("•").size > 3) {
                    // リスト形式の内容の場合はInboxStyleを使用
                    createInboxStyle(displayContent, summary)
                } else {
                    // 通常のテキストの場合はBigTextStyleを使用
                    NotificationCompat.BigTextStyle()
                        .bigText(displayContent)
                        .setSummaryText("詳細を表示するには通知を展開してください")
                }
                
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_tanesuke_foreground)
                    .setContentTitle(title)
                    .setContentText(summary) // 文頭を表示
                    .setStyle(notificationStyle)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(createPendingIntent())
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL) // 通知ドット（バッジ）を設定
                    .setNumber(1) // バッジに表示する数値
                    .setCategory(NotificationCompat.CATEGORY_REMINDER) // リマインダーカテゴリ
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // ロック画面で表示
                    .build()
                
                try {
                    with(NotificationManagerCompat.from(context)) {
                        notify(MONTHLY_NOTIFICATION_ID, notification)
                    }
                } catch (e: SecurityException) {
                    android.util.Log.e("NotificationManager", "通知送信に失敗: 権限が不足しています", e)
                    return@launch
                }
                
                // 通知履歴を保存（要約は文頭の文）
                historyService.saveNotificationHistory(
                    type = NotificationType.MONTHLY,
                    title = title,
                    content = displayContent,
                    summary = summary,
                    farmOwner = farmOwner,
                    region = region,
                    prefecture = prefecture,
                    month = month,
                    seedCount = seedCount
                )
            } catch (e: Exception) {
                // フォールバック: デフォルトタイトルで通知
                val fallbackTitle = "今月の種まきおすすめ"
                
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_tanesuke_foreground)
                    .setContentTitle(fallbackTitle)
                    .setContentText(content)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(createPendingIntent())
                    .build()
                
                try {
                    with(NotificationManagerCompat.from(context)) {
                        notify(MONTHLY_NOTIFICATION_ID, notification)
                    }
                } catch (e: SecurityException) {
                    android.util.Log.e("NotificationManager", "通知送信に失敗: 権限が不足しています", e)
                    return@launch
                }
                
                // 通知履歴を保存
                historyService.saveNotificationHistory(
                    type = NotificationType.MONTHLY,
                    title = fallbackTitle,
                    content = content,
                    farmOwner = farmOwner,
                    region = region,
                    prefecture = prefecture,
                    month = month,
                    seedCount = seedCount
                )
            }
        }
    }
    
    /**
     * GeminiAPI生成の週次通知を送信
     */
    fun sendWeeklyReminderNotificationWithContent(
        content: String,
        farmOwner: String = "",
        region: String = "",
        prefecture: String = "",
        seedCount: Int = 0
    ) {
        android.util.Log.d("NotificationManager", "週次通知送信開始 - 権限チェック")
        if (!hasNotificationPermission()) {
            android.util.Log.w("NotificationManager", "通知権限が許可されていません")
            return
        }
        android.util.Log.d("NotificationManager", "通知権限OK - 通知作成開始")
        
        // 要点を生成
        coroutineScope.launch {
            try {
                val title = geminiService.generateWeeklyNotificationTitle(emptyList(), farmOwner)
                val displayContent = buildCondensedContent(content)
                val summary = displayContent.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: ""
                
                android.util.Log.d("NotificationManager", "通知タイトル: $title")
                android.util.Log.d("NotificationManager", "通知内容（最初の100文字）: ${content.take(100)}...")
                
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_tanesuke_foreground)
                    .setContentTitle(title)
                    .setContentText(summary) // 文頭を表示
                    .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(content) // 詳細は展開時に表示
                        .setSummaryText("詳細を表示するには通知を展開してください"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(createPendingIntent())
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL) // 通知ドット（バッジ）を設定
                    .setNumber(1) // バッジに表示する数値
                    .setCategory(NotificationCompat.CATEGORY_REMINDER) // リマインダーカテゴリ
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // ロック画面で表示
                    .build()
                
                android.util.Log.d("NotificationManager", "通知オブジェクト作成完了 - ID: $WEEKLY_NOTIFICATION_ID")
                
                try {
                    with(NotificationManagerCompat.from(context)) {
                        android.util.Log.d("NotificationManager", "NotificationManagerCompat取得完了")
                        notify(WEEKLY_NOTIFICATION_ID, notification)
                        android.util.Log.d("NotificationManager", "通知送信完了 - ID: $WEEKLY_NOTIFICATION_ID")
                    }
                } catch (e: SecurityException) {
                    android.util.Log.e("NotificationManager", "通知送信に失敗: 権限が不足しています", e)
                    return@launch
                } catch (e: Exception) {
                    android.util.Log.e("NotificationManager", "通知送信に失敗", e)
                    return@launch
                }
                
                // 通知履歴を保存（要約は文頭の文）
                historyService.saveNotificationHistory(
                    type = NotificationType.WEEKLY,
                    title = title,
                    content = displayContent,
                    summary = summary,
                    farmOwner = farmOwner,
                    region = region,
                    prefecture = prefecture,
                    seedCount = seedCount
                )
            } catch (e: Exception) {
                android.util.Log.e("NotificationManager", "週次通知送信に失敗", e)
            }
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
    
    /**
     * InboxStyle通知を作成（リスト形式の内容用）
     */
    private fun createInboxStyle(content: String, summary: String): NotificationCompat.InboxStyle {
        val lines = content.split("\n").filter { it.trim().isNotEmpty() }
        val inboxStyle = NotificationCompat.InboxStyle()
            .setSummaryText("詳細を表示するには通知を展開してください")
        
        // 各項目をInboxStyleに追加（最大5項目まで）
        var itemCount = 0
        for (line in lines) {
            if (line.contains("•") && itemCount < 5) {
                val cleanLine = line.replace("•", "").trim()
                if (cleanLine.isNotEmpty()) {
                    inboxStyle.addLine(cleanLine)
                    itemCount++
                }
            }
        }
        
        // 項目が多すぎる場合は「他X項目」を追加
        val totalItems = lines.count { it.contains("•") }
        if (totalItems > 5) {
            inboxStyle.addLine("他 ${totalItems - 5} 項目...")
        }
        
        return inboxStyle
    }
    
    // 通知本文から末尾のJSONコードブロックを取り除く
    private fun removeJsonCodeBlock(content: String): String {
        val start = content.indexOf("```json")
        if (start == -1) return content
        val end = content.indexOf("```", startIndex = start + 7)
        return if (end == -1) {
            content.substring(0, start).trimEnd()
        } else {
            (content.substring(0, start) + content.substring(end + 3)).trim()
        }
    }

    // 文頭 + 各セクションの「種名のみ」を抽出して通知本文用に整形（ラベル: 名前を区切りで表示）
    private fun buildCondensedContent(content: String): String {
        val text = removeJsonCodeBlock(content)
        val lines = text.lines()
        val header = lines.firstOrNull { it.trim().isNotEmpty() }?.trim().orEmpty()

        fun extractNames(sectionMarker: String): List<String> {
            val names = mutableListOf<String>()
            var i = 0
            while (i < lines.size) {
                val line = lines[i].trim()
                if (line.startsWith(sectionMarker)) {
                    var j = i + 1
                    while (j < lines.size) {
                        val l = lines[j].trim()
                        if (l.startsWith("🌱") || l.startsWith("⚠️") || l.startsWith("🌟") || l.startsWith("```")) break
                        if (l.startsWith("• ") || l.startsWith("* ") || l.startsWith("- ")) {
                            // 行から『…』内 or 先頭の種名部分を抽出
                            val raw = l.removePrefix("• ").removePrefix("* ").removePrefix("- ").trim()
                            val inQuote = Regex("『([^』]+)』").find(raw)?.groupValues?.getOrNull(1)
                            names += (inQuote ?: raw)
                        }
                        j++
                    }
                    break
                }
                i++
            }
            return names
        }

        val thisMonth = extractNames("🌱")
        val ending = extractNames("⚠️")
        val recommend = extractNames("🌟")

        fun line(label: String, list: List<String>): String {
            val body = if (list.isEmpty()) "該当なし" else list.joinToString(separator = "、")
            return "$label$body"
        }
        val parts = mutableListOf<String>()
        if (header.isNotEmpty()) parts += header
        parts += line("今月のまき時：", thisMonth)
        parts += line("まき時終了間近：", ending)
        parts += line("おすすめの種：", recommend)
        return parts.joinToString(separator = "\n")
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
        if (!hasNotificationPermission()) {
            android.util.Log.w("NotificationManager", "通知権限が許可されていません")
            return
        }
        
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
        if (!hasNotificationPermission()) {
            android.util.Log.w("NotificationManager", "通知権限が許可されていません")
            return
        }
        
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
