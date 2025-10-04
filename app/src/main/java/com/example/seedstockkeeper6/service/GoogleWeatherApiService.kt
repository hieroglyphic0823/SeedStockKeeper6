package com.example.seedstockkeeper6.service

import com.example.seedstockkeeper6.data.GoogleWeatherResponse
import com.example.seedstockkeeper6.data.GoogleWeatherRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Google Weather API の Retrofit インターフェース
 */
interface GoogleWeatherApiService {
    @POST("v1/locations/{location}:getDailyForecast")
    suspend fun getDailyForecast(
        @Path("location") location: String, // "latitude,longitude" 形式
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body body: GoogleWeatherRequestBody = GoogleWeatherRequestBody()
    ): Response<GoogleWeatherResponse>
}
