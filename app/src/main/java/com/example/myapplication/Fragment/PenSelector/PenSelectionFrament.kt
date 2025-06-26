package com.example.myapplication.Fragment.PenSelector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.base.fragment.BaseFragmentSimple
import com.example.myapplication.Adapter.PenAdapter
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentPenSelectionFramentBinding


class PenSelectionFrament : BaseFragmentSimple<FragmentPenSelectionFramentBinding>() {
    private lateinit var adapter: PenAdapter
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPenSelectionFramentBinding.inflate(inflater, container, false)

    override fun initView() {
        adapter = PenAdapter { selectResId ->
            parentFragmentManager.setFragmentResult(
                "PEN_SELECTION_REQUEST",
                Bundle().apply { putInt("SELECTED_PEN_RES_ID", selectResId) })
            parentFragmentManager.popBackStack()
        }
        adapter.items = listOf(
            R.drawable.ic_pen1,
            R.drawable.ic_pen5,
            R.drawable.ic_pencil2,
            R.drawable.ic_pen1
        )
        binding.recyclerView2.layoutManager=GridLayoutManager(context,2)
        binding.recyclerView2.adapter = adapter
    }

    override fun initListener() {
       binding.btnBack.setOnClickListener{
           parentFragmentManager.popBackStack()
       }
    }

    override fun initData() {
    }


}