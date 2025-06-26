package com.example.myapplication.model

import android.content.Context
import android.preference.PreferenceManager

object LevelManager {
    private val svgFiles = listOf("candy.svg","orange.svg", "carrot.svg", "apple.svg", "flower.svg", "orange.svg")
    var currentLevel: Int = 1
        private set

    fun init(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        currentLevel = prefs.getInt("current_level", 1)
    }

    fun getCurrentFile(): String = svgFiles.getOrElse(currentLevel - 1) { svgFiles[0] }

    fun nextLevel(context: Context): Boolean {
        if (currentLevel >= svgFiles.size) return false
        currentLevel++
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putInt("current_level", currentLevel)
            .apply()
        return true
    }
}
