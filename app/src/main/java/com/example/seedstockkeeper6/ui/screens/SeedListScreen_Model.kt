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
    
    android.util.Log.d("getSeedStatus", "状態判定開始 - isFinished=${seed.isFinished}, isExpired=${seed.isExpired}, sowingDate=${seed.sowingDate}, calendar.size=${seed.calendar.size}")
    
    // 1. まき終わりの判定（最優先）
    if (seed.isFinished) {
        android.util.Log.d("getSeedStatus", "状態判定結果: finished (isFinished=true)")
        return "finished"
    }
    
    // 2. 期限切れの判定
    if (seed.isExpired) {
        android.util.Log.d("getSeedStatus", "状態判定結果: expired (isExpired=true)")
        return "expired"
    }
    
    // 3. 終了間近の判定
    val isUrgent = seed.calendar.any { entry ->
        val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
        val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
        sowingEndMonth == currentMonth && sowingEndYear == currentYear
    }
    if (isUrgent) {
        android.util.Log.d("getSeedStatus", "状態判定結果: urgent (終了間近)")
        return "urgent"
    }
    
    // 4. 今月まける種の判定（年をまたぐ期間にも対応）
    val isThisMonth = seed.calendar.any { entry ->
        if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
            try {
                val startMonth = entry.sowing_start_date.split("-")[1].toInt()
                val endMonth = entry.sowing_end_date.split("-")[1].toInt()
                val startYear = entry.sowing_start_date.split("-")[0].toInt()
                val endYear = entry.sowing_end_date.split("-")[0].toInt()
                
                // 年をまたぐ期間にも対応：現在の月・年が播種期間内にあるかチェック
                val isInPeriod = (startYear < currentYear || (startYear == currentYear && startMonth <= currentMonth)) &&
                                 (endYear > currentYear || (endYear == currentYear && endMonth >= currentMonth))
                
                android.util.Log.d("getSeedStatus", "今月まきどき判定 - startDate=${entry.sowing_start_date}, endDate=${entry.sowing_end_date}, currentDate=$currentDate, isInPeriod=$isInPeriod")
                isInPeriod
            } catch (e: Exception) {
                android.util.Log.e("getSeedStatus", "今月まきどき判定エラー: ${e.message}", e)
                false
            }
        } else {
            false
        }
    }
    if (isThisMonth) {
        android.util.Log.d("getSeedStatus", "状態判定結果: thisMonth (今月まきどき)")
        return "thisMonth"
    }
    
    android.util.Log.d("getSeedStatus", "状態判定結果: normal (通常)")
    return "normal"
}

