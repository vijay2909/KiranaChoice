package com.app.kiranachoice.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.app.kiranachoice.adapters.*
import com.app.kiranachoice.databinding.FragmentHomeBinding
import com.app.kiranachoice.models.Category1Model

class HomeFragment : Fragment(), Category1Adapter.CategoryClickListener {

    private var _bindingHome: FragmentHomeBinding? = null
    private val binding get() = _bindingHome!!
    private val homeViewModel by viewModels<HomeViewModel>()
    private lateinit var navController: NavController

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

        navController = Navigation.findNavController(view)

        homeViewModel.categoryList.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                binding.recyclerViewCategory1.apply {
                    setHasFixedSize(true)
                    adapter = Category1Adapter(it, this@HomeFragment)
                }
            }
        })

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

    override fun onCategoryItemClick(categoryModel: Category1Model) {
        Log.i("HomeViewModel", "onCategoryItemClick: model: $categoryModel ")
        navController.navigate(
            HomeFragmentDirections.actionNavHomeToCategoryFragment(
                categoryModel,
                categoryModel.category_name.toString()
            )
        )
    }
}