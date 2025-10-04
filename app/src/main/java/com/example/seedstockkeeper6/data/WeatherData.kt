package com.example.seedstockkeeper6.data

import java.util.Date

/**
 * 天気予報データクラス
 */
data class WeatherData(
    val date: Date,
    val temperature: Temperature,
    val weather: Weather,
    val humidity: Int,
    val windSpeed: Double,
    val precipitation: Double
)

/**
 * 気温データ
 */
data class Temperature(
    val min: Double,
    val max: Double,
    val current: Double
)

/**
 * 天気情報
 */
data class Weather(
    val main: String,        // 天気の種類（例：Clear, Rain, Snow）
    val description: String, // 詳細説明
    val icon: String         // アイコンコード
)

/**
 * 週間天気予報データ
 */
data class WeeklyWeatherData(
    val location: String,
    val currentWeather: WeatherData,
    val dailyForecast: List<WeatherData>
)

/**
 * 天気アイコンのマッピング
 */
object WeatherIconMapper {
    fun getWeatherIcon(iconCode: String): String {
        return when (iconCode) {
            "01d", "01n" -> "☀️" // Clear sky
            "02d", "02n" -> "⛅" // Few clouds
            "03d", "03n" -> "☁️" // Scattered clouds
            "04d", "04n" -> "☁️" // Broken clouds
            "09d", "09n" -> "🌧️" // Shower rain
            "10d", "10n" -> "🌦️" // Rain
            "11d", "11n" -> "⛈️" // Thunderstorm
            "13d", "13n" -> "❄️" // Snow
            "50d", "50n" -> "🌫️" // Mist
            else -> "🌤️" // Default
        }
    }
}
