package com.example.seedstockkeeper6.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.seedstockkeeper6.R

@OptIn(UnstableApi::class)
@Composable
fun VideoSplashScreen(
    modifier: Modifier = Modifier,
    onVideoEnd: () -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // 音声再生のためのオーディオ属性を設定
            // handleAudioFocus = true でオーディオフォーカスを自動取得
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build()
            setAudioAttributes(audioAttributes, true)
            
            // 音量を最大に設定
            volume = 1f
            
            // リソースURIの構築（エラーハンドリング付き）
            try {
                val videoUri = RawResourceDataSource.buildRawResourceUri(R.raw.sukesan_s)
                val mediaItem = MediaItem.fromUri(videoUri)
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_OFF
                prepare()
            } catch (e: Exception) {
            }
        }
    }

    LaunchedEffect(exoPlayer) {
        // 再生準備が完了したら再生を開始
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        // 再生準備が完了したら再生を開始
                        if (!exoPlayer.playWhenReady) {
                            exoPlayer.playWhenReady = true
                        }
                    }
                    Player.STATE_ENDED -> {
                        onVideoEnd()
                    }
                }
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            }
        })
        
        // 再生を開始（prepare()は既に呼ばれている）
        exoPlayer.playWhenReady = true
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    keepScreenOn = true
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            }
        )
    }
}


