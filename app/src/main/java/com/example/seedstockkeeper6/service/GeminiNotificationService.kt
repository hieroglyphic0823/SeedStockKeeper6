package com.example.seedstockkeeper6.service

import android.util.Log
import com.example.seedstockkeeper6.BuildConfig
import com.example.seedstockkeeper6.model.SeedPacket
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            
            // プロンプトを生成
            val prompt = promptGenerator.generateMonthlyPrompt(
                region = region,
                prefecture = prefecture,
                seedInfoUrl = seedInfo,
                userSeeds = userSeeds,
                currentMonth = currentMonth,
                farmOwner = farmOwner,
                customFarmOwner = customFarmOwner
            )
            
            // Gemini APIを呼び出し
            val response = generativeModel!!.generateContent(prompt)
            val content = response.text ?: contentFormatter.getDefaultMonthlyContent(dataProcessor.getMonthName(currentMonth))
            
            Log.d("GeminiNotiService", "月次通知内容生成完了")
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
        try {
            if (generativeModel == null) {
                Log.w("GeminiNotiService", "GeminiAPIが利用できません。デフォルトタイトルを返します。")
                return@withContext contentFormatter.getDefaultMonthlyTitle(currentMonth, farmOwner)
            }
            
            val monthName = dataProcessor.getMonthName(currentMonth)
            val tone = promptGenerator.getFarmOwnerTone(farmOwner, customFarmOwner, monthName)
            val userSeedsText = contentFormatter.formatUserSeeds(userSeeds, currentMonth)
            
            Log.d("GeminiNotiService", "月次通知タイトル生成パラメータ - region: $region, prefecture: $prefecture, farmAddress: $farmAddress")
            
            val prompt = """
                $tone
                
                以下の情報を基に、$monthName の種まきについて、$region の$prefecture での農業アドバイスのタイトルを生成してください。

                【地域情報】
                - 地域: $region
                - 都道府県: $prefecture
                - 農園住所: $farmAddress
                
                【ユーザーの種情報】
                $userSeedsText
                
                【指示】
                1. 親しみやすく、分かりやすいタイトル
                2. 月名と地域を含める
                3. 農園住所の市区町村名を含める（例：福岡市西区草場の風に乗せて）
                4. 助さんの口調に合わせる
                5. 20文字以内で簡潔に
                
                タイトルのみを返してください。
            """.trimIndent()
            
            val response = generativeModel!!.generateContent(prompt)
            val title = response.text?.trim() ?: contentFormatter.getDefaultMonthlyTitle(currentMonth, farmOwner)
            
            Log.d("GeminiNotiService", "月次通知タイトル生成完了: $title")
            title
            
        } catch (e: Exception) {
            Log.e("GeminiNotiService", "月次通知タイトル生成に失敗", e)
            contentFormatter.getDefaultMonthlyTitle(currentMonth, farmOwner)
        }
    }
    
    /**
     * 週次通知のタイトルを生成
     */
    suspend fun generateWeeklyNotificationTitle(
        userSeeds: List<SeedPacket>,
        farmOwner: String,
        customFarmOwner: String = ""
    ): String = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                Log.w("GeminiNotiService", "GeminiAPIが利用できません。デフォルトタイトルを返します。")
                return@withContext "今週の種まきについて"
            }
            
            val tone = promptGenerator.getFarmOwnerTone(farmOwner, customFarmOwner, "今週")
            val userSeedsText = contentFormatter.formatUserSeedsForWeekly(userSeeds)
            
            val prompt = """
                $tone
                
                以下の情報を基に、今週の種まきについてのタイトルを生成してください。
                
                【ユーザーの種情報】
                $userSeedsText
                
                【指示】
                1. 親しみやすく、分かりやすいタイトル
                2. 今週の種まきに関連する内容
                3. 助さんの口調に合わせる
                4. 20文字以内で簡潔に
                
                タイトルのみを返してください。
            """.trimIndent()
            
            val response = generativeModel!!.generateContent(prompt)
            val title = response.text?.trim() ?: "今週の種まきについて"
            
            Log.d("GeminiNotiService", "週次通知タイトル生成完了: $title")
            title
            
        } catch (e: Exception) {
            Log.e("GeminiNotiService", "週次通知タイトル生成に失敗", e)
            "今週の種まきについて"
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