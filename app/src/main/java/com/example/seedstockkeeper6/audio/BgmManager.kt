package com.example.seedstockkeeper6.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.seedstockkeeper6.R

/**
 * BGM管理クラス（シングルトン）
 * アプリ全体でBGMを管理する
 */
class BgmManager private constructor(context: Context) {
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    
    var isEnabled: Boolean = true
        private set
    
    var volume: Float = 0.5f
        set(value) {
            field = value.coerceIn(0f, 1f)
            exoPlayer.volume = if (isEnabled) field else 0f
        }
    
    init {
        // BGMファイルを設定（res/raw/tanesuke_bgm.mp3）
        val mediaItem = MediaItem.fromUri(
            android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.sukesan_theme}")
        )
        exoPlayer.apply {
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL // ループ再生
            volume = this@BgmManager.volume
            prepare()
        }
    }
    
    /**
     * BGMを再生
     */
    fun play() {
        if (isEnabled) {
            exoPlayer.playWhenReady = true
        }
    }
    
    /**
     * BGMを一時停止
     */
    fun pause() {
        exoPlayer.playWhenReady = false
    }
    
    /**
     * BGMを停止
     */
    fun stop() {
        exoPlayer.stop()
    }
    
    /**
     * BGMの有効/無効を設定
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        exoPlayer.volume = if (enabled) volume else 0f
        if (enabled && !exoPlayer.playWhenReady) {
            play()
        } else if (!enabled) {
            pause()
        }
    }
    
    /**
     * リソースを解放
     */
    fun release() {
        exoPlayer.release()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: BgmManager? = null
        
        /**
         * BgmManagerのインスタンスを取得（シングルトン）
         */
        fun getInstance(context: Context): BgmManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BgmManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}





