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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
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
            iconRes = R.drawable.cucumber
            imageVector = null
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
            iconRes = R.drawable.carrot
            imageVector = null
        }
        "ネギ科" -> {
            iconRes = R.drawable.onion2
            imageVector = null
        }
        "アマランサス科" -> {
            iconRes = R.drawable.spinach
            imageVector = null
        }
        "バラ科" -> {
            iconRes = R.drawable.strawberry
            imageVector = null
        }
        "ミカン科" -> {
            iconRes =  R.drawable.orange
            imageVector = null
        }
        else -> {
            iconRes = R.drawable.vegetables
            imageVector = null
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
