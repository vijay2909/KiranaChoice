package com.app.kiranachoice.views.products

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.data.db.CartDatabase
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.FragmentProductsBinding
import com.app.kiranachoice.listeners.ProductClickListener
import com.app.kiranachoice.recyclerView_adapters.VerticalProductsAdapter
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.views.authentication.AuthActivity
import com.google.firebase.auth.FirebaseAuth

class ProductsFragment : Fragment(), ProductClickListener {

    private var _bindingProduct: FragmentProductsBinding? = null
    private val binding get() = _bindingProduct!!
    private lateinit var viewModel: ProductsViewModel

    private lateinit var verticalProductsAdapter: VerticalProductsAdapter

    private lateinit var mAuth: FirebaseAuth
    private val args: ProductsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val localDatabase = CartDatabase.getInstance(requireContext().applicationContext)
        val productViewModelFactory =
            ProductViewModelFactory(args.title, DataRepository(localDatabase.databaseDao))
        viewModel =
            ViewModelProvider(this, productViewModelFactory).get(ProductsViewModel::class.java)
        _bindingProduct = FragmentProductsBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()

        verticalProductsAdapter = VerticalProductsAdapter(this)
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

        viewModel.getProducts.observe(viewLifecycleOwner, {
            Log.d("Products", "products: ${it.second}")
            verticalProductsAdapter.submitCartItem(it.first)
            verticalProductsAdapter.submitList(it.second)

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
        viewModel.updateQuantity(product.key, product.userQuantity.toString())
    }

}

