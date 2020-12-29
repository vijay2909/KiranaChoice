package com.app.kiranachoice.views.my_orders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.data.BookItemOrderModel
import com.app.kiranachoice.data.Product
import com.app.kiranachoice.utils.CANCELED
import com.app.kiranachoice.utils.USER_MY_ORDERS_REFERENCE
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderDetailsViewModel : ViewModel() {
    private val dbFire = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()

    private var isAllProductsCancelled: Boolean = true

    var updateProductStatus = MutableLiveData<Boolean>()

    fun onOrderCancel(bookItemOrderModel: BookItemOrderModel, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            mAuth.currentUser?.let { user ->
                val updateProductList = ArrayList<Product>()
                dbFire.collection(USER_REFERENCE).document(user.uid)
                    .collection(USER_MY_ORDERS_REFERENCE)
                    .document(bookItemOrderModel.key.toString())
                    .get().addOnSuccessListener {
                        it.toObject(BookItemOrderModel::class.java)?.let { bookItemOrderModel ->
                            bookItemOrderModel.productList?.forEachIndexed { index, product ->
                                if (index == position) {
                                    val p = Product(
                                        productSKU = product.productSKU,
                                        productName = product.productName,
                                        productImage = product.productImage,
                                        productSize = product.productSize,
                                        productQuantity = product.productQuantity,
                                        productMRP = product.productMRP,
                                        productPrice = product.productPrice,
                                        status = CANCELED
                                    )
                                    updateProductList.add(p)
                                } else {
                                    updateProductList.add(product)
                                    if (product.status != CANCELED) {
                                        isAllProductsCancelled = false
                                    }
                                }

                            }
                            if (isAllProductsCancelled) {
                                val statusMap = mapOf(
                                    "status" to CANCELED
                                )
                                dbFire.collection(USER_REFERENCE)
                                    .document(user.uid)
                                    .collection(USER_MY_ORDERS_REFERENCE)
                                    .document(bookItemOrderModel.key.toString())
                                    .update(statusMap)
                            }
                            val updateProductListMap = mapOf(
                                "productList" to updateProductList
                            )
                            dbFire.collection(USER_REFERENCE)
                                .document(user.uid)
                                .collection(USER_MY_ORDERS_REFERENCE)
                                .document(bookItemOrderModel.key.toString())
                                .update(updateProductListMap)
                                .addOnSuccessListener {
                                    updateProductStatus.postValue(true)
                                }
                        }
                    }

            }

        }
    }

}