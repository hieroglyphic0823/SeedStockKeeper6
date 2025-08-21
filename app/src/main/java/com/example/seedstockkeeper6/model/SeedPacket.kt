package com.example.seedstockkeeper6.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CompanionPlant(
    val plant: String = "",
    val effect: String = ""
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
    val sowing_start: Int = 0,
    val sowing_start_stage: String = "",
    val sowing_end: Int = 0,
    val sowing_end_stage: String = "",
    val harvest_start: Int = 0,
    val harvest_start_stage: String = "",
    val harvest_end: Int = 0,
    val harvest_end_stage: String = ""
)

