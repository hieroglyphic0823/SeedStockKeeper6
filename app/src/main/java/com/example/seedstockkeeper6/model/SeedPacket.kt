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
    MOISTURE_RETENTION("10", "水分保持の助け"),
    SOIL_PH_ADJUSTMENT("11", "土壌pHの調整"),
    WORKABILITY_IMPROVEMENT("12", "作業性向上"),
    YIELD_STABILIZATION("13", "収量の安定化"),
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
)
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
    val sowing_start_year: Int = 0,
    val sowing_start: Int = 0,
    val sowing_start_stage: String = "",
    val sowing_end_year: Int = 0,
    val sowing_end: Int = 0,
    val sowing_end_stage: String = "",
    val harvest_start_year: Int = 0,
    val harvest_start: Int = 0,
    val harvest_start_stage: String = "",
    val harvest_end_year: Int = 0,
    val harvest_end: Int = 0,
    val harvest_end_stage: String = ""
)

