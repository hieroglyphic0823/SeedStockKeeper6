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
            calendar = listOf(
                com.example.seedstockkeeper6.model.CalendarEntry(
                    region = "関東",
                    sowing_start_date = "2025-08-15",
                    sowing_end_date = "2025-09-15",
                    harvest_start_date = "2025-10-01",
                    harvest_end_date = "2025-12-31"
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
