package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.util.normalizeFamilyName

@Composable
fun FamilyIcon(family: String?) {
    val normalized = normalizeFamilyName(family)

    val icon = when (normalized) {
        "ナス科" -> Icons.Filled.Lens  // なければ代用
        "アブラナ科" -> Icons.Filled.BubbleChart
        "ウリ科" -> Icons.Filled.SportsBasketball
        "マメ科" -> Icons.Filled.EmojiNature
        "イネ科" -> Icons.Filled.Brightness5
        "キク科" -> Icons.Filled.LocalFlorist
        "セリ科" -> Icons.Filled.FavoriteBorder
        "ネギ科" -> Icons.Filled.Grass
        "アマランサス科" -> Icons.Filled.Eco
        "バラ科" -> Icons.Filled.FilterVintage
        "ミカン科" -> Icons.Filled.Brightness5
        else -> Icons.Filled.DeviceUnknown
    }

    Icon(
        imageVector = icon,
        contentDescription = "$normalized のアイコン",
        modifier = Modifier.size(32.dp)
    )
}
