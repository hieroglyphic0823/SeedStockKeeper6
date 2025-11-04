package com.example.seedstockkeeper6.ui.screens

import com.example.seedstockkeeper6.model.SeedPacket

/**
 * 並べ替えの種類
 */
enum class SortType(val displayName: String) {
    IMPORTANCE("重要度順"),
    REGISTRATION("登録順"),
    NAME("あいうえお順"),
    STATUS("状態順")
}

/**
 * 種の状態を判定する関数
 * 
 * 種の状態を以下の優先順位で判定します：
 * 1. まき終わり（最優先）
 * 2. 有効期限切れ
 * 3. 終了間近
 * 4. 今月まきどき
 * 5. 通常
 * 
 * @param seed 判定対象の種情報
 * @return 種の状態を表す文字列（"finished", "expired", "urgent", "thisMonth", "normal"）
 */
fun getSeedStatus(seed: SeedPacket): String {
    val currentDate = java.time.LocalDate.now()
    val currentMonth = currentDate.monthValue
    val currentYear = currentDate.year
    
    // 1. まき終わりの判定（最優先）
    if (seed.isFinished) return "finished"
    
    // 2. 期限切れの判定
    if (seed.isExpired) return "expired"
    
    // 3. 終了間近の判定
    val isUrgent = seed.calendar.any { entry ->
        val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
        val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
        sowingEndMonth == currentMonth && sowingEndYear == currentYear
    }
    if (isUrgent) return "urgent"
    
    // 4. 今月まける種の判定
    val isThisMonth = seed.calendar.any { entry ->
        if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
            try {
                val startMonth = entry.sowing_start_date.split("-")[1].toInt()
                val endMonth = entry.sowing_end_date.split("-")[1].toInt()
                startMonth <= currentMonth && endMonth >= currentMonth
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }
    if (isThisMonth) return "thisMonth"
    
    return "normal"
}

