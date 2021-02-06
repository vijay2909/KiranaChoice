package com.app.kiranachoice.views.address

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.data.AddressModel
import com.app.kiranachoice.utils.USER_ADDRESSES_REFERENCE
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor() : ViewModel() {

    private var dbFire: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null

    init {
        dbFire = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        getAddresses()
    }


    var flatNumberOrBuildingName: String? = null
    var area: String? = null
    var city: String? = null
    var pincode: String? = null
    var state: String? = null

    fun saveAddress() {
        val key = UUID.randomUUID().toString()
        val addressModel = AddressModel(
            key, flatNumberOrBuildingName, area, city, pincode, state
        )

        mAuth?.currentUser?.let { user ->
            dbFire?.collection(USER_REFERENCE)
                ?.document(user.uid)
                ?.collection(USER_ADDRESSES_REFERENCE)
                ?.document(key)
                ?.set(addressModel)
                ?.addOnSuccessListener {
                    getAddresses()
                }
        }
    }

    
    private var _addressList = MutableLiveData<List<AddressModel>>()
    val addressList: LiveData<List<AddressModel>> get() = _addressList

    private fun getAddresses() {
        mAuth?.currentUser?.let { user ->
            dbFire?.collection(USER_REFERENCE)
                ?.document(user.uid)?.collection(USER_ADDRESSES_REFERENCE)
                ?.get()
                ?.addOnSuccessListener {
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
            "pincode" to pincode,
            "state" to state
        )

        dbFire?.collection(USER_REFERENCE)
            ?.document(mAuth?.currentUser!!.uid)
            ?.collection(USER_ADDRESSES_REFERENCE)
            ?.document(addressModel.key.toString())
            ?.update(updatedDetails)
            ?.addOnSuccessListener {
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
        dbFire?.collection(USER_REFERENCE)
            ?.document(mAuth?.currentUser!!.uid)
            ?.collection(USER_ADDRESSES_REFERENCE)
            ?.document(addressModel.key.toString())
            ?.delete()
            ?.addOnSuccessListener {
                _deletedAddress.postValue(true)
                getAddresses()
            }
    }

    fun deleteFinished() {
        _deletedAddress.value = false
    }

}