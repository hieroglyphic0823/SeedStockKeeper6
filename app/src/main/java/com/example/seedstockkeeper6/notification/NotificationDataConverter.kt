package com.example.seedstockkeeper6.notification

import com.example.seedstockkeeper6.model.NotificationData
import com.example.seedstockkeeper6.model.SeedInfo
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.text.SimpleDateFormat
import java.util.*

class NotificationDataConverter {
    
    private val gson = Gson()
    
    /**
     * GeminiAPIから返されるテキスト内容をNotificationDataに変換
     */
    fun convertTextToNotificationData(
        title: String,
        content: String,
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int,
        notificationType: String = "MONTHLY",
        userId: String = ""
    ): NotificationData {
        
        return try {
            // まず、内容にJSONブロックが含まれているかチェック
            val jsonStart = content.indexOf("```json")
            
            if (jsonStart != -1) {
                val jsonEnd = content.indexOf("```", jsonStart + 7)
                
                if (jsonEnd != -1) {
                    val jsonText = content.substring(jsonStart + 7, jsonEnd).trim()
                    return parseJsonToNotificationData(jsonText, title, farmOwner, region, prefecture, month, notificationType, userId)
                }
            }
            
            // JSONブロックがない場合、純粋なJSON形式かチェック
            if (content.trim().startsWith("{")) {
                return parseJsonToNotificationData(content, title, farmOwner, region, prefecture, month, notificationType, userId)
            }
            
            // JSON形式でない場合は、テキストから構造化データを抽出
            val result = extractFromTextContent(title, content, farmOwner, region, prefecture, month, notificationType, userId)
            result
            
        } catch (e: Exception) {
            createDefaultNotificationData(title, content, farmOwner, region, prefecture, month, notificationType, userId)
        }
    }
    
    /**
     * JSONテキストをNotificationDataに変換
     */
    private fun parseJsonToNotificationData(
        jsonText: String,
        title: String,
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int,
        notificationType: String,
        userId: String
    ): NotificationData {
        val jsonObject = JsonParser.parseString(jsonText).asJsonObject
        
        val thisMonthSeeds = parseSeedInfoArray(jsonObject.getAsJsonArray("thisMonthSeeds"))
        val endingSoonSeeds = parseSeedInfoArray(jsonObject.getAsJsonArray("endingSoonSeeds"))
        val recommendedSeeds = parseSeedInfoArray(jsonObject.getAsJsonArray("recommendedSeeds"))
        
        return NotificationData(
            id = jsonObject.get("id")?.asString ?: java.util.UUID.randomUUID().toString(),
            notificationType = jsonObject.get("notificationType")?.asString ?: notificationType,
            title = jsonObject.get("title")?.asString ?: title,
            summary = jsonObject.get("summary")?.asString ?: "",
            farmOwner = jsonObject.get("farmOwner")?.asString ?: farmOwner,
            region = jsonObject.get("region")?.asString ?: region,
            prefecture = jsonObject.get("prefecture")?.asString ?: prefecture,
            month = jsonObject.get("month")?.asInt ?: month,
            thisMonthSeeds = thisMonthSeeds,
            endingSoonSeeds = endingSoonSeeds,
            recommendedSeeds = recommendedSeeds,
            closingLine = jsonObject.get("closingLine")?.asString ?: "",
            signature = jsonObject.get("signature")?.asString ?: "",
            sentAt = getCurrentTimestamp(),
            userId = jsonObject.get("userId")?.asString ?: userId,
            seedCount = calculateSeedCount(jsonObject),
            priority = jsonObject.get("priority")?.asString ?: "DEFAULT",
            channelId = jsonObject.get("channelId")?.asString ?: "seed_notifications",
            isRead = 0 // 未読として明示的に設定
        )
    }
    
    /**
     * テキスト内容から構造化データを抽出
     */
    private fun extractFromTextContent(
        title: String,
        content: String,
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int,
        notificationType: String,
        userId: String
    ): NotificationData {
        val lines = content.lines()
        val summary = lines.firstOrNull { it.isNotBlank() } ?: ""
        
        val thisMonthSeeds = extractSeedsFromSection(content, "🌱")
        val endingSoonSeeds = extractSeedsFromSection(content, "⚠️")
        val recommendedSeeds = extractSeedsFromSection(content, "🌟")
        
        
        val closingLine = extractClosingLineFromContent(content)
        val signature = extractSignatureFromContent(content)
        
        return NotificationData(
            id = java.util.UUID.randomUUID().toString(),
            notificationType = notificationType,
            title = title,
            summary = summary,
            farmOwner = farmOwner,
            region = region,
            prefecture = prefecture,
            month = month,
            thisMonthSeeds = thisMonthSeeds,
            endingSoonSeeds = endingSoonSeeds,
            recommendedSeeds = recommendedSeeds,
            closingLine = closingLine,
            signature = signature,
            sentAt = getCurrentTimestamp(),
            userId = userId,
            seedCount = thisMonthSeeds.size + endingSoonSeeds.size + recommendedSeeds.size,
            priority = "DEFAULT",
            channelId = "seed_notifications",
            isRead = 0 // 未読として明示的に設定
        )
    }
    
    /**
     * デフォルトのNotificationDataを作成
     */
    private fun createDefaultNotificationData(
        title: String,
        content: String,
        farmOwner: String,
        region: String,
        prefecture: String,
        month: Int,
        notificationType: String,
        userId: String
    ): NotificationData {
        return NotificationData(
            id = java.util.UUID.randomUUID().toString(),
            notificationType = notificationType,
            title = title,
            summary = content.lines().firstOrNull { it.isNotBlank() } ?: "",
            farmOwner = farmOwner,
            region = region,
            prefecture = prefecture,
            month = month,
            thisMonthSeeds = emptyList(),
            endingSoonSeeds = emptyList(),
            recommendedSeeds = emptyList(),
            closingLine = "",
            signature = "",
            sentAt = getCurrentTimestamp(),
            userId = userId,
            seedCount = 0,
            priority = "DEFAULT",
            channelId = "seed_notifications",
            isRead = 0 // 未読として明示的に設定
        )
    }
    
    /**
     * セクションから種の情報を抽出
     */
    private fun extractSeedsFromSection(content: String, sectionMarker: String): List<SeedInfo> {
        val seeds = mutableListOf<SeedInfo>()
        val lines = content.lines()
        var inSection = false
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            // セクションマーカーで始まる行を検出（🌟 今月のおすすめ種: のような形式も対応）
            if (trimmedLine.startsWith(sectionMarker)) {
                inSection = true
                continue
            }
            
            if (inSection) {
                // 他のセクションが始まったら終了（ただし、同じマーカーの場合は除外）
                if ((trimmedLine.startsWith("🌱") && sectionMarker != "🌱") || 
                    (trimmedLine.startsWith("⚠️") && sectionMarker != "⚠️") || 
                    (trimmedLine.startsWith("🌟") && sectionMarker != "🌟") || 
                    trimmedLine.startsWith("```")) {
                    break
                }
                
                if (trimmedLine.startsWith("*") || trimmedLine.startsWith("•") || trimmedLine.startsWith("-")) {
                    val seedInfo = parseSeedFromLine(trimmedLine)
                    if (seedInfo != null) {
                        seeds.add(seedInfo)
                    }
                }
            }
        }
        
        return seeds
    }
    
    /**
     * 行から種の情報を解析
     */
    private fun parseSeedFromLine(line: String): SeedInfo? {
        try {
            // 「*   『種名 (品種)』: 説明」の形式を解析
            val cleanLine = line.removePrefix("*").removePrefix("•").removePrefix("-").trim()
            
            val nameMatch = Regex("『([^』]+)』").find(cleanLine)
            if (nameMatch != null) {
                val nameWithVariety = nameMatch.groupValues[1]
                val colonIndex = nameWithVariety.indexOf(" (")
                
                val name = if (colonIndex != -1) {
                    nameWithVariety.substring(0, colonIndex)
                } else {
                    nameWithVariety
                }
                
                val variety = if (colonIndex != -1) {
                    val endIndex = nameWithVariety.indexOf(")", colonIndex)
                    if (endIndex != -1) {
                        nameWithVariety.substring(colonIndex + 2, endIndex)
                    } else ""
                } else ""
                
                val description = cleanLine.substringAfter(": ").trim()
                
                return SeedInfo(
                    name = name,
                    variety = variety,
                    description = description
                )
            }
        } catch (e: Exception) {
        }
        
        return null
    }
    
    
    /**
     * 結びの文を抽出
     */
    private fun extractClosingLineFromContent(content: String): String {
        val lines = content.lines()
        for (i in lines.size - 1 downTo 0) {
            val line = lines[i].trim()
            if (line.isNotEmpty() && 
                !line.contains("佐々木助三郎") && !line.contains("助三郎") && !line.contains("助さん") &&
                line.length > 10 && line.length < 100) {
                return line
            }
        }
        return ""
    }
    
    /**
     * 署名を抽出
     */
    private fun extractSignatureFromContent(content: String): String {
        val lines = content.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.contains("佐々木助三郎") || trimmed.contains("助三郎") || trimmed.contains("助さん")) {
                return trimmed
            }
        }
        return ""
    }
    
    /**
     * JSON配列からSeedInfoリストを解析
     */
    private fun parseSeedInfoArray(jsonArray: com.google.gson.JsonArray?): List<SeedInfo> {
        if (jsonArray == null) {
            return emptyList()
        }
        
        return jsonArray.mapNotNull { element ->
            try {
                val obj = element.asJsonObject
                val seedInfo = SeedInfo(
                    name = obj.get("name")?.asString ?: "",
                    variety = obj.get("variety")?.asString ?: "",
                    description = obj.get("description")?.asString ?: "",
                    expirationYear = obj.get("expirationYear")?.asInt ?: 0,
                    expirationMonth = obj.get("expirationMonth")?.asInt ?: 0
                )
                seedInfo
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * 種の総数を計算
     */
    private fun calculateSeedCount(jsonObject: com.google.gson.JsonObject): Int {
        val thisMonth = jsonObject.getAsJsonArray("thisMonthSeeds")?.size() ?: 0
        val endingSoon = jsonObject.getAsJsonArray("endingSoonSeeds")?.size() ?: 0
        val recommended = jsonObject.getAsJsonArray("recommendedSeeds")?.size() ?: 0
        return thisMonth + endingSoon + recommended
    }
    
    /**
     * 現在のタイムスタンプを取得
     */
    private fun getCurrentTimestamp(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return format.format(Date())
    }
}
