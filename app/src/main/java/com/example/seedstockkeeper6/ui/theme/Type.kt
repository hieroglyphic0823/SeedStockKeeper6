package com.example.seedstockkeeper6.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.seedstockkeeper6.R

val MPlus1p = FontFamily(
    Font(R.font.mplus1p_regular, FontWeight.Normal),
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
    titleLarge = TextStyle(
        fontFamily = MPlus1p,
        fontWeight = FontWeight.Black,
        fontSize = 22.sp
    )
    /* Other default text styles to override
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
