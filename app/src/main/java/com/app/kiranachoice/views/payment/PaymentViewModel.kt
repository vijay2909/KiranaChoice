package com.app.kiranachoice.views.payment

import android.app.Application
import androidx.lifecycle.*
import com.app.kiranachoice.data.db.CartDatabase
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.*
import com.app.kiranachoice.network.DateTimeApi
import com.app.kiranachoice.network.RetrofitClient
import com.app.kiranachoice.network.SendNotificationAPI
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class PaymentViewModel(val application: Application) : ViewModel() {

    private val dbFire: FirebaseFirestore
    private val mAuth: FirebaseAuth
    private val dbRef: FirebaseDatabase

    private val dataBase = CartDatabase.getInstance(application)
    private val repository = DataRepository(dataBase.databaseDao)

    val allProducts: LiveData<List<CartItem>>

    private var _orderPlacedDate = MutableLiveData<Long>()
    val orderPlacedDate : LiveData<Long> get() = _orderPlacedDate

    private var sequence: Int? = null

    init {
        allProducts = repository.allCartItems
        dbFire = FirebaseFirestore.getInstance()
        dbRef = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        getUserInfo()
        generateOrderId()
        getTime()
    }

    var orderId: String? = null

    private fun generateOrderId() {
        dbRef.getReference(SEQUENCE)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    sequence = snapshot.children.elementAt(0).value.toString().toInt().plus(1)
                    orderId = ORDER_ID_FORMAT + sequence
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    var user: User? = null

    private fun getUserInfo() {
        dbFire.collection(USER_REFERENCE)
            .document(mAuth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                user = it.toObject(User::class.java)
            }
    }

    var cartItems: List<CartItem>? = null

    var totalProductsAmount: LiveData<String> = Transformations.map(allProducts) { items ->
        var tAmount = 0
        items.forEach { item ->
            tAmount += if (item.quantity.toInt() > 1) {
                item.quantity.toInt().times(item.productPrice.toInt())
            } else {
                item.productPrice.toInt()
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
            cartItems?.forEach {
                val item = Product(
                    productSKU = it.productSKU,
                    productName = it.productTitle,
                    productImage = it.productImageUrl,
                    productSize = it.packagingSize,
                    productQuantity = it.quantity,
                    productMRP = it.productMRP,
                    productPrice = it.productPrice,
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
                orderId = orderId
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
            .updateChildren(mapOf(SEQUENCE to sequence))
    }

    private fun saveOrderForAdmin() {
        val key = UUID.randomUUID().toString()
        val adminOrder = AdminOrder(key, mAuth.currentUser!!.uid, orderId, sequence)
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

    private fun getTime() {
        val retrofit = RetrofitClient.getRetrofitClient()

        val api: DateTimeApi = retrofit.create(DateTimeApi::class.java)

        api.getTime().enqueue(object : Callback<CurrentDateTime> {
            override fun onResponse(
                call: Call<CurrentDateTime>,
                response: Response<CurrentDateTime>
            ) {
                val currentDateTime = response.body()
//                val unix = currentDateTime?.unixtime?.let { currentDateTime.rawOffset?.plus(it) }
                val unix = currentDateTime?.unixtime
                getDate(unix)
            }

            override fun onFailure(
                call: Call<CurrentDateTime>,
                t: Throwable
            ) {
                t.printStackTrace()
            }
        })
    }


    fun getDate(unix: Long?) {
        if (unix != null) {
            _orderPlacedDate.value = unix * 1000
        }
    }

    fun orderSaveFinished() {
        _orderSaved.value = false
    }

    fun removeCartItems() {
        viewModelScope.launch(Dispatchers.IO) {
            cartItems?.forEach { cartItem ->
                repository.delete(cartItem.productKey)
            }
        }
    }
}