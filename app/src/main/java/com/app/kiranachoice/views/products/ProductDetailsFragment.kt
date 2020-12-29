package com.app.kiranachoice.views.products

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private val viewModel: ProductDetailsViewModel by lazy {
        val localDatabase = CartDatabase.getInstance(requireContext().applicationContext)
        val factory = ProductViewModelFactory(DataRepository(localDatabase.databaseDao))
        ViewModelProvider(this, factory).get(ProductDetailsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingProductDetails = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    private val args: ProductDetailsFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        val packagingSizeAdapter = PackagingSizeAdapter()
        binding.recyclerPackagingSize.adapter = packagingSizeAdapter

        args.product?.let {
            binding.product = it
            packagingSizeAdapter.list = it.productPackagingSize

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


        viewModel.cartItems.observe(viewLifecycleOwner, {
            binding.recyclerViewSimilarProducts.apply {
                similarProductsAdapter = SimilarProductsAdapter(it,
                    args.product?.product_key.toString(),this@ProductDetailsFragment)
                adapter = similarProductsAdapter
                setHasFixedSize(true)
            }
        })


        binding.shareButton.setOnClickListener {
            val productId = args.product?.product_key

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


    override fun onStart() {
        super.onStart()
        args.product?.let {
            viewModel.getProductList(it.sub_category_name).observe(viewLifecycleOwner, { products ->
                if (this::similarProductsAdapter.isInitialized){
                    similarProductsAdapter.list = products
                }
            })
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
            val packagingSizeModel = if (product.productPackagingSize.size > 1) {
                product.productPackagingSize[packagingSize]
            } else {
                product.productPackagingSize[0]
            }
            if (viewModel.addItemToCart(product, packagingSizeModel, quantity)) {
                similarProductsAdapter.addToCartClickedItemPosition = position
                similarProductsAdapter.notifyItemChanged(position)
                Toast.makeText(requireContext(), "1 item Added.", Toast.LENGTH_SHORT).show()
            }
        } else {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }
    }

    override fun onItemClick(product: Product) {
        navController.navigate(
            ProductDetailsFragmentDirections.actionProductDetailsFragmentSelf(
                product.productTitle,
                product
            )
        )
    }


    override fun onRemoveProduct(productKey: String) {

    }

    override fun onQuantityChanged(productKey: String, quantity: String) {

    }
}