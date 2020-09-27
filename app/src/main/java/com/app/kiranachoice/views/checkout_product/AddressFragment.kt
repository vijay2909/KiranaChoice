package com.app.kiranachoice.views.checkout_product


import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.DialogAddAddressBinding
import com.app.kiranachoice.databinding.FragmentAddressBinding
import com.app.kiranachoice.models.AddressModel
import com.app.kiranachoice.recyclerView_adapters.AddressAdapter
import com.google.android.material.snackbar.Snackbar


class AddressFragment : Fragment(), AddressAdapter.AddressCardClickListener {

    private var _bindingAddress: FragmentAddressBinding? = null
    private val binding get() = _bindingAddress!!

    private lateinit var viewModel: CheckoutViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(CheckoutViewModel::class.java)
        _bindingAddress = FragmentAddressBinding.inflate(inflater, container, false)
        binding.isAddressListEmpty = true
        (activity as CheckoutActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addressAdapter = AddressAdapter(this)
        binding.recyclerViewAddressList.adapter = addressAdapter

        viewModel.addressList.observe(viewLifecycleOwner, {
            binding.progressBar.root.visibility = View.GONE
            binding.isAddressListEmpty = it.isEmpty()
            addressAdapter.list = it
        })

        binding.btnAddAddress.setOnClickListener {
            showDialogToAddAddress(null)
        }

        viewModel.updatedDetails.observe(viewLifecycleOwner, {
            if (it){
                Snackbar.make(view, getString(R.string.address_updated), Snackbar.LENGTH_SHORT).show()
                viewModel.updateFinished()
            }
        })

        viewModel.deletedAddress.observe(viewLifecycleOwner, {
            if (it){
                Snackbar.make(view, getString(R.string.delete_successful), Snackbar.LENGTH_SHORT).show()
                viewModel.deleteFinished()
            }
        })
    }

    private fun showDialogToAddAddress(addressModel: AddressModel?) {
        val view =
            DialogAddAddressBinding.inflate(LayoutInflater.from(requireContext()), null, false)

        addressModel?.let {
            view.etFlatNo.editText?.setText(it.flat_num_or_building_name)
            view.etArea.editText?.setText(it.area)
            view.etCity.editText?.setText(it.city)
            view.etPincode.editText?.setText(it.pincode)
            view.etState.editText?.setText(it.state)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view.root)
            .setCancelable(false)
            .show()

        // to make dialog corner rounded we have to set dialog's window background transparent
        val back = ColorDrawable(Color.TRANSPARENT)
        // set margin 20 around dialog
        val inset = InsetDrawable(back, 20)
        dialog.window?.setBackgroundDrawable(inset)

        view.btnCancel.setOnClickListener { dialog.dismiss() }
        view.btnCancelDialog.setOnClickListener { dialog.dismiss() }

        view.btnSave.setOnClickListener {
            binding.progressBar.root.visibility = View.VISIBLE
            if (validateAddressFields(view)) {
                dialog.dismiss()
                if (addressModel == null) viewModel.saveAddress()
                else viewModel.updateAddress(addressModel)
            } else {
                binding.progressBar.root.visibility = View.GONE
            }
        }
    }

    private fun validateAddressFields(view: DialogAddAddressBinding): Boolean {
        var result = true

        // get Values
        viewModel.flatNumberOrBuildingName = view.etFlatNo.editText?.text.toString().trim()
        viewModel.area = view.etArea.editText?.text.toString().trim()
        viewModel.city = view.etCity.editText?.text.toString().trim()
        viewModel.pincode = view.etPincode.editText?.text.toString().trim()
        viewModel.state = view.etState.editText?.text.toString().trim()

        // validate details
        when {
            viewModel.flatNumberOrBuildingName.isNullOrEmpty() -> {
                view.etFlatNo.error = getString(R.string.empty_field_error_msg)
                result = false
            }
            viewModel.area.isNullOrEmpty() -> {
                view.etArea.error = getString(R.string.empty_field_error_msg)
                result = false
            }
            viewModel.city.isNullOrEmpty() -> {
                view.etCity.error = getString(R.string.empty_field_error_msg)
                result = false
            }
            viewModel.pincode.isNullOrEmpty() -> {
                view.etPincode.error = getString(R.string.empty_field_error_msg)
                result = false
            }
            viewModel.state.isNullOrEmpty() -> {
                view.etState.error = getString(R.string.empty_field_error_msg)
                result = false
            }
        }

        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingAddress = null
    }

    override fun onEditButtonClicked(addressModel: AddressModel) {
        showDialogToAddAddress(addressModel)
    }

    override fun onDeleteButtonClicked(addressModel: AddressModel) {
        showDialogBeforeDelete(addressModel)
    }

    private fun showDialogBeforeDelete(addressModel: AddressModel) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_address))
            .setMessage(getString(R.string.are_you_sure))
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                binding.progressBar.root.visibility = View.VISIBLE
                viewModel.deleteAddress(addressModel)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

}