package com.app.kiranachoice.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.app.kiranachoice.adapters.*
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
            adapter = Category1Adapter(null)
        }

        binding.recyclerViewBestOffers.apply {
            adapter = ProductsAdapter(null)
        }

        binding.recyclerViewCategory2.apply {
            adapter = SmallBannerCategoryAdapter(null)
        }

        binding.recyclerViewBestSelling.apply {
            adapter = ProductsAdapter(null)
        }

        binding.recyclerViewCategory3.apply {
            adapter = SmallBannerCategoryAdapter(null)
        }

        binding.recyclerViewCategory4.apply {
            adapter = BannerCategoryAdapter(null)
        }

        binding.recyclerViewBestProductForYou.apply {
            adapter = ProductsAdapter(null)
        }

        binding.recyclerViewBanner3.apply {
            adapter = BigBannersAdapter(null)
        }

        binding.recyclerViewRecommendedProducts.apply {
            adapter = ProductsAdapter(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingHome = null
    }
}