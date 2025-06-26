package com.example.myapplication.music

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.base.service.api.AppMusicPlayer

class MusicLifecycleObserver (private val musicPlayer: AppMusicPlayer):DefaultLifecycleObserver{
    override fun onStart(owner: LifecycleOwner) {
        musicPlayer.resume()
    }

    override fun onStop(owner: LifecycleOwner) {
        musicPlayer.pause()
    }
}