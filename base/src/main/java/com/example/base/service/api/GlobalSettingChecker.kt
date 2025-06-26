package com.example.base.service.api


interface GlobalSettingChecker {
    fun isSoundEnabled(): Boolean
    fun isMusicEnabled(): Boolean
    fun isVibrateEnabled(): Boolean
}