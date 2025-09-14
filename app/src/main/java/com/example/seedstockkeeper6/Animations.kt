package com.example.seedstockkeeper6

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FullScreenSaveAnimation() {
    var showAnimation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // sukesan.gifのスケールアニメーション
    val animatedScale by animateFloatAsState(
        targetValue = if (showAnimation) 1f else 0.5f,
        animationSpec = tween(
            durationMillis = 500,
            easing = EaseInOutQuart
        ),
        label = "scaleAnimation"
    )
    
    // sukesan.gifの透明度アニメーション
    val animatedAlpha by animateFloatAsState(
        targetValue = if (showAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = EaseInOutQuart
        ),
        label = "alphaAnimation"
    )

    LaunchedEffect(Unit) {
        showAnimation = true
        delay(2000) // 2秒間表示
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        // sukesan.gifアニメーション
        Image(
            painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.sukesan),
            contentDescription = "保存完了",
            modifier = Modifier
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale,
                    alpha = animatedAlpha
                )
                .size(120.dp)
        )
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
