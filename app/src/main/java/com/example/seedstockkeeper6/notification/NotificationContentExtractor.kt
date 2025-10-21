package com.example.seedstockkeeper6.notification

import com.example.seedstockkeeper6.model.SeedDetail

class NotificationContentExtractor {
    
    fun extractNamesForHistory(content: String): Triple<List<String>, List<String>, List<String>> {
        fun extract(sectionEmoji: String): List<String> {
            val pattern = Regex("$sectionEmoji\\s+(?:\\*\\*)?[^:]*:?\\s*(?:\\*\\*)?")
            val sectionStart = content.indexOf(pattern.find(content)?.value ?: "")
            if (sectionStart == -1) return emptyList()
            
            val nextSectionStart = content.indexOf("\n\n", sectionStart)
            val sectionEnd = if (nextSectionStart != -1) nextSectionStart else content.length
            val sectionContent = content.substring(sectionStart, sectionEnd)
            
            val namePattern = Regex("『([^』]+)』")
            return namePattern.findAll(sectionContent).map { it.groupValues[1] }.toList()
        }
        
        return Triple(
            extract("🌱"), // thisMonth
            extract("⚠️"), // endingSoon
            extract("🌟")  // recommended
        )
    }
    
    fun extractDetailsForHistory(content: String): Triple<List<SeedDetail>, List<SeedDetail>, List<SeedDetail>> {
        fun extract(sectionEmoji: String): List<SeedDetail> {
            val pattern = Regex("$sectionEmoji\\s+(?:\\*\\*)?[^:]*:?\\s*(?:\\*\\*)?")
            val sectionStart = content.indexOf(pattern.find(content)?.value ?: "")
            if (sectionStart == -1) return emptyList()
            
            val nextSectionStart = content.indexOf("\n\n", sectionStart)
            val sectionEnd = if (nextSectionStart != -1) nextSectionStart else content.length
            val sectionContent = content.substring(sectionStart, sectionEnd)
            
            val itemPattern = Regex("\\*\\s*『([^』]+)』:?\\s*([^\\n]*)")
            return itemPattern.findAll(sectionContent).map { matchResult ->
                val name = matchResult.groupValues[1]
                val desc = matchResult.groupValues[2].trim()
                SeedDetail(name = name, desc = desc)
            }.toList()
        }
        
        return Triple(
            extract("🌱"), // thisMonth
            extract("⚠️"), // endingSoon
            extract("🌟")  // recommended
        )
    }
    
    fun extractClosingLine(content: String): String {
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
}
