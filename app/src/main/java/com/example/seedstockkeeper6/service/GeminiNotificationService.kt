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
                Log.w("GeminiNotificationService", "GeminiAPIキーが設定されていません。デフォルト内容を使用します。")
            }
        } catch (e: Exception) {
            Log.e("GeminiNotificationService", "GeminiAPI初期化に失敗", e)
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
        userSeeds: List<com.example.seedstockkeeper6.model.SeedPacket> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        try {
            val seedInfoContent = fetchSeedInfoFromUrl(seedInfoUrl)
            val monthName = getMonthName(currentMonth)
            val userSeedsInfo = formatUserSeedsForPrompt(userSeeds, currentMonth)
            
            val prompt = """
                あなたは農業の専門家です。以下の情報を基に、月次通知の内容を生成してください。

                【基本情報】
                - 地域: $region
                - 県: $prefecture
                - 現在の月: $monthName
                - 種情報URL: $seedInfoUrl

                【種情報URLの内容】
                $seedInfoContent

                【ユーザーが登録している種の情報】
                $userSeedsInfo

                【生成する通知内容の要件】
                1. 今月（$monthName）に種まきできる野菜・草花のリスト（ユーザー登録種も含む）
                2. 地域（$region）と県（$prefecture）に適した季節のおすすめ品種
                3. まき時が今月で終わる種への注意喚起（ユーザー登録種も含む）
                4. ユーザーが登録している種で今月まき時のものがあれば優先的に表示
                5. 実用的で分かりやすい内容
                6. 絵文字を使って見やすくする
                7. 各項目は簡潔に（最大3-5種類程度）

                【出力形式】
                🌱 今月まき時の種:
                • [品種名] ([種類]) - あなたの登録種
                • [品種名] ([種類])

                🌟 季節のおすすめ:
                • [おすすめ内容]
                • [おすすめ内容]

                ⚠️ まき時終了間近:
                • [品種名] ([種類]) - あなたの登録種
                • [品種名] ([種類])

                上記の形式で、ユーザーの登録種を優先的に含む実用的で分かりやすい通知内容を生成してください。
            """.trimIndent()
            
            if (generativeModel != null) {
                val response = generativeModel?.generateContent(prompt)
                response?.text ?: getDefaultMonthlyContent(monthName)
            } else {
                Log.w("GeminiNotificationService", "GeminiAPIが利用できません。デフォルト内容を返します。")
                getDefaultMonthlyContent(monthName)
            }
            
        } catch (e: Exception) {
            Log.e("GeminiNotificationService", "月次通知内容生成に失敗", e)
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
        userSeeds: List<com.example.seedstockkeeper6.model.SeedPacket> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        try {
            val seedInfoContent = fetchSeedInfoFromUrl(seedInfoUrl)
            val userSeedsInfo = formatUserSeedsForWeeklyPrompt(userSeeds)
            
            val prompt = """
                あなたは農業の専門家です。以下の情報を基に、週次リマインダー通知の内容を生成してください。

                【基本情報】
                - 地域: $region
                - 県: $prefecture
                - 種情報URL: $seedInfoUrl

                【種情報URLの内容】
                $seedInfoContent

                【ユーザーが登録している種の情報】
                $userSeedsInfo

                【生成する通知内容の要件】
                1. まき時終了の2週間前の種のリスト（ユーザー登録種も含む）
                2. 「土づくりすれば間に合う」という励ましのメッセージ
                3. 地域（$region）と県（$prefecture）に適した内容
                4. ユーザーが登録している種でまき時終了間近のものがあれば優先的に表示
                5. 実用的で分かりやすい内容
                6. 絵文字を使って見やすくする
                7. 各項目は簡潔に（最大3-5種類程度）

                【出力形式】
                ⏰ まき時終了の2週間前の種があります:

                • [品種名] ([種類]) - あなたの登録種
                  土づくりすれば間に合います！

                • [品種名] ([種類])
                  土づくりすれば間に合います！

                上記の形式で、ユーザーの登録種を優先的に含む励ましのメッセージを含む実用的な通知内容を生成してください。
            """.trimIndent()
            
            if (generativeModel != null) {
                val response = generativeModel?.generateContent(prompt)
                response?.text ?: getDefaultWeeklyContent()
            } else {
                Log.w("GeminiNotificationService", "GeminiAPIが利用できません。デフォルト内容を返します。")
                getDefaultWeeklyContent()
            }
            
        } catch (e: Exception) {
            Log.e("GeminiNotificationService", "週次通知内容生成に失敗", e)
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
            
            Log.d("GeminiNotificationService", "種情報URLから取得した内容: ${textContent.take(200)}...")
            textContent
            
        } catch (e: Exception) {
            Log.e("GeminiNotificationService", "種情報URL取得に失敗: $url", e)
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
• レタス (サラダミックス)
• キャベツ (春キャベツ)
• トマト (ミニトマト)

🌟 季節のおすすめ:
• $monthName は種まきの最適期です
• 地域に適した品種を選びましょう
• 土づくりを忘れずに！

⚠️ まき時終了間近:
• 特にありません

💡 ヒント: 種まき前に土の準備をしっかり行いましょう"""
    }
    
    /**
     * デフォルトの週次通知内容
     */
    private fun getDefaultWeeklyContent(): String {
        return """⏰ まき時終了の2週間前の種があります:

• レタス (サラダミックス)
  土づくりすれば間に合います！

• キャベツ (春キャベツ)
  土づくりすれば間に合います！

• トマト (ミニトマト)
  土づくりすれば間に合います！

💪 まだ間に合います！準備を始めましょう"""
    }
    
    /**
     * ユーザーの種データを月次通知用プロンプトにフォーマット
     */
    private fun formatUserSeedsForPrompt(seeds: List<com.example.seedstockkeeper6.model.SeedPacket>, currentMonth: Int): String {
        if (seeds.isEmpty()) {
            return "ユーザーはまだ種を登録していません。"
        }
        
        val seedsThisMonth = mutableListOf<String>()
        val seedsEndingThisMonth = mutableListOf<String>()
        
        seeds.forEach { seed ->
            seed.calendar.forEach { calendarEntry ->
                // 播種期間の確認
                val sowingStartMonth = parseMonthFromDate(calendarEntry.sowing_start_date)
                val sowingEndMonth = parseMonthFromDate(calendarEntry.sowing_end_date)
                
                if (sowingStartMonth != null && sowingEndMonth != null) {
                    // 今月が播種期間内かチェック
                    if (isMonthInRange(currentMonth, sowingStartMonth, sowingEndMonth)) {
                        seedsThisMonth.add("${seed.productName} (${seed.variety}) - 播種期間: ${sowingStartMonth}月〜${sowingEndMonth}月")
                    }
                    
                    // 今月が播種期間の終了月かチェック
                    if (currentMonth == sowingEndMonth) {
                        seedsEndingThisMonth.add("${seed.productName} (${seed.variety}) - 播種期間終了: ${sowingEndMonth}月")
                    }
                }
            }
        }
        
        val result = StringBuilder()
        result.appendLine("ユーザーが登録している種の情報:")
        
        if (seedsThisMonth.isNotEmpty()) {
            result.appendLine("今月(${currentMonth}月)に種まきできる登録種:")
            seedsThisMonth.forEach { seed ->
                result.appendLine("- $seed")
            }
        }
        
        if (seedsEndingThisMonth.isNotEmpty()) {
            result.appendLine("今月(${currentMonth}月)で播種期間が終了する登録種:")
            seedsEndingThisMonth.forEach { seed ->
                result.appendLine("- $seed")
            }
        }
        
        if (seedsThisMonth.isEmpty() && seedsEndingThisMonth.isEmpty()) {
            result.appendLine("今月に関連する登録種はありません。")
        }
        
        return result.toString()
    }
    
    /**
     * ユーザーの種データを週次通知用プロンプトにフォーマット
     */
    private fun formatUserSeedsForWeeklyPrompt(seeds: List<com.example.seedstockkeeper6.model.SeedPacket>): String {
        if (seeds.isEmpty()) {
            return "ユーザーはまだ種を登録していません。"
        }
        
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        val seedsEndingSoon = mutableListOf<String>()
        
        seeds.forEach { seed ->
            seed.calendar.forEach { calendarEntry ->
                val sowingEndMonth = parseMonthFromDate(calendarEntry.sowing_end_date)
                
                if (sowingEndMonth != null) {
                    // まき時終了の2週間前（今月の終わり頃）の種をチェック
                    if (sowingEndMonth == currentMonth) {
                        seedsEndingSoon.add("${seed.productName} (${seed.variety}) - 播種期間終了: ${sowingEndMonth}月")
                    }
                }
            }
        }
        
        val result = StringBuilder()
        result.appendLine("ユーザーが登録している種の情報:")
        
        if (seedsEndingSoon.isNotEmpty()) {
            result.appendLine("まき時終了間近の登録種:")
            seedsEndingSoon.forEach { seed ->
                result.appendLine("- $seed")
            }
        } else {
            result.appendLine("まき時終了間近の登録種はありません。")
        }
        
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
