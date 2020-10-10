package com.app.kiranachoice.views.payment

import android.app.Application
import android.text.format.DateFormat
import androidx.lifecycle.*
import com.app.kiranachoice.db.CartDatabase
import com.app.kiranachoice.db.CartItem
import com.app.kiranachoice.models.*
import com.app.kiranachoice.network.DateTimeApi
import com.app.kiranachoice.network.SendNotificationAPI
import com.app.kiranachoice.repositories.CartRepo
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
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList


class PaymentViewModel(val application: Application) : ViewModel() {

    private val dbFire: FirebaseFirestore
    private val mAuth: FirebaseAuth
    private val dbRef: FirebaseDatabase

    private val dataBase = CartDatabase.getInstance(application)
    private val repository = CartRepo(dataBase.cartDao)

    val allProducts: LiveData<List<CartItem>>

    lateinit var orderPlacedDate: String
    private var sequence: Int? = null

    init {
        allProducts = repository.allCartItems
        dbFire = FirebaseFirestore.getInstance()
        dbRef = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        getUserInfo()
        generateOrderId()
        viewModelScope.launch {
            getTime()
        }
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

    fun saveUserOrder(deliveryAddress: String) {
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
            if (this@PaymentViewModel::orderPlacedDate.isInitialized) {
                val bookItemOrderModel = BookItemOrderModel(
                    key = key,
                    productList = itemList,
                    totalAmount = totalProductsAmount.value.toString(),
                    deliveryCharge = deliveryCharge.value.toString(),
                    deliveryAddress = deliveryAddress,
                    couponCode = null,
                    isCouponApplied = false,
                    orderPlacedDate = orderPlacedDate
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
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(SendNotificationAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api: SendNotificationAPI = retrofit.create(SendNotificationAPI::class.java)

        val payload = buildNotificationPayload()

        api.sendChatNotification(payload).enqueue(object : Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {}

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {}

        })

    }

    private fun buildNotificationPayload(): JsonObject? {
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

    private suspend fun getTime() {
        withContext(Dispatchers.IO) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(DateTimeApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api: DateTimeApi = retrofit.create(DateTimeApi::class.java)

            api.getTime().enqueue(object : Callback<CurrentDateTime> {
                override fun onResponse(
                    call: Call<CurrentDateTime>,
                    response: Response<CurrentDateTime>
                ) {
                    val currentTime = response.body()
                    val unixTime =
                        currentTime?.raw_offset?.toInt()?.plus(currentTime.unixtime?.toLong()!!)
                    getDate(unixTime)
                }

                override fun onFailure(
                    call: Call<CurrentDateTime>,
                    t: Throwable
                ) {
                    t.printStackTrace()
                }
            })
        }
    }


    fun getDate(value: Long?) {
        if (value != null) {
            val unix = value * 1000
            val weekDay = DateFormat.format("EEE", unix).toString()
            val month = DateFormat.format("MMM", unix).toString()
            val date = DateFormat.format("d", unix).toString().toInt()
            val year = DateFormat.format("yy", unix).toString().toInt()

            orderPlacedDate = weekDay.plus(", $month").plus(" $date").plus(getMark(date.toString()))
                .plus(" '$year")
        }
    }

    private fun getMark(date: String): String {
        return when (date.last().toString()) {
            "1" -> "st"
            "2" -> "nd"
            "3" -> "rd"
            else -> "th"
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