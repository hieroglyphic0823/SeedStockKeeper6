package com.example.seedstockkeeper6.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SwipeToDeleteItem(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    
    // 削除ボタンの幅（dp）
    val deleteButtonWidthDp = 80.dp
    val deleteButtonWidthPx = with(density) { deleteButtonWidthDp.toPx() }
    
    // スワイプの最大オフセット（削除ボタンの幅まで）
    val maxSwipeOffset = deleteButtonWidthPx
    
    // アニメーション用のオフセット
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 300),
        label = "swipeAnimation"
    )
    
    // スワイプが削除ボタンの半分以上露出した場合の閾値
    val deleteThreshold = deleteButtonWidthPx * 0.5f
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
    ) {
        // 削除ボタン（背景）- スワイプ幅に応じて動的に表示
        if (animatedOffsetX < 0) {
            // スワイプ幅に応じた削除ボタンの幅を計算
            val currentSwipeWidth = (-animatedOffsetX).coerceAtMost(maxSwipeOffset)
            val currentButtonWidthDp = with(density) { currentSwipeWidth.toDp() }
            
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(currentButtonWidthDp)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            // 削除アニメーション
                            offsetX = -maxSwipeOffset
                            kotlinx.coroutines.delay(150)
                            onDelete()
                            // 削除後にリストを再表示するため、オフセットをリセット
                            offsetX = 0f
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "削除",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // メインコンテンツ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = animatedOffsetX.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            // ドラッグ終了時の処理
                            scope.launch {
                                if (offsetX < -deleteThreshold) {
                                    // 削除ボタンが半分以上露出している場合、完全に表示
                                    offsetX = -maxSwipeOffset
                                } else {
                                    // そうでなければ元の位置に戻す
                                    offsetX = 0f
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        // 横方向のドラッグのみ処理（縦スクロールを阻害しない）
                        if (kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y)) {
                            // 左方向のスワイプのみ許可
                            val newOffset = (offsetX + dragAmount.x).coerceIn(-maxSwipeOffset, 0f)
                            offsetX = newOffset
                        }
                    }
                }
        ) {
            content()
        }
    }
}
