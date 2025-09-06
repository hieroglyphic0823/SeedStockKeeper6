package com.example.seedstockkeeper6

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FullScreenSaveAnimation() {
    var showSeeds by remember { mutableStateOf(false) }
    
    // 種袋の振りアニメーション
    val animatedRotation by animateFloatAsState(
        targetValue = if (showSeeds) 30f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseInOutQuart
        ),
        label = "shakeAnimation"
    )
    
    // 種の落下アニメーション
    val animatedSeedOffset by animateFloatAsState(
        targetValue = if (showSeeds) 200f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = EaseInOutQuart
        ),
        label = "seedFallAnimation"
    )
    
    // 種の透明度アニメーション
    val animatedSeedAlpha by animateFloatAsState(
        targetValue = if (showSeeds) 0f else 1f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = EaseInOutQuart
        ),
        label = "seedAlphaAnimation"
    )

    LaunchedEffect(Unit) {
        delay(300)
        showSeeds = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        // 種袋（中央）
        Icon(
            painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.grain),
            contentDescription = "種袋",
            modifier = Modifier
                .graphicsLayer(
                    rotationZ = animatedRotation
                )
                .size(80.dp),
            tint = Color.Unspecified
        )
        
        // バラバラに配置された種（15個）
        if (showSeeds) {
            // 種の位置をランダムに配置
            val seedPositions = listOf(
                -120 to -80, -80 to -120, -40 to -100, 0 to -140, 40 to -100, 80 to -120, 120 to -80,
                -100 to -40, -60 to -60, -20 to -80, 20 to -80, 60 to -60, 100 to -40,
                -80 to 0, -40 to -20, 0 to -40, 40 to -20, 80 to 0,
                -60 to 40, -20 to 20, 20 to 20, 60 to 40,
                -40 to 80, 0 to 60, 40 to 80,
                -20 to 120, 20 to 120,
                0 to 160
            )
            
            seedPositions.forEachIndexed { index, (x, y) ->
                Box(
                    modifier = Modifier
                        .offset(
                            x = (x + animatedSeedOffset * 0.3f).dp,
                            y = (y + animatedSeedOffset).dp
                        )
                        .size(6.dp)
                        .graphicsLayer(alpha = animatedSeedAlpha)
                        .background(
                            color = Color(0xFF8B4513), // 茶色の種
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun AnimatedIcon(
    icon: ImageVector? = null,
    painter: Painter? = null,
    contentDescription: String?,
    tint: Color
) {
    var isPressed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(
            durationMillis = 150,
            easing = EaseInOutQuart
        ),
        label = "scaleAnimation"
    )
    
    val animatedRotation by animateFloatAsState(
        targetValue = if (isPressed) 10f else 0f,
        animationSpec = tween(
            durationMillis = 200,
            easing = EaseInOutQuart
        ),
        label = "rotationAnimation"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .graphicsLayer(
                scaleX = animatedScale,
                scaleY = animatedScale,
                rotationZ = animatedRotation
            )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                tint = tint
            )
        } else if (painter != null) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                tint = tint
            )
        }
    }
}

@Composable
fun AnimatedLogoutIcon(
    onClick: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    val animatedOffset by animateFloatAsState(
        targetValue = if (isAnimating) -35f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = EaseInOutQuart
        ),
        label = "pullAnimation"
    )
    
    val animatedRotation by animateFloatAsState(
        targetValue = if (isAnimating) 15f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = EaseInOutQuart
        ),
        label = "rotationAnimation"
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isAnimating) 1.1f else 1f,
        animationSpec = tween(
            durationMillis = 600,
            easing = EaseInOutQuart
        ),
        label = "scaleAnimation"
    )

    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                color = Color(0xFF654321), // より暗い土の色（収穫時の土）
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {
                isAnimating = true
                // アニメーション完了後にログアウト処理を実行
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(1200)
                    onClick()
                }
            }
        ) {
            Icon(
                painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.harvest),
                contentDescription = "サインアウト（ニンジンを抜く）",
                modifier = Modifier
                    .graphicsLayer(
                        translationY = animatedOffset,
                        rotationZ = animatedRotation,
                        scaleX = animatedScale,
                        scaleY = animatedScale
                    )
                    .size(20.dp),
                tint = Color.Unspecified
            )
        }
    }
}
