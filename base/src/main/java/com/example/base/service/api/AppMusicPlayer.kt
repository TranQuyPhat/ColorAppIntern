package com.example.base.service.api

import androidx.annotation.RawRes

interface AppMusicPlayer {
    fun playBackgroundMusic(@RawRes rawResId: Int)
    fun playFxMusic(@RawRes rawFxMusic: Int)
    fun pause()
    fun resume()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
}