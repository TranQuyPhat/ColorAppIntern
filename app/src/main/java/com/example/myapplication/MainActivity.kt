package com.example.myapplication

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.myapplication.R

class MainActivity : AppCompatActivity() {
    private val svgFiles = listOf("orange.svg","carrot.svg","apple.svg","flower.svg","orange.svg")
    private var currentLevel: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        currentLevel = sharedPreferences.getInt("current_level", 1)
        val fileNameToLoad = svgFiles[currentLevel - 1]

        val bundle = Bundle().apply {
            putString("file_name", fileNameToLoad)
            putInt("level", currentLevel)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.findNavController()

        if (savedInstanceState == null) {
            navController.navigate(R.id.firstFragment, bundle)
        }
    }

    fun loadNextLevel() {
        if (currentLevel >= svgFiles.size) return

        currentLevel++
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPref.edit().putInt("current_level", currentLevel).apply()

        val fileName = svgFiles[currentLevel - 1]
        val bundle = Bundle().apply {
            putString("file_name", fileName)
            putInt("level", currentLevel)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.findNavController()

        navController.navigate(R.id.firstFragment, bundle)
    }

}
