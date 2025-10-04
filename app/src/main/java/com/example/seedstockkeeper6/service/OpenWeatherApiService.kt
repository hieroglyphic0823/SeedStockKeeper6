package com.example.seedstockkeeper6.service

import com.example.seedstockkeeper6.data.OpenWeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * OpenWeatherMap API の Retrofit インターフェース
 */
interface OpenWeatherApiService {
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ja"
    ): Response<OpenWeatherResponse>
}
