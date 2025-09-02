package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.util.normalizeFamilyName

@Composable
fun FamilyIcon(
    family: String?,
    size: Dp = 40.dp,
    cornerRadius: Dp = 10.dp, // ← 角丸。四角なら 0.dp
    rotationLabel: String? = null, // ← 右上に出す「連作年数」など（例: "3年" / "3"）
    badgeProtrusion: Dp = 8.dp                   // ← アイコン外へハミ出す量（+X, -Y）
) {
    val normalized = normalizeFamilyName(family)

    val iconRes: Int? = when (normalized) {
        "イネ科" -> R.drawable.corn
        "ナス科" -> R.drawable.eggplant
        "ヒルガオ科" -> R.drawable.sweet_potato
        "アブラナ科" -> R.drawable.broccoli
        "ウリ科" -> R.drawable.cucumber
        "マメ科" -> R.drawable.bean
        "キク科" -> R.drawable.lettuce
        "セリ科" -> R.drawable.carrot
        "ネギ科" -> R.drawable.onion2
        "アマランサス科" -> R.drawable.spinach
        "バラ科" -> R.drawable.strawberry
        "ミカン科" -> R.drawable.orange
        else -> R.drawable.vegetables
    }

    Box(modifier = Modifier.size(size)) {
        // 本体（四角いタイル）
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(cornerRadius),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (iconRes != null) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = "$normalized のアイコン",
                        modifier = Modifier.size((size.value * 0.6f).dp) // 元の 24dp 相当を自動スケール
                    )
                }
            }
        }

        // 右上の枠付きバッジ（連作年数など）
        val labelText = rotationLabel?.trim().orEmpty()
        if (labelText.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = badgeProtrusion, y = -badgeProtrusion) // ★ はみ出し
                    .zIndex(1f)
            ) {
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
