package com.example.seedstockkeeper6.ui.components

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout

@Composable
fun LoadingAnimationVideoPlayer(
    modifier: Modifier = Modifier,
    assetFileName: String? = "tanesukemovie.mp4", // assets に配置する場合
    rawResId: Int? = null,                         // res/raw に配置する場合
    repeat: Boolean = true,
    mute: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = when {
                rawResId != null -> {
                    val uri = RawResourceDataSource.buildRawResourceUri(rawResId)
                    MediaItem.fromUri(uri)
                }
                !assetFileName.isNullOrBlank() -> {
                    MediaItem.fromUri(Uri.parse("asset:///" + assetFileName))
                }
                else -> null
            }
            if (mediaItem != null) {
                setMediaItem(mediaItem)
            }
            repeatMode = if (repeat) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
            volume = if (mute) 0f else 1f
            prepare()
            playWhenReady = true
        }
    }

    // ライフサイクルに合わせて再生/一時停止
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> exoPlayer.playWhenReady = true
                Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_PAUSE -> exoPlayer.playWhenReady = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                keepScreenOn = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // 全画面フィット
            }
        }
    )
}


