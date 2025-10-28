package com.example.seedstockkeeper6.utils

import com.example.seedstockkeeper6.model.SeedPacket
import java.time.LocalDate

/**
 * 播種計算用のユーティリティクラス
 * お城画面と種目録画面で統一された計算ロジックを提供
 */
object SowingCalculationUtils {
    
    /**
     * 今月まきどきの種を抽出（統一ロジック）
     * @param seeds 種のリスト
     * @param currentDate 現在の日付（テスト用に指定可能）
     * @param excludeFinished まき終わった種を除外するかどうか
     * @return 今月まきどきの種のリスト
     */
    fun getThisMonthSowingSeeds(
        seeds: List<SeedPacket>,
        currentDate: LocalDate = LocalDate.now(),
        excludeFinished: Boolean = true
    ): List<SeedPacket> {
        val currentMonth = currentDate.monthValue
        val currentYear = currentDate.year
        
        return seeds.filter { seed ->
            // まき終わった種を除外するかチェック
            if (excludeFinished && seed.isFinished) {
                return@filter false
            }
            
            // 今月が播種期間内にあるかチェック
            seed.calendar.any { entry ->
                isInSowingPeriod(entry, currentMonth, currentYear)
            }
        }
    }
    
    /**
     * 終了間近の種を抽出（統一ロジック）
     * @param seeds 種のリスト
     * @param currentDate 現在の日付（テスト用に指定可能）
     * @return 終了間近の種のリスト
     */
    fun getUrgentSeeds(
        seeds: List<SeedPacket>,
        currentDate: LocalDate = LocalDate.now()
    ): List<SeedPacket> {
        val currentMonth = currentDate.monthValue
        val currentYear = currentDate.year
        
        return seeds.filter { seed ->
            seed.calendar.any { entry ->
                val sowingEndMonth = DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
                val sowingEndYear = DateConversionUtils.getYearFromDate(entry.sowing_end_date)
                sowingEndMonth == currentMonth && sowingEndYear == currentYear
            }
        }
    }
    
    /**
     * 指定された月・年が播種期間内にあるかチェック
     * @param entry カレンダーエントリ
     * @param targetMonth 対象月
     * @param targetYear 対象年
     * @return 播種期間内にあるかどうか
     */
    private fun isInSowingPeriod(
        entry: com.example.seedstockkeeper6.model.CalendarEntry,
        targetMonth: Int,
        targetYear: Int
    ): Boolean {
        if (entry.sowing_start_date.isEmpty() || entry.sowing_end_date.isEmpty()) {
            return false
        }
        
        return try {
            val startMonth = entry.sowing_start_date.split("-")[1].toInt()
            val endMonth = entry.sowing_end_date.split("-")[1].toInt()
            val startYear = entry.sowing_start_date.split("-")[0].toInt()
            val endYear = entry.sowing_end_date.split("-")[0].toInt()
            
            // 今月が播種期間内にあるかチェック
            (startYear < targetYear || (startYear == targetYear && startMonth <= targetMonth)) &&
            (endYear > targetYear || (endYear == targetYear && endMonth >= targetMonth))
        } catch (e: Exception) {
            false
        }
    }
}

