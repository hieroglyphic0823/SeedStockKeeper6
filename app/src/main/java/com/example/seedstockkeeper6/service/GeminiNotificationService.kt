package com.example.seedstockkeeper6.service

import android.util.Log
import com.example.seedstockkeeper6.BuildConfig
import com.example.seedstockkeeper6.utils.DateUtils
import com.example.seedstockkeeper6.utils.JapaneseMonthUtils
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
     * 月次通知の内容を生成
     */
    suspend fun generateMonthlyNotificationContent(
        region: String,
        prefecture: String,
        seedInfoUrl: String,
        currentMonth: Int,
        userSeeds: List<com.example.seedstockkeeper6.model.SeedPacket> = emptyList(),
        farmOwner: String,
        customFarmOwner: String = "",
        userSettings: Map<String, String> = emptyMap()
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
                - 農場名: ${userSettings["farmName"] ?: "菜園"}

                【助さんの口調・キャラクター設定】
                $farmOwnerTone

                【種情報URLの内容】
                $seedInfoContent

                【ユーザーが登録している種の情報】
                $userSeedsInfo

                【生成する通知内容の要件】
                1. 農園主に農場名を含めて呼びかける（例：「お銀、●●農園の睦月の種まきについて」）
                2. ユーザーが登録している種で今月まき時のものがあれば優先的に表示
                3. ユーザー登録種のうちまき時が今月で終わる種への注意喚起
                   - 今月が播種期間の終了月の種を「まき時終了間近」として表示
                4. まき時終了まで2週間以上ある種には「今から土づくりすれば間に合う」という励ましのメッセージを追加
                   - 土づくり時間がある種に対して積極的に励ましの言葉をかける
                5. 登録種のコンパニオンプランツ情報を活用して、今月まけるコンパニオンプランツを提案
                   - パッケージに記載されている場合は、それをもとに植物名と効果を記載
                   - パッケージに記載がない場合でも、該当植物に対して一般的または推奨される代表的なコンパニオンプランツとその効果を、必ず1つ以上含めて記載
                   - 効果コード（01-13, 99）を厳密に使用し、該当が明確でない場合は「99」を使用
                6. ユーザーが登録している種以外の今月（$monthName）に種まきできる野菜のリスト（ユーザー登録種以外）
                7. 地域（$region）と県（$prefecture）に適した季節のおすすめ品種
                8. 実用的で分かりやすい内容
                9. 絵文字を使って見やすくする
                10. 各項目は簡潔に（最大3-5種類程度）
                11. 上記で設定した助さんの口調・キャラクターで話す
                12. ユーザー登録種とそうでない種を明確に区別する

                【コンパニオンプランツ効果コード】
                効果のフィールド（"effects"）は、以下の2桁のコードを**厳密に**使用してください。該当が明確でない場合は「99」を使用してください：
                - "01": 害虫予防
                - "02": 病気予防
                - "03": 生育促進
                - "04": 空間活用
                - "05": 風味向上
                - "06": 土壌改善
                - "07": 受粉促進
                - "08": 雑草抑制
                - "09": 景観美化
                - "10": 水分保持
                - "11": pH調整
                - "12": 効率UP
                - "13": 収量安定
                - "99": その他

                【出力形式】
                🌱 登録種について:
                
                📦 今月まき時の登録種:
                • [商品名] ([品種名]) - 播種期間: [月]、有効期限: [年月]

                📦 まき時終了間近の登録種:
                • [商品名] ([品種名]) - 播種期間: [月]、 有効期限: [年月] 
                
                🌿 登録種とコンパニオンプランツとなる今月まける種:
                • [品種名] ［コンパニオンプランツ効果］ - 播種期間:[月] 

                🌿 登録種以外のおすすめの種:
                • [品種名]  - 播種期間:[月] 

                🌟 季節の畑情報:
                • [季節の畑情報]
               
                上記の形式で、設定した助さんの口調・キャラクターで、ユーザーの登録種を優先的に含み、登録種とそうでない種を明確に区別した実用的で分かりやすい通知内容を生成してください。
            """.trimIndent()
            
            if (generativeModel != null) {
                try {
                    val response = generativeModel?.generateContent(prompt)
                    response?.text ?: getDefaultMonthlyContent(monthName)
                } catch (apiException: Exception) {
                    Log.w("GeminiNotiService", "GeminiAPI呼び出しに失敗（過負荷等）: ${apiException.message}")
                    Log.w("GeminiNotiService", "API例外の詳細: ${apiException.javaClass.simpleName}")
                    if (apiException.message?.contains("overloaded") == true || apiException.message?.contains("503") == true) {
                        Log.w("GeminiNotiService", "API過負荷のため、通知を作成できません")
                        "API過負荷のため通知を作成できません。しばらく時間をおいてから再度お試しください。"
                    } else {
                        getDefaultMonthlyContent(monthName)
                    }
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
        customFarmOwner: String = "",
        userSettings: Map<String, String> = emptyMap()
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
                - 農場名: ${userSettings["farmName"] ?: "菜園"}

                【助さんの口調・キャラクター設定】
                $farmOwnerTone

                【種情報URLの内容】
                $seedInfoContent

                【ユーザーが登録している種の情報】
                $userSeedsInfo

                【生成する通知内容の要件】
                1. 農園主に農場名を含めて呼びかける（例：「お銀、●●農園のまき時終了間近の種について」）
                2. まき時終了の2週間前の種のリスト（ユーザー登録種も含む）
                   - 播種期間の終了が2週間以内の種を対象とする
                3. 「土づくりすれば間に合う」という励ましのメッセージ
                4. 地域（$region）と県（$prefecture）に適した内容
                5. ユーザーが登録している種でまき時終了間近のものがあれば優先的に表示
                6. 実用的で分かりやすい内容
                7. 絵文字を使って見やすくする
                8. 各項目は簡潔に（最大3-5種類程度）
                9. 上記で設定した助さんの口調・キャラクターで話す
                10. ユーザー登録種とそうでない種を明確に区別する

                【出力形式】
                ⏰ まき時終了の2週間前の種があります:

                📦 まき時終了間近の登録種:
                • [商品名] ([品種名]) -  有効期限: [年月]
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
                    Log.w("GeminiNotiService", "API例外の詳細: ${apiException.javaClass.simpleName}")
                    if (apiException.message?.contains("overloaded") == true || apiException.message?.contains("503") == true) {
                        Log.w("GeminiNotiService", "API過負荷のため、通知を作成できません")
                        "API過負荷のため通知を作成できません。しばらく時間をおいてから再度お試しください。"
                    } else {
                        getDefaultWeeklyContent()
                    }
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
     * 月次通知のタイトルを生成
     */
    suspend fun generateMonthlyNotificationTitle(
        currentMonth: Int,
        farmOwner: String,
        customFarmOwner: String = ""
    ): String = withContext(Dispatchers.IO) {
        try {
            val japaneseMonth = JapaneseMonthUtils.getJapaneseMonthName(currentMonth)
            val japaneseMonthShort = JapaneseMonthUtils.getJapaneseMonthNameShort(currentMonth)
            val seasonalGreeting = JapaneseMonthUtils.getSeasonalGreeting(currentMonth)
            val sowingKeyword = JapaneseMonthUtils.getSowingKeyword(currentMonth)
            
            val actualFarmOwner = if (farmOwner == "その他" && customFarmOwner.isNotEmpty()) customFarmOwner else farmOwner
            
            val prompt = """
                あなたは水戸黄門の世界観で、農園主への月次通知タイトルを生成してください。
                
                【基本情報】
                - 現在の月: ${currentMonth}月
                - 和風月名: $japaneseMonth
                - 和風月名（短縮）: $japaneseMonthShort
                - 季節の候: $seasonalGreeting
                - 種まきキーワード: $sowingKeyword
                - 農園主: $actualFarmOwner
                
                【キャラクター別のタイトル案】
                
                📜 水戸黄門 宛て
                「◯◯月、◯◯の候にて――お出ましの時期にございます」
                例（10月）：「神無月、種まきの候にて――お出ましの時期にございます」
                風格ある文体で、黄門様への報告っぽく。
                
                🌸 お銀 宛て
                「◯◯月の風に乗せて――◯◯の候、菜園より」
                例（3月）：「弥生の風に乗せて――春の種まきの候、菜園より」
                少しやわらかくて風流な感じ。お銀の気品を意識。
                
                🍡 八兵衛 宛て
                「おい八、◯◯月だぞ！◯◯は始めどきだ」
                例（5月）：「おい八、皐月だぞ！きゅうりの種は始めどきだ」
                ちょっと砕けたフレンドリー調で、八兵衛への呼びかけに。
                
                🔔 汎用タイトル案（誰向けでも使える系）
                「長月の便り：秋の種をお忘れなく」
                「文月の候、夏野菜の収穫を楽しみに」
                「霜月の候、冬支度はいかがですか」
                「卯月便り：春まきの季節がやってきました」
                
                【要件】
                1. 和風月名（$japaneseMonth）を必ず含める
                2. 農園主（$actualFarmOwner）に適したキャラクターの口調を使用
                3. 季節感と種まきのタイミングを表現
                4. 水戸黄門の世界観に合った格調高い文体
                5. 30文字以内で簡潔に
                6. 絵文字は使用しない
                
                上記の要件に従って、農園主に適した月次通知タイトルを1つ生成してください。
            """.trimIndent()
            
            if (generativeModel != null) {
                try {
                    val response = generativeModel?.generateContent(prompt)
                    response?.text?.trim() ?: getDefaultMonthlyTitle(currentMonth, actualFarmOwner)
                } catch (apiException: Exception) {
                    Log.w("GeminiNotiService", "月次通知タイトル生成に失敗: ${apiException.message}")
                    getDefaultMonthlyTitle(currentMonth, actualFarmOwner)
                }
            } else {
                Log.w("GeminiNotiService", "GeminiAPIが利用できません。デフォルトタイトルを返します。")
                getDefaultMonthlyTitle(currentMonth, actualFarmOwner)
            }
            
        } catch (e: Exception) {
            Log.e("GeminiNotiService", "月次通知タイトル生成に失敗", e)
            getDefaultMonthlyTitle(currentMonth, farmOwner)
        }
    }
    
    /**
     * デフォルトの月次通知タイトル
     */
    private fun getDefaultMonthlyTitle(currentMonth: Int, farmOwner: String): String {
        val japaneseMonth = JapaneseMonthUtils.getJapaneseMonthNameShort(currentMonth)
        val seasonalGreeting = JapaneseMonthUtils.getSeasonalGreeting(currentMonth)
        
        return when (farmOwner) {
            "水戸黄門" -> "$japaneseMonth、${seasonalGreeting}にて――お出ましの時期にございます"
            "お銀" -> "${japaneseMonth}の風に乗せて――${seasonalGreeting}、菜園より"
            "八兵衛" -> "おい八、${japaneseMonth}だぞ！種まきは始めどきだ"
            else -> "${japaneseMonth}の便り：${seasonalGreeting}をお忘れなく"
        }
    }

    /**
     * デフォルトの月次通知内容
     */
    private fun getDefaultMonthlyContent(monthName: String): String {
        return """🌱 今月($monthName)まき時の種:

📦 あなたの登録種:
• 恋むすめ (ニンジン) - 有効期限: 2026年10月, 播種期間: 8月上旬〜9月下旬

🌿 登録種とコンパニオンプランツとなる今月まける種:
• マリーゴールド ［害虫予防(01)］ - 播種期間: 3月〜5月
• バジル ［風味向上(05)］ - 播種期間: 4月〜6月

🌿 登録種以外のおすすめの種:
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

💪 土づくり時間がある登録種:
• 春菊 (中葉春菊) - 有効期限: 2026年10月, 播種期間: 8月下旬〜9月中旬
  今から土づくりすれば間に合います！

💡 ヒント: 種まき前に土の準備をしっかり行いましょう

※ Gemini APIが一時的に利用できないため、デフォルト内容を表示しています。"""
    }
    
    /**
     * デフォルトの週次通知内容
     */
    private fun getDefaultWeeklyContent(): String {
        return """⏰ まき時終了の2週間前の種があります:

📦 あなたの登録種:
• 恋むすめ (ニンジン) - 有効期限: 2026年10月, 播種期間: 8月上旬〜9月下旬
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
     * コンパニオンプランツの効果コードを取得
     */
    private fun getCompanionPlantEffectCode(effect: String): String {
        return when (effect.lowercase()) {
            "害虫予防", "害虫忌避", "虫除け" -> "01"
            "病気予防", "病害予防", "抗菌" -> "02"
            "生育促進", "成長促進", "発育促進" -> "03"
            "空間活用", "立体栽培", "垂直栽培" -> "04"
            "風味向上", "味向上", "香り" -> "05"
            "土壌改善", "土壌改良", "土作り" -> "06"
            "受粉促進", "受粉", "花粉媒介" -> "07"
            "雑草抑制", "雑草防止", "草取り" -> "08"
            "景観美化", "見た目", "美観" -> "09"
            "水分保持", "保水", "乾燥防止" -> "10"
            "ph調整", "ph", "酸性", "アルカリ性" -> "11"
            "効率up", "効率", "収穫効率" -> "12"
            "収量安定", "収量", "安定" -> "13"
            else -> "99"
        }
    }

    /**
     * コンパニオンプランツ情報をフォーマット
     */
    private fun formatCompanionPlants(companionPlants: List<com.example.seedstockkeeper6.model.CompanionPlant>): String {
        if (companionPlants.isEmpty()) {
            return ""
        }
        
        val companionInfo = StringBuilder()
        companionPlants.forEach { companion ->
            val effects = companion.effects.map { effect ->
                val code = getCompanionPlantEffectCode(effect)
                "$effect($code)"
            }.joinToString(", ")
            companionInfo.appendLine("- ${companion.plant}: $effects")
        }
        return companionInfo.toString()
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
        val seedsWithTimeToPrepare = mutableListOf<String>() // 土づくり時間がある種
        val companionPlantsInfo = mutableListOf<String>() // コンパニオンプランツ情報
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
                            val displayName = if (seed.productName.isNotEmpty()) {
                                if (seed.variety.isNotEmpty()) {
                                    "${seed.productName} (${seed.variety})"
                                } else {
                                    seed.productName
                                }
                            } else {
                                seed.variety
                            }
                            val seasonRange = if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
                                DateUtils.getSeasonRangeFromDates(entry.sowing_start_date, entry.sowing_end_date)
                            } else {
                                DateUtils.getSeasonRangeFromMonths(startMonth, endMonth)
                            }
                            seedsThisMonth.add("$displayName - 有効期限: ${seed.expirationYear}年${seed.expirationMonth}月, 播種期間: $seasonRange")
                            isRelevant = true
                        }
                        
                        // 今月が播種期間の終了月かチェック（まき時終了間近）
                        if (currentMonth == endMonth) {
                            android.util.Log.d("GeminiNotiService", "まき時終了間近の種発見: ${seed.productName}")
                            val displayName = if (seed.productName.isNotEmpty()) {
                                if (seed.variety.isNotEmpty()) {
                                    "${seed.productName} (${seed.variety})"
                                } else {
                                    seed.productName
                                }
                            } else {
                                seed.variety
                            }
                            val seasonRange = if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
                                DateUtils.getSeasonRangeFromDates(entry.sowing_start_date, entry.sowing_end_date)
                            } else {
                                DateUtils.getSeasonRangeFromMonths(startMonth, endMonth)
                            }
                            seedsEndingThisMonth.add("$displayName - 有効期限: ${seed.expirationYear}年${seed.expirationMonth}月, 播種期間: $seasonRange")
                            isRelevant = true
                        }
                        
                        // まき時終了まで2週間以上ある種をチェック（土づくり時間がある）
                        if (currentMonth < endMonth) {
                            val monthsUntilEnd = endMonth - currentMonth
                            val currentDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
                            
                            // 2週間以上（約0.5ヶ月）の余裕がある場合
                            if (monthsUntilEnd > 0 || (monthsUntilEnd == 0 && currentDay <= 15)) {
                                android.util.Log.d("GeminiNotiService", "土づくり時間がある種発見: ${seed.productName}")
                                val displayName = if (seed.productName.isNotEmpty()) {
                                    if (seed.variety.isNotEmpty()) {
                                        "${seed.productName} (${seed.variety})"
                                    } else {
                                        seed.productName
                                    }
                                } else {
                                    seed.variety
                                }
                                val seasonRange = if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
                                    DateUtils.getSeasonRangeFromDates(entry.sowing_start_date, entry.sowing_end_date)
                                } else {
                                    DateUtils.getSeasonRangeFromMonths(startMonth, endMonth)
                                }
                                seedsWithTimeToPrepare.add("$displayName - 有効期限: ${seed.expirationYear}年${seed.expirationMonth}月, 播種期間: $seasonRange")
                                isRelevant = true
                            }
                        }
                    } catch (e: Exception) {
                        // 日付解析エラーの場合はスキップ
                        android.util.Log.w("GeminiNotiService", "日付解析エラー - seed: ${seed.productName}, startDate: ${entry.sowing_start_date}, endDate: ${entry.sowing_end_date}", e)
                    }
                }
            }
            
            // コンパニオンプランツ情報を収集（今月まき時の種のみ）
            if (seed.companionPlants.isNotEmpty()) {
                val companionInfo = formatCompanionPlants(seed.companionPlants)
                if (companionInfo.isNotEmpty()) {
                    val displayName = if (seed.productName.isNotEmpty()) {
                        if (seed.variety.isNotEmpty()) {
                            "${seed.productName} (${seed.variety})"
                        } else {
                            seed.productName
                        }
                    } else {
                        seed.variety
                    }
                    companionPlantsInfo.add("$displayName のコンパニオンプランツ:\n$companionInfo")
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
        
        if (seedsWithTimeToPrepare.isNotEmpty()) {
            result.appendLine("まき時終了まで2週間以上ある登録種（土づくり時間あり）:")
            seedsWithTimeToPrepare.forEach { seed ->
                result.appendLine("- $seed")
            }
        }
        
        if (companionPlantsInfo.isNotEmpty()) {
            result.appendLine("登録種のコンパニオンプランツ情報:")
            companionPlantsInfo.forEach { companion ->
                result.appendLine(companion)
            }
        }
        
        if (seedsThisMonth.isEmpty() && seedsEndingThisMonth.isEmpty() && seedsWithTimeToPrepare.isEmpty()) {
            result.appendLine("今月に関連する登録種はありません。")
        }
        
        android.util.Log.d("GeminiNotiService", "formatUserSeedsForPrompt結果 - 今月関連種: ${relevantSeedsCount}件/${seeds.size}件, 今月まき時: ${seedsThisMonth.size}件, まき時終了間近: ${seedsEndingThisMonth.size}件, 土づくり時間あり: ${seedsWithTimeToPrepare.size}件")
        
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
                            val displayName = if (seed.productName.isNotEmpty()) {
                                if (seed.variety.isNotEmpty()) {
                                    "${seed.productName} (${seed.variety})"
                                } else {
                                    seed.productName
                                }
                            } else {
                                seed.variety
                            }
                            val sowingStartMonth = calendarEntry.sowing_start_date.split("-")[1].toInt()
                            val seasonRange = if (calendarEntry.sowing_start_date.isNotEmpty() && calendarEntry.sowing_end_date.isNotEmpty()) {
                                DateUtils.getSeasonRangeFromDates(calendarEntry.sowing_start_date, calendarEntry.sowing_end_date)
                            } else {
                                DateUtils.getSeasonRangeFromMonths(sowingStartMonth, sowingEndMonth)
                            }
                            seedsEndingSoon.add("$displayName - 有効期限: ${seed.expirationYear}年${seed.expirationMonth}月, 播種期間: $seasonRange")
                            isRelevant = true
                        } else if (sowingEndMonth == currentMonth + 1 && currentDay <= 15) {
                            // 来月が播種期間の終了月で、今月の15日以前の場合
                            android.util.Log.d("GeminiNotiService", "2週間前の種発見（来月終了）: ${seed.productName}")
                            val displayName = if (seed.productName.isNotEmpty()) {
                                if (seed.variety.isNotEmpty()) {
                                    "${seed.productName} (${seed.variety})"
                                } else {
                                    seed.productName
                                }
                            } else {
                                seed.variety
                            }
                            val sowingStartMonth = calendarEntry.sowing_start_date.split("-")[1].toInt()
                            val seasonRange = if (calendarEntry.sowing_start_date.isNotEmpty() && calendarEntry.sowing_end_date.isNotEmpty()) {
                                DateUtils.getSeasonRangeFromDates(calendarEntry.sowing_start_date, calendarEntry.sowing_end_date)
                            } else {
                                DateUtils.getSeasonRangeFromMonths(sowingStartMonth, sowingEndMonth)
                            }
                            seedsEndingSoon.add("$displayName - 有効期限: ${seed.expirationYear}年${seed.expirationMonth}月, 播種期間: $seasonRange")
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
