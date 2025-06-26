package com.example.base.service.sound

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.annotation.RawRes
import androidx.media3.exoplayer.ExoPlayer
import com.example.base.service.api.AppMusicPlayer
import com.example.base.service.api.SessionChecker
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource

class DefaultAppMusicPlayer(
    private val context: Context,
    private val session: SessionChecker
) : AppMusicPlayer {

    private var bgPlayer: ExoPlayer? = null
    private var fxPlayer: ExoPlayer? = null
    private var currentUrl: String? = null

    @OptIn(UnstableApi::class)
    override fun playBackgroundMusic(@RawRes rawResId: Int) {
        if (!session.isMusic()) return

        val uri = Uri.parse("android.resource://${context.packageName}/$rawResId")
        val mediaItem = MediaItem.fromUri(uri)
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)

        bgPlayer = bgPlayer ?: ExoPlayer.Builder(context).build()
        bgPlayer?.apply {
            setMediaSource(mediaSource)
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
            prepare()
            play()
        }
    }

    override fun playFxMusic(@RawRes rawFxMusic: Int) {
        if (!session.isSound()) return
        val uri = Uri.parse("android.resource://${context.packageName}/$rawFxMusic")
        fxPlayer = fxPlayer ?: ExoPlayer.Builder(context).build()
        fxPlayer?.apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            play()
        }
    }

    override fun pause() {
        bgPlayer?.pause()
    }

    override fun resume() {
        bgPlayer?.play()
    }

    override fun stop() {
        bgPlayer?.stop()
    }

    override fun release() {
        bgPlayer?.release()
        fxPlayer?.release()
        bgPlayer = null
        fxPlayer = null
    }

    override fun isPlaying(): Boolean {
        return bgPlayer?.isPlaying ?: false
    }
}