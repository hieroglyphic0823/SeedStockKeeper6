package com.example.seedstockkeeper6.data

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Google Weather API のレスポンス用データクラス
 */
data class GoogleWeatherResponse(
    @SerializedName("dailyForecasts") val dailyForecasts: List<GoogleDailyForecast>?
)

data class GoogleDailyForecast(
    @SerializedName("date") val date: GoogleDate,
    @SerializedName("temperature") val temperature: GoogleTemperature,
    @SerializedName("shortForecast") val shortForecast: String?,
    @SerializedName("weatherCode") val weatherCode: Int?
)

data class GoogleDate(
    val year: Int,
    val month: Int,
    val day: Int
)

data class GoogleTemperature(
    @SerializedName("min") val min: Float,
    @SerializedName("max") val max: Float
)

/**
 * Google Weather API のリクエストボディ
 */
data class GoogleWeatherRequestBody(
    val days: Int = 7,
    val temperatureUnit: String = "CELSIUS"
)

/**
 * Google Weather API の天気コードをアイコンにマッピング
 */
object GoogleWeatherIconMapper {
    fun getWeatherIcon(weatherCode: Int?): String {
        return when (weatherCode) {
            1000 -> "☀️" // Clear
            1001 -> "☁️" // Cloudy
            1100 -> "🌤️" // Mostly Clear
            1101 -> "⛅" // Partly Cloudy
            1102 -> "☁️" // Mostly Cloudy
            2000 -> "🌫️" // Fog
            2100 -> "🌫️" // Light Fog
            3000 -> "🌪️" // Light Wind
            3001 -> "💨" // Wind
            3002 -> "🌪️" // Strong Wind
            4000 -> "🌧️" // Drizzle
            4001 -> "🌧️" // Rain
            4200 -> "🌦️" // Light Rain
            4201 -> "🌧️" // Heavy Rain
            5000 -> "❄️" // Snow
            5001 -> "🌨️" // Flurries
            5100 -> "❄️" // Light Snow
            5101 -> "🌨️" // Heavy Snow
            6000 -> "🌨️" // Freezing Drizzle
            6001 -> "🌨️" // Freezing Rain
            6200 -> "🌨️" // Light Freezing Rain
            6201 -> "🌨️" // Heavy Freezing Rain
            7000 -> "🌨️" // Ice Pellets
            7101 -> "🌨️" // Heavy Ice Pellets
            7102 -> "🌨️" // Light Ice Pellets
            8000 -> "⛈️" // Thunderstorm
            else -> "🌤️" // Default
        }
    }
}
