package com.example.seedstockkeeper6.service

import android.util.Log
import com.example.seedstockkeeper6.model.MonthlyStatistics
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.model.StatisticsUpdateResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.Calendar

/**
 * 月毎の集計データを管理するサービス
 */
class StatisticsService {
    private val db: FirebaseFirestore = Firebase.firestore
    private val collectionName = "monthly_statistics"
    
    // 集計処理の重複実行を防ぐためのフラグ
    private val processingUsers = mutableSetOf<String>()
    
    /**
     * 指定された月の集計データを取得
     */
    suspend fun getMonthlyStatistics(ownerUid: String, year: Int, month: Int): MonthlyStatistics? {
        return try {
            val docId = "${ownerUid}_${year}_${month}"
            val doc = db.collection(collectionName)
                .document(docId)
                .get()
                .await()
            
            if (doc.exists()) {
                doc.toObject(MonthlyStatistics::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("StatisticsService", "集計データ取得エラー", e)
            null
        }
    }
    
    /**
     * 現在の月の集計データを取得
     */
    suspend fun getCurrentMonthStatistics(ownerUid: String): MonthlyStatistics? {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return getMonthlyStatistics(ownerUid, year, month)
    }
    
    /**
     * 種データから月毎の集計を計算
     */
    suspend fun calculateMonthlyStatistics(
        ownerUid: String, 
        seeds: List<SeedPacket>, 
        year: Int, 
        month: Int
    ): MonthlyStatistics {
        Log.d("StatisticsService", "集計計算開始: ${year}年${month}月, 種子数: ${seeds.size}")
        
        val currentDate = LocalDate.now()
        val targetDate = LocalDate.of(year, month, 1)
        
        // 全種子数
        val totalSeeds = seeds.size
        Log.d("StatisticsService", "全種子数: $totalSeeds")
        
        // 科別分布（有効期限内の種のみ）
        val validSeeds = seeds.filter { seed ->
            val expirationDate = LocalDate.of(seed.expirationYear, seed.expirationMonth, 1)
            currentDate.isBefore(expirationDate.plusMonths(1))
        }
        Log.d("StatisticsService", "有効期限内種子数: ${validSeeds.size}")
        
        val familyDistribution = validSeeds.groupBy { it.family }
            .mapValues { it.value.size }
        Log.d("StatisticsService", "科別分布: $familyDistribution")
        
        // 今月の播種予定種子数
        val thisMonthSowingSeeds = seeds.filter { seed ->
            seed.calendar.any { entry ->
                val sowingStartMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_start_date)
                val sowingStartYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_start_date)
                val sowingStartStage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDateToStage(entry.sowing_start_date)
                val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
                val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
                val sowingEndStage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDateToStage(entry.sowing_end_date)
                
                Log.d("StatisticsService", "種: ${seed.productName}, 播種開始: ${entry.sowing_start_date}(${sowingStartYear}年${sowingStartMonth}月${sowingStartStage}), 播種終了: ${entry.sowing_end_date}(${sowingEndYear}年${sowingEndMonth}月${sowingEndStage})")
                
                // 今月の播種期間に含まれるかチェック
                val isInThisMonth = (sowingStartYear == year && sowingStartMonth == month) || 
                                  (sowingEndYear == year && sowingEndMonth == month) ||
                                  (sowingStartYear < year && sowingEndYear > year) ||
                  (sowingStartYear == year && sowingStartMonth < month && sowingEndYear == year && sowingEndMonth >= month) ||
                  (sowingStartYear == year && sowingStartMonth <= month && sowingEndYear > year) ||
                  (sowingStartYear < year && sowingEndYear == year && sowingEndMonth >= month)
                
                Log.d("StatisticsService", "今月の播種期間判定: $isInThisMonth")
                isInThisMonth
            }
        }
        Log.d("StatisticsService", "今月播種予定種子数: ${thisMonthSowingSeeds.size}")
        Log.d("StatisticsService", "今月播種予定種子: ${thisMonthSowingSeeds.map { "${it.productName}(${it.variety})" }}")
        
        // まき時終了間近の種子数（今月の下旬まで）
        val urgentSeeds = seeds.filter { seed ->
            seed.calendar.any { entry ->
                val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
                val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
                val sowingEndStage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDateToStage(entry.sowing_end_date)
                
                Log.d("StatisticsService", "終了間近チェック: ${seed.productName}, 播種終了: ${entry.sowing_end_date}(${sowingEndYear}年${sowingEndMonth}月${sowingEndStage})")
                
                val isUrgent = sowingEndMonth == month && sowingEndYear == year && sowingEndStage == "下旬"
                Log.d("StatisticsService", "終了間近判定: $isUrgent")
                isUrgent
            }
        }
        Log.d("StatisticsService", "終了間近種子数: ${urgentSeeds.size}")
        Log.d("StatisticsService", "終了間近種子: ${urgentSeeds.map { "${it.productName}(${it.variety})" }}")
        
        val statistics = MonthlyStatistics(
            year = year,
            month = month,
            totalSeeds = totalSeeds,
            familyDistribution = familyDistribution,
            thisMonthSowingCount = thisMonthSowingSeeds.size,
            urgentSeedsCount = urgentSeeds.size,
            validSeedsCount = validSeeds.size,
            lastUpdated = System.currentTimeMillis(),
            ownerUid = ownerUid
        )
        
        Log.d("StatisticsService", "集計完了: 総数=${statistics.totalSeeds}, 今月まき時=${statistics.thisMonthSowingCount}, 終了間近=${statistics.urgentSeedsCount}")
        
        return statistics
    }
    
    /**
     * 集計データを保存
     */
    suspend fun saveMonthlyStatistics(statistics: MonthlyStatistics): StatisticsUpdateResult {
        return try {
            val docId = "${statistics.ownerUid}_${statistics.year}_${statistics.month}"
            Log.d("StatisticsService", "Firebase保存開始: docId=$docId")
            Log.d("StatisticsService", "保存データ: totalSeeds=${statistics.totalSeeds}, validSeeds=${statistics.validSeedsCount}")
            
            val docRef = db.collection(collectionName).document(docId)
            Log.d("StatisticsService", "Firestore参照取得: ${docRef.path}")
            
            docRef.set(statistics).await()
            
            Log.d("StatisticsService", "Firebase保存完了: $docId")
            Log.d("StatisticsService", "保存成功: totalSeeds=${statistics.totalSeeds}")
            
            StatisticsUpdateResult(
                success = true,
                message = "集計データを保存しました",
                statistics = statistics
            )
        } catch (e: Exception) {
            Log.e("StatisticsService", "Firebase保存エラー: docId=${statistics.ownerUid}_${statistics.year}_${statistics.month}", e)
            Log.e("StatisticsService", "エラー詳細: ${e.message}")
            StatisticsUpdateResult(
                success = false,
                message = "集計データの保存に失敗しました: ${e.message}"
            )
        }
    }
    
    /**
     * 種データの変更時に集計を更新
     */
    suspend fun updateStatisticsOnSeedChange(
        ownerUid: String, 
        seeds: List<SeedPacket>
    ): StatisticsUpdateResult {
        return try {
            // 重複実行チェック
            if (processingUsers.contains(ownerUid)) {
                Log.w("StatisticsService", "集計処理が既に実行中です。スキップします: ownerUid=$ownerUid")
                return StatisticsUpdateResult(
                    success = false,
                    message = "集計処理が既に実行中です"
                )
            }
            
            processingUsers.add(ownerUid)
            
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            
            Log.d("StatisticsService", "集計更新開始: ownerUid=$ownerUid, seeds.size=${seeds.size}, year=$year, month=$month")
            
            // 種データが空の場合は警告ログを出力
            if (seeds.isEmpty()) {
                Log.w("StatisticsService", "警告: 種データが空です。ownerUid=$ownerUid")
                Log.w("StatisticsService", "この時点で種データが0件のため、集計データも0件で更新されます")
            }
            
            // 現在の月の集計を再計算
            val statistics = calculateMonthlyStatistics(ownerUid, seeds, year, month)
            
            Log.d("StatisticsService", "集計計算完了: totalSeeds=${statistics.totalSeeds}")
            
            // 保存
            val result = saveMonthlyStatistics(statistics)
            Log.d("StatisticsService", "集計保存結果: success=${result.success}")
            result
        } catch (e: Exception) {
            Log.e("StatisticsService", "集計更新エラー", e)
            StatisticsUpdateResult(
                success = false,
                message = "集計の更新に失敗しました: ${e.message}"
            )
        } finally {
            // 処理完了後にフラグをクリア
            processingUsers.remove(ownerUid)
            Log.d("StatisticsService", "集計処理完了: ownerUid=$ownerUid")
        }
    }
    
    /**
     * 集計データを手動で修正（緊急対応用）
     */
    suspend fun fixStatisticsData(ownerUid: String): StatisticsUpdateResult {
        return try {
            Log.d("StatisticsService", "集計データ修正開始: ownerUid=$ownerUid")
            
            // 現在のユーザーの全種データを取得
            val seedsSnapshot = db.collection("seeds")
                .whereEqualTo("ownerUid", ownerUid)
                .get().await()
            
            val seeds = seedsSnapshot.documents.mapNotNull { doc ->
                try {
                    val seed = doc.toObject(SeedPacket::class.java)
                    seed?.copy(id = doc.id, documentId = doc.id)
                } catch (e: Exception) {
                    Log.w("StatisticsService", "種データ解析エラー: ${doc.id}", e)
                    null
                }
            }
            
            Log.d("StatisticsService", "修正用種データ取得: ${seeds.size}件")
            
            if (seeds.isNotEmpty()) {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                
                val statistics = calculateMonthlyStatistics(ownerUid, seeds, year, month)
                val result = saveMonthlyStatistics(statistics)
                
                Log.d("StatisticsService", "集計データ修正完了: totalSeeds=${statistics.totalSeeds}")
                result
            } else {
                Log.w("StatisticsService", "修正用種データが0件のため、修正をスキップ")
                StatisticsUpdateResult(
                    success = false,
                    message = "種データが0件のため修正できません"
                )
            }
        } catch (e: Exception) {
            Log.e("StatisticsService", "集計データ修正エラー", e)
            StatisticsUpdateResult(
                success = false,
                message = "集計データの修正に失敗しました: ${e.message}"
            )
        }
    }
    
    /**
     * 複数月の集計データを一括更新
     */
    suspend fun updateMultipleMonthsStatistics(
        ownerUid: String, 
        seeds: List<SeedPacket>,
        months: List<Pair<Int, Int>> // List of (year, month) pairs
    ): List<StatisticsUpdateResult> {
        val results = mutableListOf<StatisticsUpdateResult>()
        
        for ((year, month) in months) {
            try {
                val statistics = calculateMonthlyStatistics(ownerUid, seeds, year, month)
                val result = saveMonthlyStatistics(statistics)
                results.add(result)
            } catch (e: Exception) {
                Log.e("StatisticsService", "月別集計更新エラー: ${year}年${month}月", e)
                results.add(
                    StatisticsUpdateResult(
                        success = false,
                        message = "${year}年${month}月の集計更新に失敗: ${e.message}"
                    )
                )
            }
        }
        
        return results
    }
    
    /**
     * 集計データを削除
     */
    suspend fun deleteMonthlyStatistics(ownerUid: String, year: Int, month: Int): Boolean {
        return try {
            val docId = "${ownerUid}_${year}_${month}"
            db.collection(collectionName)
                .document(docId)
                .delete()
                .await()
            
            Log.d("StatisticsService", "集計データ削除完了: $docId")
            true
        } catch (e: Exception) {
            Log.e("StatisticsService", "集計データ削除エラー", e)
            false
        }
    }
    
    /**
     * 古い集計データをクリーンアップ（1年以上前のデータを削除）
     */
    suspend fun cleanupOldStatistics(ownerUid: String): Boolean {
        return try {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val cutoffYear = currentYear - 1
            
            // 古いデータを削除
            val oldStats = db.collection(collectionName)
                .whereEqualTo("ownerUid", ownerUid)
                .whereLessThan("year", cutoffYear)
                .get()
                .await()
            
            val batch = db.batch()
            oldStats.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            
            Log.d("StatisticsService", "古い集計データクリーンアップ完了: ${oldStats.size()}件削除")
            true
        } catch (e: Exception) {
            Log.e("StatisticsService", "集計データクリーンアップエラー", e)
            false
        }
    }
}
