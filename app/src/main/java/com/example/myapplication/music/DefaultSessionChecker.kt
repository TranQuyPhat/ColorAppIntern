package com.example.myapplication.music

import android.content.Context
import com.example.base.service.api.SessionChecker
import com.example.myapplication.Fragment.setting.SettingManager

class DefaultSessionChecker() : SessionChecker {
    override fun isMusic(): Boolean = SettingManager.isMusicEnabled()
    override fun isSound(): Boolean = SettingManager.isSoundEnabled()
}
