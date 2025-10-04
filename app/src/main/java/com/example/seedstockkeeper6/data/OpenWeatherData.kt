package com.example.seedstockkeeper6.data

import com.google.gson.annotations.SerializedName

/**
 * OpenWeatherMap API のレスポンス用データクラス
 */
data class OpenWeatherResponse(
    @SerializedName("list") val list: List<OpenWeatherItem>?,
    @SerializedName("city") val city: OpenWeatherCity?
)

data class OpenWeatherItem(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: OpenWeatherMain,
    @SerializedName("weather") val weather: List<OpenWeatherWeather>,
    @SerializedName("wind") val wind: OpenWeatherWind?,
    @SerializedName("pop") val pop: Double? // 降水確率
)

data class OpenWeatherMain(
    @SerializedName("temp") val temp: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("humidity") val humidity: Int
)

data class OpenWeatherWeather(
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class OpenWeatherWind(
    @SerializedName("speed") val speed: Double
)

data class OpenWeatherCity(
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String
)

/**
 * OpenWeatherMap API の天気アイコンをマッピング
 */
object OpenWeatherIconMapper {
    fun getWeatherIcon(iconCode: String?): String {
        return when (iconCode) {
            "01d", "01n" -> "☀️" // clear sky
            "02d", "02n" -> "🌤️" // few clouds
            "03d", "03n" -> "⛅" // scattered clouds
            "04d", "04n" -> "☁️" // broken clouds
            "09d", "09n" -> "🌧️" // shower rain
            "10d", "10n" -> "🌦️" // rain
            "11d", "11n" -> "⛈️" // thunderstorm
            "13d", "13n" -> "❄️" // snow
            "50d", "50n" -> "🌫️" // mist
            else -> "🌤️" // default
        }
    }
}
