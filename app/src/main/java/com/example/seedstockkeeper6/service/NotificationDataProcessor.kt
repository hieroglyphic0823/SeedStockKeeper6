package com.example.seedstockkeeper6.service

import android.util.Log
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
            Log.e("NotificationDataProcessor", "URLからの情報取得に失敗: $url", e)
            "情報の取得に失敗しました。"
        }
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
