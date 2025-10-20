package com.example.seedstockkeeper6.utils

import java.time.YearMonth
import java.time.format.DateTimeFormatter

object DateConversionUtils {
    
    /**
     * 旬から開始日への変換
     * 上旬: 1日, 中旬: 11日, 下旬: 21日
     */
    fun convertStageToStartDate(year: Int, month: Int, stage: String): String {
        val day = when (stage) {
            "上旬" -> 1
            "中旬" -> 11
            "下旬" -> 21
            else -> 1
        }
        return String.format("%04d-%02d-%02d", year, month, day)
    }
    
    /**
     * 旬から終了日への変換
     * 上旬: 10日, 中旬: 20日, 下旬: 月末日
     */
    fun convertStageToEndDate(year: Int, month: Int, stage: String): String {
        val day = when (stage) {
            "上旬" -> 10
            "中旬" -> 20
            "下旬" -> {
                // 月末日を取得
                val lastDayOfMonth = YearMonth.of(year, month).lengthOfMonth()
                lastDayOfMonth
            }
            else -> 10
        }
        return String.format("%04d-%02d-%02d", year, month, day)
    }
    
    /**
     * 日付から旬への逆変換（表示用）
     */
    fun convertDateToStage(dateString: String): String {
        if (dateString.isEmpty()) return ""
        
        try {
            val day = dateString.substring(8, 10).toInt()
            return when {
                day <= 10 -> "上旬"
                day <= 20 -> "中旬"
                else -> "下旬"
            }
        } catch (e: Exception) {
            return ""
        }
    }
    
    /**
     * 日付から月を取得
     */
    fun getMonthFromDate(dateString: String): Int {
        if (dateString.isEmpty()) return 0
        return try {
            // YYYY-MM-DD 形式から月を抽出
            val parts = dateString.split("-")
            if (parts.size >= 2) {
                parts[1].toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 日付から年を取得
     */
    fun getYearFromDate(dateString: String): Int {
        if (dateString.isEmpty()) return 0
        return try {
            // YYYY-MM-DD 形式から年を抽出
            val parts = dateString.split("-")
            if (parts.isNotEmpty()) {
                parts[0].toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 日付から日を取得
     */
    fun getDayFromDate(dateString: String): Int {
        if (dateString.isEmpty()) return 0
        return try {
            dateString.substring(8, 10).toInt()
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 日付文字列が有効かチェック
     */
    fun isValidDate(dateString: String): Boolean {
        if (dateString.isEmpty()) return false
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            java.time.LocalDate.parse(dateString, formatter)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 日付文字列をLocalDateに変換
     */
    fun toLocalDate(dateString: String): java.time.LocalDate? {
        if (dateString.isEmpty()) return null
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            java.time.LocalDate.parse(dateString, formatter)
        } catch (e: Exception) {
            null
        }
    }
}
