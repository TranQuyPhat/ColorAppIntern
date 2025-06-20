package com.example.myapplication.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.ImageAdapter
import com.example.myapplication.Adapter.PenAdapter
import com.example.myapplication.R


class PenSelectionFrament : Fragment() {
    private lateinit var recycleView: RecyclerView
    private lateinit var adapter: PenAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pen_selection_frament, container, false)

        val btnBack = view.findViewById<View>(R.id.btnBack)

        adapter = PenAdapter { selectResId ->
            parentFragmentManager.setFragmentResult(
                "PEN_SELECTION_REQUEST",
                Bundle().apply {
                    putInt("SELECTED_PEN_RES_ID", selectResId)
                })
            parentFragmentManager.popBackStack()
        }
        adapter.items = listOf(
            R.drawable.pen1,
            R.drawable.pen5,
            R.drawable.pencil2,
            R.drawable.pen1
        )
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        recycleView = view.findViewById(R.id.recyclerView2)
        recycleView.layoutManager = GridLayoutManager(context, 2)
        recycleView.adapter = adapter

        return view
    }


}