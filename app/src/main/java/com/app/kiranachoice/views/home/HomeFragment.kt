package com.app.kiranachoice.views.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.TextView
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
import com.app.kiranachoice.data.db.CartDatabase
import com.app.kiranachoice.data.domain.Banner
import com.app.kiranachoice.data.domain.Category
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.FragmentHomeBinding
import com.app.kiranachoice.listeners.CategoryClickListener
import com.app.kiranachoice.listeners.ProductClickListener
import com.app.kiranachoice.recyclerView_adapters.Category1Adapter
import com.app.kiranachoice.recyclerView_adapters.HorizontalProductsAdapter
import com.app.kiranachoice.recyclerView_adapters.SmallBannerCategoryAdapter
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.INDEX_ONE_CATEGORY
import com.app.kiranachoice.utils.INDEX_TWO_CATEGORY
import com.app.kiranachoice.viewpager_adapters.HomeTopBannerAdapter
import com.app.kiranachoice.views.authentication.AuthActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase


class HomeFragment : Fragment(), CategoryClickListener, ProductClickListener {

    private var _bindingHome: FragmentHomeBinding? = null
    private val binding get() = _bindingHome!!

    private lateinit var viewModel: HomeViewModel

    private lateinit var navController: NavController
    private lateinit var mAuth: FirebaseAuth

    private lateinit var viewPager1: ViewPager2

    //    private lateinit var viewPager2: ViewPager2
    private lateinit var handler1: Handler

    //    private lateinit var handler2: Handler
    private lateinit var callbackTopBanner: ViewPager2.OnPageChangeCallback

    //    private lateinit var callbackMiddleBanner: ViewPager2.OnPageChangeCallback
    private lateinit var homeTopBannerImageList: List<Banner>
    private lateinit var homeMiddleBannerImageList: List<Banner>

    private lateinit var bestOfferProductsAdapter: HorizontalProductsAdapter
    private lateinit var bestSellingProductsAdapter: HorizontalProductsAdapter

    private var totalCartItem = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.show()
        _bindingHome = FragmentHomeBinding.inflate(inflater, container, false)

        val localDatabase = CartDatabase.getInstance(requireContext().applicationContext)
        val factory = HomeViewModelFactory(DataRepository(localDatabase.databaseDao))
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        homeTopBannerImageList = ArrayList()
        homeMiddleBannerImageList = ArrayList()
        viewPager1 = binding.homeBanner1
//        viewPager2 = binding.homeBanner2
        handler1 = Handler(Looper.getMainLooper())
//        handler2 = Handler(Looper.getMainLooper())
        mAuth = FirebaseAuth.getInstance()

        bestOfferProductsAdapter = HorizontalProductsAdapter(this)
        binding.recyclerViewBestOffers.apply {
            adapter = bestOfferProductsAdapter
            itemAnimator?.changeDuration = 0
            setHasFixedSize(true)
        }

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
                    viewModel.getProduct(productId).observe(viewLifecycleOwner, {
                        it?.let { product ->
                            navController.navigate(
                                HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(
                                    product.name,
                                    product
                                )
                            )
                            // todo product should null ?
                        }
                    })
                }
            }


        viewModel.totalCartItems.observe(viewLifecycleOwner, {
            totalCartItem = it
            requireActivity().invalidateOptionsMenu()
        })


        navController = Navigation.findNavController(view)

        // Home Top Banner [[START]] >>>>>>>>>>>>>
        val banner1Adapter = HomeTopBannerAdapter()
        binding.homeBanner1.adapter = banner1Adapter
        TabLayoutMediator(binding.tabLayout, binding.homeBanner1) { _, _ -> }.attach()
        viewModel.banners.observe(viewLifecycleOwner, {
            homeTopBannerImageList = it
            banner1Adapter.list = it
        })
        // Home Top Banner [[END]]>>>>>>>>>>>>>

        viewModel.eventNetworkError.observe(viewLifecycleOwner, {
            if (it) {
                Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_LONG).show()
            }
        })


        // Category [[ START ]] >>>>>>>>>>>>>>>
        val category1Adapter = Category1Adapter(this)
        binding.recyclerViewCategory1.apply {
            setHasFixedSize(true)
            adapter = category1Adapter
        }

        viewModel.firstCategories.observe(viewLifecycleOwner, {
            category1Adapter.list = it
            binding.shimmerLayout.rootLayout.stopShimmer()
            binding.shimmerLayout.root.visibility = View.GONE
            binding.actualUiLayout.visibility = View.VISIBLE
        })
        // Category [[ END ]] >>>>>>>>>>>>>>>


        // Best Offer Products [[ START ]] >>>>>>>>>>>>>>
        viewModel.bestOfferProducts.observe(viewLifecycleOwner, {
            binding.bestOfferProductsAvailable = it.second.isNotEmpty()
            bestOfferProductsAdapter.submitCartItem(it.first)
            bestOfferProductsAdapter.submitList(it.second)
        })
        // Best Offer Products [[ END ]] >>>>>>>>>>>>>>


        // Category 2 [[ START ]] >>>>>>>>>>>>>>>
        viewModel.secondCategories.observe(viewLifecycleOwner, {
            val smallBannerCategoryAdapter = SmallBannerCategoryAdapter(it, this)
            binding.recyclerViewCategory2.setHasFixedSize(true)
            binding.recyclerViewCategory2.adapter = smallBannerCategoryAdapter
        })
        // Category [[ END ]] >>>>>>>>>>>>>>>


        // Best Selling Products [[ START ]] >>>>>>>>>>>>>>>>>>>>>
        bestSellingProductsAdapter = HorizontalProductsAdapter(this)
        binding.recyclerViewBestSelling.apply {
            adapter = bestSellingProductsAdapter
            setHasFixedSize(true)
        }

        viewModel.bestSellingProducts.observe(viewLifecycleOwner, {
            binding.bestSellingProductAvailable = it.second.isNotEmpty()
            bestOfferProductsAdapter.submitCartItem(it.first)
            bestSellingProductsAdapter.submitList(it.second)

        })
        // Best Selling Products [[ START ]] >>>>>>>>>>>>>>>>>>>>>

        /*binding.recyclerViewCategory3.apply {
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
*/


        /*  binding.recyclerViewCategory4.apply {
              adapter = BannerCategoryAdapter(null)
          }
  */
        /* binding.recyclerViewBestProductForYou.apply {
             adapter = HorizontalProductsAdapter(null)
         }*/

        /*binding.recyclerViewBanner3.apply {
            adapter = BigBannersAdapter(null)
        }*/

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
        /*callbackMiddleBanner = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                handler2.removeCallbacks(slideRunnable2)
                handler2.postDelayed(slideRunnable2, 3000)
            }
        }*/

//        binding.homeBanner2.registerOnPageChangeCallback(callbackMiddleBanner)
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
    /* private val slideRunnable2 = Runnable {
         viewPager2.currentItem =
             if (homeMiddleBannerImageList.size.minus(1) == viewPager2.currentItem) 0
             else viewPager2.currentItem.plus(1)
     }*/


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)

        val menuItem = menu.findItem(R.id.cartFragment)

        val actionView = menuItem.actionView

        val cartBadgeTextView = actionView.findViewById<TextView>(R.id.cart_badge_text_view)

        if (totalCartItem == 0) {
            cartBadgeTextView.visibility = View.GONE
        } else {
            cartBadgeTextView.text = totalCartItem.toString()
            cartBadgeTextView.visibility = View.VISIBLE
        }

        actionView.setOnClickListener { onOptionsItemSelected(menuItem) }

        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.cartFragment) {
            navController.navigate(R.id.action_homeFragment_to_cartFragment)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerLayout.rootLayout.startShimmer()
        handler1.postDelayed(slideRunnable1, 3000)
//        handler2.postDelayed(slideRunnable2, 3000)
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerLayout.rootLayout.stopShimmer()

        handler1.removeCallbacks(slideRunnable1)
        viewPager1.unregisterOnPageChangeCallback(callbackTopBanner)

        /*handler2.removeCallbacks(slideRunnable2)
        viewPager2.unregisterOnPageChangeCallback(callbackMiddleBanner)*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingHome = null
    }

    override fun onCategoryItemClick(category: Category) {
        if (category.index == INDEX_ONE_CATEGORY) {
            navController.navigate(
                HomeFragmentDirections.actionNavHomeToCategoryFragment(category.name)
            )
        } else if (category.index == INDEX_TWO_CATEGORY) {
            navController.navigate(
                HomeFragmentDirections.actionHomeFragmentToProductsFragment(category.name)
            )
        }
    }


    override fun addItemToCart(
        product: Product,
        packagingSize: Int, // spinner adapter position
        quantity: String,
        position: Int
    ) {
        if (mAuth.currentUser != null) {
            val packagingSizeModel = if (product.packagingSize.size > 1) {
                product.packagingSize[packagingSize]
            } else {
                product.packagingSize[0]
            }
            viewModel.addItemToCart(product, packagingSizeModel, quantity)
        } else {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }
    }

    override fun onItemClick(product: Product) {
        navController.navigate(
            HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(
                product.name,
                product
            )
        )
    }

    override fun onRemoveProduct(productKey: String) {
        viewModel.removeProductFromCart(productKey)
    }

    override fun onQuantityChanged(product: Product/*productKey: String, quantity: String*/) {
        viewModel.updateQuantity(product.key, product.userQuantity.toString())
    }

}

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(private val dataRepository: DataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(dataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}