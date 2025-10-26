package com.example.seedstockkeeper6.repository

import android.content.Context
import com.example.seedstockkeeper6.data.WeeklyWeatherData
import com.example.seedstockkeeper6.model.MonthlyStatistics
import com.example.seedstockkeeper6.model.NotificationData
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.model.StatisticsUpdateResult
import com.example.seedstockkeeper6.service.NotificationHistoryService
import com.example.seedstockkeeper6.service.StatisticsService
import com.example.seedstockkeeper6.service.WeatherService

/**
 * お城画面のデータアクセス層
 * 各種Serviceのラッパーとして機能
 */
class CastleRepository(private val context: Context) {
    private val statisticsService = StatisticsService()
    private val weatherService = WeatherService(context)
    private val historyService = NotificationHistoryService()
    
    /**
     * 集計データを取得
     */
    suspend fun getMonthlyStatistics(uid: String): MonthlyStatistics? {
        return statisticsService.getCurrentMonthStatistics(uid)
    }
    
    /**
     * 集計データを更新
     */
    suspend fun updateStatistics(uid: String, seeds: List<SeedPacket>): StatisticsUpdateResult {
        return statisticsService.updateStatisticsOnSeedChange(uid, seeds)
    }
    
    /**
     * 集計データを修正
     */
    suspend fun fixStatistics(uid: String): StatisticsUpdateResult {
        return statisticsService.fixStatisticsData(uid)
    }
    
    /**
     * 週間天気データを取得
     */
    suspend fun getWeeklyWeather(latitude: Double, longitude: Double): WeeklyWeatherData? {
        return weatherService.getWeeklyWeather(latitude, longitude)
    }
    
    /**
     * 最新の通知データを取得
     */
    suspend fun getLatestNotification(limit: Int = 1): List<NotificationData> {
        return historyService.getUserNotificationData(limit = limit)
    }
}
