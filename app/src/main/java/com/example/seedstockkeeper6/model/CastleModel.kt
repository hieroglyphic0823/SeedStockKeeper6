package com.example.seedstockkeeper6.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 集計データの表示用データクラス
 */
data class StatisticsData(
    val thisMonthSowingCount: Int,
    val urgentSeedsCount: Int,
    val totalSeeds: Int,
    val finishedSeedsCount: Int,
    val expiredSeedsCount: Int,
    val familyDistribution: List<Pair<String, Int>>
)

/**
 * すけさんからのメッセージ生成関数
 */
fun generateSukesanMessage(
    seeds: List<SeedPacket>,
    currentMonth: Int,
    currentYear: Int,
    isPreview: Boolean,
    farmOwner: String = "水戸黄門",
    farmName: String = "菜園"
): String {
    
    val monthName = when (currentMonth) {
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
        else -> "${currentMonth}月"
    }
    
    val thisMonthSowingSeeds = com.example.seedstockkeeper6.utils.SowingCalculationUtils.getThisMonthSowingSeeds(
        seeds = seeds,
        currentDate = java.time.LocalDate.of(currentYear, currentMonth, 1),
        excludeFinished = true
    )
    
    val urgentSeeds = com.example.seedstockkeeper6.utils.SowingCalculationUtils.getUrgentSeeds(
        seeds = seeds,
        currentDate = java.time.LocalDate.of(currentYear, currentMonth, 1)
    )
    
    
    if (thisMonthSowingSeeds.isNotEmpty()) {
    }
    if (urgentSeeds.isNotEmpty()) {
    }
    
    return when {
        urgentSeeds.isNotEmpty() -> {
            val seedNames = urgentSeeds.take(3).joinToString("、") { seed ->
                "${seed.productName}${if (seed.variety.isNotEmpty()) "（${seed.variety}）" else ""}"
            }
            when (farmOwner) {
                "水戸黄門" -> "黄門様、${farmName}の${monthName}は${urgentSeeds.size}種類の種のまき時が終了間近でございます。${seedNames}の播種を早急に完了させましょう。"
                "お銀" -> "お銀、${farmName}の${monthName}は${urgentSeeds.size}種類の種のまき時が終了間近です。${seedNames}の播種を急いで完了させてくださいね。"
                "八兵衛" -> "おい八、${farmName}の${monthName}は${urgentSeeds.size}種類の種のまき時が終了間近だぞ！${seedNames}の播種を急いでやれ！"
                else -> "${farmOwner}、${farmName}の${monthName}は${urgentSeeds.size}種類の種のまき時が終了間近です。${seedNames}の播種を早急に完了させましょう。"
            }
        }
        thisMonthSowingSeeds.isNotEmpty() -> {
            val seedNames = thisMonthSowingSeeds.take(3).joinToString("、") { seed ->
                "${seed.productName}${if (seed.variety.isNotEmpty()) "（${seed.variety}）" else ""}"
            }
            when (farmOwner) {
                "水戸黄門" -> "黄門様、${farmName}の${monthName}は${thisMonthSowingSeeds.size}種類の種の播種時期でございます。${seedNames}の栽培を計画的に進めましょう。"
                "お銀" -> "お銀、${farmName}の${monthName}は${thisMonthSowingSeeds.size}種類の種の播種時期です。${seedNames}の栽培を楽しんでくださいね。"
                "八兵衛" -> "おい八、${farmName}の${monthName}は${thisMonthSowingSeeds.size}種類の種の播種時期だぞ！${seedNames}の栽培を頑張れ！"
                else -> "${farmOwner}、${farmName}の${monthName}は${thisMonthSowingSeeds.size}種類の種の播種時期です。${seedNames}の栽培を計画的に進めましょう。"
            }
        }
        seeds.isEmpty() -> {
            when (farmOwner) {
                "水戸黄門" -> "黄門様、${farmName}へようこそ。種子を登録して、栽培計画を立てましょう。"
                "お銀" -> "お銀、${farmName}へようこそ。種子を登録して、栽培計画を立ててくださいね。"
                "八兵衛" -> "おい八、${farmName}へようこそ！種子を登録して、栽培計画を立てるぞ！"
                else -> "${farmOwner}、${farmName}へようこそ。種子を登録して、栽培計画を立てましょう。"
            }
        }
        else -> {
            when (farmOwner) {
                "水戸黄門" -> "黄門様、${farmName}の${monthName}は播種時期の種子はございませんが、他の管理作業に取り組む良い機会でございます。"
                "お銀" -> "お銀、${farmName}の${monthName}は播種時期の種子はありませんが、他の管理作業に取り組む良い機会です。"
                "八兵衛" -> "おい八、${farmName}の${monthName}は播種時期の種子はないが、他の管理作業に取り組む良い機会だぞ！"
                else -> "${farmOwner}、${farmName}の${monthName}は播種時期の種子はありませんが、他の管理作業に取り組む良い機会です。"
            }
        }
    }
}

/**
 * 通知の内容からまきどきの種と期限切れ間近の種情報を抽出
 */
fun extractSeedInfoFromNotificationData(notificationData: NotificationData, allSeeds: List<SeedPacket>): Pair<List<SeedPacket>, List<SeedPacket>> {
    val thisMonthSowingSeeds = mutableListOf<SeedPacket>()
    val urgentSeeds = mutableListOf<SeedPacket>()
    
    
    // 今月まきどきの種を抽出
    notificationData.thisMonthSeeds.forEach { seedInfo ->
        val matchingSeed = allSeeds.find { it.productName == seedInfo.name }
        if (matchingSeed != null) {
            thisMonthSowingSeeds.add(matchingSeed)
        }
    }
    
    // 終了間近の種を抽出
    notificationData.endingSoonSeeds.forEach { seedInfo ->
        val matchingSeed = allSeeds.find { it.productName == seedInfo.name }
        if (matchingSeed != null) {
            urgentSeeds.add(matchingSeed)
        }
    }
    
    return thisMonthSowingSeeds to urgentSeeds
}

fun extractSeedInfoFromNotification(notificationContent: String, allSeeds: List<SeedPacket>): Pair<List<SeedPacket>, List<SeedPacket>> {
    val thisMonthSowingSeeds = mutableListOf<SeedPacket>()
    val urgentSeeds = mutableListOf<SeedPacket>()
    
    
    // まずは機械可読なJSONブロックを優先して抽出
    parseSeedsFromJsonBlock(notificationContent)?.let { (tm, urgent) ->
        return tm to urgent
    }

    // 通知の内容から種の名前を抽出（表記揺れに強い緩和パターン）
    val thisMonthPattern = Regex("🌱\\s+(?:\\*\\*)?今月まきどきの種:?\\s*(?:\\*\\*)?")
    val urgentPattern = Regex("⚠️\\s+(?:\\*\\*)?まき時終了間近:?\\s*(?:\\*\\*)?")
    
    
    // まきどきの種を抽出
    val thisMonthMatch = thisMonthPattern.find(notificationContent)
    if (thisMonthMatch != null) {
        val startIndex = thisMonthMatch.range.last + 1
        // 次のセクション（⚠️ or 🌟）までを取得
        val nextIdx1 = notificationContent.indexOf("⚠️", startIndex)
        val nextIdx2 = notificationContent.indexOf("🌟", startIndex)
        val endIndex = listOf(nextIdx1, nextIdx2).filter { it >= 0 }.minOrNull() ?: notificationContent.length
        val thisMonthText = notificationContent.substring(startIndex, endIndex).trim()
        
        
        if (thisMonthText != "該当なし") {
            // 種の名前を抽出（『種名』の形式）
            val seedNamePattern = "『([^』]+)』".toRegex()
            val matches = seedNamePattern.findAll(thisMonthText)
            matches.forEach { match ->
                val seedName = match.groupValues[1].trim()
                
                // （）と（）内の文字を除去
                val cleanSeedName = seedName.replace(Regex("\\([^)]*\\)"), "").trim()
                
                // 通知から抽出した種名をそのまま使用（実際の種データとの照合は不要）
                val extractedSeed = SeedPacket(
                    id = "extracted_${System.currentTimeMillis()}",
                    productName = cleanSeedName,
                    variety = "",
                    family = "",
                    expirationYear = 0,
                    expirationMonth = 0,
                    calendar = emptyList()
                )
                thisMonthSowingSeeds.add(extractedSeed)
            }
        }
    }
    
    // 期限切れ間近の種を抽出
    val urgentMatch = urgentPattern.find(notificationContent)
    if (urgentMatch != null) {
        val startIndex = urgentMatch.range.last + 1
        // 次のセクション（🌟 今月のおすすめ種:）までを取得
        val nextSectionIndex = notificationContent.indexOf("🌟", startIndex)
        val endIndex = if (nextSectionIndex == -1) notificationContent.length else nextSectionIndex
        val urgentText = notificationContent.substring(startIndex, endIndex).trim()
        
        
        if (urgentText != "該当なし") {
            // 種の名前を抽出（『種名』の形式）
            val seedNamePattern = "『([^』]+)』".toRegex()
            val matches = seedNamePattern.findAll(urgentText)
            matches.forEach { match ->
                val seedName = match.groupValues[1].trim()
                
                // （）と（）内の文字を除去
                val cleanSeedName = seedName.replace(Regex("\\([^)]*\\)"), "").trim()
                
                // 通知から抽出した種名をそのまま使用（実際の種データとの照合は不要）
                val extractedSeed = SeedPacket(
                    id = "extracted_${System.currentTimeMillis()}",
                    productName = cleanSeedName,
                    variety = "",
                    family = "",
                    expirationYear = 0,
                    expirationMonth = 0,
                    calendar = emptyList()
                )
                urgentSeeds.add(extractedSeed)
            }
        }
    }
    
    
    return Pair(thisMonthSowingSeeds, urgentSeeds)
}

// 通知本文末尾に含まれる機械可読JSONブロックをパース
fun parseSeedsFromJsonBlock(content: String): Pair<List<SeedPacket>, List<SeedPacket>>? {
    val codeStart = content.indexOf("```json")
    if (codeStart == -1) return null
    val codeEnd = content.indexOf("```", startIndex = codeStart + 7)
    if (codeEnd == -1) return null
    val jsonText = content.substring(codeStart + 7, codeEnd).trim()
    return try {
        val jsonObj = com.google.gson.JsonParser.parseString(jsonText).asJsonObject
        val tm = jsonObj.getAsJsonArray("this_month")?.map { it.asString } ?: emptyList()
        val urgent = jsonObj.getAsJsonArray("ending_soon")?.map { it.asString } ?: emptyList()
        val tmPackets = tm.map { name ->
            SeedPacket(
                id = "json_" + System.currentTimeMillis(),
                productName = name,
                variety = "",
                family = "",
                expirationYear = 0,
                expirationMonth = 0,
                calendar = emptyList()
            )
        }
        val urgentPackets = urgent.map { name ->
            SeedPacket(
                id = "json_" + System.currentTimeMillis(),
                productName = name,
                variety = "",
                family = "",
                expirationYear = 0,
                expirationMonth = 0,
                calendar = emptyList()
            )
        }
        tmPackets to urgentPackets
    } catch (_: Exception) {
        null
    }
}

/**
 * プレビュー用のデモデータ
 */
fun createPreviewSeedData(): List<SeedPacket> {
    return listOf(
        SeedPacket(
            id = "preview1",
            productName = "食べきりミニ大根",
            variety = "ころっ娘",
            family = "アブラナ科",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(
                CalendarEntry(
                    sowing_start_date = "2025-10-01",
                    sowing_end_date = "2025-10-31",
                    harvest_start_date = "2025-12-01",
                    harvest_end_date = "2025-12-31"
                )
            )
        ),
        SeedPacket(
            id = "preview2",
            productName = "一寸そら豆",
            variety = "ソラマメ",
            family = "マメ科",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(
                CalendarEntry(
                    sowing_start_date = "2025-10-01",
                    sowing_end_date = "2025-10-31",
                    harvest_start_date = "2026-05-01",
                    harvest_end_date = "2026-05-31"
                )
            )
        ),
        SeedPacket(
            id = "preview3",
            productName = "サラダタマネギ",
            variety = "ゆめたま",
            family = "ユリ科",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(
                CalendarEntry(
                    sowing_start_date = "2025-09-01",
                    sowing_end_date = "2025-10-31",
                    harvest_start_date = "2026-06-01",
                    harvest_end_date = "2026-06-30"
                )
            )
        )
    )
}

/**
 * プレビュー用の集計データ
 */
fun createPreviewStatisticsData(): StatisticsData {
    return StatisticsData(
        thisMonthSowingCount = 1,
        urgentSeedsCount = 0,
        totalSeeds = 2,
        finishedSeedsCount = 1,
        expiredSeedsCount = 0,
        familyDistribution = listOf(Pair("せり科", 1), Pair("きく科", 1))
    )
}
