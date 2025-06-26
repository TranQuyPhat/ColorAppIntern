package com.example.base.adapter

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T, VB : ViewBinding>(
    private val inflater: (LayoutInflater, ViewGroup, Boolean) -> VB,
    private val onClick: ((T) -> Unit)? = null
) : RecyclerView.Adapter<BaseAdapter<T, VB>.BaseViewHolder>() {

    var items: List<T> = listOf()

    inner class BaseViewHolder(val binding: VB) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: T) {
            onBind(binding, item, adapterPosition)
            binding.root.setOnClickListener {
                onClick?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(inflater(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    abstract fun onBind(binding: VB, item: T, position: Int)
}
