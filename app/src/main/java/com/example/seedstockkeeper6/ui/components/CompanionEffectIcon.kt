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
import com.example.seedstockkeeper6.model.CompanionEffectCode

@Composable
fun CompanionEffectIcon(effects: List<String>) {
    // 最大3つまで表示
    effects.take(3).forEach { effectCode ->
        SingleEffectIcon(CompanionEffectCode.fromCode(effectCode))
        if (effectCode != effects.take(3).last()) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(4.dp))
        }
    }
}

@Composable
fun SingleEffectIcon(effectCode: CompanionEffectCode) {
    // png（画像ID）かImageVectorかを判定
    val iconRes: Int?
    val imageVector: ImageVector?
    when (effectCode) {
        CompanionEffectCode.PEST_PREVENTION -> {
            iconRes = R.drawable.antivirus
            imageVector = Icons.Outlined.BugReport
        }
        CompanionEffectCode.DISEASE_PREVENTION -> {
            iconRes = R.drawable.pharmacy
            imageVector = Icons.Outlined.HealthAndSafety
        }
        CompanionEffectCode.GROWTH_PROMOTION -> {
            iconRes = R.drawable.growth2
            imageVector = Icons.Outlined.Insights
        }
        CompanionEffectCode.SPACE_UTILIZATION -> {
            iconRes = R.drawable.grass
            imageVector = Icons.Outlined.Inventory2
        }
        CompanionEffectCode.FLAVOR_ENHANCEMENT -> {
            iconRes = R.drawable.tongue
            imageVector = Icons.Outlined.LocalFlorist
        }
        CompanionEffectCode.SOIL_IMPROVEMENT -> {
            iconRes = R.drawable.regenerative
            imageVector = Icons.Outlined.Grass
        }
        CompanionEffectCode.POLLINATION_PROMOTION -> {
            iconRes = R.drawable.bee
            imageVector = Icons.Outlined.Nature
        }
        CompanionEffectCode.WEED_SUPPRESSION -> {
            iconRes = R.drawable.weeds
            imageVector = Icons.Outlined.Spa
        }
        CompanionEffectCode.LANDSCAPE_BEAUTIFICATION -> {
            iconRes = R.drawable.leaf
            imageVector = Icons.Outlined.EnergySavingsLeaf
        }
        CompanionEffectCode.MOISTURE_RETENTION -> {
            iconRes = R.drawable.absorbent
            imageVector = Icons.Outlined.WaterDrop
        }
        CompanionEffectCode.SOIL_PH_ADJUSTMENT -> {
            iconRes = R.drawable.soilphmeter
            imageVector = Icons.Outlined.Science
        }
        CompanionEffectCode.WORKABILITY_IMPROVEMENT -> {
            iconRes = R.drawable.gardener
            imageVector = Icons.Outlined.FavoriteBorder
        }
        CompanionEffectCode.YIELD_STABILIZATION -> {
            iconRes = R.drawable.harvest
            imageVector = Icons.Outlined.HealthAndSafety
        }
        CompanionEffectCode.OTHER -> {
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
                contentDescription = "${effectCode.displayName} のアイコン",
                modifier = Modifier.size(32.dp)
            )
        }
    } else if (imageVector != null) {
        Icon(
            imageVector = imageVector,
            contentDescription = "${effectCode.displayName} のアイコン",
            modifier = Modifier.size(32.dp)
        )
    }
}

