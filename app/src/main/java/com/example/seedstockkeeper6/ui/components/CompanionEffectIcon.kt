package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.R
import androidx.compose.ui.graphics.Color
import com.example.seedstockkeeper6.util.normalizeEffectName
import com.example.seedstockkeeper6.util.normalizeFamilyName
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material3.ExperimentalMaterial3Api

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

