package com.example.seedstockkeeper6.service

import android.content.Context
import android.util.Log
import com.example.seedstockkeeper6.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * 天気予報APIサービス
 * Google Weather APIを使用
 */
class WeatherService(private val context: Context) {
    
    companion object {
        private const val TAG = "WeatherService"
        private const val BASE_URL = "https://weather.googleapis.com/"
    }
    
    private val googleWeatherApi: GoogleWeatherApiService
    
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        googleWeatherApi = retrofit.create(GoogleWeatherApiService::class.java)
    }
    
    /**
     * Google Maps APIキーを取得
     */
    private fun getApiKey(): String {
        return try {
            // BuildConfigからGoogle Maps APIキーを取得（build.gradleで設定）
            val apiKey = com.example.seedstockkeeper6.BuildConfig.GOOGLE_MAPS_API_KEY
            Log.d(TAG, "取得したGoogle Maps APIキー: ${apiKey.take(10)}...")
            apiKey
        } catch (e: Exception) {
            Log.e(TAG, "APIキーの取得に失敗しました", e)
            "YOUR_API_KEY_HERE"
        }
    }
    
    /**
     * 現在の天気を取得
     */
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = getApiKey()
                val url = "$CURRENT_WEATHER_URL?lat=$latitude&lon=$longitude&appid=$apiKey&units=metric&lang=ja"
                val response = makeHttpRequest(url)
                response?.let { parseCurrentWeather(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching current weather", e)
                null
            }
        }
    }
    
    /**
     * 週間天気予報を取得
     */
    suspend fun getWeeklyWeather(latitude: Double, longitude: Double): WeeklyWeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = getApiKey()
                Log.d(TAG, "Google Weather API取得開始: lat=$latitude, lon=$longitude")
                
                // APIキーが設定されていない場合はnullを返す
                if (apiKey == "YOUR_API_KEY_HERE" || apiKey.isEmpty()) {
                    Log.d(TAG, "Google Maps APIキーが設定されていないため、天気予報を取得できません")
                    Log.d(TAG, "local.propertiesにGOOGLE_MAPS_API_KEYを設定してください")
                    return@withContext null
                }
                
                val locationString = "$latitude,$longitude"
                Log.d(TAG, "Google Weather API呼び出し: location=$locationString")
                
                val response = googleWeatherApi.getDailyForecast(locationString, apiKey)
                
                if (response.isSuccessful && response.body() != null) {
                    val forecastResponse = response.body()!!
                    Log.d(TAG, "Google Weather API成功: ${forecastResponse.dailyForecasts?.size ?: 0}日分のデータ")
                    
                    // Googleのデータ形式からアプリのデータ形式に変換
                    mapToWeeklyWeatherData(forecastResponse.dailyForecasts, latitude, longitude)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Google Weather API エラー: ${response.code()} - $errorBody")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google Weather API取得エラー", e)
                null
            }
        }
    }
    
    /**
     * Google Weather APIのデータをアプリのデータ形式に変換
     */
    private fun mapToWeeklyWeatherData(
        dailyForecasts: List<GoogleDailyForecast>?,
        latitude: Double,
        longitude: Double
    ): WeeklyWeatherData? {
        if (dailyForecasts.isNullOrEmpty()) {
            Log.w(TAG, "天気予報データが空です")
            return null
        }
        
        try {
            // 地域名を決定
            val location = when {
                latitude > 43.0 -> "北海道"
                latitude > 40.0 -> "東北"
                latitude > 36.0 -> "関東"
                latitude > 34.0 -> "中部"
                latitude > 32.0 -> "関西"
                latitude > 30.0 -> "中国・四国"
                else -> "九州"
            }
            
            // 現在の天気（最初の日）
            val firstDay = dailyForecasts[0]
            val currentWeather = WeatherData(
                date = Date(),
                temperature = Temperature(
                    min = firstDay.temperature.min.toDouble(),
                    max = firstDay.temperature.max.toDouble(),
                    current = (firstDay.temperature.min + firstDay.temperature.max) / 2.0
                ),
                weather = Weather(
                    main = "Weather",
                    description = firstDay.shortForecast ?: "情報なし",
                    icon = GoogleWeatherIconMapper.getWeatherIcon(firstDay.weatherCode)
                ),
                humidity = 60, // Google Weather APIには湿度情報がないためデフォルト値
                windSpeed = 2.0, // Google Weather APIには風速情報がないためデフォルト値
                precipitation = 0.0
            )
            
            // 週間予報
            val weeklyForecast = dailyForecasts.map { forecast ->
                val calendar = Calendar.getInstance()
                calendar.set(forecast.date.year, forecast.date.month - 1, forecast.date.day)
                
                WeatherData(
                    date = calendar.time,
                    temperature = Temperature(
                        min = forecast.temperature.min.toDouble(),
                        max = forecast.temperature.max.toDouble(),
                        current = (forecast.temperature.min + forecast.temperature.max) / 2.0
                    ),
                    weather = Weather(
                        main = "Weather",
                        description = forecast.shortForecast ?: "情報なし",
                        icon = GoogleWeatherIconMapper.getWeatherIcon(forecast.weatherCode)
                    ),
                    humidity = 60,
                    windSpeed = 2.0,
                    precipitation = 0.0
                )
            }
            
            return WeeklyWeatherData(
                location = location,
                currentWeather = currentWeather,
                dailyForecast = weeklyForecast
            )
        } catch (e: Exception) {
            Log.e(TAG, "データ変換エラー", e)
            return null
        }
    }
    
    
}