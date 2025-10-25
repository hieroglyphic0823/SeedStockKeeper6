package com.example.seedstockkeeper6.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * 通知データ処理を担当するクラス
 */
class NotificationDataProcessor {
    
    /**
     * URLから種情報を取得
     */
    suspend fun fetchSeedInfoFromUrl(url: String): String = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            connection.getInputStream().bufferedReader().use { reader ->
                reader.readText()
            }
        } catch (e: Exception) {
            "情報の取得に失敗しました。"
        }
    }
    
    /**
     * 今月のおすすめ種情報を取得
     */
    suspend fun fetchRecommendedSeedsForCurrentMonth(seedInfoUrl: String, currentMonth: Int): String = withContext(Dispatchers.IO) {
        try {
            
            // 種情報URLから情報を取得
            val seedInfo = fetchSeedInfoFromUrl(seedInfoUrl)
            
            // 今月のおすすめ種情報を生成（実際の実装では、URLの内容を解析して今月の種情報を抽出）
            val monthName = getMonthName(currentMonth)
            val recommendedSeeds = generateRecommendedSeedsForMonth(currentMonth, seedInfo)
            
            recommendedSeeds
        } catch (e: Exception) {
            generateDefaultRecommendedSeeds(currentMonth)
        }
    }
    
    /**
     * 月別のおすすめ種情報を生成
     */
    private fun generateRecommendedSeedsForMonth(month: Int, seedInfo: String): String {
        val monthName = getMonthName(month)
        
        // 農園情報の種情報URLから取得した実際のデータを解析
        if (seedInfo.isBlank() || seedInfo == "情報の取得に失敗しました。") {
            return "おすすめの種情報は取得できませんでした。"
        }
        
        // seedInfoの内容を解析して月別のおすすめ種情報を抽出
        // 実際の農園情報の種情報URLから取得したデータを使用
        return parseSeedInfoForMonth(seedInfo, month)
    }
    
    /**
     * 農園情報の種情報URLから取得したデータを解析して月別のおすすめ種情報を抽出
     */
    private fun parseSeedInfoForMonth(seedInfo: String, month: Int): String {
        // 実際の農園情報の種情報URLから取得したデータを解析
        // ここでは基本的な解析ロジックを実装
        val monthName = getMonthName(month)
        
        // seedInfoの内容を解析して、該当月の種情報を抽出
        // 実際の実装では、HTMLやJSONの解析、テキストの解析などを行う
        val lines = seedInfo.lines()
        val recommendedSeeds = mutableListOf<String>()
        
        for (line in lines) {
            if (line.contains(monthName) || line.contains("${month}月") || 
                line.contains("おすすめ") || line.contains("推奨") || 
                line.contains("•") || line.contains("・")) {
                recommendedSeeds.add(line.trim())
            }
        }
        
        return if (recommendedSeeds.isNotEmpty()) {
            recommendedSeeds.joinToString("\n")
        } else {
            "おすすめの種情報は見つかりませんでした。"
        }
    }
    
    /**
     * デフォルトのおすすめ種情報を生成
     */
    private fun generateDefaultRecommendedSeeds(month: Int): String {
        val monthName = getMonthName(month)
        return "• $monthName の季節に適した野菜の種を選びましょう\n• 地域の気候に合わせた品種を選ぶことが重要です\n• 種まきカレンダーを参考にしてください"
    }
    
    /**
     * 現在の日付を取得
     */
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    /**
     * 現在の月を取得
     */
    fun getCurrentMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MONTH) + 1
    }
    
    /**
     * 現在の年を取得
     */
    fun getCurrentYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }
    
    /**
     * 月名を取得（和風月名）
     */
    fun getMonthName(month: Int): String {
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
     * 週番号を取得
     */
    fun getWeekNumber(date: java.time.LocalDate): Int {
        val firstDayOfYear = date.withDayOfYear(1)
        val dayOfYear = date.dayOfYear
        return ((dayOfYear - firstDayOfYear.dayOfWeek.value + 6) / 7) + 1
    }
    
    /**
     * 日付文字列から月を解析
     */
    fun parseMonthFromDate(dateString: String): Int? {
        return try {
            val date = java.time.LocalDate.parse(dateString)
            date.monthValue
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 月が範囲内かチェック
     */
    fun isMonthInRange(targetMonth: Int, startMonth: Int, endMonth: Int): Boolean {
        return if (startMonth <= endMonth) {
            targetMonth in startMonth..endMonth
        } else {
            // 年をまたぐ場合（例：11月〜3月）
            targetMonth >= startMonth || targetMonth <= endMonth
        }
    }
    
    /**
     * 日本語の月名を取得（和風月名）
     */
    fun getJapaneseMonthName(month: Int): String {
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
     * 日付の妥当性をチェック
     */
    fun isValidDate(dateString: String): Boolean {
        return try {
            java.time.LocalDate.parse(dateString)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 日付の比較
     */
    fun isDateInRange(targetDate: java.time.LocalDate, startDate: java.time.LocalDate, endDate: java.time.LocalDate): Boolean {
        return targetDate.isAfter(startDate.minusDays(1)) && targetDate.isBefore(endDate.plusDays(1))
    }
    
    /**
     * 現在の日付が指定された月の範囲内かチェック
     */
    fun isCurrentMonthInRange(startMonth: Int, endMonth: Int): Boolean {
        val currentMonth = getCurrentMonth()
        return isMonthInRange(currentMonth, startMonth, endMonth)
    }
    
    /**
     * 現在の日付が指定された週の範囲内かチェック
     */
    fun isCurrentWeekInRange(startDate: String, endDate: String): Boolean {
        return try {
            val currentDate = java.time.LocalDate.now()
            val start = java.time.LocalDate.parse(startDate)
            val end = java.time.LocalDate.parse(endDate)
            isDateInRange(currentDate, start, end)
        } catch (e: Exception) {
            false
        }
    }
}
