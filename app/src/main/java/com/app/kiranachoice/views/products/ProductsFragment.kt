package com.app.kiranachoice.views.products

import android.content.Intent
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
import com.app.kiranachoice.databinding.FragmentProductsBinding
import com.app.kiranachoice.db.CartItem
import com.app.kiranachoice.models.ProductModel
import com.app.kiranachoice.recyclerView_adapters.VerticalProductsAdapter
import com.app.kiranachoice.views.authentication.AuthActivity
import com.google.firebase.auth.FirebaseAuth

class ProductsFragment : Fragment(),
    VerticalProductsAdapter.ProductListener {

    private var _bindingProduct: FragmentProductsBinding? = null
    private val binding get() = _bindingProduct!!
    private lateinit var viewModel: ProductsViewModel

    private lateinit var navController: NavController
    private var verticalProductsAdapter: VerticalProductsAdapter? = null

    private lateinit var mAuth: FirebaseAuth

    private lateinit var cartItems : List<CartItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val productViewModelFactory = ProductViewModelFactory(requireActivity().application)
        viewModel =
            ViewModelProvider(this, productViewModelFactory).get(ProductsViewModel::class.java)
        _bindingProduct = FragmentProductsBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()
        return binding.root
    }

    private val args: ProductsFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        viewModel.allCartItems.observe(viewLifecycleOwner, {
            cartItems = it
        })

        viewModel.productsList.observe(viewLifecycleOwner, {
            verticalProductsAdapter = VerticalProductsAdapter(it, cartItems , this)
            binding.recyclerViewProductList.setHasFixedSize(true)
            binding.recyclerViewProductList.adapter = verticalProductsAdapter
        })

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

    override fun onResume() {
        super.onResume()
        if (args.subCategoryModel != null) viewModel.getProductList(args.subCategoryModel, null)
        else viewModel.getProductList(null, args.categoryModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingProduct = null
    }

    override fun onAddToCartButtonClick(
        productModel: ProductModel,
        packagingSize: Int,
        quantity: String,
        position: Int
    ) {
        if (mAuth.currentUser != null) {
            val packagingSizeModel = if (productModel.productPackagingSize.size > 1) {
                productModel.productPackagingSize[packagingSize]
            } else {
                productModel.productPackagingSize[0]
            }
            val result = viewModel.addItemToCart(productModel, packagingSizeModel, quantity)
            if (result) {
                verticalProductsAdapter?.addToCartClickedItemPosition = position
                verticalProductsAdapter?.notifyItemChanged(position)
            }
        } else {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }
    }

    override fun onItemRemoved(productModel: ProductModel) {
        viewModel.deleteCartItem(productModel)
    }

    override fun onItemClick(productModel: ProductModel) {
        navController.navigate(ProductsFragmentDirections.actionProductsFragmentToProductDetailsFragment(productModel.productTitle.toString(), productModel))
    }

}