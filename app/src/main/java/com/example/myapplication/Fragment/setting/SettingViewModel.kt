package com.example.myapplication.Fragment.setting

import androidx.lifecycle.MutableLiveData
import com.example.base.viewmodel.BaseViewModel
import com.example.myapplication.model.SettingItem
import com.example.myapplication.model.SettingType

class SettingViewModel : BaseViewModel() {
    val settingItem = MutableLiveData<List<SettingItem>>()

    init {
        settingItem.value = listOf(
            SettingItem(1, "Nhạc nền", SettingManager.isMusicEnabled(), SettingType.MUSIC),
            SettingItem(2, "Âm thanh", SettingManager.isSoundEnabled(), SettingType.SOUND),
            SettingItem(3, "Rung", SettingManager.isVibrateEnabled(), SettingType.VIBRATE),
            SettingItem(
                4,
                "Thông báo",
                SettingManager.isNotificationEnabled(),
                SettingType.NOTIFICATION
            )
        )
    }


    fun updateItem(updateItem: SettingItem) {
        val list = settingItem.value?.toMutableList() ?: return
        val index = list.indexOfFirst { it.id == updateItem.id }
        if (index != -1) {
            list[index] = updateItem
            settingItem.value = list
        }
    }
}