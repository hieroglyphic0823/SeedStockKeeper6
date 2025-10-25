package com.example.seedstockkeeper6.service

import com.example.seedstockkeeper6.BuildConfig
import com.example.seedstockkeeper6.model.SukesanMessage
import com.example.seedstockkeeper6.model.MessageType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SukesanMessageService {
    private const val TAG = "SukesanMessageService"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * 今日のメッセージを取得（キャッシュ優先）
     */
    suspend fun getTodaysMessage(): SukesanMessage? {
        val userId = auth.currentUser?.uid ?: return null
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        return try {
            val docRef = db.collection("users")
                .document(userId)
                .collection("messages")
                .document(today)
            
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(SukesanMessage::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            // ネットワークエラーの場合はnullを返す（オフライン対応）
            when (e) {
                is com.google.firebase.firestore.FirebaseFirestoreException -> {
                    when (e.code) {
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE,
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                        }
                        else -> {
                        }
                    }
                }
                else -> {
                }
            }
            null
        }
    }
    
    /**
     * メッセージを保存
     */
    suspend fun saveMessage(message: SukesanMessage): Result<Unit> {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            return Result.failure(Exception("User not authenticated"))
        }
        
        return try {
            val messageWithUserId = message.copy(userId = userId)
            val docRef = db.collection("users")
                .document(userId)
                .collection("messages")
                .document(message.date)
            
            docRef.set(messageWithUserId, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // ネットワークエラーの場合は失敗を返すが、アプリは継続動作
            when (e) {
                is com.google.firebase.firestore.FirebaseFirestoreException -> {
                    when (e.code) {
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE,
                        com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                        }
                        else -> {
                        }
                    }
                }
                else -> {
                }
            }
            Result.failure(e)
        }
    }
    
    /**
     * 今日のメッセージを生成して保存（GeminiAPI使用）
     */
    suspend fun generateAndSaveTodaysMessage(
        seeds: List<com.example.seedstockkeeper6.model.SeedPacket>,
        currentMonth: Int,
        currentYear: Int,
        farmOwner: String = "水戸黄門",
        farmName: String = "菜園",
        region: String = "温暖地",
        prefecture: String = ""
    ): Result<SukesanMessage> {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        
        // 既存のメッセージをチェック
        val existingMessage = getTodaysMessage()
        if (existingMessage != null) {
            return Result.success(existingMessage)
        }
        
        
        // GeminiAPIでメッセージを生成
        val geminiMessage = generateMessageWithGemini(seeds, currentMonth, currentYear, farmOwner, farmName, region, prefecture)
        
        val message = SukesanMessage(
            id = today,
            date = today,
            message = geminiMessage,
            messageType = com.example.seedstockkeeper6.model.MessageType.DAILY,
            createdAt = Timestamp.now(),
            isRead = false
        )
        
        
        val result = saveMessage(message)
        
        return if (result.isSuccess) {
            Result.success(message)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }
    
    /**
     * GeminiAPIを使用してメッセージを生成（通知機能と同じ構造）
     */
    private suspend fun generateMessageWithGemini(
        seeds: List<com.example.seedstockkeeper6.model.SeedPacket>,
        currentMonth: Int,
        currentYear: Int,
        farmOwner: String,
        farmName: String,
        region: String,
        prefecture: String
    ): String {
        try {
            val monthName = getMonthName(currentMonth)
            val japaneseMonthName = monthName
            
            // 農園主の口調設定（通知機能と同じ）
            val farmOwnerTone = getFarmOwnerToneForMessage(farmOwner, "", monthName)
            
            // ユーザーの種子情報をフォーマット（通知機能と同じ）
            val userSeedsInfo = formatUserSeedsForMessagePrompt(seeds, currentMonth)
            
            val prompt = """
                あなたは水戸黄門の登場人物の助さんです。以下の情報を基に、農園主へ今日のメッセージを生成してください。

                【基本情報】
                - 地域: $region
                - 県: $prefecture
                - 現在の月: $monthName
                - 農園主: $farmOwner
                - 農場名: $farmName

                【助さんの口調・キャラクター設定】
                $farmOwnerTone

                【ユーザーが登録している種の情報】
                $userSeedsInfo

                【生成するメッセージの要件】
                1. 農園主に農場名を含めて呼びかける（例：「お銀、●●農園の今日の種まきについて」）
                2. ユーザーが登録している種で今日がまき時のものがあれば優先的に表示
                3. ユーザー登録種のうちまき時が今月で終わる種への注意喚起
                   - 今月が播種期間の終了月の種を「まき時終了間近」として表示
                4. 地域（$region）と県（$prefecture）に適した内容
                5. 短くて分かりやすいメッセージ（1-2文程度）

                【出力形式】
                農園主への呼びかけ + 今日のまきどきの種の情報 + 注意喚起のメッセージ

                上記の形式で、設定した助さんの口調・キャラクターで、ユーザーの登録種を優先的に含み、簡潔で分かりやすいメッセージを生成してください。
            """.trimIndent()
            
            
            // GeminiAPIを呼び出し
            val generativeModel = com.google.ai.client.generativeai.GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = BuildConfig.GEMINI_API_KEY
            )
            
            val response = generativeModel.generateContent(prompt)
            val generatedMessage = response.text ?: getDefaultMessage(farmOwner, farmName, monthName)
            
            return generatedMessage
            
        } catch (e: Exception) {
            val monthName = getMonthName(currentMonth)
            return getDefaultMessage(farmOwner, farmName, monthName)
        }
    }
    
    /**
     * 農園主の口調設定を取得（通知機能と同じ構造）
     */
    private fun getFarmOwnerToneForMessage(farmOwner: String, customFarmOwner: String, monthName: String): String {
        return when (farmOwner) {
            "水戸黄門" -> """
                あなたは助さんとして、水戸黄門に話しかける口調で話してください：
                - 丁寧で格式高い敬語
                - 「〜でござる」「〜でございます」などの丁寧語を使用
                - 農業の専門知識を分かりやすく説明
                - 自分から報告を切り出す調子で書く（冒頭で「黄門様、かしこまりました」などは不要）
                - 例：「このたびは$monthName の種まきについてお手伝いいたします」
            """.trimIndent()
            
            "お銀" -> """
                あなたは助さんとして、お銀に話しかける口調で話してください：
                - 優しく親しみやすい敬語
                - 「お銀、〜です」「〜してくださいね」
                - 女性らしい気品を保ちながら実用的なアドバイス
                - 例：「お銀、$monthName の種まきについてお手伝いします」
            """.trimIndent()
            
            "八兵衛" -> """
                あなたは助さんとして、八兵衛に話しかける口調で話してください：
                - 親分肌で、少し呆れながらも温かく見守るような口調
                - 「八兵衛、〜だぞ」「しょうがないやつだな」
                - 頼りない弟分のような存在として接する
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
     * 農園主の口調設定を取得（旧版）
     */
    private fun getFarmOwnerTone(farmOwner: String, customFarmOwner: String, timeContext: String): String {
        return when (farmOwner) {
            "水戸黄門" -> """
                あなたは助さんとして、ご隠居様（水戸黄門）に話しかける口調で話してください：
                - 丁寧で格式高い敬語
                - 「〜でござる」「〜でございます」などの丁寧語を使用
                - 農業の専門知識を分かりやすく説明
                - 自分から報告を切り出す調子で書く（冒頭で「ご隠居様、かしこまりました」などは不要）
                - 例：「このたびは$timeContext の種まきについてお手伝いいたします」
            """.trimIndent()
            
            "お銀" -> """
                あなたは助さんとして、お銀に話しかける口調で話してください：
                - 優しく親しみやすい敬語
                - 「お銀、〜です」「〜してくださいね」
                - 女性らしい気品を保ちながら実用的なアドバイス
                - 例：「お銀、$timeContext の種まきについてお手伝いします」
            """.trimIndent()
            
            "八兵衛" -> """
                あなたは助さんとして、八兵衛に話しかける口調で話してください：
                - 親分肌で、少し呆れながらも温かく見守るような口調
                - 「八兵衛、〜だぞ」「しょうがないやつだな」
                - 頼りない弟分のような存在として接する
                - 例：「八兵衛、$timeContext の種まきをしっかり覚えるのじゃ」
            """.trimIndent()
            
            else -> """
                あなたは水戸黄門の登場人物の助さんとして、農業の専門家として話してください：
                - 親しみやすく温かい口調
                - 実用的で分かりやすい説明
                - 農業の経験に基づいたアドバイス
                - 例：「$timeContext の種まきについて、お手伝いさせていただきます」
            """.trimIndent()
        }
    }
    
    /**
     * ユーザーの種子情報をフォーマット（通知機能と同じ構造）
     */
    private fun formatUserSeedsForMessagePrompt(seeds: List<com.example.seedstockkeeper6.model.SeedPacket>, currentMonth: Int): String {
        if (seeds.isEmpty()) {
            return "登録されている種子はありません。"
        }
        
        return seeds.joinToString("\n") { seed ->
            val calendarInfo = seed.calendar?.joinToString(", ") { entry ->
                val sowingStartMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_start_date)
                val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
                val sowingStartYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_start_date)
                val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
                
                val isCurrentMonth = sowingStartMonth <= currentMonth && currentMonth <= sowingEndMonth
                val isUrgent = sowingEndMonth == currentMonth && sowingEndYear == java.time.LocalDate.now().year
                
                val status = when {
                    isUrgent -> "【まき時終了間近】"
                    isCurrentMonth -> "【まきどき】"
                    else -> ""
                }
                
                "播種期間: ${entry.sowing_start_date}〜${entry.sowing_end_date} $status"
            } ?: "播種期間: 未設定"
            
            """
            • ${seed.productName}${if (seed.variety.isNotEmpty()) "（${seed.variety}）" else ""}
              - 科: ${seed.family}
              - $calendarInfo
              - 有効期限: ${seed.expirationYear}年${seed.expirationMonth}月
            """.trimIndent()
        }
    }
    
    /**
     * ユーザーの種子情報をフォーマット（旧版）
     */
    private fun formatUserSeedsForPrompt(seeds: List<com.example.seedstockkeeper6.model.SeedPacket>): String {
        if (seeds.isEmpty()) {
            return "登録されている種子はありません。"
        }
        
        return seeds.joinToString("\n") { seed ->
            val calendarInfo = seed.calendar?.joinToString(", ") { entry ->
                "播種期間: ${entry.sowing_start_date}〜${entry.sowing_end_date}"
            } ?: "播種期間: 未設定"
            
            """
            • ${seed.productName}${if (seed.variety.isNotEmpty()) "（${seed.variety}）" else ""}
              - 科: ${seed.family}
              - $calendarInfo
              - 有効期限: ${seed.expirationYear}年${seed.expirationMonth}月
            """.trimIndent()
        }
    }
    
    /**
     * デフォルトメッセージを生成
     */
    private fun getDefaultMessage(farmOwner: String, farmName: String, monthName: String): String {
        return when (farmOwner) {
            "水戸黄門" -> "黄門様、$farmName の$monthName の種まきについてお手伝いいたします。"
            "お銀" -> "お銀、$farmName の$monthName の種まきについてお手伝いします。"
            "八兵衛" -> "八兵衛、$farmName の$monthName の種まきをしっかり覚えるのじゃ。"
            else -> "$farmOwner、$farmName の$monthName の種まきについてお手伝いします。"
        }
    }
    
    /**
     * メッセージを生成（農園主に応じた今日のまきどきの種について）
     */
    private fun generateMessage(
        seeds: List<com.example.seedstockkeeper6.model.SeedPacket>,
        currentMonth: Int,
        currentYear: Int,
        farmOwner: String = "水戸黄門",
        farmName: String = "菜園"
    ): SukesanMessage {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        
        // 今日のまきどきの種を取得
        val todaySowingSeeds = seeds.filter { seed ->
            seed.calendar?.any { entry ->
                val sowingStartMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_start_date)
                val sowingStartYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_start_date)
                val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
                val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
                
                // 今月が播種期間内かチェック
                sowingStartMonth <= currentMonth && currentMonth <= sowingEndMonth && 
                sowingStartYear <= currentYear && currentYear <= sowingEndYear
            } ?: false
        }
        
        // まき時終了間近の種子（今月内で播種期間が終了する種）
        val urgentSeeds = seeds.filter { seed ->
            seed.calendar?.any { entry ->
                val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
                val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
                val sowingEndStage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDateToStage(entry.sowing_end_date)
                // 今月内で播種期間が終了する種（上旬、中旬、下旬すべて対象）
                sowingEndMonth == currentMonth && sowingEndYear == currentYear
            } ?: false
        }
        
        
        if (todaySowingSeeds.isNotEmpty()) {
        }
        if (urgentSeeds.isNotEmpty()) {
        }
        
        // 農園主と農園名は引数から取得
        
        val (message, messageType) = when {
            urgentSeeds.isNotEmpty() -> {
                val monthName = getMonthName(currentMonth)
                generateUrgentMessage(farmOwner, farmName, urgentSeeds, monthName) to MessageType.URGENT
            }
            todaySowingSeeds.isNotEmpty() -> {
                val monthName = getMonthName(currentMonth)
                generateSowingMessage(farmOwner, farmName, todaySowingSeeds, monthName) to MessageType.DAILY
            }
            seeds.isEmpty() -> {
                generateWelcomeMessage(farmOwner, farmName) to MessageType.WELCOME
            }
            else -> {
                val monthName = getMonthName(currentMonth)
                generateGeneralMessage(farmOwner, farmName, monthName) to MessageType.DAILY
            }
        }
        
        
        return SukesanMessage(
            id = today,
            date = today,
            message = message,
            messageType = messageType,
            createdAt = Timestamp.now(),
            isRead = false
        )
    }
    
    /**
     * まき時終了間近のメッセージを生成
     */
    private fun generateUrgentMessage(
        farmOwner: String,
        farmName: String,
        urgentSeeds: List<com.example.seedstockkeeper6.model.SeedPacket>,
        monthName: String
    ): String {
        val seedNames = urgentSeeds.take(3).joinToString("、") { seed ->
            "${seed.productName}${if (seed.variety.isNotEmpty()) "（${seed.variety}）" else ""}"
        }
        
        
        val message = when (farmOwner) {
            "水戸黄門" -> "黄門様、${farmName}の${monthName}は${urgentSeeds.size}種類の種のまき時が終了間近でございます。${seedNames}の播種を早急に完了させましょう。"
            "お銀" -> "お銀、${farmName}の${monthName}は${urgentSeeds.size}種類の種のまき時が終了間近です。${seedNames}の播種を急いで完了させてくださいね。"
            "八兵衛" -> "おい八、${farmName}の${monthName}は${urgentSeeds.size}種類の種のまき時が終了間近だぞ！${seedNames}の播種を急いでやれ！"
            else -> "${farmOwner}、${farmName}の${monthName}は${urgentSeeds.size}種類の種のまき時が終了間近です。${seedNames}の播種を早急に完了させましょう。"
        }
        
        return message
    }
    
    /**
     * 播種時期のメッセージを生成
     */
    private fun generateSowingMessage(
        farmOwner: String,
        farmName: String,
        sowingSeeds: List<com.example.seedstockkeeper6.model.SeedPacket>,
        monthName: String
    ): String {
        val seedNames = sowingSeeds.take(3).joinToString("、") { seed ->
            "${seed.productName}${if (seed.variety.isNotEmpty()) "（${seed.variety}）" else ""}"
        }
        
        
        val message = when (farmOwner) {
            "水戸黄門" -> "黄門様、${farmName}の${monthName}は${sowingSeeds.size}種類の種の播種時期でございます。${seedNames}の栽培を計画的に進めましょう。"
            "お銀" -> "お銀、${farmName}の${monthName}は${sowingSeeds.size}種類の種の播種時期です。${seedNames}の栽培を楽しんでくださいね。"
            "八兵衛" -> "おい八、${farmName}の${monthName}は${sowingSeeds.size}種類の種の播種時期だぞ！${seedNames}の栽培を頑張れ！"
            else -> "${farmOwner}、${farmName}の${monthName}は${sowingSeeds.size}種類の種の播種時期です。${seedNames}の栽培を計画的に進めましょう。"
        }
        
        return message
    }
    
    /**
     * ウェルカムメッセージを生成
     */
    private fun generateWelcomeMessage(farmOwner: String, farmName: String): String {
        
        val message = when (farmOwner) {
            "水戸黄門" -> "黄門様、${farmName}へようこそ。種子を登録して、栽培計画を立てましょう。"
            "お銀" -> "お銀、${farmName}へようこそ。種子を登録して、栽培計画を立ててくださいね。"
            "八兵衛" -> "おい八、${farmName}へようこそ！種子を登録して、栽培計画を立てるぞ！"
            else -> "${farmOwner}、${farmName}へようこそ。種子を登録して、栽培計画を立てましょう。"
        }
        
        return message
    }
    
    /**
     * 一般的なメッセージを生成
     */
    private fun generateGeneralMessage(farmOwner: String, farmName: String, monthName: String): String {
        
        val message = when (farmOwner) {
            "水戸黄門" -> "黄門様、${farmName}の${monthName}は播種時期の種子はございませんが、他の管理作業に取り組む良い機会でございます。"
            "お銀" -> "お銀、${farmName}の${monthName}は播種時期の種子はありませんが、他の管理作業に取り組む良い機会です。"
            "八兵衛" -> "おい八、${farmName}の${monthName}は播種時期の種子はないが、他の管理作業に取り組む良い機会だぞ！"
            else -> "${farmOwner}、${farmName}の${monthName}は播種時期の種子はありませんが、他の管理作業に取り組む良い機会です。"
        }
        
        return message
    }
    
    /**
     * 月名を取得
     */
    private fun getMonthName(month: Int): String {
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
            else -> "${month}月"
        }
    }
    
    /**
     * メッセージを既読にする
     */
    suspend fun markAsRead(messageId: String): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val docRef = db.collection("users")
                .document(userId)
                .collection("messages")
                .document(messageId)
            
            docRef.update("isRead", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
