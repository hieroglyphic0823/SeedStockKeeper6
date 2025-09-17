package com.example.seedstockkeeper6.service

import android.util.Log
import com.example.seedstockkeeper6.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class GeminiNotificationService {
    
    private var generativeModel: GenerativeModel? = null
    
    init {
        try {
            // runGeminiOcrと同じAPIキーとモデルを使用
            val apiKey = BuildConfig.GEMINI_API_KEY
            
            if (apiKey.isNotEmpty()) {
                generativeModel = GenerativeModel(
                    modelName = "gemini-2.5-flash", // runGeminiOcrと同じモデル
                    apiKey = apiKey
                )
            } else {
                Log.w("GeminiNotiService", "GeminiAPIキーが設定されていません。デフォルト内容を使用します。")
            }
        } catch (e: Exception) {
            Log.e("GeminiNotiService", "GeminiAPI初期化に失敗", e)
        }
    }
    
    /**
     * 農園主に応じた助さんの口調を取得
     */
    private fun getFarmOwnerTone(farmOwner: String, customFarmOwner: String = "", monthName: String = "今月"): String {
        return when (farmOwner) {
            "水戸黄門" -> """
                あなたは水戸黄門の登場人物の助さんとして、ご隠居様（水戸黄門）に話しかける口調で話してください：
                - 尊敬と忠誠心にあふれ、非常に丁寧で畏まった言葉遣い
                - 「ご隠居様、かしこまりました」「それがし、ただいま参上いたしました」
                - 「〜でござる」「〜でございます」などの丁寧語を使用
                - 「このたびは」「さて」「ついては」などの格式高い表現
                - 農業の知恵を教える師匠のような口調で、常に敬意を払う
                - 例：「ご隠居様、このたびは$monthName の種まきについてお教えするでござる」
            """.trimIndent()
            
            "お銀" -> """
                あなたは水戸黄門の登場人物の助さんとして、お銀に話しかける口調で話してください：
                - 親しみと信頼が感じられる、丁寧でありながらも少し柔らかな口調
                - 「お銀、ご苦労」「いかがなされたか、お銀」
                - 共に旅をする仲間として、対等の関係に近い接し方
                - 女性に対する優しさや、忍びとして互いを認め合う敬意
                - 実用的で分かりやすい説明
                - 例：「お銀、$monthName の種まきについて相談があるんだが」
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
     * 月次通知の内容を生成
     */
    suspend fun generateMonthlyNotificationContent(
        region: String,
        prefecture: String,
        seedInfoUrl: String,
        currentMonth: Int,
        userSeeds: List<com.example.seedstockkeeper6.model.SeedPacket> = emptyList(),
        farmOwner: String,
        customFarmOwner: String = ""
    ): String = withContext(Dispatchers.IO) {
        try {
            Log.d("GeminiNotiService", "月次通知生成開始 - farmOwner: $farmOwner, customFarmOwner: $customFarmOwner")
            val seedInfoContent = fetchSeedInfoFromUrl(seedInfoUrl)
            val monthName = getMonthName(currentMonth)
            val userSeedsInfo = formatUserSeedsForPrompt(userSeeds, currentMonth)
            val farmOwnerTone = getFarmOwnerTone(farmOwner, customFarmOwner, monthName)
            Log.d("GeminiNotiService", "生成された農園主トーン: $farmOwnerTone")
            
            val prompt = """
                あなたは水戸黄門の登場人物の助さんです。以下の情報を基に、農園主へ月次通知する内容を生成してください。

                【基本情報】
                - 地域: $region
                - 県: $prefecture
                - 現在の月: $monthName
                - 種情報URL: $seedInfoUrl
                - 農園主: $farmOwner${if (farmOwner == "その他" && customFarmOwner.isNotEmpty()) " ($customFarmOwner)" else ""}

                【助さんの口調・キャラクター設定】
                $farmOwnerTone

                【種情報URLの内容】
                $seedInfoContent

                【ユーザーが登録している種の情報】
                $userSeedsInfo

                【生成する通知内容の要件】
                1. 今月（$monthName）に種まきできる野菜のリスト（ユーザー登録種も含む）
                2. 地域（$region）と県（$prefecture）に適した季節のおすすめ品種
                3. まき時が今月で終わる種への注意喚起（ユーザー登録種も含む）
                   - 今月が播種期間の終了月の種は「まき時終了間近」として表示
                4. ユーザーが登録している種で今月まき時のものがあれば優先的に表示
                5. 実用的で分かりやすい内容
                6. 絵文字を使って見やすくする
                7. 各項目は簡潔に（最大3-5種類程度）
                8. 上記で設定した助さんの口調・キャラクターで話す
                9. ユーザー登録種とそうでない種を明確に区別する

                【出力形式】
                🌱 今月まき時の種:
                
                📦 今月まき時の登録種:
                • [品種名] ([種類]) - 発芽率: [発芽率]%, 有効期限: [年月]
                
                🌿 おすすめの種:
                • [品種名] ([種類]) - 今がまき時です

                🌟 季節のおすすめ:
                • [おすすめ内容]
                • [おすすめ内容]

                ⚠️ まき時終了間近:
                
                📦 まき時終了間近の登録種:
                • [品種名] ([種類]) - 発芽率: [発芽率]%, 有効期限: [年月] - 今月でまき時終了！
                
                🌿 その他の種:
                • [品種名] ([種類]) - 今月でまき時終了！

                上記の形式で、設定した助さんの口調・キャラクターで、ユーザーの登録種を優先的に含み、登録種とそうでない種を明確に区別した実用的で分かりやすい通知内容を生成してください。
            """.trimIndent()
            
            if (generativeModel != null) {
                try {
                    val response = generativeModel?.generateContent(prompt)
                    response?.text ?: getDefaultMonthlyContent(monthName)
                } catch (apiException: Exception) {
                    Log.w("GeminiNotiService", "GeminiAPI呼び出しに失敗（過負荷等）: ${apiException.message}")
                    getDefaultMonthlyContent(monthName)
                }
            } else {
                Log.w("GeminiNotiService", "GeminiAPIが利用できません。デフォルト内容を返します。")
                getDefaultMonthlyContent(monthName)
            }
            
        } catch (e: Exception) {
            Log.e("GeminiNotiService", "月次通知内容生成に失敗", e)
            getDefaultMonthlyContent(getMonthName(currentMonth))
        }
    }
    
    /**
     * 週次通知の内容を生成
     */
    suspend fun generateWeeklyNotificationContent(
        region: String,
        prefecture: String,
        seedInfoUrl: String,
        userSeeds: List<com.example.seedstockkeeper6.model.SeedPacket> = emptyList(),
        farmOwner: String,
        customFarmOwner: String = ""
    ): String = withContext(Dispatchers.IO) {
        try {
            Log.d("GeminiNotiService", "週次通知生成開始 - farmOwner: $farmOwner, customFarmOwner: $customFarmOwner")
            val seedInfoContent = fetchSeedInfoFromUrl(seedInfoUrl)
            val userSeedsInfo = formatUserSeedsForWeeklyPrompt(userSeeds)
            val farmOwnerTone = getFarmOwnerTone(farmOwner, customFarmOwner, "今週")
            Log.d("GeminiNotiService", "生成された農園主トーン: $farmOwnerTone")
            
            val prompt = """
                あなたは水戸黄門の登場人物の助さんです。以下の情報を基に、農園主へ週次リマインダー通知する内容を生成してください。

                【基本情報】
                - 地域: $region
                - 県: $prefecture
                - 種情報URL: $seedInfoUrl
                - 農園主: $farmOwner${if (farmOwner == "その他" && customFarmOwner.isNotEmpty()) " ($customFarmOwner)" else ""}

                【助さんの口調・キャラクター設定】
                $farmOwnerTone

                【種情報URLの内容】
                $seedInfoContent

                【ユーザーが登録している種の情報】
                $userSeedsInfo

                【生成する通知内容の要件】
                1. まき時終了の2週間前の種のリスト（ユーザー登録種も含む）
                   - 播種期間の終了が2週間以内の種を対象とする
                2. 「土づくりすれば間に合う」という励ましのメッセージ
                3. 地域（$region）と県（$prefecture）に適した内容
                4. ユーザーが登録している種でまき時終了間近のものがあれば優先的に表示
                5. 実用的で分かりやすい内容
                6. 絵文字を使って見やすくする
                7. 各項目は簡潔に（最大3-5種類程度）
                8. 上記で設定した助さんの口調・キャラクターで話す
                9. ユーザー登録種とそうでない種を明確に区別する

                【出力形式】
                ⏰ まき時終了の2週間前の種があります:

                📦 まき時終了間近の登録種:
                • [品種名] ([種類]) - 発芽率: [発芽率]%, 有効期限: [年月]
                  土づくりすれば間に合います！

                🌿 その他の種:
                • [品種名] ([種類])
                  土づくりすれば間に合います！

                上記の形式で、設定した助さんの口調・キャラクターで、ユーザーの登録種を優先的に含み、登録種とそうでない種を明確に区別した励ましのメッセージを含む実用的な通知内容を生成してください。
            """.trimIndent()
            
            if (generativeModel != null) {
                try {
                    val response = generativeModel?.generateContent(prompt)
                    response?.text ?: getDefaultWeeklyContent()
                } catch (apiException: Exception) {
                    Log.w("GeminiNotiService", "GeminiAPI呼び出しに失敗（過負荷等）: ${apiException.message}")
                    getDefaultWeeklyContent()
                }
            } else {
                Log.w("GeminiNotiService", "GeminiAPIが利用できません。デフォルト内容を返します。")
                getDefaultWeeklyContent()
            }
            
        } catch (e: Exception) {
            Log.e("GeminiNotiService", "週次通知内容生成に失敗", e)
            getDefaultWeeklyContent()
        }
    }
    
    /**
     * URLから種情報を取得
     */
    private suspend fun fetchSeedInfoFromUrl(url: String): String = withContext(Dispatchers.IO) {
        try {
            if (url.isEmpty()) {
                return@withContext "種情報URLが設定されていません"
            }
            
            val urlObj = URL(url)
            val connection = urlObj.openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val content = connection.getInputStream().bufferedReader().use { it.readText() }
            
            // HTMLからテキストを抽出（簡易版）
            val textContent = content
                .replace(Regex("<[^>]*>"), " ") // HTMLタグを削除
                .replace(Regex("\\s+"), " ") // 複数の空白を1つに
                .trim()
                .take(2000) // 長すぎる場合は切り詰め
            
            Log.d("GeminiNotiService", "種情報URLから取得した内容: ${textContent.take(200)}...")
            textContent
            
        } catch (e: Exception) {
            Log.e("GeminiNotiService", "種情報URL取得に失敗: $url", e)
            "種情報URLの取得に失敗しました"
        }
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
            else -> "${month}月"
        }
    }
    
    /**
     * デフォルトの月次通知内容
     */
    private fun getDefaultMonthlyContent(monthName: String): String {
        return """🌱 今月($monthName)まき時の種:

📦 あなたの登録種:
• 恋むすめ - 有効期限: 2026年10月

🌿 おすすめの種:
• レタス - 今がまき時です
• キャベツ - 今がまき時です

🌟 季節のおすすめ:
• $monthName は種まきの最適期です
• 地域に適した品種を選びましょう
• 土づくりを忘れずに！

⚠️ まき時終了間近:

📦 あなたの登録種:
• 特にありません

🌿 その他の種:
• 特にありません

💡 ヒント: 種まき前に土の準備をしっかり行いましょう

※ Gemini APIが一時的に利用できないため、デフォルト内容を表示しています。"""
    }
    
    /**
     * デフォルトの週次通知内容
     */
    private fun getDefaultWeeklyContent(): String {
        return """⏰ まき時終了の2週間前の種があります:

📦 あなたの登録種:
• 恋むすめ - 有効期限: 2026年10月
  土づくりすれば間に合います！

🌿 その他の種:
• レタス
  土づくりすれば間に合います！

• キャベツ
  土づくりすれば間に合います！

💪 まだ間に合います！準備を始めましょう

※ Gemini APIが一時的に利用できないため、デフォルト内容を表示しています。"""
    }
    
    /**
     * ユーザーの種データを月次通知用プロンプトにフォーマット
     */
    private fun formatUserSeedsForPrompt(seeds: List<com.example.seedstockkeeper6.model.SeedPacket>, currentMonth: Int): String {
        if (seeds.isEmpty()) {
            return "ユーザーはまだ種を登録していません。"
        }
        
        android.util.Log.d("GeminiNotiService", "formatUserSeedsForPrompt開始 - currentMonth: $currentMonth, 全seeds数: ${seeds.size}")
        
        val seedsThisMonth = mutableListOf<String>()
        val seedsEndingThisMonth = mutableListOf<String>()
        var relevantSeedsCount = 0
        
        seeds.forEach { seed ->
            var isRelevant = false
            
            // 播種期間のCalendarEntryを探す（種リスト画面と同じロジック）
            seed.calendar.forEach { entry ->
                if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
                    try {
                        val startMonth = entry.sowing_start_date.split("-")[1].toInt()
                        val endMonth = entry.sowing_end_date.split("-")[1].toInt()
                        
                        // 今月が播種期間内かチェック（種リスト画面と同じロジック）
                        if (startMonth <= currentMonth && endMonth >= currentMonth) {
                            android.util.Log.d("GeminiNotiService", "今月まき時の種発見: ${seed.productName}")
                            val displayName = if (seed.productName.isNotEmpty()) seed.productName else seed.variety
                            seedsThisMonth.add("$displayName - 有効期限: ${seed.expirationYear}年${seed.expirationMonth}月, 播種期間: ${startMonth}月〜${endMonth}月")
                            isRelevant = true
                        }
                        
                        // 今月が播種期間の終了月かチェック（まき時終了間近）
                        if (currentMonth == endMonth) {
                            android.util.Log.d("GeminiNotiService", "まき時終了間近の種発見: ${seed.productName}")
                            val displayName = if (seed.productName.isNotEmpty()) seed.productName else seed.variety
                            seedsEndingThisMonth.add("$displayName - 有効期限: ${seed.expirationYear}年${seed.expirationMonth}月, 播種期間終了: ${endMonth}月")
                            isRelevant = true
                        }
                    } catch (e: Exception) {
                        // 日付解析エラーの場合はスキップ
                        android.util.Log.w("GeminiNotiService", "日付解析エラー - seed: ${seed.productName}, startDate: ${entry.sowing_start_date}, endDate: ${entry.sowing_end_date}", e)
                    }
                }
            }
            
            if (isRelevant) {
                relevantSeedsCount++
            }
        }
        
        val result = StringBuilder()
        result.appendLine("ユーザーが登録している種の情報（今月関連のみ）:")
        
        if (seedsThisMonth.isNotEmpty()) {
            result.appendLine("今月(${currentMonth}月)に種まきできる登録種:")
            seedsThisMonth.forEach { seed ->
                result.appendLine("- $seed")
            }
        }
        
        if (seedsEndingThisMonth.isNotEmpty()) {
            result.appendLine("今月(${currentMonth}月)で播種期間が終了する登録種（まき時終了間近）:")
            seedsEndingThisMonth.forEach { seed ->
                result.appendLine("- $seed")
            }
        }
        
        if (seedsThisMonth.isEmpty() && seedsEndingThisMonth.isEmpty()) {
            result.appendLine("今月に関連する登録種はありません。")
        }
        
        android.util.Log.d("GeminiNotiService", "formatUserSeedsForPrompt結果 - 今月関連種: ${relevantSeedsCount}件/${seeds.size}件, 今月まき時: ${seedsThisMonth.size}件, まき時終了間近: ${seedsEndingThisMonth.size}件")
        
        return result.toString()
    }
    
    /**
     * ユーザーの種データを週次通知用プロンプトにフォーマット
     */
    private fun formatUserSeedsForWeeklyPrompt(seeds: List<com.example.seedstockkeeper6.model.SeedPacket>): String {
        if (seeds.isEmpty()) {
            return "ユーザーはまだ種を登録していません。"
        }
        
        val currentDate = java.util.Calendar.getInstance()
        val currentMonth = currentDate.get(java.util.Calendar.MONTH) + 1
        val currentDay = currentDate.get(java.util.Calendar.DAY_OF_MONTH)
        val seedsEndingSoon = mutableListOf<String>()
        var relevantSeedsCount = 0
        
        android.util.Log.d("GeminiNotiService", "formatUserSeedsForWeeklyPrompt開始 - currentMonth: $currentMonth, currentDay: $currentDay, 全seeds数: ${seeds.size}")
        
        seeds.forEach { seed ->
            var isRelevant = false
            
            seed.calendar.forEach { calendarEntry ->
                if (calendarEntry.sowing_end_date.isNotEmpty()) {
                    try {
                        val sowingEndMonth = calendarEntry.sowing_end_date.split("-")[1].toInt()
                        
                        // まき時終了の2週間前の条件をチェック
                        if (sowingEndMonth == currentMonth && currentDay >= 15) {
                            // 今月の15日以降で、今月が播種期間の終了月の場合
                            android.util.Log.d("GeminiNotiService", "2週間前の種発見（今月終了）: ${seed.productName}")
                            val displayName = if (seed.productName.isNotEmpty()) seed.productName else seed.variety
                            seedsEndingSoon.add("$displayName - 有効期限: ${seed.expirationYear}年${seed.expirationMonth}月, 播種期間終了: ${sowingEndMonth}月")
                            isRelevant = true
                        } else if (sowingEndMonth == currentMonth + 1 && currentDay <= 15) {
                            // 来月が播種期間の終了月で、今月の15日以前の場合
                            android.util.Log.d("GeminiNotiService", "2週間前の種発見（来月終了）: ${seed.productName}")
                            val displayName = if (seed.productName.isNotEmpty()) seed.productName else seed.variety
                            seedsEndingSoon.add("$displayName - 有効期限: ${seed.expirationYear}年${seed.expirationMonth}月, 播種期間終了: ${sowingEndMonth}月")
                            isRelevant = true
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("GeminiNotiService", "日付解析エラー - seed: ${seed.productName}, endDate: ${calendarEntry.sowing_end_date}", e)
                    }
                }
            }
            
            if (isRelevant) {
                relevantSeedsCount++
            }
        }
        
        val result = StringBuilder()
        result.appendLine("ユーザーが登録している種の情報（2週間前関連のみ）:")
        
        if (seedsEndingSoon.isNotEmpty()) {
            result.appendLine("まき時終了の2週間前の登録種:")
            seedsEndingSoon.forEach { seed ->
                result.appendLine("- $seed")
            }
        } else {
            result.appendLine("まき時終了の2週間前の登録種はありません。")
        }
        
        android.util.Log.d("GeminiNotiService", "formatUserSeedsForWeeklyPrompt結果 - 2週間前関連種: ${relevantSeedsCount}件/${seeds.size}件, 対象種: ${seedsEndingSoon.size}件")
        
        return result.toString()
    }
    
    /**
     * 日付文字列から月を抽出
     */
    private fun parseMonthFromDate(dateString: String): Int? {
        return try {
            // "2024-03-15" のような形式から月を抽出
            val parts = dateString.split("-")
            if (parts.size >= 2) {
                parts[1].toInt()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 指定月が範囲内かチェック
     */
    private fun isMonthInRange(targetMonth: Int, startMonth: Int, endMonth: Int): Boolean {
        return if (startMonth <= endMonth) {
            // 通常の範囲（例: 3月〜6月）
            targetMonth in startMonth..endMonth
        } else {
            // 年をまたぐ範囲（例: 11月〜2月）
            targetMonth >= startMonth || targetMonth <= endMonth
        }
    }
}
