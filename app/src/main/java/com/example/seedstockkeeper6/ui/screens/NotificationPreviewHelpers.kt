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
        android.util.Log.w("NotificationPreviewHelpers", "ユーザーが認証されていません。デモデータを使用します。")
        return getDemoData()
    }
    
    // 種データの取得
    val seeds = try {
        android.util.Log.d("NotificationPreviewHelpers", "種データ取得開始 - UID: $uid")
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        android.util.Log.d("NotificationPreviewHelpers", "現在の月: $currentMonth")
        
        val seedsSnapshot = db.collection("seeds")
            .whereEqualTo("ownerUid", uid)
            .get().await()
        
        val seedsThisMonth = mutableListOf<SeedPacket>()
        val seedsEndingThisMonth = mutableListOf<SeedPacket>()
        
        val filteredSeeds = seedsSnapshot.documents.mapNotNull { doc ->
            try {
                val seed = doc.toObject(SeedPacket::class.java)
                if (seed != null) {
                    val seedWithId = seed.copy(id = doc.id, documentId = doc.id)
                    
                    var isThisMonthSowing = false
                    var isEndingThisMonth = false
                    
                    // 今月関連の種かどうかをチェック
                    seedWithId.calendar.forEach { entry ->
                        if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
                            try {
                                val startMonth = entry.sowing_start_date.split("-")[1].toInt()
                                val endMonth = entry.sowing_end_date.split("-")[1].toInt()
                                
                                // 今月が播種期間内かチェック
                                if (startMonth <= currentMonth && endMonth >= currentMonth) {
                                    isThisMonthSowing = true
                                }
                                
                                // 今月が播種期間の終了月かチェック
                                if (currentMonth == endMonth) {
                                    isEndingThisMonth = true
                                }
                            } catch (e: Exception) {
                                // 日付解析エラーはスキップ
                            }
                        }
                    }
                    
                    // 今月関連の種のみを返す
                    if (isThisMonthSowing || isEndingThisMonth) {
                        seedWithId
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                android.util.Log.w("NotificationPreviewHelpers", "種データ解析エラー: ${doc.id}", e)
                null
            }
        }
        
        android.util.Log.d("NotificationPreviewHelpers", "今月関連の種: ${filteredSeeds.size}件")
        filteredSeeds
    } catch (e: Exception) {
        android.util.Log.e("NotificationPreviewHelpers", "種データ取得エラー", e)
        emptyList()
    }
    
    // ユーザー設定の取得
    val userSettings = try {
        val settingsSnapshot = db.collection("users")
            .document(uid)
            .collection("settings")
            .get().await()
        
        val settings = mutableMapOf<String, String>()
        settingsSnapshot.documents.forEach { doc ->
            val data = doc.data
            data?.forEach { (key, value) ->
                if (value is String) {
                    settings[key] = value
                }
            }
        }
        android.util.Log.d("NotificationPreviewHelpers", "ユーザー設定取得成功: $settings")
        settings
    } catch (e: Exception) {
        android.util.Log.w("NotificationPreviewHelpers", "ユーザー設定取得失敗、デフォルト設定を使用: ${e.message}")
        getDefaultUserSettings()
    }
    
    // 種データが空の場合はデモデータを使用
    val finalSeeds = if (seeds.isEmpty()) {
        android.util.Log.w("NotificationPreviewHelpers", "種データが空のため、デモデータを使用します。")
        getDemoData().first
    } else {
        seeds
    }
    
    return Pair(finalSeeds, userSettings)
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
                    region = "温暖地"
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
        "seedInfoUrlProvider" to "サカタのたね",
        "customFarmOwner" to ""
    )
}

/**
 * 月次通知プレビューを構築する関数
 */
fun buildMonthlyNotificationPreview(): String {
    return """🌱 今月まき時の種:
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

