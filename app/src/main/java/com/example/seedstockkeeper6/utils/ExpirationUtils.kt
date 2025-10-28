package com.example.seedstockkeeper6.utils

import java.time.LocalDate
import java.time.YearMonth

/**
 * 有効期限関連のユーティリティ関数
 */
object ExpirationUtils {
    
    /**
     * 指定された年・月が現在の日付より期限切れかどうかを判定
     * @param expirationYear 有効期限年
     * @param expirationMonth 有効期限月
     * @return 期限切れの場合true
     */
    fun isExpired(expirationYear: Int, expirationMonth: Int): Boolean {
        if (expirationYear <= 0 || expirationMonth <= 0) {
            return false // 有効期限が設定されていない場合は期限切れではない
        }
        
        val currentDate = LocalDate.now()
        val currentYearMonth = YearMonth.of(currentDate.year, currentDate.monthValue)
        val expirationYearMonth = YearMonth.of(expirationYear, expirationMonth)
        
        return currentYearMonth.isAfter(expirationYearMonth)
    }
    
    /**
     * SeedPacketが期限切れかどうかを判定
     * @param seedPacket チェック対象の種パケット
     * @return 期限切れの場合true
     */
    fun isSeedExpired(seedPacket: com.example.seedstockkeeper6.model.SeedPacket): Boolean {
        return isExpired(seedPacket.expirationYear, seedPacket.expirationMonth)
    }
    
    /**
     * 有効期限切れの種のリストをフィルタリング
     * @param seeds チェック対象の種リスト
     * @return 期限切れの種のリスト
     */
    fun getExpiredSeeds(seeds: List<com.example.seedstockkeeper6.model.SeedPacket>): List<com.example.seedstockkeeper6.model.SeedPacket> {
        return seeds.filter { isSeedExpired(it) }
    }
    
    /**
     * 有効期限切れでない種のリストをフィルタリング
     * @param seeds チェック対象の種リスト
     * @return 有効な種のリスト
     */
    fun getValidSeeds(seeds: List<com.example.seedstockkeeper6.model.SeedPacket>): List<com.example.seedstockkeeper6.model.SeedPacket> {
        return seeds.filter { !isSeedExpired(it) }
    }
    
    /**
     * まき終わりでない種のリストをフィルタリング
     * @param seeds チェック対象の種リスト
     * @return まき終わりでない種のリスト
     */
    fun getUnfinishedSeeds(seeds: List<com.example.seedstockkeeper6.model.SeedPacket>): List<com.example.seedstockkeeper6.model.SeedPacket> {
        return seeds.filter { !it.isFinished }
    }
    
    /**
     * 通知に含めるべき種のリストをフィルタリング（まき終わり・期限切れを除外）
     * @param seeds チェック対象の種リスト
     * @return 通知に含めるべき種のリスト
     */
    fun getNotificationEligibleSeeds(seeds: List<com.example.seedstockkeeper6.model.SeedPacket>): List<com.example.seedstockkeeper6.model.SeedPacket> {
        return seeds.filter { seed ->
            !seed.isFinished && !isSeedExpired(seed)
        }
    }
}

