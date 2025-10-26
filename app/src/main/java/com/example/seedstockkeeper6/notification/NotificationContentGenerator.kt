package com.example.seedstockkeeper6.notification

import com.example.seedstockkeeper6.model.NotificationData
import com.example.seedstockkeeper6.model.SeedInfo

class NotificationContentGenerator {
    
    /**
     * NotificationDataから通知文を生成
     */
    fun generateContent(notificationData: NotificationData): String {
        val content = StringBuilder()
        
        // ヘッダー（summary）
        if (notificationData.summary.isNotEmpty()) {
            content.append(notificationData.summary).append("\n\n")
        }
        
        // 今月まきどきの種
        if (notificationData.thisMonthSeeds.isNotEmpty()) {
            content.append("🌱まきどき\n")
            notificationData.thisMonthSeeds.forEach { seed ->
                content.append("*   『${seed.name} (${seed.variety})』: ${seed.description}\n")
            }
            content.append("\n")
        }
        
        // 終了間近の種
        if (notificationData.endingSoonSeeds.isNotEmpty()) {
            content.append("⏳期限間近:\n")
            notificationData.endingSoonSeeds.forEach { seed ->
                content.append("*   『${seed.name} (${seed.variety})』: ${seed.description}\n")
            }
            content.append("\n")
        }
        
        // おすすめの種
        if (notificationData.recommendedSeeds.isNotEmpty()) {
            content.append("🎯 今月のおすすめ:\n")
            notificationData.recommendedSeeds.forEach { seed ->
                content.append("*   『${seed.name} (${seed.variety})』: ${seed.description}\n")
            }
            content.append("\n")
        }
        
        // 結びの文
        if (notificationData.closingLine.isNotEmpty()) {
            content.append(notificationData.closingLine).append("\n")
        }
        
        // 署名
        if (notificationData.signature.isNotEmpty()) {
            content.append(notificationData.signature)
        }
        
        return content.toString()
    }
    
    /**
     * 通知用の簡潔なコンテンツを生成（アプリ通知用）
     */
    fun generateCondensedContent(notificationData: NotificationData): String {
        val content = StringBuilder()
        
        // 今月まきどきの種（最大3つまで）
        if (notificationData.thisMonthSeeds.isNotEmpty()) {
            content.append("🌱まきどき\n")
            notificationData.thisMonthSeeds.take(3).forEach { seed ->
                content.append(" ${seed.name} (${seed.variety})\n")
            }
            content.append("\n")
        }
        
        // 終了間近の種（最大3つまで）
        if (notificationData.endingSoonSeeds.isNotEmpty()) {
            content.append("⏳期限間近:\n")
            notificationData.endingSoonSeeds.take(3).forEach { seed ->
                val expirationInfo = if (seed.expirationYear > 0 && seed.expirationMonth > 0) {
                    " (${seed.expirationYear}/${seed.expirationMonth})"
                } else {
                    ""
                }
                content.append(" ${seed.name} (${seed.variety})${expirationInfo}\n")
            }
            content.append("\n")
        }
        
        // おすすめの種（最大3つまで）
        if (notificationData.recommendedSeeds.isNotEmpty()) {
            content.append("🎯 今月のおすすめ:\n")
            notificationData.recommendedSeeds.take(3).forEach { seed ->
                content.append(" ${seed.name} (${seed.variety})\n")
            }
        }
        
        return content.toString()
    }
    
    /**
     * お城画面用の1行表示コンテンツを生成
     */
    fun generateSingleLineContent(notificationData: NotificationData): String {
        val content = StringBuilder()
        
        // 今月まきどきの種（1行表示）
        if (notificationData.thisMonthSeeds.isNotEmpty()) {
            val seedNames = notificationData.thisMonthSeeds.take(5).map { it.name }
            content.append("🌱まきどき：${seedNames.joinToString("、")}")
            if (notificationData.thisMonthSeeds.size > 5) {
                content.append("...")
            }
            content.append("\n")
        }
        
        // 終了間近の種（1行表示）
        if (notificationData.endingSoonSeeds.isNotEmpty()) {
            val seedNames = notificationData.endingSoonSeeds.take(5).map { seed ->
                val expirationInfo = if (seed.expirationYear > 0 && seed.expirationMonth > 0) {
                    "${seed.name}（${seed.expirationYear}/${seed.expirationMonth}）"
                } else {
                    seed.name
                }
                expirationInfo
            }
            content.append("⏳期限間近：${seedNames.joinToString("、")}")
            if (notificationData.endingSoonSeeds.size > 5) {
                content.append("...")
            }
            content.append("\n")
        }
        
        // おすすめの種（1行表示）
        if (notificationData.recommendedSeeds.isNotEmpty()) {
            val seedNames = notificationData.recommendedSeeds.take(5).map { it.name }
            content.append("🎯おすすめ：${seedNames.joinToString("、")}")
            if (notificationData.recommendedSeeds.size > 5) {
                content.append("...")
            }
        }
        
        return content.toString()
    }
    
    /**
     * 通知タイトルにアイコンを追加
     */
    fun generateTitleWithIcon(notificationData: NotificationData): String {
        return notificationData.title
    }
    
    /**
     * 通知の要約文を生成
     */
    fun generateSummary(notificationData: NotificationData): String {
        return when (notificationData.notificationType) {
            "MONTHLY" -> {
                val thisMonthNames = notificationData.thisMonthSeeds.take(3).map { it.name }
                if (thisMonthNames.isNotEmpty()) {
                    "🌱まきどき：${thisMonthNames.joinToString("、")}"
                } else {
                    "今月の種まき情報"
                }
            }
            "WEEKLY" -> {
                val endingNames = notificationData.endingSoonSeeds.take(3).map { it.name }
                if (endingNames.isNotEmpty()) {
                    "⚠️期限間近：${endingNames.joinToString("、")}"
                } else {
                    "種まき期限のお知らせ"
                }
            }
            "CUSTOM" -> "カスタム通知"
            else -> "種まきのお知らせ"
        }
    }
}
