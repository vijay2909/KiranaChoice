package com.app.kiranachoice.views.products

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.BuildConfig
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.FragmentProductDetailsBinding
import com.app.kiranachoice.listeners.ProductClickListener
import com.app.kiranachoice.recyclerView_adapters.AboutProductAdapter
import com.app.kiranachoice.recyclerView_adapters.PackagingSizeAdapter
import com.app.kiranachoice.recyclerView_adapters.SimilarProductsAdapter
import com.app.kiranachoice.utils.SOCIAL_META_TAG_KIRANA_CHOICE_IMAGE
import com.app.kiranachoice.utils.SOCIAL_META_TAG_TITLE
import com.app.kiranachoice.utils.themeColor
import com.google.android.material.transition.MaterialContainerTransform
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailsFragment : Fragment(), ProductClickListener {

    private var _bindingProductDetails: FragmentProductDetailsBinding? = null
    private val binding get() = _bindingProductDetails!!

    private lateinit var similarProductsAdapter: SimilarProductsAdapter
    private lateinit var packagingSizeAdapter: PackagingSizeAdapter

    private val args: ProductDetailsFragmentArgs by navArgs()

    private val viewModel: ProductDetailsViewModel by viewModels()

    private val productsList = ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = com.app.kiranachoice.R.id.nav_host_fragment
            duration = resources.getInteger(com.app.kiranachoice.R.integer.reply_motion_duration_large).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(com.app.kiranachoice.R.attr.colorSurface))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingProductDetails = FragmentProductDetailsBinding.inflate(inflater, container, false)

        similarProductsAdapter = SimilarProductsAdapter(args.product.key, this)
        binding.recyclerViewSimilarProducts.apply {
            setHasFixedSize(true)
            adapter = similarProductsAdapter
        }

        packagingSizeAdapter = PackagingSizeAdapter()
        binding.recyclerPackagingSize.adapter = packagingSizeAdapter

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.product = args.product
        packagingSizeAdapter.list = args.product.packagingSize

        if (!args.product.aboutTheProduct.isNullOrEmpty()) {
            binding.isListEmpty = false
            val aboutProductAdapter = AboutProductAdapter()
            binding.recyclerViewAboutProduct.apply {
                aboutProductAdapter.list = args.product.aboutTheProduct
                setHasFixedSize(true)
                adapter = aboutProductAdapter
            }
        } else {
            binding.isListEmpty = true
        }

        viewModel.getProducts.observe(viewLifecycleOwner, {
            binding.isSimilarProductsListEmpty = it.isEmpty()
            productsList.clear()
            productsList.addAll(it)
            productsList.remove(args.product)
            similarProductsAdapter.productsList = productsList
        })


        binding.shareButton.setOnClickListener {
            val productId = args.product.id

            Firebase.dynamicLinks.shortLinkAsync {
                link = Uri.parse("https://www.kiranachoice.com/refer.php?productId=$productId")
                domainUriPrefix = "https://kiranachoice.page.link"
                androidParameters(BuildConfig.APPLICATION_ID) {
                    minimumVersion = 1
                }
                socialMetaTagParameters {
                    title = SOCIAL_META_TAG_TITLE
                    imageUrl = Uri.parse(SOCIAL_META_TAG_KIRANA_CHOICE_IMAGE)
                }
            }.addOnSuccessListener { (shortLink, _) ->
                // Short link created

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, "Try this amazing app: $shortLink")
                startActivity(Intent.createChooser(intent, "Share using"))
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _bindingProductDetails = null
    }

    override fun addItemToCart(
        product: Product
    ) {
        viewModel.addItemToCart(product)
    }

    override fun onItemClick(view: View, product: Product) {
        findNavController().navigate(
            ProductDetailsFragmentDirections.actionProductDetailsFragmentSelf(
                product.name,
                product
            )
        )
    }


    override fun onRemoveProduct(product: Product) {
        viewModel.removeProductFromCart(product)
    }

    override fun onQuantityChanged(product: Product) {
        viewModel.updateCartItemQuantity(product)
    }
}