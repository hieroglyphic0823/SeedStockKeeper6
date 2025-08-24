package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.EnergySavingsLeaf
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Nature
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.util.normalizeEffectName

@Composable
fun CompanionEffectIcon(CompanionEffect: String?) {
    val normalized = normalizeEffectName(CompanionEffect)
    // png（画像ID）かImageVectorかを判定
    val iconRes: Int?
    val imageVector: ImageVector?
    when (normalized) {
        "害虫予防" -> {
            iconRes = R.drawable.antivirus
            imageVector = Icons.Outlined.BugReport
        }
        "病気予防" -> {
            iconRes = R.drawable.pharmacy
            imageVector = Icons.Outlined.HealthAndSafety
        }
        "生育促進" -> {
            iconRes = R.drawable.growth2
            imageVector = Icons.Outlined.Insights
        }
        "空間活用" -> {
            iconRes = R.drawable.grass
            imageVector = Icons.Outlined.Inventory2
        }
        "風味向上" -> {
            iconRes = R.drawable.tongue
            imageVector = Icons.Outlined.LocalFlorist
        }
        "土壌改善" -> {
            iconRes = R.drawable.regenerative
            imageVector = Icons.Outlined.Grass
        }
        "受粉促進" -> {
            iconRes = R.drawable.bee
            imageVector = Icons.Outlined.Nature
        }
        "雑草抑制" -> {
            iconRes = R.drawable.weeds
            imageVector = Icons.Outlined.Spa
        }
        "景観美化" -> {
            iconRes = R.drawable.leaf
            imageVector = Icons.Outlined.EnergySavingsLeaf
        }
        "水分保持" -> {
            iconRes = R.drawable.absorbent
            imageVector = Icons.Outlined.WaterDrop
        }
        "土壌pH調整" -> {
            iconRes = R.drawable.soilphmeter
            imageVector = Icons.Outlined.Science
        }
        "作業性向上" -> {
            iconRes = R.drawable.gardener
            imageVector = Icons.Outlined.FavoriteBorder
        }
        "収量安定化" -> {
            iconRes = R.drawable.harvest
            imageVector = Icons.Outlined.HealthAndSafety
        }
        else -> {
            iconRes = R.drawable.soil
            imageVector = Icons.Outlined.FavoriteBorder
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
                modifier = Modifier.size(32.dp)
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

