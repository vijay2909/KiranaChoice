package com.app.kiranachoice.views.address


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.location.*
import android.location.LocationListener
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.DialogAddAddressBinding
import com.app.kiranachoice.databinding.FragmentAddressBinding
import com.app.kiranachoice.models.AddressModel
import com.app.kiranachoice.recyclerView_adapters.AddressAdapter
import com.app.kiranachoice.utils.isNotNullOrEmpty
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import java.util.*

private const val REQUEST_PERMISSION_CODE = 1

class AddressFragment : Fragment(), AddressAdapter.AddressCardClickListener{

    private var _bindingAddress: FragmentAddressBinding? = null
    private val binding get() = _bindingAddress!!

    private lateinit var viewModel: AddressViewModel
    private lateinit var locationManager: LocationManager

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(AddressViewModel::class.java)
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
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        var addresses: List<Address>

                        addresses = geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


                        val address: String =
                            addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                        val city: String = addresses[0].locality
                        val state: String = addresses[0].adminArea
                        val country: String = addresses[0].countryName
                        val postalCode: String = addresses[0].postalCode
                        val knownName: String = addresses[0].featureName // On
                        Log.d(TAG, "address: $address")
                        Log.i(TAG, "city: $city\n state: $state\n country: $country, postalCode: $postalCode, knownName: $knownName")
                    }
                }
            }
        }

        mFusedLocationClient.removeLocationUpdates(locationCallback)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addressAdapter = AddressAdapter(this)
        binding.recyclerViewAddressList.adapter = addressAdapter


        binding.btnAddAddress.setOnClickListener {
            showDialogToAddAddress(null)
        }

        viewModel.addressList.observe(viewLifecycleOwner, {
            binding.progressBar.root.visibility = View.GONE
            binding.isAddressListEmpty = it.isEmpty()
            addressAdapter.list = it
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

        view.btnGetCurrentLocation.setOnClickListener {
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
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                return
            }
        }
    }


//    private fun getCurrentLocation() {
//        // get the location manager
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            onGPS()
//        } else {
//            getLocation()
//        }
//
//    }

    private fun onGPS() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Enable GPS").setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }.setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun getLocation() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                mFusedLocationClient.lastLocation.addOnSuccessListener {
                    it.let { location ->
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val addresses: List<Address>

                        addresses = geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


                        val address: String =
                            addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                        val city: String = addresses[0].locality
                        val state: String = addresses[0].adminArea
                        val country: String = addresses[0].countryName
                        val postalCode: String = addresses[0].postalCode
                        val knownName: String = addresses[0].featureName // On
                        Log.d(TAG, "address: $address")
                        Log.i(TAG, "\ncity: $city\n state: $state\n country: $country,\n postalCode: $postalCode,\n knownName: $knownName")
                    }
                }
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) && shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                showPermissionDeniedDialog()
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

    companion object {
        private const val TAG = "AddressFragment"
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