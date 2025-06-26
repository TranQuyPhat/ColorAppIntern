package com.example.myapplication.model

enum class SettingType {
    MUSIC, SOUND, VIBRATE, NOTIFICATION
}

data class SettingItem(
    val id: Int,
    val title: String,
    var enabled: Boolean,
    val type: SettingType
)