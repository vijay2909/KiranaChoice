package com.app.kiranachoice.views.products

import android.os.Bundle
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