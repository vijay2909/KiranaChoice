package com.app.kiranachoice.views.my_orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.data.network_models.BookItemOrderModel
import com.app.kiranachoice.utils.USER_MY_ORDERS_REFERENCE
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MyOrdersViewModel @Inject constructor(
    val dbFire: FirebaseFirestore,
    val mAuth: FirebaseAuth
) : ViewModel() {

    private var _showProgressBar = MutableLiveData(false)
    val showProgressBar : LiveData<Boolean> get() = _showProgressBar

    private fun hideProgressBar(){
        _showProgressBar.value = false
    }

    private fun showProgressBar(){
        _showProgressBar.value = true
    }

    private var _ordersList = MutableLiveData<List<BookItemOrderModel>>()
    val ordersList: LiveData<List<BookItemOrderModel>> get() = _ordersList

    private var _authRequired = MutableLiveData(false)
    val authRequired: LiveData<Boolean> get() = _authRequired

    fun onUserAuthComplete() {
        _authRequired.value = false
    }

    private fun getOrders() {
        showProgressBar()

        val user = mAuth.currentUser
        if (user != null) {
            dbFire.collection(USER_REFERENCE)
                .document(user.uid)
                .collection(USER_MY_ORDERS_REFERENCE)
                .get()
                .addOnSuccessListener {
                    Timber.i("getOrders success block")
                    val listOfOrders = it.toObjects(BookItemOrderModel::class.java)
                    for (listOfOrder in listOfOrders) {
                        Timber.d("order: $listOfOrder")
                    }
                    _ordersList.postValue(listOfOrders)
                    hideProgressBar()
                }
        } else {
            hideProgressBar()
            _authRequired.value = true
        }
    }

    init {
        getOrders()
    }

}