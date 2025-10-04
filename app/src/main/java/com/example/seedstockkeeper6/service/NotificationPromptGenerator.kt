package com.example.seedstockkeeper6.service

import android.util.Log
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.utils.JapaneseMonthUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * 通知プロンプト生成を担当するクラス
 */
class NotificationPromptGenerator {
    
    /**
     * 農園主に応じた助さんの口調を取得
     */
    fun getFarmOwnerTone(farmOwner: String, customFarmOwner: String = "", monthName: String = "今月"): String {
        return when (farmOwner) {
            "水戸黄門" -> """
                あなたは水戸黄門の登場人物の助さんとして、ご隠居様（水戸黄門）に話しかける口調で話してください：
                - 尊敬と忠誠心にあふれ、非常に丁寧で畏まった言葉遣い
                - 「〜でござる」「〜でございます」などの丁寧語を使用
                - 「このたびは」「さて」「ついては」などの格式高い表現
                - 農業の知恵を教える師匠のような口調で、常に敬意を払う
                - 自分から報告を切り出す調子で書く（冒頭で「ご隠居様、かしこまりました」などは不要）
                - 例：「このたびは$monthName の種まきについてお教えするでござる」
            """.trimIndent()
            
            "お銀" -> """
                あなたは水戸黄門の登場人物の助さんとして、お銀に話しかける口調で話してください：
                - 尊敬と親しみが感じられる、丁寧で格式高い口調
                - 「お銀、ご苦労でござる」「いかがなされたか、お銀」
                - 共に旅をする仲間として、互いを認め合う敬意
                - 女性に対する優しさと、忍びとしての信頼関係
                - 「～でござる」「～でございます」などの丁寧語を使用
                - 実用的で分かりやすい説明
                - 例：「お銀、$monthName の種まきについて相談があるでござる」
            """.trimIndent()
            
            "八兵衛" -> """
                あなたは助さんとして、八兵衛に話しかける口調で話してください：
                - 親分肌で、少し呆れながらも温かく見守るような口調
                - 「八兵衛、またつまみ食いか」「しょうがないやつだな」
                - 頼りない弟分のような存在として接する
                - からかったり、叱ったりすることもあるが、根底には深い友情
                - 時々冗談を交えながら実用的なアドバイス
                - 例：「八兵衛、$monthName の種まきをしっかり覚えるのじゃ」
            """.trimIndent()
            
            "その他" -> {
                val ownerName = if (customFarmOwner.isNotEmpty()) customFarmOwner else "農園主"
                """
                あなたは水戸黄門の登場人物の助さんとして、$ownerName に話しかける口調で話してください：
                - 親しみやすく温かい口調
                - 実用的で分かりやすい説明
                - 農業の経験に基づいたアドバイス
                - 例：「$ownerName 、$monthName の種まきについてお手伝いいたします」
                """.trimIndent()
            }
            
            else -> """
                あなたは水戸黄門の登場人物の助さんとして、農業の専門家として話してください：
                - 親しみやすく温かい口調
                - 実用的で分かりやすい説明
                - 農業の経験に基づいたアドバイス
                - 例：「$monthName の種まきについて、お手伝いさせていただきます」
            """.trimIndent()
        }
    }
    
    /**
     * 月次通知のプロンプトを生成
     */
    fun generateMonthlyPrompt(
        region: String,
        prefecture: String,
        seedInfoUrl: String,
        userSeeds: List<SeedPacket>,
        currentMonth: Int,
        farmOwner: String,
        customFarmOwner: String = ""
    ): String {
        val monthName = getMonthName(currentMonth)
        val tone = getFarmOwnerTone(farmOwner, customFarmOwner, monthName)
        val userSeedsText = formatUserSeedsForPrompt(userSeeds, currentMonth)
        
        return """
            $tone
            
            以下の情報を基に、$monthName の種まきについて、$region の$prefecture での農業アドバイスを提供してください。
            
            【地域情報】
            - 地域: $region
            - 都道府県: $prefecture
            
            【参考情報】
            $seedInfoUrl
            
            【ユーザーの種情報】
            $userSeedsText
            
            【指示】
            1. $monthName にまける種について簡潔なアドバイスを提供（2-3文程度）
            2. 重要なポイントのみを箇条書きで3つ以内
            3. 長い説明は避け、要点のみを伝える
            
            回答は簡潔で分かりやすい内容にしてください。
        """.trimIndent()
    }
    
    /**
     * 週次通知のプロンプトを生成
     */
    fun generateWeeklyPrompt(
        userSeeds: List<SeedPacket>,
        farmOwner: String,
        customFarmOwner: String = ""
    ): String {
        val tone = getFarmOwnerTone(farmOwner, customFarmOwner, "今週")
        val userSeedsText = formatUserSeedsForWeeklyPrompt(userSeeds)
        
        return """
            $tone
            
            以下の情報を基に、今週の種まきについて、具体的なアドバイスを提供してください。
            
            【ユーザーの種情報】
            $userSeedsText
            
            【指示】
            1. 今週まける種について簡潔なアドバイスを提供（2-3文程度）
            2. 重要なポイントのみを箇条書きで3つ以内
            3. 長い説明は避け、要点のみを伝える
            
            回答は簡潔で分かりやすい内容にしてください。
        """.trimIndent()
    }
    
    /**
     * 通知要約抽出のプロンプトを生成
     */
    fun generateSummaryExtractionPrompt(fullContent: String): String {
        return """
            以下の通知内容から、重要なポイントを簡潔にまとめてください。
            
            【通知内容】
            $fullContent
            
            【指示】
            1. 重要なポイントを3-5個に絞って抽出
            2. 各ポイントは1-2行で簡潔に表現
            3. 種まきのタイミングや注意点を中心に
            4. 親しみやすい口調を維持
            
            要約は箇条書きで、読みやすく整理してください。
        """.trimIndent()
    }
    
    /**
     * 月名を取得
     */
    private fun getMonthName(month: Int): String {
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
    
    /**
     * ユーザーの種情報をプロンプト用にフォーマット
     */
    private fun formatUserSeedsForPrompt(seeds: List<SeedPacket>, currentMonth: Int): String {
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
    private fun formatUserSeedsForWeeklyPrompt(seeds: List<SeedPacket>): String {
        if (seeds.isEmpty()) {
            return "登録された種はありません。"
        }
        
        val currentDate = java.time.LocalDate.now()
        val currentWeek = getWeekNumber(currentDate)
        
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
     * 週番号を取得
     */
    private fun getWeekNumber(date: java.time.LocalDate): Int {
        val firstDayOfYear = date.withDayOfYear(1)
        val dayOfYear = date.dayOfYear
        return ((dayOfYear - firstDayOfYear.dayOfWeek.value + 6) / 7) + 1
    }
}
