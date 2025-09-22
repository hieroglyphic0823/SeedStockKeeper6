package com.example.seedstockkeeper6.preview

import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.model.Cultivation
import com.example.seedstockkeeper6.model.CalendarEntry
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.example.seedstockkeeper6.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseUser
import java.util.Calendar

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
    android.util.Log.d("PreviewHelpers", "createPreviewSeedListViewModel開始")
    val viewModel = SeedListViewModel()
    
    // プレビュー用のデモデータを設定
    val demoSeeds = listOf(
        SeedPacket(
            id = "demo1",
            productName = "今月まけるにんじん",
            variety = "ニンジン",
            family = "せり科",
            productNumber = "DEMO001",
            company = "サンプル種苗",
            originCountry = "日本",
            expirationYear = 2026,
            expirationMonth = 10,
            contents = "100粒",
            germinationRate = "85",
            seedTreatment = "無処理",
            imageUrls = emptyList(),
            features = listOf("甘みが強い", "栽培しやすい"),
            cultivation = Cultivation(),
            calendar = listOf(
                CalendarEntry(
                    id = "cal1",
                    region = "暖地",
                    sowing_start_date = "2025-05-01",
                    sowing_end_date = "2025-10-10",
                    harvest_start_date = "2025-12-11",
                    harvest_end_date = "2025-12-30"
                )
            ),
            companionPlants = emptyList(),
            documentId = "demo1",
            ownerUid = "demo_user"
        ),
        SeedPacket(
            id = "demo2",
            productName = "打越一寸",
            variety = "ダイコン",
            family = "アブラナ科",
            productNumber = "DEMO002",
            company = "サンプル種苗",
            originCountry = "日本",
            expirationYear = 2026,
            expirationMonth = 10,
            contents = "200粒",
            germinationRate = "90",
            seedTreatment = "無処理",
            imageUrls = emptyList(),
            features = listOf("大きく育つ", "甘みがある"),
            cultivation = Cultivation(),
            calendar = listOf(
                CalendarEntry(
                    id = "cal3",
                    region = "暖地",
                    sowing_start_date = "2025-09-05", // 現在の月（9月）に調整
                    sowing_end_date = "2025-09-25",   // 現在の月（9月）に調整
                    harvest_start_date = "2025-12-05",
                    harvest_end_date = "2025-12-25"
                )
            ),
            companionPlants = emptyList(),
            documentId = "demo2",
            ownerUid = "demo_user"
        ),
        SeedPacket(
            id = "demo3",
            productName = "ブロッコリー",
            variety = "緑嶺",
            family = "アブラナ科",
            productNumber = "DEMO003",
            company = "サンプル種苗",
            originCountry = "日本",
            expirationYear = 2026,
            expirationMonth = 10,
            contents = "50粒",
            germinationRate = "88",
            seedTreatment = "無処理",
            imageUrls = emptyList(),
            features = listOf("収穫量が多い", "病気に強い"),
            cultivation = Cultivation(),
            calendar = listOf(
                CalendarEntry(
                    id = "cal4",
                    region = "暖地",
                    sowing_start_date = "2025-09-10", // 現在の月（9月）に調整
                    sowing_end_date = "2025-09-30",   // 現在の月（9月）に調整
                    harvest_start_date = "2025-12-10",
                    harvest_end_date = "2025-12-31"
                )
            ),
            companionPlants = emptyList(),
            documentId = "demo3",
            ownerUid = "demo_user"
        )
    )
    
    // デモデータをViewModelに設定
    viewModel.setDemoSeeds(demoSeeds)
    
    // デバッグログ出力
    android.util.Log.d("PreviewHelpers", "プレビュー用デモデータを設定しました: ${demoSeeds.size}件")
    demoSeeds.forEach { seed ->
        android.util.Log.d("PreviewHelpers", "商品名: ${seed.productName}, 播種期間: ${seed.calendar.map { "${it.sowing_start_date}〜${it.sowing_end_date}" }}")
    }
    
    return viewModel
}

// プレビュー用のSettingsViewModel
fun createPreviewSettingsViewModel(
    isEditMode: Boolean = false,
    hasExistingData: Boolean = true
): SettingsViewModel {
    val viewModel = SettingsViewModel()
    
    // プレビュー用のデモデータを設定
    if (hasExistingData) {
        viewModel.updateFarmName("サンプル農園")
        viewModel.updateDefaultRegion("温暖地")
        viewModel.updateFarmOwner("お銀")
        viewModel.updateCustomFarmOwner("")
        // hasExistingDataをtrueに設定
        viewModel.hasExistingData = true
    }
    
    // 編集モードの設定
    if (isEditMode) {
        viewModel.enterEditMode()
    }
    
    return viewModel
}

// 農園主別のプレビュー用SettingsViewModel
fun createPreviewSettingsViewModelWithFarmOwner(
    farmOwner: String,
    customFarmOwner: String = "",
    isEditMode: Boolean = false,
    hasExistingData: Boolean = true
): SettingsViewModel {
    val viewModel = SettingsViewModel()
    
    // プレビュー用のデモデータを設定
    if (hasExistingData) {
        viewModel.updateFarmName("サンプル農園")
        viewModel.updateDefaultRegion("温暖地")
        viewModel.updateFarmOwner(farmOwner)
        viewModel.updateCustomFarmOwner(customFarmOwner)
        // hasExistingDataをtrueに設定
        viewModel.hasExistingData = true
    }
    
    // 編集モードの設定
    if (isEditMode) {
        viewModel.enterEditMode()
    }
    
    return viewModel
}

// プレビュー用のCalendarViewModel（SeedInputViewModelのラッパー）
fun createPreviewCalendarViewModel(
    isEditMode: Boolean = false,
    hasExistingData: Boolean = true
): SeedInputViewModel {
    return createPreviewSeedInputViewModel(isEditMode, hasExistingData)
}

// プレビュー用のCompanionPlantsViewModel（SeedInputViewModelのラッパー）
fun createPreviewCompanionPlantsViewModel(
    isEditMode: Boolean = false,
    hasExistingData: Boolean = true,
    hasCompanionPlants: Boolean = true
): SeedInputViewModel {
    val viewModel = SeedInputViewModel()
    
    // デモデータを設定
    val demoPacket = if (hasExistingData) {
        SeedPacket(
            id = "demo-seed-id",
            documentId = "demo-document-id",
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
                    sowing_start_date = "2025-08-15",
                    sowing_end_date = "2025-09-15",
                    harvest_start_date = "2025-10-01",
                    harvest_end_date = "2025-12-31"
                )
            ),
            cultivation = com.example.seedstockkeeper6.model.Cultivation(
                notes = "種まきは深さ1cm程度に。発芽まで土を乾かさないよう注意。間引きは本葉2-3枚の頃に行う。",
                harvesting = "根の直径が2-3cmになったら収穫。葉も食用可能。",
                soilPrep_per_sqm = com.example.seedstockkeeper6.model.SoilPrep(
                    chemical_fertilizer_g = 50
                )
            ),
            companionPlants = if (hasCompanionPlants) {
                listOf(
                    com.example.seedstockkeeper6.model.CompanionPlant(
                        plant = "ネギ",
                        effects = listOf(
                            com.example.seedstockkeeper6.model.CompanionEffectCode.PEST_PREVENTION.code,
                            com.example.seedstockkeeper6.model.CompanionEffectCode.DISEASE_PREVENTION.code
                        )
                    ),
                    com.example.seedstockkeeper6.model.CompanionPlant(
                        plant = "レタス",
                        effects = listOf(
                            com.example.seedstockkeeper6.model.CompanionEffectCode.SPACE_UTILIZATION.code,
                            com.example.seedstockkeeper6.model.CompanionEffectCode.WEED_SUPPRESSION.code
                        )
                    ),
                    com.example.seedstockkeeper6.model.CompanionPlant(
                        plant = "マリーゴールド",
                        effects = listOf(
                            com.example.seedstockkeeper6.model.CompanionEffectCode.PEST_PREVENTION.code,
                            com.example.seedstockkeeper6.model.CompanionEffectCode.LANDSCAPE_BEAUTIFICATION.code
                        )
                    )
                )
            } else {
                emptyList()
            }
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

// プレビュー用の週次通知内容生成
fun generatePreviewWeeklyNotificationContent(farmOwner: String, customFarmOwner: String = ""): String {
    val currentDate = Calendar.getInstance()
    val currentMonth = currentDate.get(Calendar.MONTH) + 1
    val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)
    
    // 農園主に応じた口調
    val tone = when (farmOwner) {
        "水戸黄門" -> "殿、まき時終了の2週間前の種がございます。今から土づくりをなされば、きっと間に合いますぞ！"
        "お銀" -> "お銀です。まき時終了の2週間前の種がありますよ。土づくりをすれば間に合いますから、頑張ってくださいね。"
        "八兵衛" -> "八兵衛でございます。まき時終了の2週間前の種がございます。土づくりをすれば間に合いますぞ！"
        "その他" -> if (customFarmOwner.isNotEmpty()) {
            "$customFarmOwner　さん、まき時終了の2週間前の種があります。土づくりをすれば間に合いますよ。"
        } else {
            "まき時終了の2週間前の種があります。土づくりをすれば間に合います。"
        }
        else -> "まき時終了の2週間前の種があります。土づくりをすれば間に合います。"
    }
    
    return """⏰ まき時終了の2週間前の種があります:

📦 あなたの登録種:
• 恋むすめ (ニンジン) - 有効期限: 2026年10月, 播種期間: 8月〜9月
  土づくりすれば間に合います！

🌿 その他の種:
• レタス - 土づくりすれば間に合います！

$tone"""
}

