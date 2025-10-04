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
 * OpenWeatherMap APIを使用
 */
class WeatherService(private val context: Context) {

    companion object {
        private const val TAG = "WeatherService"
        private const val BASE_URL = "https://api.openweathermap.org/"
    }

    private val openWeatherApi: OpenWeatherApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        openWeatherApi = retrofit.create(OpenWeatherApiService::class.java)
    }
    
    /**
     * OpenWeatherMap APIキーを取得
     */
    private fun getApiKey(): String {
        return try {
            // BuildConfigからOpenWeatherMap APIキーを取得（build.gradleで設定）
            val apiKey = com.example.seedstockkeeper6.BuildConfig.OPENWEATHER_API_KEY
            Log.d(TAG, "取得したOpenWeatherMap APIキー: ${apiKey.take(10)}...")
            apiKey
        } catch (e: Exception) {
            Log.e(TAG, "APIキーの取得に失敗しました", e)
            "YOUR_API_KEY_HERE"
        }
    }
    
    
    /**
     * 週間天気予報を取得
     */
    suspend fun getWeeklyWeather(latitude: Double, longitude: Double): WeeklyWeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = getApiKey()
                Log.d(TAG, "OpenWeatherMap API取得開始: lat=$latitude, lon=$longitude")

                // APIキーが設定されていない場合はnullを返す
                if (apiKey == "YOUR_API_KEY_HERE" || apiKey.isEmpty()) {
                    Log.d(TAG, "OpenWeatherMap APIキーが設定されていないため、天気予報を取得できません")
                    Log.d(TAG, "local.propertiesにOPENWEATHER_API_KEYを設定してください")
                    return@withContext null
                }
                
                Log.d(TAG, "OpenWeatherMap API呼び出し: lat=$latitude, lon=$longitude")
                Log.d(TAG, "使用するAPIキー: ${apiKey.take(10)}...")

                val response = openWeatherApi.getForecast(latitude, longitude, apiKey)
                
                if (response.isSuccessful && response.body() != null) {
                    val forecastResponse = response.body()!!
                    Log.d(TAG, "OpenWeatherMap API成功: ${forecastResponse.list?.size ?: 0}件のデータ")

                    // OpenWeatherMapのデータ形式からアプリのデータ形式に変換
                    mapToWeeklyWeatherData(forecastResponse, latitude, longitude)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "OpenWeatherMap API エラー: ${response.code()} - $errorBody")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "OpenWeatherMap API取得エラー", e)
                null
            }
        }
    }
    
    /**
     * OpenWeatherMap APIのデータをアプリのデータ形式に変換
     */
    private fun mapToWeeklyWeatherData(
        openWeatherResponse: OpenWeatherResponse,
        latitude: Double,
        longitude: Double
    ): WeeklyWeatherData? {
        val weatherList = openWeatherResponse.list
        if (weatherList.isNullOrEmpty()) {
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

            // 現在の天気（最初のデータ）
            val firstItem = weatherList[0]
            val currentWeather = WeatherData(
                date = Date(firstItem.dt * 1000), // Unix timestamp to Date
                temperature = Temperature(
                    min = firstItem.main.tempMin,
                    max = firstItem.main.tempMax,
                    current = firstItem.main.temp
                ),
                weather = Weather(
                    main = firstItem.weather.firstOrNull()?.main ?: "情報なし",
                    description = firstItem.weather.firstOrNull()?.description ?: "情報なし",
                    icon = OpenWeatherIconMapper.getWeatherIcon(firstItem.weather.firstOrNull()?.icon)
                ),
                humidity = firstItem.main.humidity,
                windSpeed = firstItem.wind?.speed ?: 0.0,
                precipitation = firstItem.pop ?: 0.0
            )

            // 週間予報（3時間ごとのデータから日別に集約）
            val dailyForecast = weatherList
                .filter { it.dt * 1000 > System.currentTimeMillis() } // 未来のデータのみ
                .groupBy { 
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = it.dt * 1000
                    calendar.get(Calendar.DAY_OF_YEAR)
                }
                .map { (_, dayItems) ->
                    val dayItem = dayItems.first() // その日の最初のデータを使用
            WeatherData(
                        date = Date(dayItem.dt * 1000),
                        temperature = Temperature(
                            min = dayItems.minOf { it.main.tempMin },
                            max = dayItems.maxOf { it.main.tempMax },
                            current = dayItem.main.temp
                        ),
                        weather = Weather(
                            main = dayItem.weather.firstOrNull()?.main ?: "情報なし",
                            description = dayItem.weather.firstOrNull()?.description ?: "情報なし",
                            icon = OpenWeatherIconMapper.getWeatherIcon(dayItem.weather.firstOrNull()?.icon)
                        ),
                        humidity = dayItem.main.humidity,
                        windSpeed = dayItem.wind?.speed ?: 0.0,
                        precipitation = dayItem.pop ?: 0.0
                    )
                }
                .take(7) // 最大7日分

            return WeeklyWeatherData(
                location = location,
                currentWeather = currentWeather,
                dailyForecast = dailyForecast
            )
        } catch (e: Exception) {
            Log.e(TAG, "データ変換エラー", e)
            return null
        }
    }
    
    
}