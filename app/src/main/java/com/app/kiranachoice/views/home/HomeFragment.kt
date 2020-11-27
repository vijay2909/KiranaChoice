package com.app.kiranachoice.views.home

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentHomeBinding
import com.app.kiranachoice.db.CartItem
import com.app.kiranachoice.listeners.ProductClickListener
import com.app.kiranachoice.models.BannerImageModel
import com.app.kiranachoice.models.Category1Model
import com.app.kiranachoice.models.ProductModel
import com.app.kiranachoice.recyclerView_adapters.*
import com.app.kiranachoice.utils.BEST_OFFER_PRODUCT
import com.app.kiranachoice.utils.BEST_SELLING_PRODUCT
import com.app.kiranachoice.viewpager_adapters.HomeMiddleBannerAdapter
import com.app.kiranachoice.viewpager_adapters.HomeTopBannerAdapter
import com.app.kiranachoice.views.authentication.AuthActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(), Category1Adapter.CategoryClickListener, ProductClickListener {

    private var _bindingHome: FragmentHomeBinding? = null
    private val binding get() = _bindingHome!!

    private lateinit var viewModel: HomeViewModel

    private lateinit var navController: NavController
    private lateinit var mAuth: FirebaseAuth

    private lateinit var viewPager1: ViewPager2
    private lateinit var viewPager2: ViewPager2
    private lateinit var handler1: Handler
    private lateinit var handler2: Handler
    private lateinit var callbackTopBanner: ViewPager2.OnPageChangeCallback
    private lateinit var callbackMiddleBanner: ViewPager2.OnPageChangeCallback
    private lateinit var homeTopBannerImageList: List<BannerImageModel>
    private lateinit var homeMiddleBannerImageList: List<BannerImageModel>

    private lateinit var bestOfferProductsAdapter: HorizontalProductsAdapter
    private lateinit var bestSellingProductsAdapter: HorizontalProductsAdapter

    private lateinit var cartItems : List<CartItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val factory = HomeViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        (activity as AppCompatActivity).supportActionBar?.show()
        _bindingHome = FragmentHomeBinding.inflate(inflater, container, false)
        homeTopBannerImageList = ArrayList()
        homeMiddleBannerImageList = ArrayList()
        viewPager1 = binding.homeBanner1
        viewPager2 = binding.homeBanner2
        handler1 = Handler(Looper.getMainLooper())
        handler2 = Handler(Looper.getMainLooper())
        mAuth = FirebaseAuth.getInstance()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Firebase.dynamicLinks
            .getDynamicLink(requireActivity().intent)
            .addOnSuccessListener(requireActivity()) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }

                deepLink?.let {
                    val productId = deepLink.toString().substringAfter("=")
                    viewModel.getProductDetails(productId)
                }

            }
            .addOnFailureListener(requireActivity()) { e ->
                Log.w(
                    TAG,
                    "getDynamicLink:onFailure: ${e.message}"
                )
            }

        viewModel.cartItems.observe(viewLifecycleOwner, {
            cartItems = it
        })

        navController = Navigation.findNavController(view)

        viewModel.product.observe(viewLifecycleOwner, {
            it?.let {
                navController.navigate(
                    HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(
                        it.productTitle.toString(),
                        it
                    )
                )
                viewModel.productShouldBeNull()
            }
        })


        // Home Top Banner [[START]] >>>>>>>>>>>>>
        val banner1Adapter = HomeTopBannerAdapter()
        binding.homeBanner1.adapter = banner1Adapter
        TabLayoutMediator(binding.tabLayout, binding.homeBanner1) { _, _ -> }.attach()
        viewModel.bannersList.observe(viewLifecycleOwner, {
            homeTopBannerImageList = it
            banner1Adapter.list = it
        })
        // Home Top Banner [[END]]>>>>>>>>>>>>>


        // Category [[ START ]] >>>>>>>>>>>>>>>
        val category1Adapter = Category1Adapter(this)
        binding.recyclerViewCategory1.apply {
            setHasFixedSize(true)
            adapter = category1Adapter
        }
        viewModel.categoryList.observe(viewLifecycleOwner, {
            Log.w(TAG, "onViewCreated: categoryList observe method")
            category1Adapter.list = it
            binding.shimmerLayout.rootLayout.stopShimmer()
            binding.shimmerLayout.root.visibility = View.GONE
            binding.actualUiLayout.visibility = View.VISIBLE
        })
        // Category [[ END ]] >>>>>>>>>>>>>>>


        // Best Offer Products [[ START ]] >>>>>>>>>>>>>>
        viewModel.bestOfferProductList.observe(viewLifecycleOwner, {
            binding.bestOfferProductsAvailable = it.isNotEmpty()
            bestOfferProductsAdapter = HorizontalProductsAdapter(it, cartItems,this)
            binding.recyclerViewBestOffers.apply {
                setHasFixedSize(true)
                adapter = bestOfferProductsAdapter
            }
        })
        // Best Offer Products [[ END ]] >>>>>>>>>>>>>>


        val smallBannerCategoryAdapter = SmallBannerCategoryAdapter()
        binding.recyclerViewCategory2.adapter = smallBannerCategoryAdapter

        viewModel.category2.observe(viewLifecycleOwner, {
            binding.recyclerViewCategory2.setHasFixedSize(true)
            smallBannerCategoryAdapter.list = it
        })


        // Best Selling Products [[ START ]] >>>>>>>>>>>>>>>>>>>>>

        viewModel.bestSellingProductList.observe(viewLifecycleOwner, {
            binding.bestSellingProductAvailable = it.isNotEmpty()
            bestSellingProductsAdapter = HorizontalProductsAdapter(it, cartItems,this)
            binding.recyclerViewBestSelling.apply {
                setHasFixedSize(true)
                adapter = bestSellingProductsAdapter
            }
        })
        // Best Selling Products [[ START ]] >>>>>>>>>>>>>>>>>>>>>

        binding.recyclerViewCategory3.apply {
            adapter = SmallBannerCategoryAdapter()
        }

        // Home Middle Banner [[START]] >>>>>>>>>>>>>
        val banner2Adapter = HomeMiddleBannerAdapter()
        binding.homeBanner2.adapter = banner2Adapter
        TabLayoutMediator(binding.tabLayout2, binding.homeBanner2) { _, _ -> }.attach()
        viewModel.bannersList.observe(viewLifecycleOwner, {
            homeMiddleBannerImageList = it
            banner2Adapter.list = it
        })
        // Home Middle Banner [[END]]>>>>>>>>>>>>>


        binding.recyclerViewCategory4.apply {
            adapter = BannerCategoryAdapter(null)
        }

       /* binding.recyclerViewBestProductForYou.apply {
            adapter = HorizontalProductsAdapter(null)
        }*/

        binding.recyclerViewBanner3.apply {
            adapter = BigBannersAdapter(null)
        }

//        binding.recyclerViewRecommendedProducts.apply {
//            adapter = HorizontalProductsAdapter(null)
//        }


        // Top Banner Callback >>>>>>>>>>>>>>>>>>>>>>>>>>
        callbackTopBanner = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                handler1.removeCallbacks(slideRunnable1)
                handler1.postDelayed(slideRunnable1, 3000)
            }
        }

        binding.homeBanner1.registerOnPageChangeCallback(callbackTopBanner)
        // Top Banner Callback >>>>>>>>>>>>>>>>>>>>>>>>>>

        // Middle banner callback >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        callbackMiddleBanner = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                handler2.removeCallbacks(slideRunnable2)
                handler2.postDelayed(slideRunnable2, 3000)
            }
        }

        binding.homeBanner2.registerOnPageChangeCallback(callbackMiddleBanner)
        // Middle banner callback >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        binding.searchCard.setOnClickListener {
            view.findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

        viewModel.navigateToAuthActivity.observe(viewLifecycleOwner, {
            if (it) {
                startActivity(Intent(requireContext(), AuthActivity::class.java))
                viewModel.authActivityNavigated()
            }
        })

        viewModel.alreadyAddedMsg.observe(viewLifecycleOwner, {
            it?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.productAdded.observe(viewLifecycleOwner, {
            if (it) {
                Toast.makeText(requireContext(), "1 item added", Toast.LENGTH_SHORT).show()
                viewModel.productAddedSuccessful()
            }
        })
    }

    private val slideRunnable1 = Runnable {
        viewPager1.currentItem =
            if (homeTopBannerImageList.size.minus(1) == viewPager1.currentItem) 0
            else viewPager1.currentItem.plus(1)
    }
    private val slideRunnable2 = Runnable {
        viewPager2.currentItem =
            if (homeMiddleBannerImageList.size.minus(1) == viewPager2.currentItem) 0
            else viewPager2.currentItem.plus(1)
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerLayout.rootLayout.startShimmer()
        handler1.postDelayed(slideRunnable1, 3000)
        handler2.postDelayed(slideRunnable2, 3000)
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerLayout.rootLayout.stopShimmer()

        handler1.removeCallbacks(slideRunnable1)
        viewPager1.unregisterOnPageChangeCallback(callbackTopBanner)

        handler2.removeCallbacks(slideRunnable2)
        viewPager2.unregisterOnPageChangeCallback(callbackMiddleBanner)
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

    override fun addItemToCart(
        productModel: ProductModel,
        packagingSize: Int,
        quantity: String,
        position: Int
    ) {
        Log.i(TAG, "addItemToCart: called")
        Log.w(TAG, "currentUser: ${mAuth.currentUser}" )
        if (mAuth.currentUser != null) {
            val packagingSizeModel = if (productModel.productPackagingSize.size > 1) {
                productModel.productPackagingSize[packagingSize]
            } else {
                productModel.productPackagingSize[0]
            }
            when (viewModel.addItemToCart(productModel, packagingSizeModel, quantity)) {
                BEST_OFFER_PRODUCT -> {
                    bestOfferProductsAdapter.addToCartClickedItemPosition = position
                    bestOfferProductsAdapter.notifyItemChanged(position)
                }
                BEST_SELLING_PRODUCT -> {
                    bestSellingProductsAdapter.addToCartClickedItemPosition = position
                    bestSellingProductsAdapter.notifyItemChanged(position)
                }
            }
        } else {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }

    }

    override fun onItemClick(productModel: ProductModel) {
        navController.navigate(
            HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(
                productModel.productTitle.toString(),
                productModel
            )
        )
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}