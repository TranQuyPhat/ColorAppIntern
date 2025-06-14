package com.example.myapplication.Adapter

import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.caverock.androidsvg.SVG
import com.example.myapplication.R

class ImageAdapter(
    private val items: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileName = items[position]

        try {
            val inputStream = holder.itemView.context.assets.open(fileName)
            val svg = SVG.getFromInputStream(inputStream)
            val drawable = PictureDrawable(svg.renderToPicture())
            holder.imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            holder.imageView.setImageDrawable(drawable)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        holder.itemView.setOnClickListener {
            onClick(fileName)
        }
    }

    override fun getItemCount() = items.size
}
