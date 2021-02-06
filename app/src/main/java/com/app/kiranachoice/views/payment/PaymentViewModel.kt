package com.app.kiranachoice.views.payment

import androidx.lifecycle.*
import com.app.kiranachoice.data.AdminOrder
import com.app.kiranachoice.data.BookItemOrderModel
import com.app.kiranachoice.data.Product
import com.app.kiranachoice.network.SendNotificationAPI
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class PaymentViewModel @Inject constructor(private val dataRepository: DataRepository) : ViewModel() {

    private val dbFire: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dbRef: FirebaseDatabase = FirebaseDatabase.getInstance()

    val cartItems = dataRepository.allCartItems


    val user = dataRepository.user

    init {
        viewModelScope.launch {
            dataRepository.getUserDetails()
            dataRepository.generateOrderId()
        }
        getTime()
    }


//    var cartItems: List<CartItem>? = null

    var totalProductsAmount: LiveData<String> = Transformations.map(cartItems) { items ->
        var tAmount = 0
        items.forEach { item ->
            tAmount += if (item.orderQuantity > 1) {
                item.orderQuantity.times(item.packagingSize[item.packagingIndex].price!!.toInt())
            } else {
                item.packagingSize[item.packagingIndex].price!!.toInt()
            }
        }
        tAmount.toString().toPriceAmount()
    }

    private var _totalAmount = MutableLiveData<String>()
    val totalAmount: LiveData<String> get() = _totalAmount

    val deliveryCharge: LiveData<String> = Transformations.map(totalProductsAmount) { amt ->

        val amount = amt.filter { it.isDigit() }.removeSuffix("00")

        if (amount.toInt() > MAXIMUM_AMOUNT_TO_AVOID_DELIVERY_CHARGE) {
            _totalAmount.value = amount
            DELIVERY_FREE
        } else {
            _totalAmount.value = amount.toInt().plus(DELIVERY_CHARGE).toString()
            DELIVERY_CHARGE.toString()
        }
    }


    private var _orderSaved = MutableLiveData<Boolean>()
    val orderSaved: LiveData<Boolean> get() = _orderSaved

    fun saveUserOrder(deliveryAddress: String, couponCode: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val itemList = ArrayList<Product>()
            cartItems.value?.forEach {
                val item = Product(
                    productSKU = it.product_sku,
                    productName = it.name,
                    productImage = it.image,
                    productSize = it.packagingSize[it.packagingIndex].size,
                    productQuantity = it.orderQuantity,
                    productMRP = it.packagingSize[it.packagingIndex].mrp,
                    productPrice = it.packagingSize[it.packagingIndex].price,
                )
                itemList.add(item)
            }

            val key = UUID.randomUUID().toString()

            val bookItemOrderModel = BookItemOrderModel(
                key = key,
                userUid = mAuth.currentUser?.uid,
                productList = itemList,
                invoiceAmount = totalProductsAmount.value.toString().substringBefore("."),
                deliveryCharge = deliveryCharge.value.toString(),
                deliveryAddress = deliveryAddress,
                couponCode = couponCode,
                couponApplied = couponCode != null,
                orderPlacedDate = orderPlacedDate.value,
                orderId = dataRepository.orderId
            )

            dbFire.collection(USER_REFERENCE)
                .document(mAuth.currentUser!!.uid)
                .collection(USER_MY_ORDERS_REFERENCE)
                .document(key)
                .set(bookItemOrderModel)
                .addOnSuccessListener {
                    _orderSaved.postValue(true)
                    updateOrderIdSequence()
                    saveOrderForAdmin()
                    sendNotificationToAdmin()
                }
        }
    }

    private fun updateOrderIdSequence() {
        dbRef.getReference(SEQUENCE)
            .updateChildren(mapOf(SEQUENCE to dataRepository.sequence))
    }


    private fun saveOrderForAdmin() {
        val key = UUID.randomUUID().toString()
        val adminOrder = AdminOrder(key, mAuth.currentUser!!.uid, dataRepository.orderId, dataRepository.sequence)
        dbFire.collection(ADMIN_REFERENCE)
            .document(key)
            .set(adminOrder)
    }

    private fun sendNotificationToAdmin() {
        val api = SendNotificationAPI.getInstance()

        val payload = buildNotificationPayload()

        api.sendChatNotification(payload).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {}

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {}

        })

    }

    private fun buildNotificationPayload(): JsonObject {
        // compose notification json payload
        val payload = JsonObject()
        payload.addProperty("to", "/topics/" + "orders")

        // compose data payload here
        val data = JsonObject()
        data.addProperty("title", "Order Received.")
        data.addProperty("message", "A new order received.")

        // add data payload
        payload.add("notification", data)
        return payload
    }

    /*private var _orderPlacedDate = MutableLiveData<Long>()
    val orderPlacedDate : LiveData<Long> get() = _orderPlacedDate*/

    val orderPlacedDate = dataRepository.orderPlacedDate

    private fun getTime() {
        dataRepository.getTime()
        /*apiService.getTime("3K01ECC74C9F", "json", "IN").enqueue(object :
            Callback<CurrentDateTime> {
            override fun onResponse(
                call: Call<CurrentDateTime>,
                response: Response<CurrentDateTime>
            ) {
                if (response.isSuccessful && response.body() != null) {

                    val timestamp = response.body()!!.zones[0].timestamp
                    _orderPlacedDate.value = (timestamp * 1_000).toLong()
                }
            }

            override fun onFailure(call: Call<CurrentDateTime>, t: Throwable) {
            }
        })*/
    }


    fun orderSaveFinished() {
        _orderSaved.value = false
    }


    fun removeCartItems() {
        viewModelScope.launch(Dispatchers.IO) {
            cartItems.value?.forEach { product ->
                dataRepository.removeFromCart(product.key)
            }
        }
    }
}