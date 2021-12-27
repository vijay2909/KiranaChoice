package com.app.kiranachoice.views.my_orders

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.kiranachoice.R
import com.app.kiranachoice.data.network_models.BookItemOrderModel
import com.app.kiranachoice.databinding.DialogLoginFirstBinding
import com.app.kiranachoice.databinding.FragmentMyOrdersBinding
import com.app.kiranachoice.recyclerView_adapters.MyOrdersAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyOrdersFragment : Fragment(), MyOrdersAdapter.OrderClickListener {

    private var _bindingMyOrder: FragmentMyOrdersBinding? = null
    private val binding get() = _bindingMyOrder!!

    private val viewModel by viewModels<MyOrdersViewModel>()

    private lateinit var myOrdersAdapter: MyOrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingMyOrder = FragmentMyOrdersBinding.inflate(inflater, container, false)

        myOrdersAdapter = MyOrdersAdapter(this)
        binding.recyclerViewMyOrdersList.adapter = myOrdersAdapter
        binding.recyclerViewMyOrdersList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.authRequired.observe(viewLifecycleOwner, { isAuthRequired ->
            if (isAuthRequired) {
                showAuthFirstDialog()
                viewModel.onUserAuthComplete()
            }
        })

        viewModel.showProgressBar.observe(viewLifecycleOwner, { showProgress ->
            if (showProgress) binding.progressBar.root.visibility = View.VISIBLE
            else binding.progressBar.root.visibility = View.GONE
        })

        viewModel.ordersList.observe(viewLifecycleOwner, {
            binding.isOrderAvailable = !it.isNullOrEmpty()
            if (it.isNullOrEmpty()) binding.imagNoOrder.visibility = View.VISIBLE
            else binding.imagNoOrder.visibility = View.INVISIBLE
            myOrdersAdapter.list = it
        })


    }

    private fun showAuthFirstDialog() {
        val authDialogView =
            DialogLoginFirstBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(authDialogView.root)
            .setCancelable(false)
            .show()

        // to make dialog corner rounded we have to set dialog's window background transparent
        val back = ColorDrawable(Color.TRANSPARENT)
        // set margin 20 around dialog
        val inset = InsetDrawable(back, 20)
        dialog.window?.setBackgroundDrawable(inset)

        authDialogView.btnLogin.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.action_myOrdersFragment_to_authActivity)
        }

        authDialogView.imgClose.setOnClickListener {
            dialog.dismiss()
            findNavController().popBackStack()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _bindingMyOrder = null
    }

    override fun onOrderClicked(bookItemOrderModel: BookItemOrderModel) {
        requireView().findNavController().navigate(
            MyOrdersFragmentDirections.actionMyOrdersFragmentToOrderDetailsFragment(
                bookItemOrderModel
            )
        )
    }

}