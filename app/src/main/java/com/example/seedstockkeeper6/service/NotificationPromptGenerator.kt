package com.example.seedstockkeeper6.service

import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.utils.JapaneseMonthUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * 通知プロンプト生成を担当するクラス
 */
class NotificationPromptGenerator {
    
    private val dataProcessor = NotificationDataProcessor()
    
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
  "title": "${dataProcessor.getJapaneseMonthName(currentMonth)}すけさん便り",
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
- endingSoonSeedsの各項目で、expirationYearとexpirationMonthは【ユーザーの種情報】から抽出してください
- ユーザーの種情報の「有効期限: YYYY年MM月」の形式から年と月を抽出
- 例：「有効期限: 2026年10月」→ expirationYear: 2026, expirationMonth: 10
- 例：「有効期限: 2026年11月」→ expirationYear: 2026, expirationMonth: 11
- 有効期限が設定されていない種はendingSoonSeedsに含めないでください
- 有効期限が不明な場合は、expirationYear: 0, expirationMonth: 0 としてください

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
        customFarmOwner: String = "",
        recommendedSeeds: String = "",
        region: String = "温暖地"
    ): String {
        val tone = getFarmOwnerTone(farmOwner, customFarmOwner, "今週")
        val userSeedsText = formatUserSeedsForWeeklyPrompt(userSeeds)
        // 週番号と月名を算出
        val today = java.time.LocalDate.now()
        val weekFields = java.time.temporal.WeekFields.of(java.util.Locale.JAPAN)
        val weekNumber = today.get(weekFields.weekOfMonth())
        val monthName = getMonthName(today.monthValue)
        
        // デバッグログ: 週次通知のプロンプト情報
        android.util.Log.d("NotificationPromptGenerator", "週次通知プロンプト生成 - 週番号: $weekNumber, 地域: $region")
        android.util.Log.d("NotificationPromptGenerator", "ユーザー種情報数: ${userSeeds.size}")
        android.util.Log.d("NotificationPromptGenerator", "ユーザー種情報詳細: $userSeedsText")
        
        return """
            $tone
            
            以下の情報を基に、今週の種まきについて、具体的なアドバイスを提供してください。
            
            【ユーザーの種情報】
            $userSeedsText
            
            【参考情報（おすすめ種情報）】
            $recommendedSeeds
            
            【指示】
            1. 今月まきどきの種、期限間近の種、おすすめの種を分類して提供
            2. 今月まきどきの種：今月が播種時期の種
            3. 期限間近の種：まき時が終了間近の種（有効期限も含める）
            4. おすすめの種：【参考情報（おすすめ種情報）】から地域・月に合わせて選出、必ず3つ含める
            5. おすすめの種は、ユーザーが持っていない種やコンパニオンプランツとなる種を優先的に選出する
            6. 農園主の口調に合わせた挨拶と説明文を使用する
            7. 農園主の性格に応じた言葉遣いで親しみやすく説明する

            【重要】回答は必ず以下のJSON形式で出力してください：

            ```json
            {
              "notificationType": "WEEKLY",
              "title": "${dataProcessor.getJapaneseMonthName(today.monthValue)}（第${weekNumber}週）すけさん便り",
              "summary": "農園主への挨拶文",
              "farmOwner": "$farmOwner",
              "region": "$region",
              "prefecture": "",
              "month": ${today.monthValue},
              "thisMonthSeeds": [
                {
                  "name": "種名",
                  "variety": "品種名",
                  "description": "説明文（40文字以内）"
                }
              ],
              "endingSoonSeeds": [
                {
                  "name": "種名",
                  "variety": "品種名",
                  "description": "説明文（40文字以内）",
                  "expirationYear": 2025,
                  "expirationMonth": 10
                }
              ],
              "recommendedSeeds": [
                {
                  "name": "種名",
                  "variety": "品種名",
                  "description": "説明文（40文字以内）"
                }
              ],
              "advice": "アドバイス文",
              "closingLine": "結びの文",
              "signature": "署名"
            }
            ```

            【有効期限の設定について】
            - endingSoonSeedsの各項目で、expirationYearとexpirationMonthは【ユーザーの種情報】から抽出してください
            - ユーザーの種情報の「有効期限: YYYY年MM月」の形式から年と月を抽出
            - 例：「有効期限: 2026年10月」→ expirationYear: 2026, expirationMonth: 10
            - 例：「有効期限: 2026年11月」→ expirationYear: 2026, expirationMonth: 11
            - 有効期限が設定されていない種はendingSoonSeedsに含めないでください
            - 有効期限が不明な場合は、expirationYear: 0, expirationMonth: 0 としてください

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
            
            【おすすめの種の選出について】
            - おすすめの種は【参考情報（おすすめ種情報）】から選出してください
            - ユーザーが既に持っている種は避け、新しい種やコンパニオンプランツとなる種を優先してください
            - 地域（$region）と月（${today.monthValue}月）に適した種を選出してください
            - 必ず3つ含めてください

            【重要】回答は必ずJSON形式のみで出力してください。
            - テキスト形式は使用しないでください
            - JSON以外の説明文は含めないでください
            - ```json``` コードブロックは使用しないでください
            - 純粋なJSONオブジェクトのみを出力してください
        """.trimIndent()
    }
    
    /**
     * 和風月名を取得（1..12）
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
                // デバッグログ: 種情報の有効期限
                android.util.Log.d("NotificationPromptGenerator", "種情報デバッグ - 種名: ${seed.productName}")
                android.util.Log.d("NotificationPromptGenerator", "種情報デバッグ - 種の有効期限: ${seed.expirationYear}年${seed.expirationMonth}月")
                
                // カレンダーエントリの有効期限を確認
                seed.calendar?.forEachIndexed { index, entry ->
                    android.util.Log.d("NotificationPromptGenerator", "種情報デバッグ - カレンダーエントリ$index: ${entry.expirationYear}年${entry.expirationMonth}月")
                }
                
                // 種の有効期限を優先的に使用（カレンダーエントリは間違っている可能性があるため）
                val expirationInfo = if (seed.expirationYear > 0 && seed.expirationMonth > 0) {
                    android.util.Log.d("NotificationPromptGenerator", "種情報デバッグ - 使用する有効期限: 種の有効期限 ${seed.expirationYear}年${seed.expirationMonth}月")
                    " - 有効期限: ${seed.expirationYear}年${seed.expirationMonth}月"
                } else {
                    // 種の有効期限がない場合のみカレンダーエントリを使用
                    seed.calendar?.firstOrNull()?.let { entry ->
                        android.util.Log.d("NotificationPromptGenerator", "種情報デバッグ - 使用する有効期限: カレンダーエントリ ${entry.expirationYear}年${entry.expirationMonth}月")
                        if (entry.expirationYear > 0 && entry.expirationMonth > 0) {
                            " - 有効期限: ${entry.expirationYear}年${entry.expirationMonth}月"
                        } else {
                            ""
                        }
                    } ?: run {
                        android.util.Log.d("NotificationPromptGenerator", "種情報デバッグ - 有効期限情報なし")
                        ""
                    }
                }
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
     * ユーザーの種情報からおすすめの種を取得（週番号に応じて今月または翌月）
     */
    private fun getRecommendedSeedsFromUserSeeds(
        userSeeds: List<SeedPacket>,
        weekNumber: Int,
        region: String
    ): String {
        val currentDate = java.time.LocalDate.now()
        val currentMonth = currentDate.monthValue
        val currentYear = currentDate.year
        
        // 週番号に応じて対象月を決定
        val targetMonth = if (weekNumber <= 2) {
            currentMonth
        } else {
            // 3週以降は翌月
            if (currentMonth == 12) 1 else currentMonth + 1
        }
        
        val targetYear = if (weekNumber <= 2) {
            currentYear
        } else {
            if (currentMonth == 12) currentYear + 1 else currentYear
        }
        
        // デバッグログ: 対象月と地域の情報
        android.util.Log.d("NotificationPromptGenerator", "おすすめの種抽出 - 対象月: $targetMonth, 対象年: $targetYear, 地域: $region")
        
        // 対象月の地域区分に応じたおすすめの種を抽出
        val recommendedSeeds = userSeeds.filter { seed ->
            seed.calendar?.any { entry ->
                val isRegionMatch = entry.region == region
                val isMonthMatch = isSeedRecommendedForMonth(entry, targetMonth, targetYear)
                android.util.Log.d("NotificationPromptGenerator", "種チェック - ${seed.productName}: 地域一致=$isRegionMatch, 月一致=$isMonthMatch")
                isRegionMatch && isMonthMatch
            } ?: false
        }
        
        android.util.Log.d("NotificationPromptGenerator", "抽出されたおすすめの種数: ${recommendedSeeds.size}")
        
        if (recommendedSeeds.isEmpty()) {
            android.util.Log.d("NotificationPromptGenerator", "おすすめの種が見つかりませんでした")
            return "おすすめの種は登録されていません。"
        }
        
        val content = StringBuilder()
        val monthName = getMonthName(targetMonth)
        val title = if (weekNumber <= 2) "🎯 今月のおすすめ" else "🔥 来月のおすすめ"
        
        content.appendLine("$title ($monthName):")
        recommendedSeeds.take(3).forEach { seed ->
            content.appendLine("・${seed.productName} (${seed.variety}) - ${seed.family}")
            seed.calendar?.firstOrNull { it.region == region }?.let { entry ->
                content.appendLine("  播種期間: ${entry.sowing_start_date} ～ ${entry.sowing_end_date}")
            }
        }
        
        return content.toString().trim()
    }
    
    /**
     * 種が対象月におすすめかどうかを判定
     */
    private fun isSeedRecommendedForMonth(
        entry: com.example.seedstockkeeper6.model.CalendarEntry,
        targetMonth: Int,
        targetYear: Int
    ): Boolean {
        val dataProcessor = com.example.seedstockkeeper6.service.NotificationDataProcessor()
        val sowingStartMonth = dataProcessor.parseMonthFromDate(entry.sowing_start_date)
        val sowingEndMonth = dataProcessor.parseMonthFromDate(entry.sowing_end_date)
        
        return sowingStartMonth != null && sowingEndMonth != null && 
               dataProcessor.isMonthInRange(targetMonth, sowingStartMonth, sowingEndMonth)
    }
    
    /**
     * 週次通知用のユーザー種情報をフォーマット
     */
    private fun formatUserSeedsForWeeklyPrompt(seeds: List<SeedPacket>): String {
        android.util.Log.d("NotificationPromptGenerator", "formatUserSeedsForWeeklyPrompt開始 - 種数: ${seeds.size}")
        if (seeds.isEmpty()) {
            android.util.Log.d("NotificationPromptGenerator", "種が空のため、デフォルトメッセージを返す")
            return "登録された種はありません。"
        }
        
        android.util.Log.d("NotificationPromptGenerator", "formatUserSeedsForWeeklyPrompt - 週番号: ${getWeekNumber(java.time.LocalDate.now())}")
        
        val currentDate = java.time.LocalDate.now()
        val currentWeek = getWeekNumber(currentDate)
        val currentMonth = currentDate.monthValue
        val currentYear = currentDate.year
        
        // 期限間近の種を抽出（週に応じて条件を変更）
        val urgentSeeds = seeds.filter { seed ->
            val isExpiringThisMonth = seed.calendar?.any { entry ->
                if (entry.expirationYear > 0 && entry.expirationMonth > 0) {
                    val expirationDate = java.time.LocalDate.of(entry.expirationYear, entry.expirationMonth, 1)
                    expirationDate.monthValue == currentMonth && expirationDate.year == currentYear
                } else {
                    false
                }
            } ?: false
            
            val isExpiringNextMonth = seed.calendar?.any { entry ->
                if (entry.expirationYear > 0 && entry.expirationMonth > 0) {
                    val expirationDate = java.time.LocalDate.of(entry.expirationYear, entry.expirationMonth, 1)
                    val nextMonth = if (currentMonth == 12) 1 else currentMonth + 1
                    val nextYear = if (currentMonth == 12) currentYear + 1 else currentYear
                    expirationDate.monthValue == nextMonth && expirationDate.year == nextYear
                } else {
                    false
                }
            } ?: false
            
            // 1週目・2週目：当月期限切れの種のみ
            // 3週目以降：当月・翌月期限切れの種
            if (currentWeek <= 2) {
                isExpiringThisMonth
            } else {
                isExpiringThisMonth || isExpiringNextMonth
            }
        }
        
        // 今週まける種を抽出
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
        
        val content = StringBuilder()
        
        // 今週まける種
        if (relevantSeeds.isNotEmpty()) {
            content.appendLine("🌱 今週まける種:")
            relevantSeeds.forEach { seed ->
                content.appendLine("・${seed.productName} (${seed.variety})")
                content.appendLine("  科: ${seed.family}")
                content.appendLine("  播種期間: ${seed.calendar?.firstOrNull()?.sowing_start_date} ～ ${seed.calendar?.firstOrNull()?.sowing_end_date}")
                content.appendLine("  収穫期間: ${seed.calendar?.firstOrNull()?.harvest_start_date} ～ ${seed.calendar?.firstOrNull()?.harvest_end_date}")
                
                val expirationInfo = if (seed.expirationYear > 0 && seed.expirationMonth > 0) {
                    "有効期限: ${seed.expirationYear}年${seed.expirationMonth}月"
                } else {
                    // 種の有効期限がない場合のみカレンダーエントリを使用
                    seed.calendar?.firstOrNull()?.let { entry ->
                        if (entry.expirationYear > 0 && entry.expirationMonth > 0) {
                            "有効期限: ${entry.expirationYear}年${entry.expirationMonth}月"
                        } else {
                            "有効期限: 未設定"
                        }
                    } ?: "有効期限: 未設定"
                }
                content.appendLine("  $expirationInfo")
                
                if (seed.companionPlants.isNotEmpty()) {
                    content.appendLine("  コンパニオンプランツ: ${formatCompanionPlants(seed.companionPlants)}")
                }
            }
            content.appendLine()
        }
        
        // 期限間近の種
        if (urgentSeeds.isNotEmpty()) {
            content.appendLine("⚠️ 期限間近の種:")
            urgentSeeds.forEach { seed ->
                // デバッグログ: 種情報の有効期限
                android.util.Log.d("NotificationPromptGenerator", "週次通知デバッグ - 種名: ${seed.productName}")
                android.util.Log.d("NotificationPromptGenerator", "週次通知デバッグ - 種の有効期限: ${seed.expirationYear}年${seed.expirationMonth}月")
                
                // カレンダーエントリの有効期限を確認
                seed.calendar?.forEachIndexed { index, entry ->
                    android.util.Log.d("NotificationPromptGenerator", "週次通知デバッグ - カレンダーエントリ$index: ${entry.expirationYear}年${entry.expirationMonth}月")
                }
                
                content.appendLine("・${seed.productName} (${seed.variety})")
                content.appendLine("  科: ${seed.family}")
                
                val expirationInfo = if (seed.expirationYear > 0 && seed.expirationMonth > 0) {
                    android.util.Log.d("NotificationPromptGenerator", "週次通知デバッグ - 使用する有効期限: 種の有効期限 ${seed.expirationYear}年${seed.expirationMonth}月")
                    "有効期限: ${seed.expirationYear}年${seed.expirationMonth}月"
                } else {
                    // 種の有効期限がない場合のみカレンダーエントリを使用
                    seed.calendar?.firstOrNull()?.let { entry ->
                        android.util.Log.d("NotificationPromptGenerator", "週次通知デバッグ - 使用する有効期限: カレンダーエントリ ${entry.expirationYear}年${entry.expirationMonth}月")
                        if (entry.expirationYear > 0 && entry.expirationMonth > 0) {
                            "有効期限: ${entry.expirationYear}年${entry.expirationMonth}月"
                        } else {
                            "有効期限: 未設定"
                        }
                    } ?: run {
                        android.util.Log.d("NotificationPromptGenerator", "週次通知デバッグ - 有効期限情報なし")
                        "有効期限: 未設定"
                    }
                }
                content.appendLine("  $expirationInfo")
            }
            content.appendLine()
        }
        
        // おすすめの種情報を追加（週番号に応じてタイトルを変更）
        val recommendedTitle = if (currentWeek <= 2) "🎯 今月のおすすめ" else "🔥 来月のおすすめ"
        android.util.Log.d("NotificationPromptGenerator", "おすすめの種タイトル: $recommendedTitle (週番号: $currentWeek)")
        content.appendLine("$recommendedTitle:")
        
        // ユーザーの種情報からおすすめの種を選出（簡単な例として、期限間近でない種を選出）
        val recommendedSeeds = seeds.filter { seed ->
            !urgentSeeds.contains(seed) && relevantSeeds.contains(seed)
        }.take(3)
        
        android.util.Log.d("NotificationPromptGenerator", "おすすめの種選出結果: ${recommendedSeeds.size}個")
        recommendedSeeds.forEachIndexed { index, seed ->
            android.util.Log.d("NotificationPromptGenerator", "おすすめの種$index: ${seed.productName} (${seed.variety})")
        }
        
        if (recommendedSeeds.isNotEmpty()) {
            recommendedSeeds.forEach { seed ->
                content.appendLine("・${seed.productName} (${seed.variety})")
                content.appendLine("  科: ${seed.family}")
                content.appendLine("  播種期間: ${seed.calendar?.firstOrNull()?.sowing_start_date} ～ ${seed.calendar?.firstOrNull()?.sowing_end_date}")
            }
        } else {
            content.appendLine("  おすすめの種はありません")
        }
        
        val result = content.toString().trim()
        android.util.Log.d("NotificationPromptGenerator", "formatUserSeedsForWeeklyPrompt完了 - 生成されたプロンプト: $result")
        return result
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
