package com.example.myapplication.Fragment.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.base.clickWithSound
import com.example.base.fragment.BaseFragment
import com.example.myapplication.Adapter.SettingAdapter
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSettingBinding
import com.example.myapplication.model.SettingItem
import com.example.myapplication.model.SettingType
import com.example.myapplication.music.DefaultSessionChecker


class SettingFragment : BaseFragment<FragmentSettingBinding, SettingViewModel>() {
    private lateinit var adapter: SettingAdapter
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSettingBinding.inflate(inflater, container, false)

    override fun initView() {
        adapter = SettingAdapter { updatedItem ->
            viewModel.updateItem(updatedItem) // chỉ lưu tạm vào ViewModel
        }
        binding.recyclerSettings.adapter = adapter
        binding.recyclerSettings.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun initListener() {
        binding.btnBack.clickWithSound { findNavController().navigateUp() }
        binding.btnApply.clickWithSound {
            val currentItems = viewModel.settingItem.value ?: return@clickWithSound

            SettingManager.applySettingItems(currentItems)
            findNavController().navigateUp()

            MainActivity.musicPlayer?.let { player ->
                if (!SettingManager.isMusicEnabled()) {
                    player.stop()
                } else if (!player.isPlaying()) {
                    player.playBackgroundMusic(com.example.base.R.raw.ukulele)
                }
            }

        }
    }


    override fun initData() {
        viewModel.settingItem.observe(viewLifecycleOwner) { item ->
            adapter.submitList(item)
        }
    }

    fun applySetting(item: SettingItem) {
        when (item.type) {
            SettingType.SOUND -> SettingManager.setSoundEnabled(item.enabled)
            SettingType.MUSIC -> SettingManager.setMusicEnabled(item.enabled)
            SettingType.VIBRATE -> SettingManager.setVibrateEnabled(item.enabled)
            SettingType.NOTIFICATION -> SettingManager.setNotificationEnabled(item.enabled)
        }
    }

}