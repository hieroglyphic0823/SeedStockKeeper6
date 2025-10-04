package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seedstockkeeper6.data.WeatherData
import com.example.seedstockkeeper6.data.OpenWeatherIconMapper
import com.example.seedstockkeeper6.data.WeeklyWeatherData
import java.text.SimpleDateFormat
import java.util.*

/**
 * 週間天気予報カード
 */
@Composable
fun WeeklyWeatherCard(
    weeklyWeatherData: WeeklyWeatherData?,
    isLoading: Boolean = false,
    error: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            
            when {
                isLoading -> {
                    LoadingWeatherContent()
                }
                error != null -> {
                    ErrorWeatherContent(error)
                }
                weeklyWeatherData != null -> {
                    WeatherContent(weeklyWeatherData)
                }
                else -> {
                    EmptyWeatherContent()
                }
            }
        }
    }
}

/**
 * 天気予報コンテンツ
 */
@Composable
private fun WeatherContent(weeklyWeatherData: WeeklyWeatherData) {
    // すべて横並びで表示（現在の天気も含む）
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        // 現在の天気を最初に追加
        item {
            DailyWeatherItem(weeklyWeatherData.currentWeather, isToday = true)
        }
        
        // 週間予報
        items(weeklyWeatherData.dailyForecast) { weatherData ->
            DailyWeatherItem(weatherData)
        }
    }
}


/**
 * 日別天気アイテム
 */
@Composable
private fun DailyWeatherItem(weatherData: WeatherData, isToday: Boolean = false) {
    val dateFormat = SimpleDateFormat("M/d", Locale.getDefault())
    val dayFormat = SimpleDateFormat("E", Locale.getDefault())
    
    Column(
        modifier = Modifier
            .width(if (isToday) 80.dp else 70.dp)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 天気アイコン（最上部）
        Text(
            text = OpenWeatherIconMapper.getWeatherIcon(weatherData.weather.icon),
            fontSize = 20.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 月日と曜日を横並び
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isToday) "今日" else dayFormat.format(weatherData.date),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isToday)
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!isToday) {
                Text(
                    text = dateFormat.format(weatherData.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 最高気温と最低気温を横並び
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${weatherData.temperature.max.toInt()}°",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color = Color.Red
            )
            Text(
                text = "/",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${weatherData.temperature.min.toInt()}°",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color = Color.Blue
            )
        }
    }
}

/**
 * ローディング表示
 */
@Composable
private fun LoadingWeatherContent() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "天気予報を取得中...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * エラー表示
 */
@Composable
private fun ErrorWeatherContent(error: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⚠️",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 空の状態表示
 */
@Composable
private fun EmptyWeatherContent() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🌤️",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "天気予報を取得できませんでした",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
