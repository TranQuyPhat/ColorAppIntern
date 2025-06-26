package com.example.myapplication.Fragment.home

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.example.base.clickWithSound
import com.example.base.fragment.BaseFragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.model.LevelManager


class HomeFragment : BaseFragment<FragmentHomeBinding,HomeViewModel>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    )= FragmentHomeBinding.inflate(inflater,container,false)


    override fun initView() {
        LevelManager.init(requireContext())
    }

    override fun initListener() {
        binding.btnDrawing.clickWithSound {
            val bundle = Bundle().apply {
                putString("file_name", LevelManager.getCurrentFile())
                putInt("level", LevelManager.currentLevel)
            }
            findNavController().navigate(R.id.action_homeFragment_to_imageFragment, bundle)
        }

        binding.btnArtCollection.clickWithSound {
            findNavController().navigate(R.id.homeToGrid)
        }
        binding.btnSettings.clickWithSound(){
            findNavController().navigate(R.id.homeToSetting)
        }

        parentFragmentManager.setFragmentResultListener("LOAD_NEXT_LEVEL", this) { _, _ ->
            loadNextLevel()
        }
    }

    override fun initData() {
    }

    private fun loadNextLevel() {
        if (!LevelManager.nextLevel(requireContext())) return

        val bundle = Bundle().apply {
            putString("file_name", LevelManager.getCurrentFile())
            putInt("level", LevelManager.currentLevel)
        }

        findNavController().navigate(R.id.imageFragment, bundle)
    }
}