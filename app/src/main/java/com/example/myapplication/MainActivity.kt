package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.base.service.api.AppMusicPlayer
import com.example.base.service.api.SoundManager
import com.example.base.service.sound.DefaultAppMusicPlayer
import com.example.base.service.sound.DefaultSoundManager
import com.example.myapplication.music.DefaultSessionChecker
import com.example.myapplication.music.MusicLifecycleObserver

class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var musicPlayer: AppMusicPlayer
        lateinit var soundManager: SoundManager
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.findNavController()

        if (savedInstanceState == null) {
            navController.navigate(R.id.homeFragment)
        }
        musicPlayer = DefaultAppMusicPlayer(applicationContext, DefaultSessionChecker())
        musicPlayer.playBackgroundMusic(com.example.base.R.raw.ukulele)
        lifecycle.addObserver(MusicLifecycleObserver(musicPlayer))

        soundManager = DefaultSoundManager(applicationContext, DefaultSessionChecker())
        soundManager.loadSounds()
    }
    override fun onDestroy() {
        super.onDestroy()
        soundManager.destroy()
        musicPlayer.release()
    }


}
