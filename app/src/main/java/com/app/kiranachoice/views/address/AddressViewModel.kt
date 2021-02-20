package com.app.kiranachoice.views.address

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.data.network_models.AddressModel
import com.app.kiranachoice.utils.USER_ADDRESSES_REFERENCE
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val dbFire: FirebaseFirestore,
    val mAuth: FirebaseAuth
) : ViewModel() {


    init {
        getAddresses()
    }


    var flatNumberOrBuildingName: String? = null
    var area: String? = null
    var city: String? = null
    var pinCode: String? = null
    var state: String? = null

    private val _navigateToAuthentication = MutableLiveData<Boolean>()
    val navigateToAuthentication: LiveData<Boolean> get() = _navigateToAuthentication

    fun navigateToAuthenticationComplete() {
        _navigateToAuthentication.value = false
    }


    fun saveAddress() {
        mAuth.currentUser?.let { user ->
            val addressFirestoreReference = dbFire.collection(USER_REFERENCE)
                .document(user.uid)
                .collection(USER_ADDRESSES_REFERENCE)

            val key = addressFirestoreReference.id
            Timber.i("key: $key")

            val addressModel = AddressModel(
                key, flatNumberOrBuildingName, area, city, pinCode, state
            )

            addressFirestoreReference.document(key)
                .set(addressModel)
                .addOnSuccessListener { getAddresses() }
        }
    }


    private var _addressList = MutableLiveData<List<AddressModel>>()
    val addressList: LiveData<List<AddressModel>> get() = _addressList

    private fun getAddresses() {
        mAuth.currentUser?.let { user ->
            dbFire.collection(USER_REFERENCE)
                .document(user.uid).collection(USER_ADDRESSES_REFERENCE)
                .get()
                .addOnSuccessListener {
                    val addresses = it.toObjects(AddressModel::class.java)
                    _addressList.postValue(addresses)
                }
        }
    }

    private var _updatedDetails = MutableLiveData<Boolean>()
    val updatedDetails: LiveData<Boolean> get() = _updatedDetails

    fun updateAddress(addressModel: AddressModel) {
        val updatedDetails = mapOf(
            "flat_num_or_building_name" to flatNumberOrBuildingName,
            "area" to area,
            "city" to city,
            "pincode" to pinCode,
            "state" to state
        )

        dbFire.collection(USER_REFERENCE)
            .document(mAuth.currentUser!!.uid)
            .collection(USER_ADDRESSES_REFERENCE)
            .document(addressModel.key.toString())
            .update(updatedDetails)
            .addOnSuccessListener {
                _updatedDetails.postValue(true)
                getAddresses()
            }
    }

    fun updateFinished() {
        _updatedDetails.value = false
    }

    private var _deletedAddress = MutableLiveData<Boolean>()
    val deletedAddress: LiveData<Boolean> get() = _deletedAddress

    fun deleteAddress(addressModel: AddressModel) {
        dbFire.collection(USER_REFERENCE)
            .document(mAuth.currentUser!!.uid)
            .collection(USER_ADDRESSES_REFERENCE)
            .document(addressModel.key.toString())
            .delete()
            .addOnSuccessListener {
                _deletedAddress.postValue(true)
                getAddresses()
            }
    }

    fun deleteFinished() {
        _deletedAddress.value = false
    }

}