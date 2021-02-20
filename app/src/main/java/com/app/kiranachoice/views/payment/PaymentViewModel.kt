package com.app.kiranachoice.views.payment

import androidx.lifecycle.*
import com.app.kiranachoice.data.database_models.asNetworkProduct
import com.app.kiranachoice.data.network_models.AdminOrder
import com.app.kiranachoice.data.network_models.BookItemOrderModel
import com.app.kiranachoice.data.network_models.Product
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
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class PaymentViewModel @Inject constructor(
    val dbFire: FirebaseFirestore,
    val mAuth: FirebaseAuth,
    private val dbRef: FirebaseDatabase,
    val sendNotificationAPI: SendNotificationAPI,
    private val dataRepository: DataRepository
    ) : ViewModel() {

    suspend fun getCartItems() = withContext(Dispatchers.IO){
        dataRepository.getCartItems()
    }


    val user = dataRepository.user

    init {
        viewModelScope.launch {
            dataRepository.getUserDetails()
            dataRepository.generateOrderId()
        }
        getTime()
    }


    val totalProductsAmount = dataRepository.getTotalInvoiceAmount()


    private var _totalAmount = MutableLiveData<String>()
    val totalAmount: LiveData<String> get() = _totalAmount

    val deliveryCharge: LiveData<String> = Transformations.map(totalProductsAmount) { amt ->
        Timber.d("amount: %s", amt)
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
            dataRepository.getCartItems().forEach {
                val item = it.asNetworkProduct()
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

        val payload = buildNotificationPayload()

        sendNotificationAPI.sendChatNotification(payload).enqueue(object : Callback<JsonObject> {
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

    val orderPlacedDate = dataRepository.orderPlacedDate

    private fun getTime() {
        dataRepository.getTime()
    }


    fun orderSaveFinished() {
        _orderSaved.value = false
    }


    fun removeCartItems() {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.getCartItems().forEach { cartItem ->
                dataRepository.removeFromCart(cartItem)
            }
        }
    }
}