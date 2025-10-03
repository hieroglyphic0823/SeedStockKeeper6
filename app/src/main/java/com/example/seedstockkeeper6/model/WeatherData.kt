package com.example.seedstockkeeper6.model

data class WeatherData(
    val date: String,
    val temperature: String,
    val condition: String,
    val icon: String
)

data class WeeklyWeather(
    val location: String,
    val days: List<WeatherData>
)
