package com.example.seedstockkeeper6.model

data class SeedPacket(
    val id: String? = null,
    val productName: String = "",
    val variety: String = "",
    val family: String = "",
    val productNumber: String = "",
    val company: String = "",
    val originCountry: String = "",
    val expirationDate: String = "",
    val contents: String = "",
    val germinationRate: String = "",
    val seedTreatment: String = "",
    val imageUrls: List<String> = emptyList(),
    val features: List<String> = emptyList(),
    val cultivation: Cultivation = Cultivation(),
    val calendar: List<CalendarEntry> = emptyList(),
    val documentId: String? = null // FirestoreのドキュメントIDを保持する場合
)

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

data class SoilPrep(
    val compost_kg: Int = 0,
    val dolomite_lime_g: Int = 0,
    val chemical_fertilizer_g: Int = 0
)

data class CalendarEntry(
    val region: String = "",
    val sowing: String = "",
    val harvest: String = ""
)
