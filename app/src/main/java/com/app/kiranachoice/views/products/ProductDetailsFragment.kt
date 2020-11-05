package com.app.kiranachoice.views.products

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.databinding.FragmentProductDetailsBinding
import com.app.kiranachoice.recyclerView_adapters.AboutProductAdapter
import com.app.kiranachoice.recyclerView_adapters.HorizontalProductsAdapter
import com.app.kiranachoice.recyclerView_adapters.PackagingSizeAdapter
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase

class ProductDetailsFragment : Fragment() {

    private var _bindingProductDetails: FragmentProductDetailsBinding? = null
    private val binding get() = _bindingProductDetails!!

    private lateinit var viewModel : ProductDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ProductDetailsViewModel::class.java)
        _bindingProductDetails = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val args: ProductDetailsFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.productModel = args.productModel

        val packagingSizeAdapter = PackagingSizeAdapter()
        binding.recyclerPackagingSize.adapter = packagingSizeAdapter

        packagingSizeAdapter.list = args.productModel.productPackagingSize

        if (!args.productModel.aboutTheProduct.isNullOrEmpty()) {
            binding.isListEmpty = false
            val aboutProductAdapter = AboutProductAdapter()
            binding.recyclerViewAboutProduct.apply {
                aboutProductAdapter.list = args.productModel.aboutTheProduct
                setHasFixedSize(true)
                adapter = aboutProductAdapter
            }
        } else {
            binding.isListEmpty = true
        }

        val similarProductsAdapter = HorizontalProductsAdapter(null)
        binding.recyclerViewSimilarProducts.apply {
            setHasFixedSize(true)
            adapter = similarProductsAdapter
        }

        viewModel.productsList.observe(viewLifecycleOwner, {
            similarProductsAdapter.list = it
        })

        binding.shareButton.setOnClickListener {
            val productId = args.productModel.product_key
            val dynamicLink = Firebase.dynamicLinks.dynamicLink { // or Firebase.dynamicLinks.shortLinkAsync
                link = Uri.parse("https://www.kiranachoice.com/refer.php?productId=$productId")
                domainUriPrefix = "https://kiranachoice.page.link"
                androidParameters("com.app.kiranachoice") {

                    minimumVersion = 1
                }
                googleAnalyticsParameters {
                    source = "orkut"
                    medium = "social"
                    campaign = "example-promo"
                }
                socialMetaTagParameters {
                    title = "Example of a Dynamic Link"
                    description = "This link works whether the app is installed or not!"
                }
            }

            Log.i(TAG, "dynamicLink uri : ${dynamicLink.uri}")

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "Try this amazing app: " + dynamicLink.uri)
            startActivity(Intent.createChooser(intent, "Share using"))
        }

    }

    companion object {
        private const val TAG = "ProductDetailsFragment"
    }

    override fun onStart() {
        super.onStart()
        viewModel.getProductList(args.productModel.sub_category_name.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingProductDetails = null
    }
}