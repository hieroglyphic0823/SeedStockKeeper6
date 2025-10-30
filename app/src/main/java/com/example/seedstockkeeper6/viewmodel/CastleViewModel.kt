package com.example.seedstockkeeper6.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seedstockkeeper6.data.WeeklyWeatherData
import com.example.seedstockkeeper6.model.MonthlyStatistics
import com.example.seedstockkeeper6.model.NotificationData
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.model.StatisticsData
import com.example.seedstockkeeper6.model.StatisticsUpdateResult
import com.example.seedstockkeeper6.model.createPreviewStatisticsData
import com.example.seedstockkeeper6.repository.CastleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * お城画面のViewModel
 * データ取得、状態管理、ビジネスロジックを担当
 */
class CastleViewModel(private val context: Context) : ViewModel() {
    private val repository = CastleRepository(context)
    
    // 集計データの状態
    private val _monthlyStatistics = MutableStateFlow<MonthlyStatistics?>(null)
    val monthlyStatistics: StateFlow<MonthlyStatistics?> = _monthlyStatistics.asStateFlow()
    
    private val _isLoadingStatistics = MutableStateFlow(false)
    val isLoadingStatistics: StateFlow<Boolean> = _isLoadingStatistics.asStateFlow()
    
    // 天気データの状態
    private val _weeklyWeatherData = MutableStateFlow<WeeklyWeatherData?>(null)
    val weeklyWeatherData: StateFlow<WeeklyWeatherData?> = _weeklyWeatherData.asStateFlow()
    
    private val _isLoadingWeather = MutableStateFlow(false)
    val isLoadingWeather: StateFlow<Boolean> = _isLoadingWeather.asStateFlow()
    
    private val _weatherError = MutableStateFlow<String?>(null)
    val weatherError: StateFlow<String?> = _weatherError.asStateFlow()
    
    // 通知データの状態
    private val _latestNotification = MutableStateFlow<NotificationData?>(null)
    val latestNotification: StateFlow<NotificationData?> = _latestNotification.asStateFlow()
    
    private val _isLoadingNotification = MutableStateFlow(true)
    val isLoadingNotification: StateFlow<Boolean> = _isLoadingNotification.asStateFlow()
    
    /**
     * 集計データを取得・更新
     */
    fun loadStatistics(seeds: List<SeedPacket>, isPreview: Boolean = false) {
        if (isPreview) {
            // プレビュー時は何もしない（固定データを使用）
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoadingStatistics.value = true
                
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val uid = auth.currentUser?.uid ?: return@launch
                
                // まず現在の集計データを取得
                _monthlyStatistics.value = repository.getMonthlyStatistics(uid)
                
                // 集計データが古い場合、または種データが変更された場合は再計算
                val needsRecalculation = _monthlyStatistics.value == null || 
                    !_monthlyStatistics.value!!.isValid() || 
                    _monthlyStatistics.value!!.totalSeeds != seeds.size
                
                if (needsRecalculation) {
                    if (seeds.isEmpty()) {
                        // 既存の集計データが0件の場合は修正を試行
                        if (_monthlyStatistics.value?.totalSeeds == 0) {
                            val fixResult = repository.fixStatistics(uid)
                            if (fixResult.success) {
                                _monthlyStatistics.value = fixResult.statistics
                            }
                        }
                    } else {
                        val result = repository.updateStatistics(uid, seeds)
                        if (result.success) {
                            _monthlyStatistics.value = result.statistics
                        }
                    }
                }
            } catch (e: Exception) {
                // エラーハンドリング
            } finally {
                _isLoadingStatistics.value = false
            }
        }
    }
    
    /**
     * 天気データを取得
     */
    fun loadWeatherData(latitude: Double, longitude: Double, isPreview: Boolean = false) {
        if (isPreview || latitude == 0.0 || longitude == 0.0) {
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoadingWeather.value = true
                _weatherError.value = null
                _weeklyWeatherData.value = repository.getWeeklyWeather(latitude, longitude)
            } catch (e: Exception) {
                _weatherError.value = "天気予報の取得に失敗しました: ${e.message}"
            } finally {
                _isLoadingWeather.value = false
            }
        }
    }
    
    /**
     * 通知データを取得
     */
    fun loadNotificationData(isPreview: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoadingNotification.value = true
                
                if (isPreview) {
                    // プレビュー時は固定データ
                    _latestNotification.value = createPreviewNotificationData()
                } else {
                    // 月次・週次のうち最新の通知を選択
                    val notifications = repository.getLatestNotification(limit = 50)
                    val targetTypes = setOf("MONTHLY", "WEEKLY")
                    val latest = notifications
                        .filter { it.notificationType in targetTypes }
                        .maxByOrNull { data ->
                            try {
                                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).apply {
                                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                                }
                                formatter.parse(data.sentAt)?.time ?: 0L
                            } catch (e: Exception) {
                                0L
                            }
                        }
                    _latestNotification.value = latest
                }
            } catch (e: Exception) {
                _latestNotification.value = null
            } finally {
                _isLoadingNotification.value = false
            }
        }
    }
    
    /**
     * 集計データからStatisticsDataを生成
     */
    fun generateStatisticsData(seeds: List<SeedPacket>, isPreview: Boolean = false): StatisticsData {
        if (isPreview) {
            return createPreviewStatisticsData()
        }
        
        // 常に最新の種データから計算するように変更
        return calculateStatisticsFromSeeds(seeds)
    }
    
    /**
     * 種データから直接集計を計算（フォールバック用）
     */
    private fun calculateStatisticsFromSeeds(seeds: List<SeedPacket>): StatisticsData {
        val currentDate = LocalDate.now()
        val currentMonth = currentDate.monthValue
        val currentYear = currentDate.year
        
        // まき終わった種を除外した有効な種のリスト
        val activeSeeds = seeds.filter { !it.isFinished }
        
        // 今月の播種予定種子数（まき終わった種を除外）
        val thisMonthSowingSeeds = com.example.seedstockkeeper6.utils.SowingCalculationUtils.getThisMonthSowingSeeds(
            seeds = activeSeeds,
            currentDate = currentDate,
            excludeFinished = false // 既にフィルタリング済みなのでfalse
        )
        
        // 終了間近の種子数（まき終わった種を除外）
        val urgentSeeds = com.example.seedstockkeeper6.utils.SowingCalculationUtils.getUrgentSeeds(
            seeds = activeSeeds,
            currentDate = currentDate
        )
        
        // 有効期限内の種（まき終わった種を除外）
        val validSeeds = activeSeeds.filter { seed ->
            val expirationDate = LocalDate.of(seed.expirationYear, seed.expirationMonth, 1)
            currentDate.isBefore(expirationDate.plusMonths(1))
        }
        
        // 期限切れの種（まき終わった種を除外）
        val expiredSeeds = activeSeeds.filter { seed ->
            val expirationDate = LocalDate.of(seed.expirationYear, seed.expirationMonth, 1)
            currentDate.isAfter(expirationDate.plusMonths(1))
        }
        
        // まき終わった種の数（全種から計算）
        val finishedSeeds = seeds.filter { it.isFinished }
        
        // デバッグログを追加
        android.util.Log.d("CastleViewModel", "統計計算結果:")
        android.util.Log.d("CastleViewModel", "  全種子数: ${seeds.size}")
        android.util.Log.d("CastleViewModel", "  まき終わり: ${finishedSeeds.size}")
        android.util.Log.d("CastleViewModel", "  今月播種予定: ${thisMonthSowingSeeds.size}")
        android.util.Log.d("CastleViewModel", "  終了間近: ${urgentSeeds.size}")
        android.util.Log.d("CastleViewModel", "  期限切れ: ${expiredSeeds.size}")
        android.util.Log.d("CastleViewModel", "  まき終わり種詳細: ${finishedSeeds.map { "${it.productName}(${it.variety})" }}")
        
        // 科別分布（全ての種を対象）
        val familyDist = seeds.groupBy { it.family }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
        
        return StatisticsData(
            thisMonthSowingCount = thisMonthSowingSeeds.size,
            urgentSeedsCount = urgentSeeds.size,
            totalSeeds = seeds.size,
            finishedSeedsCount = finishedSeeds.size,
            expiredSeedsCount = expiredSeeds.size,
            familyDistribution = familyDist
        )
    }
    
    /**
     * プレビュー用の通知データを作成
     */
    private fun createPreviewNotificationData(): NotificationData {
        val currentDate = LocalDate.now()
        return NotificationData(
            id = "preview",
            title = "弥生の風に乗せて――春の種まきの候、菜園より",
            summary = "お銀、菜園の弥生は1種類の種の播種時期です。恋むすめ（ニンジン）の栽培を楽しんでくださいね。",
            farmOwner = "お銀",
            region = "温暖地",
            prefecture = "東京都",
            month = currentDate.monthValue,
            thisMonthSeeds = listOf(
                com.example.seedstockkeeper6.model.SeedInfo(
                    name = "恋むすめ",
                    variety = "ニンジン",
                    description = "春の種まきに最適な品種です"
                )
            ),
            endingSoonSeeds = listOf(
                com.example.seedstockkeeper6.model.SeedInfo(
                    name = "春菊",
                    variety = "中葉春菊",
                    description = "まき時終了間近です"
                )
            ),
            sentAt = currentDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) + "T12:00:00.000Z",
            userId = "preview",
            seedCount = 1,
            isRead = 0 // プレビューでは未読として表示
        )
    }
}
