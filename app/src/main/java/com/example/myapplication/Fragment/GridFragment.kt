package com.example.myapplication.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.ImageAdapter
import com.example.myapplication.Adapter.PenAdapter
import com.example.myapplication.R

class GridFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grid, container, false)
        val svgFiles = listOf(
            "orange.svg",
            "carrot.svg",
            "apple.svg",
            "flower.svg",
        )
        val recyclerView = view.findViewById<RecyclerView>(R.id.imagelist)
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        val imageAdapter = ImageAdapter { fileName ->
            val bundle = Bundle().apply {
                putString("file_name", fileName)
                putInt("level", 0)
            }
            findNavController().navigate(R.id.imageFragment, bundle)
        }

        imageAdapter.items = svgFiles
        recyclerView.adapter = imageAdapter

        return view
    }


}