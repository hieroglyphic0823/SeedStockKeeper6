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
        recommendedSeeds: String,
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

【重要】農園主は「$farmOwner」です。必ずこの農園主宛の内容を生成してください。

【地域情報】
- 地域: $region
- 都道府県: $prefecture

【参考情報（おすすめ種情報）】
$recommendedSeeds

【ユーザーの種情報】
$userSeedsText

【指示】
1. 文頭に、農園主「$farmOwner」に対応した一言あいさつを入れる
2. 「今月まきどき」「終了間近」「おすすめ」の3セクションに分けてリスト表示する
3. 各種については「今、なぜまくべきか」「注意点」など、簡潔に説明（必ず50文字以内の1文）
4. 全体はくどくなりすぎないように。文量を抑え、読みやすく親しみやすく。
5. キャラクターに応じた言葉遣いにする（以下参照）

【各セクションの内容】
- 「今月まきどき」: ユーザーが登録している種で今月が播種期間の種
- 「終了間近」: ユーザーが登録している種で今月が播種期間の終了月の種（【ユーザーの種情報】の「有効期限: YYYY年MM月」からexpirationYearとexpirationMonthを抽出して設定）
- 「おすすめ」: 【参考情報（おすすめ種情報）】で提供された種から、 地域: $region・今月に適した種を選出（最低1つは必ず含める）

【JSON出力形式】
```json
{
  "notificationType": "MONTHLY",
  "title": "${getJapaneseMonthName(currentMonth)}すけさん便り",
  "summary": "農園主への挨拶文",
  "farmOwner": "$farmOwner",
  "region": "$region",
  "prefecture": "$prefecture",
  "month": $currentMonth,
  "thisMonthSeeds": [
    {
      "name": "種名",
      "variety": "品種名",
      "description": "説明文（50文字以内）"
    }
  ],
  "endingSoonSeeds": [
    {
      "name": "種名",
      "variety": "品種名",
      "description": "説明文（50文字以内）",
      "expirationYear": 2026,
      "expirationMonth": 10
    }
  ],
  "recommendedSeeds": [
    {
      "name": "種名",
      "variety": "品種名",
      "description": "説明文（50文字以内）"
    },
    {
      "name": "種名2",
      "variety": "品種名2",
      "description": "説明文（50文字以内）"
    }
  ],
  "closingLine": "結びの文",
  "signature": "署名"
}
```

【有効期限の設定について】
- endingSoonSeedsの各項目で、expirationYearとexpirationMonthは【ユーザーの種情報】の「有効期限: YYYY年MM月」から抽出してください
- 例：「有効期限: 2026年10月」→ expirationYear: 2026, expirationMonth: 10
- 例：「有効期限: 2026年11月」→ expirationYear: 2026, expirationMonth: 11

【文頭フォーマット（summaryフィールドに設定）】
- 農園主が「水戸黄門」の場合:
  「ご隠居様、$monthName の作物について、進言申し上げまする。」
- 農園主が「お銀」の場合:
  「お銀殿、$monthName の作物について進言申し上げまする。」
- 農園主が「八兵衛」の場合:
  「八兵衛殿、$monthName の作物について進言申し上げまする。」
- その他の場合:
  「$farmOwner 殿、$monthName の作物について進言申し上げまする。」

【結びの文（closingLineフィールドに設定）】
農園主のキャラクターと季節・天候に合った励ましのメッセージ（36文字以内）を農園主のキャラクターに応じて生成してください：
- 農園主（「水戸黄門」「お銀」「八兵衛」のいずれか）に応じた言葉遣い
- $monthName を反映した内容
- 例：
  - 「ご無理なさらず、温かくして作業なされませ。」
  - 「寒さに気をつけて、土と向き合ってくだされ。」
  - 「防寒大事だぞ！明日も気張ってこーぜ！」

【署名（signatureフィールドに設定）】
農園主に応じた署名を使用してください：
- 「水戸黄門」宛：署名「佐々木助三郎 拝」
- 「お銀」宛：署名「佐々木助三郎 拝」
- 「八兵衛」宛：署名「助三郎 より」
- その他：署名「助さんより」

【重要】現在の農園主は「$farmOwner」です。この農園主のキャラクターに応じた励ましのメッセージと署名を必ず使用してください。

【言葉遣いの注意】
- 水戸黄門: 格式高い丁寧語（「〜でござる」「〜でございます」）
- お銀: 親しみやすい丁寧語（「〜でござる」）
- 八兵衛: 親しみやすい口調（「〜だ」「〜じゃ」）
- その他: 親しみやすく温かい口調

必ずJSON形式で回答してください。テキスト形式は使用しないでください。
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
        // 週番号と月名を算出
        val today = java.time.LocalDate.now()
        val weekFields = java.time.temporal.WeekFields.of(java.util.Locale.JAPAN)
        val weekNumber = today.get(weekFields.weekOfMonth())
        val monthName = getMonthName(today.monthValue)
        
        return """
            $tone
            
            以下の情報を基に、今週の種まきについて、具体的なアドバイスを提供してください。
            
            【ユーザーの種情報】
            $userSeedsText
            
            【指示】
            1. 今週まける種について簡潔なアドバイスを提供（2-3文程度）
            2. 重要なポイントのみを箇条書きで3つ以内
            3. 農園主の口調に合わせた挨拶と説明文を使用する
            4. 農園主の性格に応じた言葉遣いで親しみやすく説明する

            【重要】回答は必ず以下のJSON形式で出力してください：

            ```json
            {
              "notificationType": "WEEKLY",
              "title": "種まき期限のお知らせ",
              "summary": "農園主への挨拶文",
              "farmOwner": "$farmOwner",
              "region": "温暖地",
              "prefecture": "",
              "month": ${today.monthValue},
              "thisMonthSeeds": [],
              "endingSoonSeeds": [
                {
                  "name": "種名",
                  "variety": "品種名",
                  "description": "説明文（40文字以内）"
                }
              ],
              "recommendedSeeds": [],
              "advice": "アドバイス文",
              "closingLine": "結びの文",
              "signature": "署名"
            }
            ```

            【文頭フォーマット（summaryフィールドに設定）】
            - 農園主が「水戸黄門」の場合:
              「ご隠居様、$monthName（第$weekNumber 週）となりました。さて、このたびは下記の作物について、進言申し上げまする。」
            - 農園主が「お銀」の場合:
              「お銀殿、$monthName（第$weekNumber 週）となりましたな。ついては、下記の作物について進言申し上げまする。」
            - 農園主が「八兵衛」の場合:
              「八兵衛！$monthName も第$weekNumber 週にござるぞ！さてさて、今週の畑仕事について、下記の種にて心得ておくがよろしかろう。」
            - その他の場合:
              「$farmOwner 殿、$monthName（第$weekNumber 週）となりました。今週の種まきについて進言申し上げまする。」

            【アドバイス（adviceフィールドに設定）】
            今週の種まきについて簡潔なアドバイスを1行で生成してください。

            【結びの文（closingLineフィールドに設定）】
            今週の農作業を励ますメッセージ（36文字以内）を農園主のキャラクターに応じて生成してください。

            【署名（signatureフィールドに設定）】
            農園主に応じた署名を使用してください：
            - 「水戸黄門」宛：署名「佐々木助三郎 拝」
            - 「お銀」宛：署名「佐々木助三郎 拝」  
            - 「八兵衛」宛：署名「助三郎 より」
            - その他：署名「助さんより」
            
            【言葉遣いの注意】
            - 農園主の設定に応じて適切な敬語や親しみやすい表現を使用
            - 水戸黄門: 格式高い丁寧語（「〜でござる」「〜でございます」）
            - お銀: 親しみやすい丁寧語（「〜でござる」）
            - 八兵衛: 親しみやすい口調（「〜だ」「〜じゃ」）
            - その他: 親しみやすく温かい口調
            
            必ずJSON形式で回答してください。テキスト形式は使用しないでください。
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
        
        val thisMonthSeeds = mutableListOf<SeedPacket>()
        val endingThisMonthSeeds = mutableListOf<SeedPacket>()
        
        // 種を分類
        seeds.forEach { seed ->
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
        
        // 今月まきどきの種
        if (thisMonthSeeds.isNotEmpty()) {
            content.appendLine("🌱 今月まきどきの種:")
            thisMonthSeeds.forEach { seed ->
                content.appendLine("・${seed.productName} (${seed.variety}) - ${seed.family}")
            }
            content.appendLine()
        } else {
            content.appendLine("🌱 今月まきどきの種: 該当なし")
            content.appendLine()
        }
        
        // 終了間近の種
        if (endingThisMonthSeeds.isNotEmpty()) {
            content.appendLine("⚠️ 終了間近:")
            endingThisMonthSeeds.forEach { seed ->
                val expirationInfo = seed.calendar?.firstOrNull()?.let { entry ->
                    if (entry.expirationYear > 0 && entry.expirationMonth > 0) {
                        " - 有効期限: ${entry.expirationYear}年${entry.expirationMonth}月"
                    } else {
                        ""
                    }
                } ?: ""
                content.appendLine("・${seed.productName} (${seed.variety}) - ${seed.family}${expirationInfo}")
            }
            content.appendLine()
        } else {
            content.appendLine("⚠️ 終了間近: 該当なし")
            content.appendLine()
        }
        
        return content.toString().trim()
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
