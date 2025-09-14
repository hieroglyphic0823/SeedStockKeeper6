package com.example.seedstockkeeper6.preview

import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.example.seedstockkeeper6.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseUser

// プレビュー用のモックFirebaseUser（簡易版）
fun createMockFirebaseUser(): FirebaseUser? = null

// プレビュー用のSeedInputViewModel
fun createPreviewSeedInputViewModel(
    isEditMode: Boolean = false,
    hasExistingData: Boolean = true
): SeedInputViewModel {
    val viewModel = SeedInputViewModel()
    
    // デモデータを設定
    val demoPacket = if (hasExistingData) {
        SeedPacket(
            id = "demo-seed-id", // DisplayModeの判定用にIDを設定
            documentId = "demo-document-id", // DisplayModeの判定用にdocumentIdを設定
            productName = "恋むすめ",
            variety = "ニンジン",
            family = "せり科",
            expirationYear = 2026,
            expirationMonth = 10,
            productNumber = "CR-2024-001",
            company = "サカタのタネ",
            originCountry = "日本",
            contents = "5ml",
            germinationRate = "85",
            seedTreatment = "無処理",
            imageUrls = listOf(
                "https://picsum.photos/300/400?random=1",
                "https://picsum.photos/300/400?random=2"
            ),
            calendar = listOf(
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "関東",
                    sowing_start_date = "2025-08-15", // 8月15日（中旬）
                    sowing_end_date = "2025-09-15",   // 9月15日（中旬）
                    harvest_start_date = "2025-10-01", // 10月1日（上旬）
                    harvest_end_date = "2025-12-31"    // 12月31日（下旬）
                )
            ),
            cultivation = com.example.seedstockkeeper6.model.Cultivation(
                notes = "種まきは深さ1cm程度に。発芽まで土を乾かさないよう注意。間引きは本葉2-3枚の頃に行う。",
                harvesting = "根の直径が2-3cmになったら収穫。葉も食用可能。",
                soilPrep_per_sqm = com.example.seedstockkeeper6.model.SoilPrep(
                    chemical_fertilizer_g = 50
                )
            ),
            companionPlants = listOf(
                com.example.seedstockkeeper6.model.CompanionPlant(
                    plant = "ネギ",
                    effects = listOf(com.example.seedstockkeeper6.model.CompanionEffectCode.PEST_PREVENTION.code)
                ),
                com.example.seedstockkeeper6.model.CompanionPlant(
                    plant = "レタス",
                    effects = listOf(com.example.seedstockkeeper6.model.CompanionEffectCode.SPACE_UTILIZATION.code)
                )
            )
        )
    } else {
        SeedPacket() // 新規作成の場合は空のパケット
    }
    
    viewModel.setSeed(demoPacket)
    
    // 編集モードの場合は編集モードを有効にする
    if (isEditMode) {
        viewModel.enterEditMode()
    }
    
    return viewModel
}

// プレビュー用のSeedListViewModel
fun createPreviewSeedListViewModel(): SeedListViewModel {
    val viewModel = SeedListViewModel()
    // 必要に応じてデモデータを設定
    return viewModel
}

// プレビュー用のSettingsViewModel
fun createPreviewSettingsViewModel(
    isEditMode: Boolean = false,
    hasExistingData: Boolean = true
): SettingsViewModel {
    val viewModel = SettingsViewModel()
    // プレビュー用の状態設定は内部で行う
    
    return viewModel
}
