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
        
        // 実際の実装では、seedInfoの内容を解析して今月の種情報を抽出
        // ここではデモ用の推奨種情報を生成
        return when (month) {
            1 -> "• 春キャベツ - 寒さに強く、春の収穫に最適\n• レタス - 早春の種まきで新鮮なサラダを\n• ホウレンソウ - 栄養豊富で育てやすい"
            2 -> "• トマト - 夏野菜の定番、苗から育てる\n• ナス - 紫色の美しい実が楽しめる\n• ピーマン - カラフルで栄養価が高い"
            3 -> "• キュウリ - 夏の定番野菜、つる性\n• オクラ - ネバネバ成分で健康に良い\n• ゴーヤ - 苦味が特徴の夏野菜"
            4 -> "• カボチャ - 秋の収穫、保存がきく\n• サツマイモ - 甘くて栄養豊富\n• 大根 - 冬の定番野菜"
            5 -> "• 白菜 - 冬の鍋物に欠かせない\n• ブロッコリー - 栄養価が高い緑黄色野菜\n• カリフラワー - 白い花蕾が美しい"
            6 -> "• ネギ - 薬味として重宝\n• ニラ - 独特の香りが特徴\n• ニンニク - 香り高い調味料"
            7 -> "• トウモロコシ - 夏の甘い味覚\n• 枝豆 - ビールのおつまみに最適\n• エダマメ - タンパク質豊富"
            8 -> "• スイカ - 夏の定番果物\n• メロン - 甘くて香り高い\n• カボチャ - 秋の収穫準備"
            9 -> "• ダイコン - 冬の定番野菜\n• カブ - 根と葉の両方を楽しめる\n• ニンジン - カロテン豊富な根菜"
            10 -> "• ハクサイ - 冬の鍋物の主役\n• キャベツ - 一年中楽しめる葉物\n• レタス - サラダの定番"
            11 -> "• ブロッコリー - 栄養価の高い緑黄色野菜\n• カリフラワー - 白い花蕾が美しい\n• ケール - スーパーフードとして注目"
            12 -> "• ホウレンソウ - 冬の栄養補給\n• 小松菜 - 寒さに強く育てやすい\n• チンゲンサイ - 中華料理に欠かせない"
            else -> "• 季節の野菜 - その時期に適した種を選びましょう"
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
