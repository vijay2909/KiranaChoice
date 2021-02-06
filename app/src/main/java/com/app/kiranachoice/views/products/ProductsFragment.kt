package com.app.kiranachoice.views.products

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.FragmentProductsBinding
import com.app.kiranachoice.listeners.ProductClickListener
import com.app.kiranachoice.recyclerView_adapters.VerticalProductsAdapter
import com.app.kiranachoice.views.authentication.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Provides
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProductsFragment : Fragment(), ProductClickListener {

    private var _bindingProduct: FragmentProductsBinding? = null
    private val binding get() = _bindingProduct!!
    private lateinit var verticalProductsAdapter: VerticalProductsAdapter

    private lateinit var mAuth: FirebaseAuth

    //    @delegate:Inject
    private val args: ProductsFragmentArgs by navArgs()


    val viewModel: ProductsViewModel by viewModels()

    private var cartItems: List<CartItem>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingProduct = FragmentProductsBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()

        verticalProductsAdapter = VerticalProductsAdapter(cartItems, this)
        binding.recyclerViewProductList.setHasFixedSize(true)
        binding.recyclerViewProductList.adapter = verticalProductsAdapter

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        viewModel.products.observe(viewLifecycleOwner, { products ->
            verticalProductsAdapter.submitList(products)
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _bindingProduct = null
    }


    override fun addItemToCart(
        product: Product,
        packagingIndex: Int
    ) {
        val packagingSizeModel = product.packagingSize[packagingIndex]
        viewModel.addItemToCart(product, packagingSizeModel)
    }

    override fun onItemClick(view: View, product: Product) {
        findNavController().navigate(
            ProductsFragmentDirections.actionProductsFragmentToProductDetailsFragment(
                product.name, product
            )
        )
    }

    override fun onRemoveProduct(productKey: String) {
        viewModel.deleteCartItem(productKey)
    }

    override fun onQuantityChanged(product: Product) {
        viewModel.updateQuantity(product.key, product.orderQuantity)
    }

}

