package com.example.seedstockkeeper6.model

import kotlinx.serialization.Serializable
import java.util.UUID

enum class CompanionEffectCode(val code: String, val displayName: String) {
    PEST_PREVENTION("01", "害虫予防"),
    DISEASE_PREVENTION("02", "病気予防"),
    GROWTH_PROMOTION("03", "生育促進"),
    SPACE_UTILIZATION("04", "空間活用"),
    FLAVOR_ENHANCEMENT("05", "風味向上"),
    SOIL_IMPROVEMENT("06", "土壌改善"),
    POLLINATION_PROMOTION("07", "受粉促進"),
    WEED_SUPPRESSION("08", "雑草抑制"),
    LANDSCAPE_BEAUTIFICATION("09", "景観美化"),
    MOISTURE_RETENTION("10", "水分保持"),
    SOIL_PH_ADJUSTMENT("11", "pH調整"),
    WORKABILITY_IMPROVEMENT("12", "効率UP"),
    YIELD_STABILIZATION("13", "収量安定"),
    OTHER("99", "その他");

    companion object {
        fun fromCode(code: String): CompanionEffectCode {
            return values().find { it.code == code } ?: OTHER
        }
        
        fun fromDisplayName(displayName: String): CompanionEffectCode {
            return values().find { it.displayName == displayName } ?: OTHER
        }
    }
}

@Serializable
data class CompanionPlant(
    val plant: String = "",
    val effects: List<String> = emptyList() // 2桁のコードのリスト
)

@Serializable
data class SeedPacket(
    val id: String? = null,
    val productName: String = "",
    val variety: String = "",
    val family: String = "",
    val productNumber: String = "",
    val company: String = "",
    val originCountry: String = "",
    val expirationYear: Int = 0,
    val expirationMonth: Int = 0,
    val contents: String = "",
    val germinationRate: String = "",
    val seedTreatment: String = "",
    val imageUrls: List<String> = emptyList(),
    val features: List<String> = emptyList(),
    val cultivation: Cultivation = Cultivation(),
    val calendar: List<CalendarEntry> = emptyList(),
    val companionPlants: List<CompanionPlant> = emptyList(),
    val documentId: String? = null, // FirestoreのドキュメントIDを保持する場合
    val selectedRegion: String? = null, //
    val ownerUid: String = "" // Firestoreオーナー
) {
    // 指定された月・年・期間（上旬・中旬・下旬）に播種期間が含まれるかチェック
    fun isSowingIn(month: Int, year: Int, periodIndex: Int): Boolean {
        return calendar.any { entry ->
            val sowingStartMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_start_date)
            val sowingStartYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_start_date)
            val sowingStartDay = com.example.seedstockkeeper6.utils.DateConversionUtils.getDayFromDate(entry.sowing_start_date)
            
            val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
            val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
            val sowingEndDay = com.example.seedstockkeeper6.utils.DateConversionUtils.getDayFromDate(entry.sowing_end_date)
            
            // デバッグログ出力（恋むすめの商品または現在の月のみ）
            val today = java.time.LocalDate.now()
            val currentMonth = today.monthValue
            val currentYear = today.year
            if ((productName.contains("恋むすめ") || productName.contains("恋")) || 
                (month == currentMonth && year == currentYear)) {
                android.util.Log.d("SeedPacket", "=== isSowingIn デバッグ ===")
                android.util.Log.d("SeedPacket", "チェック対象: 月=$month, 年=$year, 期間=$periodIndex")
                android.util.Log.d("SeedPacket", "entry.region: ${entry.region}")
                android.util.Log.d("SeedPacket", "sowing_start_date: ${entry.sowing_start_date}")
                android.util.Log.d("SeedPacket", "sowing_end_date: ${entry.sowing_end_date}")
                android.util.Log.d("SeedPacket", "sowingStartMonth: $sowingStartMonth, sowingStartYear: $sowingStartYear, sowingStartDay: $sowingStartDay")
                android.util.Log.d("SeedPacket", "sowingEndMonth: $sowingEndMonth, sowingEndYear: $sowingEndYear, sowingEndDay: $sowingEndDay")
            }
            
            // 同じ月・年の場合のみチェック
            if (sowingStartMonth == month && sowingStartYear == year) {
                val periodStartDay = when (periodIndex) {
                    0 -> 1   // 上旬: 1-10日
                    1 -> 11  // 中旬: 11-20日
                    2 -> 21  // 下旬: 21-31日
                    else -> 1
                }
                val periodEndDay = when (periodIndex) {
                    0 -> 10
                    1 -> 20
                    2 -> 31
                    else -> 10
                }
                
                // 播種期間がこの期間と重なるかチェック
                val isOverlap = sowingStartDay <= periodEndDay && sowingEndDay >= periodStartDay
                
                // デバッグログ出力
                if ((productName.contains("恋むすめ") || productName.contains("恋")) || 
                    (month == currentMonth && year == currentYear)) {
                    android.util.Log.d("SeedPacket", "期間チェック: periodStartDay=$periodStartDay, periodEndDay=$periodEndDay")
                    android.util.Log.d("SeedPacket", "重複チェック: sowingStartDay($sowingStartDay) <= periodEndDay($periodEndDay) && sowingEndDay($sowingEndDay) >= periodStartDay($periodStartDay)")
                    android.util.Log.d("SeedPacket", "結果: $isOverlap")
                }
                
                isOverlap
            } else {
                // デバッグログ出力
                if ((productName.contains("恋むすめ") || productName.contains("恋")) || 
                    (month == currentMonth && year == currentYear)) {
                    android.util.Log.d("SeedPacket", "月年不一致: sowingStartMonth($sowingStartMonth) != month($month) || sowingStartYear($sowingStartYear) != year($year)")
                }
                false
            }
        }
    }
    
    // 指定された月・年・期間（上旬・中旬・下旬）に収穫期間が含まれるかチェック
    fun isHarvestIn(month: Int, year: Int, periodIndex: Int): Boolean {
        return calendar.any { entry ->
            val harvestStartMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.harvest_start_date)
            val harvestStartYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.harvest_start_date)
            val harvestStartDay = com.example.seedstockkeeper6.utils.DateConversionUtils.getDayFromDate(entry.harvest_start_date)
            
            val harvestEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.harvest_end_date)
            val harvestEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.harvest_end_date)
            val harvestEndDay = com.example.seedstockkeeper6.utils.DateConversionUtils.getDayFromDate(entry.harvest_end_date)
            
            // 同じ月・年の場合のみチェック
            if (harvestStartMonth == month && harvestStartYear == year) {
                val periodStartDay = when (periodIndex) {
                    0 -> 1   // 上旬: 1-10日
                    1 -> 11  // 中旬: 11-20日
                    2 -> 21  // 下旬: 21-31日
                    else -> 1
                }
                val periodEndDay = when (periodIndex) {
                    0 -> 10
                    1 -> 20
                    2 -> 31
                    else -> 10
                }
                
                // 収穫期間がこの期間と重なるかチェック
                harvestStartDay <= periodEndDay && harvestEndDay >= periodStartDay
            } else {
                false
            }
        }
    }
}
@Serializable
data class Cultivation(
    val spacing_cm_row_min: Int = 0,
    val spacing_cm_row_max: Int = 0,
    val spacing_cm_plant_min: Int = 0,
    val spacing_cm_plant_max: Int = 0,
    val germinationTemp_c: String = "",
    val growingTemp_c: String = "",
    val soilPrep_per_sqm: SoilPrep = SoilPrep(),
    val notes: String = "",
    val harvesting: String = ""
)
@Serializable
data class SoilPrep(
    val compost_kg: Int = 0,
    val dolomite_lime_g: Int = 0,
    val chemical_fertilizer_g: Int = 0
)
@Serializable
data class CalendarEntry(
    val id: String = UUID.randomUUID().toString(),
    val region: String = "",
    
    // 播種期間（具体的な日付で保存）
    val sowing_start_date: String = "", // "2025-03-01" 形式
    val sowing_end_date: String = "",   // "2025-03-20" 形式
    
    // 収穫期間（具体的な日付で保存）
    val harvest_start_date: String = "", // "2025-06-11" 形式
    val harvest_end_date: String = ""    // "2025-06-30" 形式
)

