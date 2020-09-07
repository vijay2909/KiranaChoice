package com.app.kiranachoice.views.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.recyclerView_adapters.VerticalProductsAdapter
import com.app.kiranachoice.databinding.FragmentProductsBinding

class ProductsFragment : Fragment() {

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
        viewModel.productsList.observe(viewLifecycleOwner, {
            binding.recyclerViewProductList.apply {
                setHasFixedSize(true)
                adapter = VerticalProductsAdapter(it)
            }
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

}