package com.app.kiranachoice.views.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.app.kiranachoice.R
import com.app.kiranachoice.data.database_models.CartItem
import com.app.kiranachoice.data.domain.Banner
import com.app.kiranachoice.data.domain.Category
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.FragmentHomeBinding
import com.app.kiranachoice.listeners.CategoryClickListener
import com.app.kiranachoice.listeners.ProductClickListener
import com.app.kiranachoice.recyclerView_adapters.Category1Adapter
import com.app.kiranachoice.recyclerView_adapters.HorizontalProductsAdapter
import com.app.kiranachoice.recyclerView_adapters.SmallBannerCategoryAdapter
import com.app.kiranachoice.utils.INDEX_ONE_CATEGORY
import com.app.kiranachoice.utils.INDEX_TWO_CATEGORY
import com.app.kiranachoice.viewpager_adapters.HomeTopBannerAdapter
import com.app.kiranachoice.views.authentication.AuthActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialElevationScale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), CategoryClickListener, ProductClickListener {

    private var _bindingHome: FragmentHomeBinding? = null
    private val binding get() = _bindingHome!!

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var mAuth: FirebaseAuth

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

    private lateinit var homeBannerAdapter: HomeTopBannerAdapter
    private lateinit var category1Adapter: Category1Adapter

    private var totalCartItem = 0

    private lateinit var cartItems: List<CartItem>
    private var bestOfferProducts = ArrayList<Product>()
    private val bestSellingProducts = ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingHome = FragmentHomeBinding.inflate(inflater, container, false)

        homeTopBannerImageList = ArrayList()
        homeMiddleBannerImageList = ArrayList()
        viewPager1 = binding.homeBanner1
//        viewPager2 = binding.homeBanner2
        handler1 = Handler(Looper.getMainLooper())
//        handler2 = Handler(Looper.getMainLooper())

        // Home Top Banner [[START]] >>>>>>>>>>>>>
        homeBannerAdapter = HomeTopBannerAdapter()
        binding.homeBanner1.adapter = homeBannerAdapter
        TabLayoutMediator(binding.tabLayout, binding.homeBanner1) { _, _ -> }.attach()
        // Home Top Banner [[END]]>>>>>>>>>>>>>


        // Category [[ START ]] >>>>>>>>>>>>>>>
        category1Adapter = Category1Adapter(this)
        binding.recyclerViewCategory1.apply {
            setHasFixedSize(true)
            adapter = category1Adapter
        }
        // Category [[ END ]] >>>>>>>>>>>>>>>

        runBlocking {
            cartItems = viewModel.getCartItems()
        }


        // Best Selling Products [[ START ]] >>>>>>>>>>>>>>>>>>>>>
        bestSellingProductsAdapter = HorizontalProductsAdapter(this)
        binding.recyclerViewBestSelling.apply {
            adapter = bestSellingProductsAdapter
            itemAnimator?.changeDuration = 0
        }
        // Best Selling Products [[ END ]] >>>>>>>>>>>>>>>>>>>>>

        // Best Offer Products [[ START ]] >>>>>>>>>>>>>>>>>>>>>
        bestOfferProductsAdapter = HorizontalProductsAdapter(this)
        binding.recyclerViewBestOffers.apply {
            adapter = bestOfferProductsAdapter
            itemAnimator?.changeDuration = 0
        }
        // Best Offer Products [[ END ]] >>>>>>>>>>>>>>>>>>>>>

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }


        // Home Top Banner
        viewModel.banners.observe(viewLifecycleOwner, {
            homeTopBannerImageList = it
            homeBannerAdapter.list = it
        })


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
                            findNavController().navigate(
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


        viewModel.eventNetworkError.observe(viewLifecycleOwner, {
            if (it) {
                Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_LONG).show()
            }
        })


        // Category >>>>>>>>>>>>>>>
        viewModel.firstCategories.observe(viewLifecycleOwner, {
            category1Adapter.list = it
            binding.shimmerLayout.rootLayout.stopShimmer()
            binding.shimmerLayout.root.visibility = View.GONE
            binding.actualUiLayout.visibility = View.VISIBLE
        })
        // Category [[ END ]] >>>>>>>>>>>>>


        // Best Offer And Selling Products [[ START ]] >>>>>>>>>>>>>>
        runBlocking {
            bestOfferProducts.addAll(viewModel.bestOfferProducts())
            bestSellingProducts.addAll(viewModel.bestSellingProducts())

            cartItems.forEach { cartItem ->
                bestOfferProducts.singleOrNull { it.key == cartItem.productKey }?.let {
                    it.addedInCart = true
                    it.userOrderQuantity = cartItem.quantity
                }
                bestSellingProducts.singleOrNull { it.key == cartItem.productKey }?.let {
                    it.addedInCart = true
                    it.userOrderQuantity = cartItem.quantity
                }
            }

            binding.bestSellingProductAvailable = bestSellingProducts.isNotEmpty()
            binding.bestOfferProductsAvailable = bestOfferProducts.isNotEmpty()

            bestOfferProductsAdapter.productsList = bestOfferProducts
            bestSellingProductsAdapter.productsList = bestSellingProducts
        }
        // Best Offer And Selling Products [[ END ]] >>>>>>>>>>>>>>


        // Category 2 [[ START ]] >>>>>>>>>>>>>>>
        viewModel.secondCategories.observe(viewLifecycleOwner, {
            val smallBannerCategoryAdapter = SmallBannerCategoryAdapter(it, this)
            binding.recyclerViewCategory2.setHasFixedSize(true)
            binding.recyclerViewCategory2.adapter = smallBannerCategoryAdapter
        })
        // Category [[ END ]] >>>>>>>>>>>>>>>


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
            findNavController().navigate(R.id.action_homeFragment_to_cartFragment)
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

    override fun onDestroy() {
        super.onDestroy()
        _bindingHome = null
    }

    override fun onCategoryItemClick(view: View, category: Category) {
        if (category.index == INDEX_ONE_CATEGORY) {
            val categoryDetailTransitionName = getString(R.string.category_detail_transition_name)
            val extras = FragmentNavigatorExtras(view to categoryDetailTransitionName)
            val categoryDirection =
                HomeFragmentDirections.actionNavHomeToCategoryFragment(category.name)
            findNavController().navigate(categoryDirection, extras)
        } else if (category.index == INDEX_TWO_CATEGORY) {
            val categoryProductTransitionName = getString(R.string.category_product_transition_name)
            val extras = FragmentNavigatorExtras(view to categoryProductTransitionName)
            val productDirection =
                HomeFragmentDirections.actionHomeFragmentToProductsFragment(category.name)
            findNavController().navigate(productDirection, extras)
        }
    }


    override fun addItemToCart(product: Product) {
        viewModel.addItemToCart(product)
    }


    override fun onItemClick(view: View, product: Product) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val productDetailTransitionName = getString(R.string.product_detail_transition_name)
        val extras = FragmentNavigatorExtras(view to productDetailTransitionName)
        val directions = HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(
            product.name,
            product
        )
        findNavController().navigate(directions, extras)
    }

    override fun onRemoveProduct(product: Product) {
        viewModel.removeProductFromCart(product)
    }

    override fun onQuantityChanged(product: Product) {
        viewModel.updateCartItemQuantity(product)
    }
}