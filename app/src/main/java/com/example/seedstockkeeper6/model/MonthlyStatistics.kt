package com.example.seedstockkeeper6.model

import kotlinx.serialization.Serializable
import com.google.firebase.firestore.PropertyName

/**
 * 月毎の集計データを保存するモデル
 */
@Serializable
data class MonthlyStatistics(
    val year: Int = 0,
    val month: Int = 0,
    val totalSeeds: Int = 0,
    val familyDistribution: Map<String, Int> = emptyMap(),
    val thisMonthSowingCount: Int = 0,
    val urgentSeedsCount: Int = 0,
    @PropertyName("validSeedsCount")
    val validSeedsCount: Int = 0, // 有効期限内の種子数
    val lastUpdated: Long = System.currentTimeMillis(),
    val ownerUid: String = ""
) {
    /**
     * 科別分布の上位3科を取得
     */
    fun getTopFamilies(limit: Int = 3): List<Pair<String, Int>> {
        return familyDistribution.toList()
            .sortedByDescending { it.second }
            .take(limit)
    }
    
    /**
     * 指定された科の種子数を取得
     */
    fun getFamilyCount(family: String): Int {
        return familyDistribution[family] ?: 0
    }
    
    /**
     * 統計データが有効かどうかチェック（24時間以内に更新されているか）
     */
    fun isValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastUpdated) < 24 * 60 * 60 * 1000 // 24時間
    }
}

/**
 * 集計データの更新結果
 */
data class StatisticsUpdateResult(
    val success: Boolean,
    val message: String,
    val statistics: MonthlyStatistics? = null
)
