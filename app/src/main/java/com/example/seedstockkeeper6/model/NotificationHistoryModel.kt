package com.example.seedstockkeeper6.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * 通知履歴関連のデータモデルとヘルパー関数
 */

// プレビュー用のデモデータ
fun createPreviewNotificationData(): List<NotificationData> {
    return listOf(
        NotificationData(
            id = "preview1",
            title = "弥生の風に乗せて――春の種まきの候、菜園より",
            summary = "お銀、菜園の弥生は1種類の種の播種時期です。恋むすめ（ニンジン）の栽培を楽しんでくださいね。",
            farmOwner = "お銀",
            region = "温暖地",
            prefecture = "東京都",
            month = 3,
            thisMonthSeeds = listOf(
                SeedInfo(
                    name = "恋むすめ",
                    variety = "ニンジン",
                    description = "春の種まきに最適な品種です"
                )
            ),
            endingSoonSeeds = listOf(
                SeedInfo(
                    name = "春菊",
                    variety = "中葉春菊",
                    description = "まき時終了間近です"
                )
            ),
            sentAt = "2024-03-15T12:00:00.000Z",
            userId = "preview",
            seedCount = 1,
            isRead = 0 // 未読
        ),
        NotificationData(
            id = "preview2",
            title = "卯月の雨に潤う――新緑の種まきの候、菜園より",
            summary = "お銀、菜園の卯月は2種類の種の播種時期です。レタスとネギの栽培を楽しんでくださいね。",
            farmOwner = "お銀",
            region = "温暖地",
            prefecture = "東京都",
            month = 4,
            thisMonthSeeds = listOf(
                SeedInfo(
                    name = "レタス",
                    variety = "サニーレタス",
                    description = "春の種まきに最適な品種です"
                ),
                SeedInfo(
                    name = "ネギ",
                    variety = "九条ネギ",
                    description = "春の種まきに最適な品種です"
                )
            ),
            endingSoonSeeds = emptyList(),
            sentAt = "2024-04-15T12:00:00.000Z",
            userId = "preview",
            seedCount = 2,
            isRead = 1 // 既読
        )
    )
}

// 本文から種プレビュー（種名, 説明）を抽出
fun extractSeedPreviewItems(content: String, maxItems: Int = 3): List<Pair<String, String>> {
    // セクション境界を考慮して、「• 」行から『種名』っぽいものと、その次行の簡潔説明を拾う
    val lines = content.lines()
    val items = mutableListOf<Pair<String, String>>()
    var i = 0
    while (i < lines.size && items.size < maxItems) {
        val line = lines[i].trim()
        // 箇条書き・種名候補（記号は「•」「*」「-」のいずれかを許容）
        if (line.startsWith("• ") || line.startsWith("* ") || line.startsWith("- ")) {
            val name = line.removePrefix("• ").removePrefix("* ").removePrefix("- ").trim()
            // 次行を説明文候補として取得（同じ箇条書きでない、かつ見出しでない）
            val desc = if (i + 1 < lines.size) {
                val next = lines[i + 1].trim()
                if (!next.startsWith("• ") && !next.startsWith("* ") && !next.startsWith("- ") && !next.startsWith("🌱") && !next.startsWith("⚠️") && !next.startsWith("🌟") && !next.startsWith("```")) next else ""
            } else ""
            if (name.isNotEmpty()) {
                items += name to desc
            }
        }
        i++
    }
    return items
}

// 「今月まきどき」「まき時終了間近」各セクションの先頭アイテム名を1行サマリに整形
data class SectionSummary(val thisMonth: String, val endingSoon: String)

fun extractSectionSummaries(content: String): SectionSummary {
    // JSONコードブロックがあれば最優先で使う
    val jsonStart = content.indexOf("```json")
    if (jsonStart != -1) {
        val jsonEnd = content.indexOf("```", startIndex = jsonStart + 7)
        if (jsonEnd != -1) {
            val jsonText = content.substring(jsonStart + 7, jsonEnd).trim()
            try {
                val obj = com.google.gson.JsonParser.parseString(jsonText).asJsonObject
                val tm = obj.getAsJsonArray("this_month")?.map { it.asString } ?: emptyList()
                val es = obj.getAsJsonArray("ending_soon")?.map { it.asString } ?: emptyList()
                return SectionSummary(
                    thisMonth = tm.firstOrNull() ?: "",
                    endingSoon = es.firstOrNull() ?: ""
                )
            } catch (_: Exception) {
                // fall through to text parsing
            }
        }
    }
    // テキストから抽出（見出し→次の箇条書き1件を拾う）
    var thisMonth = ""
    var endingSoon = ""
    val lines = content.lines()
    var i = 0
    while (i < lines.size) {
        val line = lines[i].trim()
        if (line.startsWith("🌱")) {
            // 次の箇条書き行
            var j = i + 1
            while (j < lines.size) {
                val l = lines[j].trim()
                if (l.startsWith("• ") || l.startsWith("* ") || l.startsWith("- ")) {
                    thisMonth = l.removePrefix("• ").removePrefix("* ").removePrefix("- ").trim()
                    break
                }
                if (l.startsWith("⚠️") || l.startsWith("🌟") || l.startsWith("```")) break
                j++
            }
        }
        if (line.startsWith("⚠️")) {
            var j = i + 1
            while (j < lines.size) {
                val l = lines[j].trim()
                if (l.startsWith("• ") || l.startsWith("* ") || l.startsWith("- ")) {
                    endingSoon = l.removePrefix("• ").removePrefix("* ").removePrefix("- ").trim()
                    break
                }
                if (l.startsWith("🌟") || l.startsWith("```")) break
                j++
            }
        }
        i++
    }
    return SectionSummary(thisMonth = thisMonth, endingSoon = endingSoon)
}

// セクション毎に（種名, 説明）一覧を抽出
fun extractSectionItems(content: String, sectionMarker: String): List<Pair<String, String>> {
    val text = removeJsonCodeBlock(content)
    val lines = text.lines()
    val results = mutableListOf<Pair<String, String>>()
    var i = 0
    while (i < lines.size) {
        val line = lines[i].trim()
        if (line.startsWith(sectionMarker)) {
            var j = i + 1
            while (j < lines.size) {
                val l = lines[j].trim()
                if (l.startsWith("🌱") || l.startsWith("⚠️") || l.startsWith("🌟") || l.startsWith("```")) break
                if (l.startsWith("• ") || l.startsWith("* ") || l.startsWith("- ")) {
                    val raw = l.removePrefix("• ").removePrefix("* ").removePrefix("- ").trim()
                    val nameInQuote = Regex("『([^』]+)』").find(raw)?.groupValues?.getOrNull(1)
                    val name = (nameInQuote ?: raw).replace("：", "").trim()
                    // 説明は次行（箇条書きや見出しでない）を説明として扱う
                    val desc = if (j + 1 < lines.size) {
                        val next = lines[j + 1].trim()
                        if (!next.startsWith("• ") && !next.startsWith("* ") && !next.startsWith("- ") && !next.startsWith("🌱") && !next.startsWith("⚠️") && !next.startsWith("🌟") && !next.startsWith("```")) {
                            // 説明文の先頭の「：」を削除
                            next.removePrefix("：").trim()
                        } else ""
                    } else ""
                    results += name to desc
                }
                j++
            }
        }
        i++
    }
    return results
}

// JSONコードブロック除去（履歴側にも再利用）
fun removeJsonCodeBlock(content: String): String {
    val start = content.indexOf("```json")
    if (start == -1) return content
    val end = content.indexOf("```", startIndex = start + 7)
    return if (end == -1) content.substring(0, start).trimEnd() else (content.substring(0, start) + content.substring(end + 3)).trim()
}

fun buildClosingLine(farmOwner: String): String {
    return when (farmOwner) {
        "水戸黄門" -> "かしこ\n佐々木助三郎 拝"
        "お銀" -> "ご自愛くだされ\n佐々木助三郎 拝"
        "八兵衛" -> "しっかり働けよ！\n助三郎 より"
        else -> "本日も良き栽培となりますよう。助さんより"
    }
}

// 通知内容から署名部分を抽出
fun extractSignature(content: String): String {
    val lines = content.lines()
    // 最後の数行から署名を探す
    for (i in lines.size - 1 downTo maxOf(0, lines.size - 5)) {
        val line = lines[i].trim()
        if (line.contains("佐々木助三郎 拝") || line.contains("助三郎 より") || line.contains("助さんより")) {
            return line
        }
    }
    return ""
}

// 通知内容からアドバイスと署名を抽出
fun extractAdviceAndSignature(content: String): Pair<String, String> {
    val lines = content.lines()
    var advice = ""
    var signature = ""

    for (i in maxOf(0, lines.size - 10) until lines.size) {
    }

    // 最後の数行からアドバイスと署名を探す
    for (i in lines.size - 1 downTo maxOf(0, lines.size - 10)) {
        val line = lines[i].trim()

        // 署名を探す
        if (line.contains("佐々木助三郎 拝") || line.contains("助三郎 より") || line.contains("助さんより")) {
            signature = line
        }
        // アドバイスを探す（署名の前の行で、短い文）
        else if (line.isNotEmpty() && line.length <= 50 && !line.startsWith("🌱") && !line.startsWith("⚠️") && !line.startsWith("🌟") && !line.startsWith("【") && !line.contains("佐々木助三郎") && !line.contains("助三郎") && !line.contains("助さん")) {
            if (advice.isEmpty()) {
                advice = line
            }
        }
    }

    return advice to signature
}

fun formatDateTime(dateTimeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
        
        val date = inputFormat.parse(dateTimeString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateTimeString
    }
}

fun extractAdviceFromContent(content: String): String {
    val lines = content.lines()
    val jsonStartIndex = content.indexOf("```json")
    val jsonEndIndex = if (jsonStartIndex != -1) content.indexOf("```", jsonStartIndex + 7) else -1
    
    // 最後の数行からアドバイス（結びの一言）を探す
    for (i in lines.size - 1 downTo maxOf(0, lines.size - 25)) {
        val line = lines[i].trim()
        
        // JSONブロック内の行は除外
        if (jsonStartIndex != -1 && jsonEndIndex != -1) {
            val lineStartIndex = content.indexOf(line)
            if (lineStartIndex >= jsonStartIndex && lineStartIndex <= jsonEndIndex) {
                continue
            }
        }
        
        // 署名の前の行で、アドバイス文を探す
        if (line.isNotEmpty() && line.length <= 100 && 
            !line.startsWith("🌱") && !line.startsWith("⚠️") && !line.startsWith("🌟") && 
            !line.startsWith("【") && !line.startsWith("```") && !line.startsWith("{") && !line.startsWith("}") &&
            !line.contains("佐々木助三郎") && !line.contains("助三郎") && !line.contains("助さん") &&
            !line.contains("\"") && !line.contains("name") && !line.contains("desc") &&
            !line.contains("```") && !line.contains("json") &&
            // アドバイスらしい文の条件を拡張
            (line.contains("ご無理") || line.contains("お祈り") || line.contains("心より") || 
             line.contains("どうぞ") || line.contains("季節") || line.contains("時節") ||
             line.contains("温かく") || line.contains("寒さ") || line.contains("作業") ||
             line.contains("実り") || line.contains("豊作") || line.contains("収穫") ||
             line.contains("ご自愛") || line.contains("励まれ") || line.contains("肌寒") ||
             line.contains("秋深") || line.contains("農作業") || line.contains("無理なき"))) {
            return line
        }
    }
    return ""
}
