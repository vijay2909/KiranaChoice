package com.app.kiranachoice.views.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.kiranachoice.MainViewModel
import com.app.kiranachoice.databinding.FragmentCartBinding
import com.app.kiranachoice.recyclerView_adapters.CartItemAdapter
import com.app.kiranachoice.views.MainViewModelFactory

class CartFragment : Fragment() {

    private var _bindingCart: FragmentCartBinding? = null
    private val binding get() = _bindingCart!!

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainViewModelFactory = MainViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(
            requireActivity(),
            mainViewModelFactory
        ).get(MainViewModel::class.java)
        _bindingCart = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cartItemAdapter = CartItemAdapter()

        binding.recyclerViewCartList.adapter = cartItemAdapter

        binding.recyclerViewCartList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        viewModel.allCartItems.observe(viewLifecycleOwner, {
            it.let {
                cartItemAdapter.submitList(it)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingCart = null
    }
}