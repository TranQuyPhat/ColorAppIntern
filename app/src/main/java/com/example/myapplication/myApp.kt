package com.example.myapplication

import android.app.Application
import com.example.base.service.api.GlobalSettingChecker
import com.example.base.service.api.GlobalSoundPlayer
import com.example.base.service.sound.SettingCheckerProvider
import com.example.base.service.sound.SoundPlayerProvider
import com.example.myapplication.Fragment.setting.SettingManager

class myApp:Application() {
    override fun onCreate() {
        super.onCreate()
        SettingManager.init(this)
        SettingCheckerProvider.checker = object : GlobalSettingChecker {
            override fun isSoundEnabled() = SettingManager.isSoundEnabled()
            override fun isMusicEnabled() = SettingManager.isMusicEnabled()
            override fun isVibrateEnabled() = SettingManager.isVibrateEnabled()
        }
        SoundPlayerProvider.soundPlayer = object : GlobalSoundPlayer {
            override fun play(soundResId: Int) {
                MainActivity.soundManager.play(soundResId)
            }
        }

    }
}