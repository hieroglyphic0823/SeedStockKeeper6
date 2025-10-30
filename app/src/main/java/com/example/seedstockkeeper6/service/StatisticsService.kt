package com.example.seedstockkeeper6.service

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
        
        val currentDate = LocalDate.now()
        val targetDate = LocalDate.of(year, month, 1)
        
        // 全種子数
        val totalSeeds = seeds.size
        
        // 有効期限内の種（カウント用に保持）
        val validSeeds = seeds.filter { seed ->
            val expirationDate = LocalDate.of(seed.expirationYear, seed.expirationMonth, 1)
            currentDate.isBefore(expirationDate.plusMonths(1))
        }
        
        // 科別分布（全ての種を対象）
        val familyDistribution = seeds.groupBy { it.family }
            .mapValues { it.value.size }
        
        // 今月の播種予定種子数（統一ロジックを使用）
        val thisMonthSowingSeeds = com.example.seedstockkeeper6.utils.SowingCalculationUtils.getThisMonthSowingSeeds(
            seeds = seeds,
            currentDate = targetDate,
            excludeFinished = true
        )
        
        // まき時終了間近の種子数（統一ロジックを使用）
        val urgentSeeds = com.example.seedstockkeeper6.utils.SowingCalculationUtils.getUrgentSeeds(
            seeds = seeds,
            currentDate = targetDate
        )
        
        val statistics = MonthlyStatistics(
            year = year,
            month = month,
            totalSeeds = totalSeeds,
            familyDistribution = familyDistribution,
            thisMonthSowingCount = thisMonthSowingSeeds.size,
            urgentSeedsCount = urgentSeeds.size,
            validSeedsCount = validSeeds.size,
            valid = true, // Firestore用の有効性フラグ
            lastUpdated = System.currentTimeMillis(),
            ownerUid = ownerUid
        )
        
        
        return statistics
    }
    
    /**
     * 集計データを保存
     */
    suspend fun saveMonthlyStatistics(statistics: MonthlyStatistics): StatisticsUpdateResult {
        return try {
            val docId = "${statistics.ownerUid}_${statistics.year}_${statistics.month}"
            
            val docRef = db.collection(collectionName).document(docId)
            
            docRef.set(statistics).await()
            
            
            StatisticsUpdateResult(
                success = true,
                message = "集計データを保存しました",
                statistics = statistics
            )
        } catch (e: Exception) {
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
                return StatisticsUpdateResult(
                    success = false,
                    message = "集計処理が既に実行中です"
                )
            }
            
            processingUsers.add(ownerUid)
            
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            
            
            // 種データが空の場合は警告ログを出力
            if (seeds.isEmpty()) {
            }
            
            // 現在の月の集計を再計算
            val statistics = calculateMonthlyStatistics(ownerUid, seeds, year, month)
            
            
            // 保存
            val result = saveMonthlyStatistics(statistics)
            result
        } catch (e: Exception) {
            StatisticsUpdateResult(
                success = false,
                message = "集計の更新に失敗しました: ${e.message}"
            )
        } finally {
            // 処理完了後にフラグをクリア
            processingUsers.remove(ownerUid)
        }
    }
    
    /**
     * 集計データを手動で修正（緊急対応用）
     */
    suspend fun fixStatisticsData(ownerUid: String): StatisticsUpdateResult {
        return try {
            
            // 現在のユーザーの全種データを取得
            val seedsSnapshot = db.collection("seeds")
                .whereEqualTo("ownerUid", ownerUid)
                .get().await()
            
            val seeds = seedsSnapshot.documents.mapNotNull { doc ->
                try {
                    val seed = doc.toObject(SeedPacket::class.java)
                    seed?.copy(id = doc.id, documentId = doc.id)
                } catch (e: Exception) {
                    null
                }
            }
            
            
            if (seeds.isNotEmpty()) {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1
                
                val statistics = calculateMonthlyStatistics(ownerUid, seeds, year, month)
                val result = saveMonthlyStatistics(statistics)
                
                result
            } else {
                StatisticsUpdateResult(
                    success = false,
                    message = "種データが0件のため修正できません"
                )
            }
        } catch (e: Exception) {
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
            
            true
        } catch (e: Exception) {
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
            
            true
        } catch (e: Exception) {
            false
        }
    }
}
