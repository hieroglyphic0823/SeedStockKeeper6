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
    Font(R.font.mplus1p_medium, FontWeight.Medium),
    Font(R.font.mplus1p_bold, FontWeight.Bold)
)

val NotoSansJp = FontFamily(
    Font(R.font.notosansjp_regular, FontWeight.Normal),
    Font(R.font.notosansjp_medium, FontWeight.Medium),
    Font(R.font.notosansjp_bold, FontWeight.Bold)
)

// メモリ節約のため、システムフォントを使用
val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
)
