package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.seedstockkeeper6.ui.components.LoadingAnimationVideoPlayer

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SplashVideoPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingAnimationVideoPlayer(modifier = Modifier.fillMaxSize())
        }
    }
}


