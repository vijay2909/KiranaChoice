package com.app.kiranachoice.views.my_orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.kiranachoice.databinding.DialogCancelOrderConfirmationBinding
import com.app.kiranachoice.databinding.FragmentOrderDetailsBinding
import com.app.kiranachoice.recyclerView_adapters.ProductDetailAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderDetailsFragment : Fragment(), ProductDetailAdapter.CancelOrderListener {

    private var _bindingOrder: FragmentOrderDetailsBinding? = null
    private val binding get() = _bindingOrder!!

    private lateinit var viewModel : OrderDetailsViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(OrderDetailsViewModel::class.java)
        _bindingOrder = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val args: OrderDetailsFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.bookItemOrderModel = args.bookItemOrderModel

        val productDetailsAdapter = ProductDetailAdapter(this)
        binding.recyclerViewItemList.apply {
            adapter = productDetailsAdapter
            setHasFixedSize(true)
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        productDetailsAdapter.list = args.bookItemOrderModel.productList!!

        viewModel.updateProductStatus.observe(viewLifecycleOwner, {
            binding.progressBar.root.visibility = View.INVISIBLE
            findNavController().popBackStack()
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _bindingOrder = null
    }

    override fun onOrderCancel(position: Int) {
        showCancelOrderConfirmationDialog(position)
    }

    private fun showCancelOrderConfirmationDialog(position: Int) {
        val view =
            DialogCancelOrderConfirmationBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view.root)
            .setCancelable(false)
            .show()

        view.btnNo.setOnClickListener { dialog.dismiss() }

        view.btnYes.setOnClickListener {
            binding.progressBar.root.visibility = View.VISIBLE
            dialog.dismiss()
            viewModel.onOrderCancel(args.bookItemOrderModel, position)
        }

    }

}