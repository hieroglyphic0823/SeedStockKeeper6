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
        
        // 科別分布（有効期限内の種のみ）
        val validSeeds = seeds.filter { seed ->
            val expirationDate = LocalDate.of(seed.expirationYear, seed.expirationMonth, 1)
            currentDate.isBefore(expirationDate.plusMonths(1))
        }
        
        val familyDistribution = validSeeds.groupBy { it.family }
            .mapValues { it.value.size }
        
        // 今月の播種予定種子数
        val thisMonthSowingSeeds = seeds.filter { seed ->
            seed.calendar.any { entry ->
                val sowingStartMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_start_date)
                val sowingStartYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_start_date)
                sowingStartMonth == month && sowingStartYear == year
            }
        }
        
        // まき時終了間近の種子数（今月の下旬まで）
        val urgentSeeds = seeds.filter { seed ->
            seed.calendar.any { entry ->
                val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
                val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
                val sowingEndStage = com.example.seedstockkeeper6.utils.DateConversionUtils.convertDateToStage(entry.sowing_end_date)
                sowingEndMonth == month && sowingEndYear == year && sowingEndStage == "下旬"
            }
        }
        
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
            db.collection(collectionName)
                .document(docId)
                .set(statistics)
                .await()
            
            Log.d("StatisticsService", "集計データ保存完了: $docId")
            StatisticsUpdateResult(
                success = true,
                message = "集計データを保存しました",
                statistics = statistics
            )
        } catch (e: Exception) {
            Log.e("StatisticsService", "集計データ保存エラー", e)
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
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            
            Log.d("StatisticsService", "集計更新開始: ownerUid=$ownerUid, seeds.size=${seeds.size}, year=$year, month=$month")
            
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
