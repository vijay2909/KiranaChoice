package com.app.kiranachoice.views.my_orders

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.FragmentOrderDetailsBinding
import com.app.kiranachoice.recyclerView_adapters.ProductDetailAdapter


class OrderDetailsFragment : Fragment() {

    private var _bindingOrder : FragmentOrderDetailsBinding? = null
    private val binding get() = _bindingOrder!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bindingOrder = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val args : OrderDetailsFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.bookItemOrderModel = args.bookItemOrderModel

        val productDetailsAdapter = ProductDetailAdapter()
        binding.recyclerViewItemList.adapter = productDetailsAdapter

        productDetailsAdapter.list = args.bookItemOrderModel.productList!!
    }
    

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingOrder = null
    }

}