package com.example.seedstockkeeper6.model

import kotlinx.serialization.Serializable
import java.util.UUID
import com.google.firebase.firestore.PropertyName

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
    val ownerUid: String = "", // Firestoreオーナー
    @get:PropertyName("isFinished")
    var isFinished: Boolean = false, // まき終わりフラグ
    @get:PropertyName("isExpired")
    var isExpired: Boolean = false,   // 有効期限切れフラグ
    @get:PropertyName("sowingDate")
    val sowingDate: String = ""  // まいた日（"YYYY-MM-DD"形式）
) {
    // 指定された月・年・期間（上旬・中旬・下旬）に播種期間が含まれるかチェック
    fun isSowingIn(month: Int, year: Int, periodIndex: Int, isPreview: Boolean = false): Boolean {
        return calendar.any { entry ->
            val sowingStart = com.example.seedstockkeeper6.utils.DateConversionUtils.toLocalDate(entry.sowing_start_date)
            val sowingEnd = com.example.seedstockkeeper6.utils.DateConversionUtils.toLocalDate(entry.sowing_end_date)
            isPeriodInRange(sowingStart, sowingEnd, year, month, periodIndex)
        }
    }
    
    // 指定された月・年・期間（上旬・中旬・下旬）に収穫期間が含まれるかチェック
    fun isHarvestIn(month: Int, year: Int, periodIndex: Int, isPreview: Boolean = false): Boolean {
        return calendar.any { entry ->
            val harvestStart = com.example.seedstockkeeper6.utils.DateConversionUtils.toLocalDate(entry.harvest_start_date)
            val harvestEnd = com.example.seedstockkeeper6.utils.DateConversionUtils.toLocalDate(entry.harvest_end_date)
            isPeriodInRange(harvestStart, harvestEnd, year, month, periodIndex)
        }
    }
    
    // 指定された月・年が期限切れかチェック
    fun isExpired(month: Int, year: Int, isPreview: Boolean = false): Boolean {
        val currentDate = if (isPreview) {
            java.time.LocalDate.of(2025, 5, 1) // プレビュー時は2025年5月1日を使用
        } else {
            java.time.LocalDate.now()
        }
        val expirationDate = java.time.LocalDate.of(expirationYear, expirationMonth, 1)
        val targetDate = java.time.LocalDate.of(year, month, 1)
        return targetDate.isAfter(expirationDate)
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
    val harvest_end_date: String = "",   // "2025-06-30" 形式
    
    // 有効期限（年月で保存）
    val expirationYear: Int = 0,
    val expirationMonth: Int = 0
)

// 拡張関数：日付を期間キーに変換
fun dateToPeriodKey(date: java.time.LocalDate?): Int? {
    if (date == null) return null
    val month = date.monthValue
    val year = date.year
    val day = date.dayOfMonth
    val periodIndex = when {
        day <= 10 -> 0 // 上旬
        day <= 20 -> 1 // 中旬
        else -> 2      // 下旬
    }
    return (year * 100 + month) * 10 + periodIndex
}

// 拡張関数：期間が範囲内にあるかチェック
fun isPeriodInRange(
    start: java.time.LocalDate?,
    end: java.time.LocalDate?,
    year: Int,
    month: Int,
    periodIndex: Int
): Boolean {
    val startKey = dateToPeriodKey(start) ?: return false
    val endKey = dateToPeriodKey(end) ?: return false
    val targetKey = (year * 100 + month) * 10 + periodIndex
    return targetKey in startKey..endKey
}

