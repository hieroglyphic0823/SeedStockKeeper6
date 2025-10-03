package com.example.seedstockkeeper6.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.model.MonthlyStatistics
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.example.seedstockkeeper6.service.GeminiNotificationService
import com.example.seedstockkeeper6.service.StatisticsService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * 月次通知用のWorker
 */
class MonthlyNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val notificationManager = NotificationManager(context)
    private val geminiService = GeminiNotificationService()
    private val statisticsService = StatisticsService()
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    
    override suspend fun doWork(): Result {
        return try {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                return Result.failure()
            }
            
            // ユーザーの通知設定を確認
            android.util.Log.d("MonthlyNotificationWorker", "通知設定取得開始 - UID: $uid")
            val notificationSettings = getNotificationSettings(uid)
            android.util.Log.d("MonthlyNotificationWorker", "取得した通知設定: $notificationSettings")
            
            if (notificationSettings["notificationFrequency"] != "月一回") {
                android.util.Log.d("MonthlyNotificationWorker", "月次通知が無効のため終了")
                return Result.success()
            }
            
            // ユーザーの地域・県設定を取得
            val region = notificationSettings["defaultRegion"] ?: "温暖地"
            val prefecture = notificationSettings["selectedPrefecture"] ?: ""
            val seedInfoUrl = getSeedInfoUrl(notificationSettings)
            android.util.Log.d("MonthlyNotificationWorker", "地域設定 - region: $region, prefecture: $prefecture, seedInfoUrl: $seedInfoUrl")
            
            // 農園主設定を取得
            val farmOwner = notificationSettings["farmOwner"] as? String ?: "水戸黄門"
            val customFarmOwner = notificationSettings["customFarmOwner"] as? String ?: ""
            android.util.Log.d("MonthlyNotificationWorker", "農園主設定 - farmOwner: $farmOwner, customFarmOwner: $customFarmOwner")
            
            // 現在の月を取得
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            
            // 集計データを取得（効率化）
            val monthlyStatistics = statisticsService.getCurrentMonthStatistics(uid)
            val userSeeds = if (monthlyStatistics != null && monthlyStatistics.isValid()) {
                // 集計データが有効な場合は、最小限のデータのみ取得
                getSeedsForUserOptimized(uid, monthlyStatistics)
            } else {
                // 集計データが古い場合は従来の方法で取得
                getSeedsForUser(uid)
            }
            
            // GeminiAPIで通知内容を生成
            val notificationContent = geminiService.generateMonthlyNotificationContent(
                region = region,
                prefecture = prefecture,
                seedInfoUrl = seedInfoUrl,
                currentMonth = currentMonth,
                userSeeds = userSeeds,
                farmOwner = farmOwner,
                customFarmOwner = customFarmOwner
            )
            
            // 通知を送信
            notificationManager.sendMonthlyRecommendationNotificationWithContent(notificationContent)
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("MonthlyNotificationWorker", "月次通知の送信に失敗", e)
            Result.failure()
        }
    }
    
    private suspend fun getNotificationSettings(uid: String): Map<String, String> {
        return try {
            android.util.Log.d("MonthlyNotificationWorker", "Firebase設定取得開始 - UID: $uid")
            val settingsDoc = db.collection("users").document(uid)
                .collection("settings").document("general").get().await()
            android.util.Log.d("MonthlyNotificationWorker", "Firebase設定取得完了")
            
            android.util.Log.d("MonthlyNotificationWorker", "Firebase設定ドキュメント存在: ${settingsDoc.exists()}")
            
            if (settingsDoc.exists()) {
                val farmOwnerFromFirebase = settingsDoc.getString("farmOwner")
                val customFarmOwnerFromFirebase = settingsDoc.getString("customFarmOwner")
                android.util.Log.d("MonthlyNotificationWorker", "Firebaseから取得 - farmOwner: $farmOwnerFromFirebase, customFarmOwner: $customFarmOwnerFromFirebase")
                
                val settings = mapOf(
                    "notificationFrequency" to (settingsDoc.getString("notificationFrequency") ?: "なし"),
                    "selectedWeekday" to (settingsDoc.getString("selectedWeekday") ?: "月曜日"),
                    "defaultRegion" to (settingsDoc.getString("defaultRegion") ?: "温暖地"),
                    "selectedPrefecture" to (settingsDoc.getString("selectedPrefecture") ?: ""),
                    "farmOwner" to (farmOwnerFromFirebase ?: "水戸黄門"),
                    "customFarmOwner" to (customFarmOwnerFromFirebase ?: ""),
                    "seedInfoUrlProvider" to (settingsDoc.getString("seedInfoUrlProvider") ?: "サカタのたね"),
                    "customSeedInfoUrl" to (settingsDoc.getString("customSeedInfoUrl") ?: "")
                )
                android.util.Log.d("MonthlyNotificationWorker", "取得した設定: $settings")
                settings
            } else {
                android.util.Log.d("MonthlyNotificationWorker", "Firebase設定ドキュメントが存在しません")
                val defaultSettings = mapOf(
                    "notificationFrequency" to "なし",
                    "defaultRegion" to "温暖地",
                    "selectedPrefecture" to "",
                    "farmOwner" to "水戸黄門",
                    "customFarmOwner" to "",
                    "seedInfoUrlProvider" to "サカタのたね",
                    "customSeedInfoUrl" to ""
                )
                android.util.Log.d("MonthlyNotificationWorker", "デフォルト設定を使用: $defaultSettings")
                defaultSettings
            }
        } catch (e: Exception) {
            android.util.Log.e("MonthlyNotificationWorker", "通知設定の取得に失敗", e)
            val errorSettings = mapOf(
                "notificationFrequency" to "なし",
                "defaultRegion" to "温暖地",
                "selectedPrefecture" to "",
                "farmOwner" to "水戸黄門",
                "customFarmOwner" to "",
                "seedInfoUrlProvider" to "サカタのたね",
                "customSeedInfoUrl" to ""
            )
            android.util.Log.d("MonthlyNotificationWorker", "エラー時デフォルト設定: $errorSettings")
            errorSettings
        }
    }
    
    private fun getSeedInfoUrl(settings: Map<String, String>): String {
        val provider = settings["seedInfoUrlProvider"] ?: "サカタのたね"
        val customUrl = settings["customSeedInfoUrl"] ?: ""
        
        return when (provider) {
            "サカタのたね" -> "https://sakata-netshop.com/shop/default.aspx"
            "たねのタキイ" -> "https://sakata-netshop.com/shop/pages/sowingcalendar.aspx"
            "その他" -> customUrl
            else -> "https://sakata-netshop.com/shop/default.aspx"
        }
    }
    
    private suspend fun getSeedsForUser(uid: String): List<SeedPacket> {
        return try {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            android.util.Log.d("MonthlyNotificationWorker", "種データ取得開始 - UID: $uid, 現在の月: $currentMonth")
            
            val snapshot = db.collection("seeds")
                .whereEqualTo("ownerUid", uid)
                .get().await()
            
            val seedsThisMonth = mutableListOf<SeedPacket>()
            val seedsEndingThisMonth = mutableListOf<SeedPacket>()
            
            val seeds = snapshot.documents.mapNotNull { doc ->
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
                        
                        if (isThisMonthSowing || isEndingThisMonth) {
                            if (isThisMonthSowing) {
                                seedsThisMonth.add(seedWithId)
                                android.util.Log.d("MonthlyNotificationWorker", "今月蒔ける種発見: ${seedWithId.productName}")
                            }
                            if (isEndingThisMonth) {
                                seedsEndingThisMonth.add(seedWithId)
                                android.util.Log.d("MonthlyNotificationWorker", "今月蒔き時終了の種発見: ${seedWithId.productName}")
                            }
                            seedWithId
                        } else {
                            null
                        }
                    } else {
                        android.util.Log.w("MonthlyNotificationWorker", "Failed to convert document ${doc.id} to SeedPacket")
                        null
                    }
                } catch (e: Exception) {
                    android.util.Log.w("MonthlyNotificationWorker", "種データの変換に失敗: ${doc.id}", e)
                    null
                }
            }
            
            android.util.Log.d("MonthlyNotificationWorker", "種データ取得完了 - 全件数: ${snapshot.documents.size}, 今月関連: ${seeds.size}")
            android.util.Log.d("MonthlyNotificationWorker", "今月蒔ける種: ${seedsThisMonth.size}件, 今月蒔き時終了の種: ${seedsEndingThisMonth.size}件")
            seeds
        } catch (e: Exception) {
            android.util.Log.e("MonthlyNotificationWorker", "種データの取得に失敗", e)
            emptyList()
        }
    }
    
    private fun getSeedsToSowThisMonth(seeds: List<SeedPacket>, currentMonth: Int): List<SeedPacket> {
        return seeds.filter { seed ->
            seed.calendar.any { entry ->
                val startMonth = extractMonthFromDate(entry.sowing_start_date)
                val endMonth = extractMonthFromDate(entry.sowing_end_date)
                currentMonth in startMonth..endMonth
            }
        }
    }
    
    private fun getSeedsEndingThisMonth(seeds: List<SeedPacket>, currentMonth: Int): List<SeedPacket> {
        return seeds.filter { seed ->
            seed.calendar.any { entry ->
                val endMonth = extractMonthFromDate(entry.sowing_end_date)
                endMonth == currentMonth
            }
        }
    }
    
    private fun extractMonthFromDate(dateString: String): Int {
        return try {
            // "2025-04-20" 形式から月を抽出
            dateString.split("-")[1].toInt()
        } catch (e: Exception) {
            1 // デフォルト値
        }
    }
    
    private fun getSeasonalRecommendations(currentMonth: Int): List<String> {
        return when (currentMonth) {
            3, 4, 5 -> listOf("春野菜の種まきシーズンです", "トマト、ナス、ピーマンの準備を始めましょう", "レタス、キャベツの種まきが最適です")
            6, 7, 8 -> listOf("夏野菜の種まきシーズンです", "キュウリ、ズッキーニの種まきが最適です", "オクラ、モロヘイヤの種まきを始めましょう")
            9, 10, 11 -> listOf("秋野菜の種まきシーズンです", "大根、カブの種まきが最適です", "ホウレンソウ、小松菜の種まきを始めましょう")
            12, 1, 2 -> listOf("冬野菜の種まきシーズンです", "室内での種まきがおすすめです", "春の準備として、プランター栽培を始めましょう")
            else -> emptyList()
        }
    }
}

/**
 * 週次通知用のWorker
 */
class WeeklyNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val notificationManager = NotificationManager(context)
    private val geminiService = GeminiNotificationService()
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    
    override suspend fun doWork(): Result {
        return try {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                return Result.failure()
            }
            
            // ユーザーの通知設定を確認
            val notificationSettings = getNotificationSettings(uid)
            android.util.Log.d("WeeklyNotificationWorker", "取得した通知設定: $notificationSettings")
            
            if (notificationSettings["notificationFrequency"] != "週１回") {
                return Result.success()
            }
            
            // ユーザーの地域・県設定を取得
            val region = notificationSettings["defaultRegion"] ?: "温暖地"
            val prefecture = notificationSettings["selectedPrefecture"] ?: ""
            val seedInfoUrl = getSeedInfoUrl(notificationSettings)
            
            // 農園主設定を取得
            val farmOwner = notificationSettings["farmOwner"] as? String ?: "水戸黄門"
            val customFarmOwner = notificationSettings["customFarmOwner"] as? String ?: ""
            android.util.Log.d("WeeklyNotificationWorker", "農園主設定 - farmOwner: $farmOwner, customFarmOwner: $customFarmOwner")
            
            // ユーザーの種データを取得
            val userSeeds = getSeedsForUser(uid)
            
            // GeminiAPIで通知内容を生成
            val notificationContent = geminiService.generateWeeklyNotificationContent(
                region = region,
                prefecture = prefecture,
                seedInfoUrl = seedInfoUrl,
                userSeeds = userSeeds,
                farmOwner = farmOwner,
                customFarmOwner = customFarmOwner
            )
            
            // 通知を送信
            notificationManager.sendWeeklyReminderNotificationWithContent(notificationContent)
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("WeeklyNotificationWorker", "週次通知の送信に失敗", e)
            Result.failure()
        }
    }
    
    private suspend fun getNotificationSettings(uid: String): Map<String, String> {
        return try {
            val settingsDoc = db.collection("users").document(uid)
                .collection("settings").document("general").get().await()
            
            if (settingsDoc.exists()) {
                mapOf(
                    "notificationFrequency" to (settingsDoc.getString("notificationFrequency") ?: "なし"),
                    "selectedWeekday" to (settingsDoc.getString("selectedWeekday") ?: "月曜日"),
                    "defaultRegion" to (settingsDoc.getString("defaultRegion") ?: "温暖地"),
                    "selectedPrefecture" to (settingsDoc.getString("selectedPrefecture") ?: ""),
                    "farmOwner" to (settingsDoc.getString("farmOwner") ?: "水戸黄門"),
                    "customFarmOwner" to (settingsDoc.getString("customFarmOwner") ?: ""),
                    "seedInfoUrlProvider" to (settingsDoc.getString("seedInfoUrlProvider") ?: "サカタのたね"),
                    "customSeedInfoUrl" to (settingsDoc.getString("customSeedInfoUrl") ?: "")
                )
            } else {
                mapOf(
                    "notificationFrequency" to "なし",
                    "defaultRegion" to "温暖地",
                    "selectedPrefecture" to "",
                    "farmOwner" to "水戸黄門",
                    "customFarmOwner" to "",
                    "seedInfoUrlProvider" to "サカタのたね",
                    "customSeedInfoUrl" to ""
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("WeeklyNotificationWorker", "通知設定の取得に失敗", e)
            mapOf(
                "notificationFrequency" to "なし",
                "defaultRegion" to "温暖地",
                "selectedPrefecture" to "",
                "farmOwner" to "水戸黄門",
                "customFarmOwner" to "",
                "seedInfoUrlProvider" to "サカタのたね",
                "customSeedInfoUrl" to ""
            )
        }
    }
    
    private fun getSeedInfoUrl(settings: Map<String, String>): String {
        val provider = settings["seedInfoUrlProvider"] ?: "サカタのたね"
        val customUrl = settings["customSeedInfoUrl"] ?: ""
        
        return when (provider) {
            "サカタのたね" -> "https://sakata-netshop.com/shop/default.aspx"
            "たねのタキイ" -> "https://sakata-netshop.com/shop/pages/sowingcalendar.aspx"
            "その他" -> customUrl
            else -> "https://sakata-netshop.com/shop/default.aspx"
        }
    }
    
    private suspend fun getSeedsForUser(uid: String): List<SeedPacket> {
        return try {
            val currentDate = Calendar.getInstance()
            val currentMonth = currentDate.get(Calendar.MONTH) + 1
            val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)
            android.util.Log.d("WeeklyNotificationWorker", "種データ取得開始 - UID: $uid, 現在の月: $currentMonth, 現在の日: $currentDay")
            
            val snapshot = db.collection("seeds")
                .whereEqualTo("ownerUid", uid)
                .get().await()
            
            val seedsEndingSoon = mutableListOf<SeedPacket>()
            
            val seeds = snapshot.documents.mapNotNull { doc ->
                try {
                    val seed = doc.toObject(SeedPacket::class.java)
                    if (seed != null) {
                        val seedWithId = seed.copy(id = doc.id, documentId = doc.id)
                        
                        // 2週間前関連の種かどうかをチェック
                        val isRelevantForWeekly = seedWithId.calendar.any { entry ->
                            if (entry.sowing_end_date.isNotEmpty()) {
                                try {
                                    val sowingEndMonth = entry.sowing_end_date.split("-")[1].toInt()
                                    // まき時終了の2週間前の条件
                                    (sowingEndMonth == currentMonth && currentDay >= 15) || 
                                    (sowingEndMonth == currentMonth + 1 && currentDay <= 15)
                                } catch (e: Exception) {
                                    false
                                }
                            } else {
                                false
                            }
                        }
                        
                        if (isRelevantForWeekly) {
                            seedsEndingSoon.add(seedWithId)
                            android.util.Log.d("WeeklyNotificationWorker", "2週間前関連の種発見: ${seedWithId.productName}")
                            seedWithId
                        } else {
                            null
                        }
                    } else {
                        android.util.Log.w("WeeklyNotificationWorker", "Failed to convert document ${doc.id} to SeedPacket")
                        null
                    }
                } catch (e: Exception) {
                    android.util.Log.w("WeeklyNotificationWorker", "種データの変換に失敗: ${doc.id}", e)
                    null
                }
            }
            
            android.util.Log.d("WeeklyNotificationWorker", "種データ取得完了 - 全件数: ${snapshot.documents.size}, 2週間前関連: ${seeds.size}")
            android.util.Log.d("WeeklyNotificationWorker", "まき時終了の2週間前の種: ${seedsEndingSoon.size}件")
            seeds
        } catch (e: Exception) {
            android.util.Log.e("WeeklyNotificationWorker", "種データの取得に失敗", e)
            emptyList()
        }
    }
    
    private fun getSeedsEndingSoon(seeds: List<SeedPacket>): List<SeedPacket> {
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        
        // 2週間後の日付を計算
        calendar.add(Calendar.DAY_OF_MONTH, 14)
        val twoWeeksLater = calendar.time
        
        return seeds.filter { seed ->
            seed.calendar.any { entry ->
                val endDate = parseDate(entry.sowing_end_date)
                endDate != null && endDate.after(currentDate) && endDate.before(twoWeeksLater)
            }
        }
    }
    
    private fun parseDate(dateString: String): java.util.Date? {
        return try {
            val parts = dateString.split("-")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt() - 1 // Calendarの月は0ベース
                val day = parts[2].toInt()
                
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                calendar.time
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.w("WeeklyNotificationWorker", "日付の解析に失敗: $dateString", e)
            null
        }
    }
}

/**
 * 集計データを使用して最適化された種データ取得
 */
private suspend fun getSeedsForUserOptimized(uid: String, statistics: MonthlyStatistics): List<SeedPacket> {
    return try {
        android.util.Log.d("MonthlyNotificationWorker", "最適化された種データ取得開始 - UID: $uid")
        
        // 集計データから必要な情報を取得
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        
        // 今月関連の種のみを取得（集計データの情報を活用）
        val db = Firebase.firestore
        val seedsSnapshot = db.collection("seeds")
            .whereEqualTo("ownerUid", uid)
            .get().await()
        
        val seeds = seedsSnapshot.documents.mapNotNull { doc ->
            try {
                val seed = doc.toObject(SeedPacket::class.java)
                if (seed != null) {
                    val seedWithId = seed.copy(id = doc.id, documentId = doc.id)
                    
                    var isThisMonthSowing = false
                    var isEndingThisMonth = false
                    
                    // 今月関連の種かどうかをチェック
                    seedWithId.calendar.forEach { entry ->
                        val sowingStartMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_start_date)
                        val sowingStartYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_start_date)
                        val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
                        val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
                        val sowingEndStage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDateToStage(entry.sowing_end_date)
                        
                        if (sowingStartMonth == currentMonth && sowingStartYear == currentYear) {
                            isThisMonthSowing = true
                        }
                        
                        // 今月内で播種期間が終了する種（上旬、中旬、下旬すべて対象）
                        if (sowingEndMonth == currentMonth && sowingEndYear == currentYear) {
                            isEndingThisMonth = true
                        }
                    }
                    
                    if (isThisMonthSowing || isEndingThisMonth) {
                        seedWithId
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                android.util.Log.w("MonthlyNotificationWorker", "種データ解析エラー: ${doc.id}", e)
                null
            }
        }
        
        android.util.Log.d("MonthlyNotificationWorker", "最適化された種データ取得完了: ${seeds.size}件")
        seeds
    } catch (e: Exception) {
        android.util.Log.e("MonthlyNotificationWorker", "最適化された種データ取得エラー", e)
        emptyList()
    }
}
