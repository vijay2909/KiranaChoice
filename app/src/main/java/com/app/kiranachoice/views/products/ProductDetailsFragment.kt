package com.app.kiranachoice.views.products

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.data.db.CartDatabase
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.FragmentProductDetailsBinding
import com.app.kiranachoice.listeners.ProductClickListener
import com.app.kiranachoice.recyclerView_adapters.AboutProductAdapter
import com.app.kiranachoice.recyclerView_adapters.PackagingSizeAdapter
import com.app.kiranachoice.recyclerView_adapters.SimilarProductsAdapter
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.views.authentication.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase

class ProductDetailsFragment : Fragment(), ProductClickListener {

    private var _bindingProductDetails: FragmentProductDetailsBinding? = null
    private val binding get() = _bindingProductDetails!!

    private lateinit var navController: NavController

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private lateinit var similarProductsAdapter: SimilarProductsAdapter

    private val args: ProductDetailsFragmentArgs by navArgs()

    private val viewModel: ProductDetailsViewModel by lazy {
        val localDatabase = CartDatabase.getInstance(requireContext().applicationContext)
        val factory = ProductViewModelFactory(args.title, DataRepository(localDatabase.databaseDao))
        ViewModelProvider(this, factory).get(ProductDetailsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingProductDetails = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        val packagingSizeAdapter = PackagingSizeAdapter()
        binding.recyclerPackagingSize.adapter = packagingSizeAdapter

        args.product?.let {
            binding.product = it
            packagingSizeAdapter.list = it.packagingSize

            if (!it.aboutTheProduct.isNullOrEmpty()) {
                binding.isListEmpty = false
                val aboutProductAdapter = AboutProductAdapter()
                binding.recyclerViewAboutProduct.apply {
                    aboutProductAdapter.list = it.aboutTheProduct
                    setHasFixedSize(true)
                    adapter = aboutProductAdapter
                }
            } else {
                binding.isListEmpty = true
            }
        }

        viewModel.getProducts.observe(viewLifecycleOwner, {
            binding.recyclerViewSimilarProducts.apply {
                similarProductsAdapter = SimilarProductsAdapter(it.second, args.product?.key,it.first,this@ProductDetailsFragment)
                adapter = similarProductsAdapter
                setHasFixedSize(true)
            }
        })


        binding.shareButton.setOnClickListener {
            val productId = args.product?.id

            Firebase.dynamicLinks.shortLinkAsync {
                link = Uri.parse("https://www.kiranachoice.com/refer.php?productId=$productId")
                domainUriPrefix = "https://kiranachoice.page.link"
                androidParameters("com.app.kiranachoice") {
                    minimumVersion = 1
                }
                socialMetaTagParameters {
                    title = "Kirana Choice - Online Shopping App"
                    imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/kirana-choice-a1e20.appspot.com/o/kiran%20choice%20logo.png?alt=media&token=73ad2448-72a5-4935-b0f6-98286eb03b26")
//                    description = "This link works whether the app is installed or not!"
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
        product: Product,
        packagingSize: Int,
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
            ProductDetailsFragmentDirections.actionProductDetailsFragmentSelf(
                product.name,
                product
            )
        )
    }


    override fun onRemoveProduct(productKey: String) {

    }

    override fun onQuantityChanged(product: Product/*productKey: String, quantity: String*/) {

    }
}