package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex

enum class LabelPosition {
    Top, Bottom, Center, TopStart, TopEnd, BottomStart, BottomEnd
}

@Composable
fun IconWithLabel(
    icon: @Composable () -> Unit, // アイコン描画用ラムダ
    label: String,
    position: LabelPosition = LabelPosition.Bottom,
    iconBoxSize: Dp = 40.dp,
    labelShape: Shape = RoundedCornerShape(6.dp)
) {
    val alignment = when (position) {
        LabelPosition.Top -> Alignment.TopCenter
        LabelPosition.Bottom -> Alignment.BottomCenter
        LabelPosition.Center -> Alignment.Center
        LabelPosition.TopStart -> Alignment.TopStart
        LabelPosition.TopEnd -> Alignment.TopEnd
        LabelPosition.BottomStart -> Alignment.BottomStart
        LabelPosition.BottomEnd -> Alignment.BottomEnd
    }

    Box(modifier = Modifier.size(iconBoxSize)) {
        // アイコン（背景含めて何でもOK）
        icon()

        // 背景なしラベル
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface, // 必要に応じて色変更
            modifier = Modifier
                .align(alignment)
                .zIndex(1f)
        )
    }
}
