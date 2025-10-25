package com.example.seedstockkeeper6.service

import com.example.seedstockkeeper6.model.SeedPacket
import java.text.SimpleDateFormat
import java.util.*

/**
 * 通知コンテンツのフォーマットを担当するクラス
 */
class NotificationContentFormatter {
    
    /**
     * デフォルトの月次通知タイトルを生成
     */
    fun getDefaultMonthlyTitle(currentMonth: Int, farmOwner: String): String {
        val monthName = getJapaneseMonthName(currentMonth)
        return when (farmOwner) {
            "水戸黄門" -> "$monthName の種まきについて"
            "お銀" -> "$monthName の種まきのご案内"
            "八兵衛" -> "$monthName の種まきだぞ"
            else -> "$monthName の種まきについて"
        }
    }
    
    /**
     * デフォルトの月次通知内容を生成
     */
    fun getDefaultMonthlyContent(monthName: String): String {
        return """
            $monthName の種まきについてお知らせいたします。
            
            【今月のポイント】
            • 適切な播種時期の確認
            • 土づくりと種まきの準備
            • 天候に応じた管理
            
            詳細は種まきカレンダーをご確認ください。
        """.trimIndent()
    }
    
    /**
     * デフォルトの週次通知内容を生成
     */
    fun getDefaultWeeklyContent(): String {
        return """
            今週の種まきについてお知らせいたします。
            
            【今週のポイント】
            • 種まきのタイミング確認
            • 土の準備と種まき作業
            • 発芽後の管理
            
            詳細は種まきカレンダーをご確認ください。
        """.trimIndent()
    }
    
    /**
     * 通知要約を手動で抽出
     */
    fun extractSummaryManually(content: String): String {
        val lines = content.split("\n").filter { it.trim().isNotEmpty() }
        
        val importantPoints = mutableListOf<String>()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            // 重要なキーワードを含む行を抽出
            if (trimmedLine.contains("種まき") || 
                trimmedLine.contains("播種") || 
                trimmedLine.contains("発芽") ||
                trimmedLine.contains("収穫") ||
                trimmedLine.contains("管理") ||
                trimmedLine.contains("注意") ||
                trimmedLine.contains("ポイント") ||
                trimmedLine.contains("タイミング")) {
                
                // 箇条書きの形式に統一
                val formattedLine = if (trimmedLine.startsWith("•") || trimmedLine.startsWith("-")) {
                    trimmedLine
                } else {
                    "• $trimmedLine"
                }
                
                importantPoints.add(formattedLine)
            }
        }
        
        return if (importantPoints.isEmpty()) {
            "今月の種まきについて、詳細な情報をご確認ください。"
        } else {
            importantPoints.take(5).joinToString("\n")
        }
    }
    
    /**
     * ユーザーの種情報をフォーマット
     */
    fun formatUserSeeds(seeds: List<SeedPacket>, currentMonth: Int): String {
        if (seeds.isEmpty()) {
            return "登録された種はありません。"
        }
        
        val relevantSeeds = seeds.filter { seed ->
            seed.calendar?.any { entry ->
                val startMonth = parseMonthFromDate(entry.sowing_start_date)
                val endMonth = parseMonthFromDate(entry.sowing_end_date)
                startMonth != null && endMonth != null && isMonthInRange(currentMonth, startMonth, endMonth)
            } ?: false
        }
        
        return if (relevantSeeds.isEmpty()) {
            "今月まける種は登録されていません。"
        } else {
            relevantSeeds.joinToString("\n") { seed ->
                buildString {
                    appendLine("・${seed.productName} (${seed.variety})")
                    appendLine("  科: ${seed.family}")
                    appendLine("  播種期間: ${seed.calendar?.firstOrNull()?.sowing_start_date} ～ ${seed.calendar?.firstOrNull()?.sowing_end_date}")
                    appendLine("  収穫期間: ${seed.calendar?.firstOrNull()?.harvest_start_date} ～ ${seed.calendar?.firstOrNull()?.harvest_end_date}")
                    if (seed.companionPlants.isNotEmpty()) {
                        appendLine("  コンパニオンプランツ: ${formatCompanionPlants(seed.companionPlants)}")
                    }
                }
            }
        }
    }
    
    /**
     * 月次通知用の種情報を分類してフォーマット
     */
    fun formatMonthlySeedInfo(
        userSeeds: List<SeedPacket>, 
        currentMonth: Int,
        recommendedSeeds: String = ""
    ): String {
        val thisMonthSeeds = mutableListOf<SeedPacket>()
        val endingThisMonthSeeds = mutableListOf<SeedPacket>()
        
        // ユーザーの種を分類
        userSeeds.forEach { seed ->
            seed.calendar?.forEach { entry ->
                val startMonth = parseMonthFromDate(entry.sowing_start_date)
                val endMonth = parseMonthFromDate(entry.sowing_end_date)
                
                if (startMonth != null && endMonth != null) {
                    // 今月が播種期間内かチェック
                    if (isMonthInRange(currentMonth, startMonth, endMonth)) {
                        thisMonthSeeds.add(seed)
                    }
                    // 今月が播種期間の終了月かチェック
                    if (currentMonth == endMonth) {
                        endingThisMonthSeeds.add(seed)
                    }
                }
            }
        }
        
        val content = StringBuilder()
        
        // 1. 今月まきどきの種情報
        if (thisMonthSeeds.isNotEmpty()) {
            content.appendLine("🌱 今月まきどきの種:")
            thisMonthSeeds.take(5).forEach { seed ->
                content.appendLine("• ${seed.productName} (${seed.variety}) - ${seed.family}")
            }
            if (thisMonthSeeds.size > 5) {
                content.appendLine("他 ${thisMonthSeeds.size - 5} 種類")
            }
            content.appendLine()
        }
        
        // 2. 終了間近の種情報
        if (endingThisMonthSeeds.isNotEmpty()) {
            content.appendLine("⚠️ まき時終了間近:")
            endingThisMonthSeeds.take(3).forEach { seed ->
                content.appendLine("• ${seed.productName} (${seed.variety}) - ${seed.family}")
            }
            if (endingThisMonthSeeds.size > 3) {
                content.appendLine("他 ${endingThisMonthSeeds.size - 3} 種類")
            }
            content.appendLine()
        }
        
        // 3. おすすめの種情報（農園情報の種情報URLから）
        if (recommendedSeeds.isNotBlank()) {
            content.appendLine("🌟 今月のおすすめ種:")
            content.appendLine(recommendedSeeds)
        }
        
        return content.toString().trim()
    }
    
    /**
     * 週次通知用のユーザー種情報をフォーマット
     */
    fun formatUserSeedsForWeekly(seeds: List<SeedPacket>): String {
        if (seeds.isEmpty()) {
            return "登録された種はありません。"
        }
        
        val currentDate = java.time.LocalDate.now()
        
        val relevantSeeds = seeds.filter { seed ->
            seed.calendar?.any { entry ->
                val startDate = try {
                    java.time.LocalDate.parse(entry.sowing_start_date)
                } catch (e: Exception) {
                    null
                }
                val endDate = try {
                    java.time.LocalDate.parse(entry.sowing_end_date)
                } catch (e: Exception) {
                    null
                }
                
                startDate != null && endDate != null && 
                currentDate.isAfter(startDate.minusDays(7)) && 
                currentDate.isBefore(endDate.plusDays(7))
            } ?: false
        }
        
        return if (relevantSeeds.isEmpty()) {
            "今週まける種は登録されていません。"
        } else {
            relevantSeeds.joinToString("\n") { seed ->
                buildString {
                    appendLine("・${seed.productName} (${seed.variety})")
                    appendLine("  科: ${seed.family}")
                    appendLine("  播種期間: ${seed.calendar?.firstOrNull()?.sowing_start_date} ～ ${seed.calendar?.firstOrNull()?.sowing_end_date}")
                    appendLine("  収穫期間: ${seed.calendar?.firstOrNull()?.harvest_start_date} ～ ${seed.calendar?.firstOrNull()?.harvest_end_date}")
                    if (seed.companionPlants.isNotEmpty()) {
                        appendLine("  コンパニオンプランツ: ${formatCompanionPlants(seed.companionPlants)}")
                    }
                }
            }
        }
    }
    
    /**
     * コンパニオンプランツをフォーマット
     */
    private fun formatCompanionPlants(companionPlants: List<com.example.seedstockkeeper6.model.CompanionPlant>): String {
        return companionPlants.joinToString(", ") { companion ->
            "${companion.plant} (${companion.effects.joinToString(", ") { getCompanionPlantEffectCode(it) }})"
        }
    }
    
    /**
     * コンパニオンプランツの効果コードを取得
     */
    private fun getCompanionPlantEffectCode(effect: String): String {
        return when (effect.lowercase()) {
            "pest_control" -> "害虫防除"
            "nutrient_fixation" -> "栄養固定"
            "shade_provision" -> "日陰提供"
            "soil_improvement" -> "土壌改善"
            "attract_beneficials" -> "益虫誘引"
            "disease_prevention" -> "病気予防"
            "weed_suppression" -> "雑草抑制"
            "pollination_support" -> "受粉支援"
            else -> effect
        }
    }
    
    /**
     * 日付文字列から月を解析
     */
    private fun parseMonthFromDate(dateString: String): Int? {
        return try {
            val date = java.time.LocalDate.parse(dateString)
            date.monthValue
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 月が範囲内かチェック
     */
    private fun isMonthInRange(targetMonth: Int, startMonth: Int, endMonth: Int): Boolean {
        return if (startMonth <= endMonth) {
            targetMonth in startMonth..endMonth
        } else {
            // 年をまたぐ場合（例：11月〜3月）
            targetMonth >= startMonth || targetMonth <= endMonth
        }
    }
    
    /**
     * 日本語の月名を取得（和風月名）
     */
    private fun getJapaneseMonthName(month: Int): String {
        return when (month) {
            1 -> "睦月"
            2 -> "如月"
            3 -> "弥生"
            4 -> "卯月"
            5 -> "皐月"
            6 -> "水無月"
            7 -> "文月"
            8 -> "葉月"
            9 -> "長月"
            10 -> "神無月"
            11 -> "霜月"
            12 -> "師走"
            else -> "今月"
        }
    }
}
