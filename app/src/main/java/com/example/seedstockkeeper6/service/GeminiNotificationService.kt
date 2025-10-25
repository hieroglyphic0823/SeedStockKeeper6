package com.example.seedstockkeeper6.service

import com.example.seedstockkeeper6.BuildConfig
import com.example.seedstockkeeper6.model.SeedPacket
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

/**
 * Gemini APIを使用した通知生成サービス
 */
class GeminiNotificationService {
    
    private var generativeModel: GenerativeModel? = null
    private val promptGenerator = NotificationPromptGenerator()
    private val contentFormatter = NotificationContentFormatter()
    private val dataProcessor = NotificationDataProcessor()
    
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
            }
        } catch (e: Exception) {
        }
    }
    
    /**
     * 月次通知の内容を生成
     */
    suspend fun generateMonthlyNotificationContent(
        region: String,
        prefecture: String,
        seedInfoUrl: String,
        userSeeds: List<SeedPacket>,
        currentMonth: Int,
        farmOwner: String,
        customFarmOwner: String = ""
    ): String = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                return@withContext contentFormatter.getDefaultMonthlyContent(dataProcessor.getMonthName(currentMonth))
            }
            
            // 参考情報を取得
            val seedInfo = dataProcessor.fetchSeedInfoFromUrl(seedInfoUrl)
            
            // 今月のおすすめ種情報を取得
            val recommendedSeeds = dataProcessor.fetchRecommendedSeedsForCurrentMonth(seedInfoUrl, currentMonth)
            
            // プロンプトを生成
            val prompt = promptGenerator.generateMonthlyPrompt(
                region = region,
                prefecture = prefecture,
                seedInfoUrl = seedInfo,
                recommendedSeeds = recommendedSeeds,
                userSeeds = userSeeds,
                currentMonth = currentMonth,
                farmOwner = farmOwner,
                customFarmOwner = customFarmOwner
            )
            
            // Gemini APIを呼び出し
            val response = generativeModel!!.generateContent(prompt)
            val content = response.text ?: contentFormatter.getDefaultMonthlyContent(dataProcessor.getMonthName(currentMonth))
            
            content
            
        } catch (e: Exception) {
            contentFormatter.getDefaultMonthlyContent(dataProcessor.getMonthName(currentMonth))
        }
    }

    /**
     * 月次通知のタイトルを生成
     */
    suspend fun generateMonthlyNotificationTitle(
        region: String,
        prefecture: String,
        seedInfoUrl: String,
        userSeeds: List<SeedPacket>,
        currentMonth: Int,
        farmOwner: String,
        customFarmOwner: String = "",
        farmAddress: String = ""
    ): String = withContext(Dispatchers.IO) {
        // 仕様変更: 月次タイトルは「和名月 + すけさん便り」に固定
        val wafuu = getJapaneseMonthName(currentMonth)
        val title = "${wafuu}すけさん便り"
        title
    }
    
    /**
     * リトライ機能付きでGemini APIを呼び出し
     */
    private suspend fun generateContentWithRetry(prompt: String, maxRetries: Int = 2): String? {
        var lastException: Exception? = null
        
        repeat(maxRetries + 1) { attempt ->
            try {
                val response = generativeModel!!.generateContent(prompt)
                return response.text
            } catch (e: Exception) {
                lastException = e
                
                // 503エラー（過負荷）の場合は少し待ってからリトライ
                if (e.message?.contains("503") == true || e.message?.contains("overloaded") == true) {
                    if (attempt < maxRetries) {
                        val delayMs = (attempt + 1) * 2000L // 2秒、4秒、6秒...
                        delay(delayMs)
                    }
                } else {
                    // 503以外のエラーは即座に失敗
                    return@repeat
                }
            }
        }
        
        return null
    }
    
    /**
     * 週次通知のタイトルを生成
     */
    suspend fun generateWeeklyNotificationTitle(
        userSeeds: List<SeedPacket>,
        farmOwner: String,
        customFarmOwner: String = ""
    ): String = withContext(Dispatchers.IO) {
        // 仕様変更: 週次タイトルは「和名月（第n週）すけさん便り」に固定
        val cal = java.util.Calendar.getInstance()
        val month = cal.get(java.util.Calendar.MONTH) + 1
        val week = cal.get(java.util.Calendar.WEEK_OF_MONTH)
        val wafuu = getJapaneseMonthName(month)
        val title = "${wafuu}（第${week}週）すけさん便り"
        title
    }

    // 和風月名を取得（1..12）
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
     * 週次通知の内容を生成
     */
    suspend fun generateWeeklyNotificationContent(
        userSeeds: List<SeedPacket>,
        farmOwner: String,
        customFarmOwner: String = "",
        recommendedSeeds: String = "",
        region: String = "温暖地",
        seedInfoUrl: String = ""
    ): String = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                android.util.Log.w("GeminiNotificationService", "generativeModelがnullのため、デフォルト週次通知コンテンツを使用")
                return@withContext contentFormatter.getDefaultWeeklyContent()
            }
            
            // 農園情報の種情報URLからおすすめの種情報を取得（月次通知と同様）
            val actualRecommendedSeeds = if (seedInfoUrl.isNotEmpty()) {
                val currentDate = java.time.LocalDate.now()
                val weekFields = java.time.temporal.WeekFields.of(java.util.Locale.JAPAN)
                val weekNumber = currentDate.get(weekFields.weekOfMonth())
                val currentMonth = currentDate.monthValue
                
                // 週番号に応じて対象月を決定
                val targetMonth = if (weekNumber <= 2) {
                    currentMonth
                } else {
                    if (currentMonth == 12) 1 else currentMonth + 1
                }
                
                android.util.Log.d("GeminiNotificationService", "週次通知 - 対象月: $targetMonth, 週番号: $weekNumber")
                dataProcessor.fetchRecommendedSeedsForCurrentMonth(seedInfoUrl, targetMonth)
            } else {
                recommendedSeeds
            }
            
            android.util.Log.d("GeminiNotificationService", "週次通知 - 取得したおすすめ種情報: $actualRecommendedSeeds")
            
            // プロンプトを生成
            val prompt = promptGenerator.generateWeeklyPrompt(
                userSeeds = userSeeds,
                farmOwner = farmOwner,
                customFarmOwner = customFarmOwner,
                recommendedSeeds = actualRecommendedSeeds,
                region = region
            )
            
            // デバッグログ: プロンプトの内容
            android.util.Log.d("GeminiNotificationService", "週次通知プロンプト送信開始")
            android.util.Log.d("GeminiNotificationService", "プロンプト内容: $prompt")
            
            // Gemini APIを呼び出し
            val response = generativeModel!!.generateContent(prompt)
            val rawContent = response.text ?: contentFormatter.getDefaultWeeklyContent()
            
            // デバッグログ: Gemini APIの応答内容
            android.util.Log.d("GeminiNotificationService", "週次通知Gemini API応答受信")
            android.util.Log.d("GeminiNotificationService", "生の応答内容: $rawContent")
            
            // JSON形式の応答を強制
            val content = if (rawContent.trim().startsWith("{") || rawContent.contains("```json")) {
                // コードブロック内のJSONを抽出
                if (rawContent.contains("```json")) {
                    val jsonStart = rawContent.indexOf("```json") + 7
                    val jsonEnd = rawContent.indexOf("```", jsonStart)
                    if (jsonEnd > jsonStart) {
                        rawContent.substring(jsonStart, jsonEnd).trim()
                    } else {
                        rawContent
                    }
                } else {
                    rawContent
                }
            } else {
                // JSON形式でない場合はデフォルトのJSON形式を生成
                android.util.Log.w("GeminiNotificationService", "JSON形式でない応答を受信、デフォルト形式を使用")
                generateDefaultWeeklyJson(farmOwner, customFarmOwner)
            }
            
            android.util.Log.d("GeminiNotificationService", "最終的なコンテンツ: $content")
            content
            
        } catch (e: Exception) {
            android.util.Log.e("GeminiNotificationService", "週次通知生成エラー", e)
            android.util.Log.d("GeminiNotificationService", "デフォルト週次通知コンテンツを使用")
            contentFormatter.getDefaultWeeklyContent()
        }
    }
    
    /**
     * デフォルトの週次通知JSON形式を生成
     */
    private fun generateDefaultWeeklyJson(farmOwner: String, customFarmOwner: String): String {
        val today = java.time.LocalDate.now()
        val weekFields = java.time.temporal.WeekFields.of(java.util.Locale.JAPAN)
        val weekNumber = today.get(weekFields.weekOfMonth())
        val monthName = getJapaneseMonthName(today.monthValue)
        
        return """
        {
          "notificationType": "WEEKLY",
          "title": "${monthName}（第${weekNumber}週）すけさん便り",
          "summary": "今週の種まきについてお知らせいたします。",
          "farmOwner": "$farmOwner",
          "region": "温暖地",
          "prefecture": "",
          "month": ${today.monthValue},
          "thisMonthSeeds": [
            {
              "name": "恋むすめ",
              "variety": "ニンジン",
              "description": "今月が播種時期の種です"
            }
          ],
          "endingSoonSeeds": [],
          "recommendedSeeds": [
            {
              "name": "ハクサイ",
              "variety": "冬の鍋物の主役",
              "description": "冬の鍋物に欠かせない野菜です"
            }
          ],
          "advice": "今週は種まきの準備を整えましょう。土づくりと種の準備を忘れずに。",
          "closingLine": "健やかな種まきをお祈りしております。",
          "signature": "助さんより"
        }
        """.trimIndent()
    }
    
    /**
     * 通知要約を抽出
     */
    suspend fun extractNotificationSummary(fullContent: String): String = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                return@withContext contentFormatter.extractSummaryManually(fullContent)
            }
            
            // プロンプトを生成
            val prompt = promptGenerator.generateSummaryExtractionPrompt(fullContent)
            
            // Gemini APIを呼び出し
            val response = generativeModel!!.generateContent(prompt)
            val summary = response.text?.trim() ?: contentFormatter.extractSummaryManually(fullContent)
            
            summary
            
        } catch (e: Exception) {
            contentFormatter.extractSummaryManually(fullContent)
        }
    }
}
