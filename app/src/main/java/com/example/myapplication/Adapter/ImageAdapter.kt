package com.example.myapplication.Adapter

import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.caverock.androidsvg.SVG
import com.example.base.adapter.BaseAdapter
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemImageBinding

class ImageAdapter(
    onClick: (String) -> Unit
) : BaseAdapter<String, ItemImageBinding>(ItemImageBinding::inflate, onClick) {

    override fun onBind(binding: ItemImageBinding, item: String, position: Int) {
        try {
            val inputStream = binding.root.context.assets.open(item)
            val svg = SVG.getFromInputStream(inputStream)
            val drawable = PictureDrawable(svg.renderToPicture())
            binding.imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            binding.imageView.setImageDrawable(drawable)
            inputStream.close()
        } catch (e: Exception) {
            Toast.makeText(binding.root.context, "Không thể tải $item", Toast.LENGTH_SHORT).show()
        }
    }
}
