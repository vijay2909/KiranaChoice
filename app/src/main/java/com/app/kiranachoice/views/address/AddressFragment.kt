package com.app.kiranachoice.views.address


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.DialogAddAddressBinding
import com.app.kiranachoice.databinding.FragmentAddressBinding
import com.app.kiranachoice.data.network_models.AddressModel
import com.app.kiranachoice.recyclerView_adapters.AddressAdapter
import com.app.kiranachoice.utils.isNotNullOrEmpty
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

private const val REQUEST_PERMISSION_CODE = 1

@AndroidEntryPoint
class AddressFragment : Fragment(), AddressAdapter.AddressCardClickListener {

    private var _bindingAddress: FragmentAddressBinding? = null
    private val binding get() = _bindingAddress!!

    private val viewModel: AddressViewModel by viewModels()

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var addAddressView: DialogAddAddressBinding

    private lateinit var deliveryAddress: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*viewModel = ViewModelProvider(this).get(AddressViewModel::class.java)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()*/
        _bindingAddress = FragmentAddressBinding.inflate(inflater, container, false)
        binding.isAddressListEmpty = true

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 20 * 1000

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    for (location in it.locations) {
                        setLocation(location)
                    }
                }
            }
        }

        mFusedLocationClient.removeLocationUpdates(locationCallback)

        return binding.root
    }

    private fun setLocation(location: Location) {
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address>

        addresses = geoCoder.getFromLocation(
            location.latitude,
            location.longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        addAddressView.apply {
            flatNo.setText(addresses[0].featureName)
            area.setText(addresses[0].subLocality)
            city.setText(addresses[0].locality)
            pinCode.setText(addresses[0].postalCode)
            state.setText(addresses[0].adminArea)
        }
    }

    private val args: AddressFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addressAdapter = AddressAdapter(this)
        binding.recyclerViewAddressList.adapter = addressAdapter


        binding.btnAddAddress.setOnClickListener {
            showDialogToAddAddress(null)
        }

        viewModel.addressList.observe(viewLifecycleOwner, {
            binding.isAddressListEmpty = it.isEmpty()
            addressAdapter.list = it
            binding.progressBar.root.visibility = View.GONE
            if (it.isNotEmpty()) binding.btnConfirmOrder.isEnabled = true
        })

        viewModel.updatedDetails.observe(viewLifecycleOwner, {
            if (it) {
                Snackbar.make(view, getString(R.string.address_updated), Snackbar.LENGTH_SHORT)
                    .show()
                viewModel.updateFinished()
            }
        })

        viewModel.deletedAddress.observe(viewLifecycleOwner, {
            if (it) {
                Snackbar.make(view, getString(R.string.delete_successful), Snackbar.LENGTH_SHORT)
                    .show()
                viewModel.deleteFinished()
            }
        })

        binding.btnConfirmOrder.setOnClickListener {
            view.findNavController().navigate(
                AddressFragmentDirections.actionAddressFragmentToPaymentFragment(
                    deliveryAddress,
                    args.totalAmount,
                    args.couponCode,
                    args.couponDescription
                )
            )
        }
    }


    private fun showDialogToAddAddress(addressModel: AddressModel?) {
        addAddressView =
            DialogAddAddressBinding.inflate(LayoutInflater.from(requireContext()))

        addressModel?.let {
            addAddressView.etFlatNo.editText?.setText(it.flat_num_or_building_name)
            addAddressView.etArea.editText?.setText(it.area)
            addAddressView.etCity.editText?.setText(it.city)
            addAddressView.etPincode.editText?.setText(it.pincode)
            addAddressView.etState.editText?.setText(it.state)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(addAddressView.root)
            .setCancelable(false)
            .show()

        // to make dialog corner rounded we have to set dialog's window background transparent
        val back = ColorDrawable(Color.TRANSPARENT)
        // set margin 20 around dialog
        val inset = InsetDrawable(back, 20)
        dialog.window?.setBackgroundDrawable(inset)

        addAddressView.btnCancel.setOnClickListener { dialog.dismiss() }
        addAddressView.btnCancelDialog.setOnClickListener { dialog.dismiss() }

        addAddressView.btnSave.setOnClickListener {
            binding.progressBar.root.visibility = View.VISIBLE
            if (validateAddressFields(addAddressView)) {
                dialog.dismiss()
                if (addressModel == null) viewModel.saveAddress()
                else viewModel.updateAddress(addressModel)
            } else {
                binding.progressBar.root.visibility = View.GONE
            }
        }

        addAddressView.btnGetCurrentLocation.setOnClickListener {
            getLocation()
        }
    }

    private fun validateAddressFields(view: DialogAddAddressBinding): Boolean {

        // get Values
        viewModel.flatNumberOrBuildingName = view.etFlatNo.editText?.text.toString().trim()
        viewModel.area = view.etArea.editText?.text.toString().trim()
        viewModel.city = view.etCity.editText?.text.toString().trim()
        viewModel.pincode = view.etPincode.editText?.text.toString().trim()
        viewModel.state = view.etState.editText?.text.toString().trim()

        return view.flatNo.isNotNullOrEmpty(getString(R.string.empty_field_error_msg)) &&
                view.area.isNotNullOrEmpty(getString(R.string.empty_field_error_msg)) &&
                view.city.isNotNullOrEmpty(getString(R.string.empty_field_error_msg)) &&
                view.pinCode.isNotNullOrEmpty(getString(R.string.empty_field_error_msg)) &&
                view.state.isNotNullOrEmpty(getString(R.string.empty_field_error_msg))

    }


    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.permission_denied))
            .setMessage(getString(R.string.permission_denied_msg))
            .setPositiveButton(getString(R.string.app_setting)) { _, _ ->
                // send to app settings if permission is denied permanently
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showPermissionDeniedDialog()
                } else if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    getLocation()
                } else {
                    // permission is denied, you can ask for permission again, if you want
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        REQUEST_PERMISSION_CODE
                    )
                }
            }
        }
    }

    private fun getLocation() {
        when {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                mFusedLocationClient.lastLocation.addOnSuccessListener {
                    setLocation(it)
                }
            }

            else -> {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    REQUEST_PERMISSION_CODE
                )
            }
        }

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

    override fun onAddressCardSelect(addressModel: AddressModel) {
        deliveryAddress = "${addressModel.flat_num_or_building_name}," +
                " ${addressModel.area}," +
                " ${addressModel.city}," +
                " ${addressModel.state}," +
                " ${addressModel.pincode}"
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