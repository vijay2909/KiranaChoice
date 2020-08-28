package com.app.kiranachoice.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.app.kiranachoice.R
import com.app.kiranachoice.adapters.CategoryAdapter
import com.app.kiranachoice.adapters.ProductsAdapter
import com.app.kiranachoice.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _bindingHome : FragmentHomeBinding? = null
    private val binding get() = _bindingHome!!
    private val homeViewModel by viewModels<HomeViewModel>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _bindingHome = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewCategory1.apply {
            adapter = CategoryAdapter(null)
        }

        binding.recyclerViewCategory2.apply {
            adapter = ProductsAdapter(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingHome = null
    }
}