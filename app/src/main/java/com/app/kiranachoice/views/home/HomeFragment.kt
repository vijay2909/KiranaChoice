package com.app.kiranachoice.views.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentHomeBinding
import com.app.kiranachoice.models.Banner
import com.app.kiranachoice.models.Category1Model
import com.app.kiranachoice.recyclerView_adapters.*
import com.app.kiranachoice.viewpager_adapters.HomeTopBannerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment(), Category1Adapter.CategoryClickListener {

    private var _bindingHome: FragmentHomeBinding? = null
    private val binding get() = _bindingHome!!

    private val homeViewModel by viewModels<HomeViewModel>()

    private lateinit var navController: NavController

    private lateinit var viewPager: ViewPager2
    private lateinit var handler: Handler
    private lateinit var callback: ViewPager2.OnPageChangeCallback
    private val imageList = ArrayList<Banner>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bindingHome = FragmentHomeBinding.inflate(inflater, container, false)
        viewPager = binding.homeBanner1
        handler = Handler(Looper.getMainLooper())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        homeViewModel.categoryList.observe(viewLifecycleOwner, {
            binding.shimmerLayout.root.stopShimmer()
            binding.shimmerLayout.root.visibility = View.GONE
            binding.actualUiLayout.visibility = View.VISIBLE
            binding.recyclerViewCategory1.apply {
                setHasFixedSize(true)
                adapter = Category1Adapter(it, this@HomeFragment)
            }
        })

        binding.recyclerViewBestOffers.apply {
            adapter = HorizontalProductsAdapter(null)
        }

        binding.recyclerViewCategory2.apply {
            adapter = SmallBannerCategoryAdapter(null)
        }

        binding.recyclerViewBestSelling.apply {
            adapter = HorizontalProductsAdapter(null)
        }

        binding.recyclerViewCategory3.apply {
            adapter = SmallBannerCategoryAdapter(null)
        }

        binding.recyclerViewCategory4.apply {
            adapter = BannerCategoryAdapter(null)
        }

        binding.recyclerViewBestProductForYou.apply {
            adapter = HorizontalProductsAdapter(null)
        }

        binding.recyclerViewBanner3.apply {
            adapter = BigBannersAdapter(null)
        }

        binding.recyclerViewRecommendedProducts.apply {
            adapter = HorizontalProductsAdapter(null)
        }

        imageList.add(Banner(R.drawable.placeholder_image))
        imageList.add(Banner(R.drawable.ic_add_box))
        imageList.add(Banner(R.drawable.ic_minus_box))

        viewPager.adapter = HomeTopBannerAdapter(imageList)
        TabLayoutMediator(binding.tabLayout, viewPager) { _, _ -> }.attach()

        callback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                handler.removeCallbacks(slideRunnable)
                handler.postDelayed(slideRunnable, 3000)
            }
        }
        viewPager.registerOnPageChangeCallback(callback)

        binding.searchCard.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private val slideRunnable = Runnable {
        viewPager.currentItem =
            if (imageList.size.minus(1) == viewPager.currentItem) 0
            else viewPager.currentItem.plus(1)
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerLayout.root.startShimmer()
        handler.postDelayed(slideRunnable, 3000)
    }

    override fun onPause() {
        super.onPause()
        imageList.clear()
        binding.shimmerLayout.root.stopShimmer()
        handler.removeCallbacks(slideRunnable)
        viewPager.unregisterOnPageChangeCallback(callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingHome = null
    }

    override fun onCategoryItemClick(categoryModel: Category1Model) {
        navController.navigate(
            HomeFragmentDirections.actionNavHomeToCategoryFragment(
                categoryModel,
                categoryModel.category_name.toString()
            )
        )
    }
}