package com.example.myapplication.Fragment.setting

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.model.SettingItem
import com.example.myapplication.model.SettingType

object SettingManager {
    private const val PREF_NAME = "user_settings"
    private const val KEY_MUSIC = "music_enabled"
    private const val KEY_SOUND = "sound_enabled"
    private const val KEY_VIBRATE = "vibrate_enabled"
    private const val KEY_NOTIFICATION = "notification_enabled"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setMusicEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_MUSIC, enabled).apply()
    fun isMusicEnabled(): Boolean = prefs.getBoolean(KEY_MUSIC, true)

    fun setSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND, true)

    fun setVibrateEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_VIBRATE, enabled).apply()
    fun isVibrateEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATE, false)

    fun setNotificationEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_NOTIFICATION, enabled).apply()
    fun isNotificationEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATION, true)

    fun applySettingItems(items: List<SettingItem>) {
        items.forEach { item ->
            when (item.type) {
                SettingType.MUSIC -> setMusicEnabled(item.enabled)
                SettingType.SOUND -> setSoundEnabled(item.enabled)
                SettingType.VIBRATE -> setVibrateEnabled(item.enabled)
                SettingType.NOTIFICATION -> setNotificationEnabled(item.enabled)
            }
        }
    }


}
