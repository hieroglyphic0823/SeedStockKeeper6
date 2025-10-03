package com.example.seedstockkeeper6.service

import android.util.Log
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
            "水戸黄門" -> "ご隠居様、$monthName の種まきについて"
            "お銀" -> "お銀、$monthName の種まきのご案内"
            "八兵衛" -> "八兵衛、$monthName の種まきだぞ"
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
            
            【注意事項】
            • 種まき前の土の状態確認
            • 適切な深さと間隔での播種
            • 発芽後の水やり管理
            
            詳細な情報は種まきカレンダーをご確認ください。
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
            
            【注意事項】
            • 天候の変化に注意
            • 適切な水やり
            • 害虫・病気の予防
            
            詳細な情報は種まきカレンダーをご確認ください。
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
     * 日本語の月名を取得
     */
    private fun getJapaneseMonthName(month: Int): String {
        return when (month) {
            1 -> "1月"
            2 -> "2月"
            3 -> "3月"
            4 -> "4月"
            5 -> "5月"
            6 -> "6月"
            7 -> "7月"
            8 -> "8月"
            9 -> "9月"
            10 -> "10月"
            11 -> "11月"
            12 -> "12月"
            else -> "今月"
        }
    }
}
