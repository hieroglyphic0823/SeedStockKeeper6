package com.example.seedstockkeeper6.service

import android.util.Log
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
                Log.w("GeminiNotiService", "GeminiAPIキーが設定されていません。デフォルト内容を使用します。")
            }
        } catch (e: Exception) {
            Log.e("GeminiNotiService", "GeminiAPI初期化に失敗", e)
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
                Log.w("GeminiNotiService", "GeminiAPIが利用できません。デフォルト内容を返します。")
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
            
            Log.d("GeminiNotiService", "月次通知内容生成完了")
            Log.d("GeminiNotiService", "Gemini APIから返された完全なコンテンツ:")
            Log.d("GeminiNotiService", "==========================================")
            Log.d("GeminiNotiService", content)
            Log.d("GeminiNotiService", "==========================================")
            content
            
        } catch (e: Exception) {
            Log.e("GeminiNotiService", "月次通知内容生成に失敗", e)
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
        Log.d("GeminiNotiService", "月次通知タイトル（固定フォーマット）: $title")
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
                Log.w("GeminiNotiService", "Gemini API呼び出し失敗 (試行 ${attempt + 1}/${maxRetries + 1}): ${e.message}")
                
                // 503エラー（過負荷）の場合は少し待ってからリトライ
                if (e.message?.contains("503") == true || e.message?.contains("overloaded") == true) {
                    if (attempt < maxRetries) {
                        val delayMs = (attempt + 1) * 2000L // 2秒、4秒、6秒...
                        Log.d("GeminiNotiService", "Gemini API過負荷のため、${delayMs}ms待機してリトライします")
                        delay(delayMs)
                    }
                } else {
                    // 503以外のエラーは即座に失敗
                    return@repeat
                }
            }
        }
        
        Log.e("GeminiNotiService", "Gemini API呼び出しが${maxRetries + 1}回失敗しました", lastException)
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
        Log.d("GeminiNotiService", "週次通知タイトル（固定フォーマット）: $title")
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
        customFarmOwner: String = ""
    ): String = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                Log.w("GeminiNotiService", "GeminiAPIが利用できません。デフォルト内容を返します。")
                return@withContext contentFormatter.getDefaultWeeklyContent()
            }
            
            // プロンプトを生成
            val prompt = promptGenerator.generateWeeklyPrompt(
                userSeeds = userSeeds,
                farmOwner = farmOwner,
                customFarmOwner = customFarmOwner
            )
            
            // Gemini APIを呼び出し
            val response = generativeModel!!.generateContent(prompt)
            val content = response.text ?: contentFormatter.getDefaultWeeklyContent()
            
            Log.d("GeminiNotiService", "週次通知内容生成完了")
            Log.d("GeminiNotiService", "Gemini APIから返された完全なコンテンツ:")
            Log.d("GeminiNotiService", "==========================================")
            Log.d("GeminiNotiService", content)
            Log.d("GeminiNotiService", "==========================================")
            content
            
        } catch (e: Exception) {
            Log.e("GeminiNotiService", "週次通知内容生成に失敗", e)
            contentFormatter.getDefaultWeeklyContent()
        }
    }
    
    /**
     * 通知要約を抽出
     */
    suspend fun extractNotificationSummary(fullContent: String): String = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                Log.w("GeminiNotiService", "GeminiAPIが利用できません。手動抽出を実行します。")
                return@withContext contentFormatter.extractSummaryManually(fullContent)
            }
            
            // プロンプトを生成
            val prompt = promptGenerator.generateSummaryExtractionPrompt(fullContent)
            
            // Gemini APIを呼び出し
            val response = generativeModel!!.generateContent(prompt)
            val summary = response.text?.trim() ?: contentFormatter.extractSummaryManually(fullContent)
            
            Log.d("GeminiNotiService", "通知要約抽出完了")
            summary
            
        } catch (e: Exception) {
            Log.e("GeminiNotiService", "通知要約抽出に失敗", e)
            contentFormatter.extractSummaryManually(fullContent)
        }
    }
}