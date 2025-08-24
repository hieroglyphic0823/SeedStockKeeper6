package com.example.seedstockkeeper6.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.seedstockkeeper6.R

val MPlus1p = FontFamily(
    Font(R.font.mplus1p_thin, FontWeight.Thin),
    Font(R.font.mplus1p_light, FontWeight.Light),
    Font(R.font.mplus1p_regular, FontWeight.Normal),
    Font(R.font.mplus1p_medium, FontWeight.Medium),
    Font(R.font.mplus1p_bold, FontWeight.Bold),
    Font(R.font.mplus1p_extrabold, FontWeight.ExtraBold),
    Font(R.font.mplus1p_black, FontWeight.Black)
)

val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = MPlus1p,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MPlus1p,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = MPlus1p,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MPlus1p,
        fontWeight = FontWeight.Black,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MPlus1p,
        fontWeight = FontWeight.Black,
        fontSize = 18.sp
    ),
    titleSmall = TextStyle(
        fontFamily = MPlus1p,
        fontWeight = FontWeight.Black,
        fontSize = 16.sp
    )
)
