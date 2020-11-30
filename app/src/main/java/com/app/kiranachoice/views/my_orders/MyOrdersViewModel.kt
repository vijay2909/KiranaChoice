package com.app.kiranachoice.views.my_orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.models.BookItemOrderModel
import com.app.kiranachoice.utils.USER_MY_ORDERS_REFERENCE
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyOrdersViewModel : ViewModel() {

    private val dbFire : FirebaseFirestore
    private var mAuth : FirebaseAuth? = null

    init {
        mAuth = FirebaseAuth.getInstance()
        dbFire = FirebaseFirestore.getInstance()
    }

    private var _ordersList = MutableLiveData<List<BookItemOrderModel>>()
    val ordersList : LiveData<List<BookItemOrderModel>> get() = _ordersList

    fun getOrders() {
        mAuth?.currentUser?.let { user ->
            dbFire.collection(USER_REFERENCE)
                .document(user.uid)
                .collection(USER_MY_ORDERS_REFERENCE)
                .get()
                .addOnSuccessListener {
                    _ordersList.postValue(it.toObjects(BookItemOrderModel::class.java))
                }
        }
    }


}