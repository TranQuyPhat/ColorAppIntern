package com.example.base.service.api

import androidx.annotation.RawRes


interface SoundManager {
    fun loadSounds()
    fun play(@RawRes soundResId: Int, loop: Int = 0)
    fun destroy()
}