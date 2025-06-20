package com.example.myapplication.Adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.base.adapter.BaseAdapter
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemImageBinding

class PenAdapter(
    onClick: (Int) -> Unit
) : BaseAdapter<Int, ItemImageBinding>(ItemImageBinding::inflate, onClick) {
    override fun onBind(binding: ItemImageBinding, item: Int, position: Int) {
        binding.imageView.setImageResource(item)
    }
}
