package com.example.seedstockkeeper6.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.example.seedstockkeeper6.service.GeminiNotificationService
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
            if (notificationSettings["notificationFrequency"] != "月一回") {
                return Result.success()
            }
            
            // ユーザーの地域・県設定を取得
            val region = notificationSettings["defaultRegion"] ?: "温暖地"
            val prefecture = notificationSettings["selectedPrefecture"] ?: ""
            val seedInfoUrl = getSeedInfoUrl(notificationSettings)
            
            // 現在の月を取得
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            
            // ユーザーの種データを取得
            val userSeeds = getSeedsForUser(uid)
            
            // GeminiAPIで通知内容を生成
            val notificationContent = geminiService.generateMonthlyNotificationContent(
                region = region,
                prefecture = prefecture,
                seedInfoUrl = seedInfoUrl,
                currentMonth = currentMonth,
                userSeeds = userSeeds
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
            val settingsDoc = db.collection("users").document(uid)
                .collection("settings").document("general").get().await()
            
            if (settingsDoc.exists()) {
                mapOf(
                    "notificationFrequency" to (settingsDoc.getString("notificationFrequency") ?: "なし"),
                    "selectedWeekday" to (settingsDoc.getString("selectedWeekday") ?: "月曜日"),
                    "defaultRegion" to (settingsDoc.getString("defaultRegion") ?: "温暖地"),
                    "selectedPrefecture" to (settingsDoc.getString("selectedPrefecture") ?: ""),
                    "seedInfoUrlProvider" to (settingsDoc.getString("seedInfoUrlProvider") ?: "サカタのたね"),
                    "customSeedInfoUrl" to (settingsDoc.getString("customSeedInfoUrl") ?: "")
                )
            } else {
                mapOf(
                    "notificationFrequency" to "なし",
                    "defaultRegion" to "温暖地",
                    "selectedPrefecture" to "",
                    "seedInfoUrlProvider" to "サカタのたね",
                    "customSeedInfoUrl" to ""
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("MonthlyNotificationWorker", "通知設定の取得に失敗", e)
            mapOf(
                "notificationFrequency" to "なし",
                "defaultRegion" to "温暖地",
                "selectedPrefecture" to "",
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
            val snapshot = db.collection("users").document(uid)
                .collection("seeds").get().await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(SeedPacket::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    android.util.Log.w("MonthlyNotificationWorker", "種データの変換に失敗: ${doc.id}", e)
                    null
                }
            }
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
            if (notificationSettings["notificationFrequency"] != "週１回") {
                return Result.success()
            }
            
            // ユーザーの地域・県設定を取得
            val region = notificationSettings["defaultRegion"] ?: "温暖地"
            val prefecture = notificationSettings["selectedPrefecture"] ?: ""
            val seedInfoUrl = getSeedInfoUrl(notificationSettings)
            
            // ユーザーの種データを取得
            val userSeeds = getSeedsForUser(uid)
            
            // GeminiAPIで通知内容を生成
            val notificationContent = geminiService.generateWeeklyNotificationContent(
                region = region,
                prefecture = prefecture,
                seedInfoUrl = seedInfoUrl,
                userSeeds = userSeeds
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
                    "seedInfoUrlProvider" to (settingsDoc.getString("seedInfoUrlProvider") ?: "サカタのたね"),
                    "customSeedInfoUrl" to (settingsDoc.getString("customSeedInfoUrl") ?: "")
                )
            } else {
                mapOf(
                    "notificationFrequency" to "なし",
                    "defaultRegion" to "温暖地",
                    "selectedPrefecture" to "",
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
            val snapshot = db.collection("users").document(uid)
                .collection("seeds").get().await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(SeedPacket::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    android.util.Log.w("WeeklyNotificationWorker", "種データの変換に失敗: ${doc.id}", e)
                    null
                }
            }
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
