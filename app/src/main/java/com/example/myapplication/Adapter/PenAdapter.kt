package com.example.myapplication.Adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class PenAdapter(
    private val penDrawables: List<Int>,
    private val onPenSelected: (Int) -> Unit
) : RecyclerView.Adapter<PenAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resId = penDrawables[position]
        holder.imageView.setImageResource(resId)

        holder.itemView.setOnClickListener {
            onPenSelected(resId)
        }
    }

    override fun getItemCount() = penDrawables.size
}
