package com.example.seedstockkeeper6.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

@Composable
fun SeedStockTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Pixelなどで動的カラーが使えるなら true
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
object AppColors {

    val sowingWithinExpiration: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primary

    val harvestWithinExpiration: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.secondary

    val expired: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    val calendarMonthBackgroundWithinExpiration: Color//有効期限内
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)

    val calendarMonthBackgroundExpired: Color//有効期限切れ
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)

    // 他に必要な色があればここに追加
    val textPaintColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface

    val outline: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.outline
}