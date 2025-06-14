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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PenSelectionFrament.newInstance] factory method to
 * create an instance of this fragment.
 */
class PenSelectionFrament : Fragment() {
    private lateinit var recycleView: RecyclerView
    private lateinit var adapter: PenAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pen_selection_frament, container, false)

        val penDrawables = listOf(
            R.drawable.pen1,
            R.drawable.pen5,
            R.drawable.pencil2,
            R.drawable.pen1
        )
        val btnBack = view.findViewById<View>(R.id.btnBack)

        adapter = PenAdapter(penDrawables) { selectedResId ->
            // Truyền resource ID đã chọn về ImageFragment
            parentFragmentManager.setFragmentResult(
                "PEN_SELECTION_REQUEST",
                Bundle().apply { putInt("SELECTED_PEN_RES_ID", selectedResId) }
            )

            // Quay lại màn hình trước
            parentFragmentManager.popBackStack()
        }
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        recycleView = view.findViewById(R.id.recyclerView2)
        recycleView.layoutManager = GridLayoutManager(context, 2)
        recycleView.adapter = adapter

        return view
    }


}