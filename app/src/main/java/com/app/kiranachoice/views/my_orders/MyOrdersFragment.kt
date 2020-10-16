package com.app.kiranachoice.views.my_orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.app.kiranachoice.databinding.FragmentMyOrdersBinding
import com.app.kiranachoice.models.BookItemOrderModel
import com.app.kiranachoice.recyclerView_adapters.MyOrdersAdapter

class MyOrdersFragment : Fragment(), MyOrdersAdapter.OrderClickListener {

    private var _bindingMyOrder: FragmentMyOrdersBinding? = null
    private val binding get() = _bindingMyOrder!!

    private val viewModel by viewModels<MyOrdersViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bindingMyOrder = FragmentMyOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myOrdersAdapter = MyOrdersAdapter(this)
        binding.recyclerViewMyOrdersList.adapter = myOrdersAdapter

        viewModel.ordersList.observe(viewLifecycleOwner, {
            myOrdersAdapter.list = it
            binding.progressBar.root.visibility = View.GONE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingMyOrder = null
    }

    override fun onOrderClicked(bookItemOrderModel: BookItemOrderModel) {
        requireView().findNavController().navigate(MyOrdersFragmentDirections.actionMyOrdersFragmentToOrderDetailsFragment(bookItemOrderModel))
    }

}