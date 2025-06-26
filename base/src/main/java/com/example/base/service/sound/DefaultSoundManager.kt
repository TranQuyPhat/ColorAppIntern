package com.example.base.service.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import com.example.base.R
import com.example.base.service.api.SessionChecker
import com.example.base.service.api.SoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class DefaultSoundManager(
    private val context: Context,
    private val session: SessionChecker
) : SoundManager {

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun loadSounds() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        scope.launch {
            val sounds = listOf(R.raw.touch_1) // Add more if needed
            sounds.forEach { resId ->
                val soundId = soundPool?.load(context, resId, 1)
                if (soundId != null) {
                    soundMap[resId] = soundId
                }
            }
        }
    }

    override fun play(@RawRes soundResId: Int, loop: Int) {
        if (!session.isSound()) return
        soundMap[soundResId]?.let { soundId ->
            soundPool?.play(soundId, 1f, 1f, 1, loop, 1f)
        }
    }

    override fun destroy() {
        scope.cancel()
        soundMap.clear()
        soundPool?.release()
        soundPool = null
    }
}