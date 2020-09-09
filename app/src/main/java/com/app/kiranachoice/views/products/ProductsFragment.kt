package com.app.kiranachoice.views.products

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.recyclerView_adapters.VerticalProductsAdapter
import com.app.kiranachoice.databinding.FragmentProductsBinding
import com.app.kiranachoice.db.Product

class ProductsFragment : Fragment(), VerticalProductsAdapter.ProductListener {

    private var _bindingProduct: FragmentProductsBinding? = null
    private val binding get() = _bindingProduct!!
    private val viewModel by viewModels<ProductsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bindingProduct = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val args: ProductsFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val verticalProductsAdapter = VerticalProductsAdapter(this)
        binding.recyclerViewProductList.adapter = verticalProductsAdapter

        viewModel.productsList.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) verticalProductsAdapter.data = it
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.getProductList(args.subCategoryModel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingProduct = null
    }

    override fun addItem(product: Product) {
        viewModel.insert(product)
    }

    companion object {
        private const val TAG = "ProductsFragment"
    }

}