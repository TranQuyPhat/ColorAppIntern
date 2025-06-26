package com.example.myapplication.Adapter

import android.view.View
import com.example.base.adapter.BaseAdapter
import com.example.myapplication.Fragment.setting.SettingManager
import com.example.myapplication.databinding.ItemSettingBinding
import com.example.myapplication.model.SettingItem
import com.example.myapplication.model.SettingType

class SettingAdapter(private val onTongle: (SettingItem) -> Unit) :
    BaseAdapter<SettingItem, ItemSettingBinding>(ItemSettingBinding::inflate) {
    override fun onBind(binding: ItemSettingBinding, item: SettingItem, position: Int) {
        binding.tvSettingTitle.text = item.title

        // Chỉ hiển thị switch cho các type hỗ trợ
        when (item.type) {
            SettingType.MUSIC,
            SettingType.SOUND,
            SettingType.VIBRATE -> {
                binding.switchToggle.visibility = View.VISIBLE
                binding.switchToggle.setOnCheckedChangeListener(null)
                binding.switchToggle.isChecked = item.enabled
                binding.switchToggle.setOnCheckedChangeListener { _, isChecked ->
                    onTongle(item.copy(enabled = isChecked))
                }
            }
            else -> {
                binding.switchToggle.visibility = View.GONE
            }
        }
    }


    fun submitList(list: List<SettingItem>) {
        items = list
        notifyDataSetChanged()
    }

}