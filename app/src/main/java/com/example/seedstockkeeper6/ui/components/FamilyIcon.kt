package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.util.normalizeFamilyName
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.example.seedstockkeeper6.R
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme

@Composable
fun FamilyIcon(family: String?) {
    val normalized = normalizeFamilyName(family)

    // png（画像ID）かImageVectorかを判定
    val iconRes: Int? // 画像リソース
    val imageVector: ImageVector? // アイコン
    when (normalized) {
        "イネ科" -> {
            iconRes = R.drawable.corn
            imageVector = null
        }
        "ナス科" -> {
            iconRes = R.drawable.eggplant
            imageVector = null
        }
        "ヒルガオ科" -> {
            iconRes = R.drawable.sweet_potato
            imageVector = null
        }
        "アブラナ科" -> {
            iconRes = R.drawable.broccoli
            imageVector = null
        }
        "ウリ科" -> {
            iconRes = null
            imageVector = Icons.Filled.SportsBasketball
        }
        "マメ科" -> {
            iconRes = R.drawable.bean
            imageVector = null
        }
        "キク科" -> {
            iconRes = R.drawable.lettuce
            imageVector = null
        }
        "セリ科" -> {
            iconRes = null
            imageVector = Icons.Filled.FavoriteBorder
        }
        "ネギ科" -> {
            iconRes = null
            imageVector = Icons.Filled.Grass
        }
        "アマランサス科" -> {
            iconRes = null
            imageVector = Icons.Filled.Eco
        }
        "バラ科" -> {
            iconRes = null
            imageVector = Icons.Filled.FilterVintage
        }
        "ミカン科" -> {
            iconRes = null
            imageVector = Icons.Filled.Brightness5
        }
        else -> {
            iconRes = null
            imageVector = Icons.Filled.DeviceUnknown
        }
    }

    if (iconRes != null) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary), // テーマ色で背景、border無し
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "$normalized のアイコン",
                modifier = Modifier.size(24.dp)
            )
        }
    } else if (imageVector != null) {
        Icon(
            imageVector = imageVector,
            contentDescription = "$normalized のアイコン",
            modifier = Modifier.size(32.dp)
        )
    }
}
