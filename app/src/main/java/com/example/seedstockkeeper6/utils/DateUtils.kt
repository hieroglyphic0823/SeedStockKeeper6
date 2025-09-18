package com.example.seedstockkeeper6.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    
    /**
     * 日付文字列から旬を取得
     * @param dateString "2024-10-15" 形式の日付文字列
     * @return "10月中旬" 形式の旬文字列
     */
    fun getSeasonFromDate(dateString: String): String {
        return try {
            if (dateString.isEmpty()) return ""
            
            val parts = dateString.split("-")
            if (parts.size < 3) return ""
            
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            
            val season = when {
                day <= 10 -> "上旬"
                day <= 20 -> "中旬"
                else -> "下旬"
            }
            
            "${month}月${season}"
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * 播種期間の開始日と終了日から旬の範囲を取得
     * @param startDate 開始日 "2024-10-01"
     * @param endDate 終了日 "2024-10-31"
     * @return "10月上旬〜10月下旬" 形式の旬範囲文字列
     */
    fun getSeasonRangeFromDates(startDate: String, endDate: String): String {
        return try {
            if (startDate.isEmpty() || endDate.isEmpty()) return ""
            
            val startSeason = getSeasonFromDate(startDate)
            val endSeason = getSeasonFromDate(endDate)
            
            if (startSeason.isEmpty() || endSeason.isEmpty()) return ""
            
            // 同じ月の場合は "10月上旬〜下旬" 形式
            if (startSeason.startsWith(endSeason.substringBefore("月"))) {
                val month = startSeason.substringBefore("月")
                val startPart = startSeason.substringAfter("月")
                val endPart = endSeason.substringAfter("月")
                
                if (startPart == endPart) {
                    startSeason
                } else {
                    "${month}月${startPart}〜${endPart}"
                }
            } else {
                // 異なる月の場合は "10月上旬〜11月中旬" 形式
                "$startSeason〜$endSeason"
            }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * 月の旬を取得（日付が不明な場合）
     * @param month 月（1-12）
     * @param isStart 開始月かどうか（true: 上旬、false: 下旬）
     * @return "10月上旬" または "10月下旬" 形式
     */
    fun getMonthSeason(month: Int, isStart: Boolean = true): String {
        return try {
            val season = if (isStart) "上旬" else "下旬"
            "${month}月${season}"
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * 播種期間の月から旬の範囲を推定
     * @param startMonth 開始月
     * @param endMonth 終了月
     * @return "10月上旬〜11月下旬" 形式の旬範囲文字列
     */
    fun getSeasonRangeFromMonths(startMonth: Int, endMonth: Int): String {
        return try {
            val startSeason = getMonthSeason(startMonth, true)
            val endSeason = getMonthSeason(endMonth, false)
            
            if (startSeason == endSeason) {
                startSeason
            } else {
                "$startSeason〜$endSeason"
            }
        } catch (e: Exception) {
            ""
        }
    }
}
