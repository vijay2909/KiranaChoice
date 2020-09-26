package com.app.kiranachoice.views.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.kiranachoice.MainViewModel
import com.app.kiranachoice.MainViewModelFactory
import com.app.kiranachoice.databinding.FragmentCartBinding
import com.app.kiranachoice.db.CartItem
import com.app.kiranachoice.recyclerView_adapters.CartItemAdapter

class CartFragment : Fragment(), CartItemAdapter.CartListener {

    private var _bindingCart: FragmentCartBinding? = null
    private val binding get() = _bindingCart!!

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainViewModelFactory = MainViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(requireActivity(), mainViewModelFactory)
            .get(MainViewModel::class.java)
        _bindingCart = FragmentCartBinding.inflate(inflater, container, false)
        binding.mainViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cartItemAdapter = CartItemAdapter(this)

        binding.recyclerViewCartList.adapter = cartItemAdapter

        binding.recyclerViewCartList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        viewModel.getAllCartItems().observe(viewLifecycleOwner, {
            it?.let {
                binding.isListEmpty = it.isEmpty()
                viewModel.cartItems = it // this list use to know total amount
                cartItemAdapter.submitList(it)
                viewModel.getTotalPayableAmount() // calculate total amount
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingCart = null
    }

    override fun removeCartItem(cartItem: CartItem) {
        viewModel.removeCartItem(cartItem)
    }

    override fun onQuantityChange(cartItem: CartItem, amountPlus: Int?, amountMinus: Int?) {
        if (amountPlus != null) viewModel.setTotalAmount(amountPlus)
        else viewModel.setTotalAmount(null, amountMinus)
    }
}