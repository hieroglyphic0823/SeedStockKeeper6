package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    size: Dp = 48.dp,
    cornerRadius: Dp = 10.dp, // ← 角丸。四角なら 0.dp
    rotationLabel: String? = null, // ← 右上に出す「連作年数」など（例: "3年" / "3"）
    badgeProtrusion: Dp = 8.dp,                   // ← アイコン外へハミ出す量（+X, -Y）
    showCircleBorder: Boolean = false // ← 円い枠を表示するかどうか
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
        "セリ科", "せり科" -> R.drawable.carrot
        "ヒガンバナ科" -> R.drawable.onion2
        "アマランサス科" -> R.drawable.spinach
        "バラ科" -> R.drawable.strawberry
        "ミカン科" -> R.drawable.orange
        "アカザ科" -> R.drawable.spinach // ホウレンソウなど
        "シソ科" -> R.drawable.perilla // ハーブ
        "ユリ科（ネギ類）" -> R.drawable.onion2 // ネギ類
        "ショウガ科" -> R.drawable.ginger // ショウガ
        "アオイ科" -> R.drawable.okra // オクラ
        else -> R.drawable.vegetables
    }

    Box(modifier = Modifier.size(size)) {
        // 本体（四角いタイル）
        Surface(
            color = if (showCircleBorder) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surface, // 円い枠の場合はsurfaceContainerLowest
            contentColor = if (showCircleBorder) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
            shape = if (showCircleBorder) CircleShape else RoundedCornerShape(4.dp), // 円い枠の場合は円形
            tonalElevation = if (showCircleBorder) 2.dp else 0.dp, // 円い枠の場合はエレベーションを追加
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
                        modifier = Modifier.size(24.dp) // 24.dpに統一（デフォルト）
                    )
                }
            }
        }

        // 右上の丸いバッジ（連作年数など）
        val labelText = rotationLabel?.trim().orEmpty()
        if (labelText.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = CircleShape, // 丸い形状に変更
                tonalElevation = 2.dp, // Material3ではエレベーションで区別
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

/**
 * コンパニオンプランツと同じスタイルの丸い科名アイコン
 */
@Composable
fun FamilyIconCircle(
    family: String?
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
        "セリ科", "せり科" -> R.drawable.carrot
        "ヒガンバナ科" -> R.drawable.onion2
        "アマランサス科" -> R.drawable.spinach
        "バラ科" -> R.drawable.strawberry
        "ミカン科" -> R.drawable.orange
        "アカザ科" -> R.drawable.spinach // ホウレンソウなど
        "シソ科" -> R.drawable.perilla // ハーブ
        "ユリ科（ネギ類）" -> R.drawable.onion2 // ネギ類
        "ショウガ科" -> R.drawable.ginger // ショウガ
        "アオイ科" -> R.drawable.okra // オクラ
        else -> R.drawable.vegetables
    }

    // コンパニオンプランツと同じスタイルの丸いアイコン
    if (iconRes != null) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "$normalized のアイコン",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
