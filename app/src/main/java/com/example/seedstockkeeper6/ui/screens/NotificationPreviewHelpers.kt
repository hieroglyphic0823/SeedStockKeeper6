package com.example.seedstockkeeper6.ui.screens

import com.example.seedstockkeeper6.model.SeedPacket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * 種情報URLを取得する関数
 */
fun getSeedInfoUrl(userSettings: Map<String, String>): String {
    val provider = userSettings["seedInfoUrlProvider"] ?: "サカタのたね"
    val customUrl = userSettings["customSeedInfoUrl"] ?: ""
    
    return when (provider) {
        "サカタのたね" -> "https://sakata-netshop.com/shop/default.aspx"
        "たねのタキイ" -> "https://sakata-netshop.com/shop/pages/sowingcalendar.aspx"
        "その他" -> customUrl
        else -> "https://sakata-netshop.com/shop/default.aspx"
    }
}

/**
 * ユーザーデータを読み込む関数
 */
suspend fun loadUserData(
    auth: FirebaseAuth,
    db: com.google.firebase.firestore.FirebaseFirestore
): Pair<List<SeedPacket>, Map<String, String>> {
    val uid = auth.currentUser?.uid
    if (uid == null) {
        return getDemoData()
    }
    
    // 種データの取得
    val seeds = try {
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        
        val seedsSnapshot = db.collection("seeds")
            .whereEqualTo("ownerUid", uid)
            .get().await()
        
        val seedsThisMonth = mutableListOf<SeedPacket>()
        val seedsEndingThisMonth = mutableListOf<SeedPacket>()
        
        val allSeeds = seedsSnapshot.documents.mapNotNull { doc ->
            try {
                val seed = doc.toObject(SeedPacket::class.java)
                if (seed != null) {
                    val seedWithId = seed.copy(id = doc.id, documentId = doc.id)
                    seedWithId
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
        
        allSeeds
    } catch (e: Exception) {
        emptyList()
    }
    
    // ユーザー設定の取得
    val userSettings = try {
        val settingsDoc = db.collection("users")
            .document(uid)
            .collection("settings")
            .document("general")
            .get().await()
        
        val settings = mutableMapOf<String, String>()
        if (settingsDoc.exists()) {
            val data = settingsDoc.data
            data?.forEach { (key, value) ->
                when (value) {
                    is String -> settings[key] = value
                    is Double -> settings[key] = value.toString()
                    is Long -> settings[key] = value.toString()
                    is Boolean -> settings[key] = value.toString()
                }
            }
        }
        settings
    } catch (e: Exception) {
        getDefaultUserSettings()
    }
    
    // デモデータは使用しない - 実際のユーザーデータのみを使用
    return Pair(seeds, userSettings)
}

/**
 * デモデータを取得する関数
 */
fun getDemoData(): Pair<List<SeedPacket>, Map<String, String>> {
    val demoSeeds = listOf(
        SeedPacket(
            id = "demo1",
            documentId = "demo1",
            productName = "恋むすめ",
            variety = "ニンジン",
            family = "せり科",
            germinationRate = "85",
            expirationYear = 2026,
            expirationMonth = 10,
            calendar = listOf(
                com.example.seedstockkeeper6.model.CalendarEntry(
                    sowing_start_date = "2024-03-01",
                    sowing_end_date = "2024-05-31",
                    harvest_start_date = "2024-07-01",
                    harvest_end_date = "2024-09-30",
                    region = "温暖地",
                    expirationYear = 2026,
                    expirationMonth = 10
                )
            ),
            companionPlants = listOf(
                com.example.seedstockkeeper6.model.CompanionPlant(
                    plant = "レタス",
                    effects = listOf("01", "02")
                ),
                com.example.seedstockkeeper6.model.CompanionPlant(
                    plant = "ネギ",
                    effects = listOf("01", "03")
                )
            ),
            cultivation = com.example.seedstockkeeper6.model.Cultivation(
                notes = "デモデータ"
            ),
            imageUrls = emptyList(),
            ownerUid = "demo"
        )
    )
    
    val demoSettings = mapOf(
        "farmOwner" to "水戸黄門",
        "defaultRegion" to "温暖地",
        "selectedPrefecture" to "茨城県",
        "seedInfoUrlProvider" to "サカタのたね",
        "customFarmOwner" to ""
    )
    
    return Pair(demoSeeds, demoSettings)
}

/**
 * デフォルトのユーザー設定を取得する関数
 */
private fun getDefaultUserSettings(): Map<String, String> {
    return mapOf(
        "farmOwner" to "水戸黄門",
        "defaultRegion" to "温暖地",
        "selectedPrefecture" to "茨城県",
        "farmAddress" to "茨城県水戸市",
        "seedInfoUrlProvider" to "サカタのたね",
        "customFarmOwner" to ""
    )
}

/**
 * 月次通知プレビューを構築する関数
 */
fun buildMonthlyNotificationPreview(): String {
    return """🌱 まきどきの種:
• 恋むすめ (ニンジン) - 播種期間: 8月上旬〜9月下旬
• サラダミックス (レタス) - 播種期間: 3月中旬〜5月上旬

🌟 季節のおすすめ:
• 春野菜の種まきシーズンです
• トマト、ナス、ピーマンの準備を始めましょう
• レタス、キャベツの種まきが最適です

⚠️ まき時終了間近:
• 春菊 (中葉春菊) - 播種期間: 8月下旬〜9月中旬"""
}

/**
 * 週次通知プレビューを構築する関数
 */
fun buildWeeklyNotificationPreview(): String {
    return """⏰ まき時終了の2週間前の種があります:

• 恋むすめ (ニンジン) - 播種期間: 8月上旬〜9月下旬
  土づくりすれば間に合います！

• 大根 (青首大根) - 播種期間: 8月中旬〜10月上旬
  土づくりすれば間に合います！"""
}

/**
 * おすすめの種情報を取得する関数
 */
fun getRecommendedSeedsInfo(userSettings: Map<String, String>): String {
    val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
    
    return when (currentMonth) {
        1 -> "• 春キャベツ - 寒さに強く、春の収穫に最適\n• レタス - 早春の種まきで新鮮なサラダを\n• ホウレンソウ - 栄養豊富で育てやすい"
        2 -> "• トマト - 夏野菜の定番、苗から育てる\n• ナス - 紫色の美しい実が楽しめる\n• ピーマン - カラフルで栄養価が高い"
        3 -> "• キュウリ - 夏の定番野菜、つる性\n• オクラ - ネバネバ成分で健康に良い\n• ゴーヤ - 苦味が特徴の夏野菜"
        4 -> "• カボチャ - 秋の収穫、保存がきく\n• サツマイモ - 甘くて栄養豊富\n• 大根 - 冬の定番野菜"
        5 -> "• 白菜 - 冬の鍋物に欠かせない\n• ブロッコリー - 栄養価が高い緑黄色野菜\n• カリフラワー - 白い花蕾が美しい"
        6 -> "• ネギ - 薬味として重宝\n• ニラ - 独特の香りが特徴\n• ニンニク - 香り高い調味料"
        7 -> "• トウモロコシ - 夏の甘い味覚\n• 枝豆 - ビールのおつまみに最適\n• エダマメ - タンパク質豊富"
        8 -> "• スイカ - 夏の定番果物\n• メロン - 甘くて香り高い\n• カボチャ - 秋の収穫準備"
        9 -> "• ダイコン - 冬の定番野菜\n• カブ - 根と葉の両方を楽しめる\n• ニンジン - カロテン豊富な根菜"
        10 -> "• ハクサイ - 冬の鍋物の主役\n• キャベツ - 一年中楽しめる葉物\n• レタス - サラダの定番"
        11 -> "• ブロッコリー - 栄養価の高い緑黄色野菜\n• カリフラワー - 白い花蕾が美しい\n• ケール - スーパーフードとして注目"
        12 -> "• 春菊 - 冬の鍋物に欠かせない\n• 水菜 - シャキシャキした食感\n• 小松菜 - 栄養価が高い緑黄色野菜"
        else -> "• 季節の野菜 - 今が種まきの最適期\n• 栄養豊富な野菜 - 健康な食生活に\n• 育てやすい野菜 - 初心者にもおすすめ"
    }
}

