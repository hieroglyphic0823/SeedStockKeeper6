package com.example.seedstockkeeper6.notification

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.core.app.NotificationCompat
import com.example.seedstockkeeper6.model.SeedPacket

class NotificationContentBuilder {
    
    fun buildMonthlyNotificationContent(
        seedsThisMonth: List<SeedPacket>,
        seedsEndingSoon: List<SeedPacket>,
        recommendedSeeds: List<SeedPacket>
    ): String {
        val content = StringBuilder()
        
        // 今月まきどきの種
        if (seedsThisMonth.isNotEmpty()) {
            content.append("🌱 今月まきどきの種:\n")
            seedsThisMonth.forEach { seed ->
                content.append("• ${seed.productName} (${seed.variety})\n")
            }
            content.append("\n")
        }
        
        // 終了間近の種
        if (seedsEndingSoon.isNotEmpty()) {
            content.append("⚠️ 終了間近:\n")
            seedsEndingSoon.forEach { seed ->
                content.append("• ${seed.productName} (${seed.variety})\n")
            }
            content.append("\n")
        }
        
        // おすすめの種
        if (recommendedSeeds.isNotEmpty()) {
            content.append("🌟 今月のおすすめ種:\n")
            recommendedSeeds.forEach { seed ->
                content.append("• ${seed.productName} (${seed.variety})\n")
            }
        }
        
        return content.toString()
    }
    
    fun buildWeeklyNotificationContent(seedsEndingSoon: List<SeedPacket>): String {
        val content = StringBuilder()
        
        content.append("⚠️ まき時終了間近の種があります:\n\n")
        seedsEndingSoon.forEach { seed ->
            content.append("• ${seed.productName} (${seed.variety}) (${seed.expirationYear}/${seed.expirationMonth})\n")
        }
        
        content.append("早めに種まきを完了させてください。")
        
        return content.toString()
    }
    
    fun createInboxStyle(content: String, summary: String): NotificationCompat.InboxStyle {
        val lines = content.split("\n").filter { it.isNotBlank() }
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("種まき通知")
            .setSummaryText(summary)
        
        // 最大5行まで表示
        lines.take(5).forEach { line ->
            inboxStyle.addLine(line)
        }
        
        return inboxStyle
    }
    
    fun removeJsonCodeBlock(content: String): String {
        val jsonStart = content.indexOf("```json")
        val jsonEnd = content.indexOf("```", jsonStart + 7)
        
        return if (jsonStart != -1 && jsonEnd != -1) {
            content.substring(0, jsonStart) + content.substring(jsonEnd + 3)
        } else {
            content
        }
    }
    
    fun buildCondensedContent(content: String): SpannableString {
        val lines = content.lines()
        val nonEmptyLines = lines.filter { it.isNotBlank() }
        
        // 最大10行まで表示（通知の制限を考慮）
        val displayLines = nonEmptyLines.take(10)
        val fullContent = displayLines.joinToString("\n")
        
        val spannableString = SpannableString(fullContent)
        
        // セクションラベルを太字にする
        val sectionLabels = listOf("🌱", "⚠️", "🌟", "【今月のまき時】", "【まき時終了間近】", "【おすすめの種】")
        sectionLabels.forEach { label ->
            val startIndex = fullContent.indexOf(label)
            if (startIndex != -1) {
                spannableString.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    startIndex + label.length,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        
        return spannableString
    }
}

