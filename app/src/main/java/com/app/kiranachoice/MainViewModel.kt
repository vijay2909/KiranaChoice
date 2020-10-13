package com.app.kiranachoice

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.app.kiranachoice.db.CartDao
import com.app.kiranachoice.db.CartDatabase
import com.app.kiranachoice.db.CartItem
import com.app.kiranachoice.models.CouponModel
import com.app.kiranachoice.models.ProductModel
import com.app.kiranachoice.models.User
import com.app.kiranachoice.repositories.CartRepo
import com.app.kiranachoice.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : ViewModel() {
    private var mAuth: FirebaseAuth? = null
    private var dbFire: FirebaseFirestore? = null
    private var storage: FirebaseStorage? = null
    private var dbRef: FirebaseDatabase? = null

    private val database: CartDatabase
    private val cartDao: CartDao
    private val cartRepo: CartRepo

    private var _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    var userName: String? = null

    var fileExtension: String? = null
    private var _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> get() = _imageUrl

    private var amount = 0L
    private var savedAmt = 0L

    private var _productTotalAmount = MutableLiveData<String>()
    val productTotalAmount: LiveData<String> get() = _productTotalAmount

//    private var _deliveryFee = MutableLiveData<String>()
//    val deliveryFee: LiveData<String> get() = _deliveryFee

    private var _totalAmount = MutableLiveData<String>()
    val totalAmount: LiveData<String> get() = _totalAmount

    var allCartItems: LiveData<List<CartItem>>

    var cartItems: List<CartItem> = ArrayList()

    init {
        mAuth = FirebaseAuth.getInstance()
        dbFire = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        dbRef = FirebaseDatabase.getInstance()

        database = CartDatabase.getInstance(application)
        cartDao = database.cartDao
        cartRepo = CartRepo(cartDao)

        allCartItems = cartRepo.allCartItems

        viewModelScope.launch {
            getAllProducts()
        }

        getCoupons()
    }

    fun getAllCartItems() {
        allCartItems = cartRepo.allCartItems
    }

    private var fakeProductList = ArrayList<ProductModel>()

    private suspend fun getAllProducts() {
        withContext(Dispatchers.IO) {
            dbRef?.getReference(PRODUCT_REFERENCE)
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        fakeProductList.clear()
                        snapshot.children.forEach { snap ->
                            snap.getValue(ProductModel::class.java)
                                ?.let { it -> fakeProductList.add(it) }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}

                })
        }
    }

    fun getUserDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            mAuth?.currentUser?.let { user ->
                dbFire?.collection(USER_REFERENCE)?.document(user.uid)
                    ?.get()
                    ?.addOnSuccessListener { snapShot ->
                        if (snapShot.exists()) {
                            _user.postValue(snapShot.toObject(User::class.java))
                        }
                    }
            }
        }
    }

    private var _currentProgress = MutableLiveData<Int>()
    val currentProgress: LiveData<Int> get() = _currentProgress

    fun updateImage(uri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uri != null) {
                val ref: StorageReference? = storage?.getReference(USER_IMAGE_REFERENCE)

                val pathRef: StorageReference? =
                    ref?.child("${user.value?.phoneNumber} ${System.currentTimeMillis()}.$fileExtension")

                val uploadTask = pathRef?.putFile(uri)

                uploadTask?.addOnProgressListener {
                    val progress: Double =
                        100.0 * it.bytesTransferred / it.totalByteCount
                    _currentProgress.postValue(progress.toInt())
                }

                uploadTask?.addOnSuccessListener {
                    pathRef.downloadUrl.addOnSuccessListener { uri ->
                        _imageUrl.postValue(uri.toString())
                        if (user.value?.imageUrl != null) {
                            storage?.getReferenceFromUrl(user.value?.imageUrl.toString())?.delete()
                        }
                    }
                }
            }
        }
    }

    private var _onDetailsUpdate = MutableLiveData<Boolean>()
    val onDetailsUpdate: LiveData<Boolean> get() = _onDetailsUpdate

    fun saveData() {
        val userDetails = mapOf(
            "name" to userName,
            "imageUrl" to imageUrl.value
        )

        mAuth?.currentUser?.let { user ->
            dbFire?.collection(USER_REFERENCE)?.document(user.uid)
                ?.update(userDetails)
                ?.addOnSuccessListener {
                    getUserDetails()
                    _onDetailsUpdate.postValue(true)
                }
        }
    }

    fun onDetailsUpdated() {
        _onDetailsUpdate.value = false
    }


    private var fakeList = ArrayList<ProductModel>()
    private var _resultList = MutableLiveData<List<ProductModel>>()
    val resultList: LiveData<List<ProductModel>> get() = _resultList

    fun getResultFromProducts(userInput: String) {
        viewModelScope.launch(Dispatchers.Default) {
            fakeList.clear()
            fakeProductList.forEach { productModel ->
                for (item in productModel.searchableText) {
                    if (item.word?.contains(userInput)!!) {
                        fakeList.add(productModel)
                        break
                    }
                }
            }
        }
        _resultList.value = fakeList
    }

    fun removeCartItem(cartItem: CartItem) = viewModelScope.launch(Dispatchers.IO) {
        cartRepo.delete(cartItem.productKey)
        getAllCartItems()
    }

    private var _savedAmount = MutableLiveData<String>()
    val savedAmount: LiveData<String> get() = _savedAmount

    fun getTotalPayableAmount(cartItems: List<CartItem>) {
        amount = 0L
        savedAmt = 0L

        cartItems.forEach { cartItem ->
            amount = if (cartItem.quantity.toInt() > 1) {
                amount.plus(
                    cartItem.quantity.toInt().times(cartItem.productPrice.toInt())
                )
            } else {
                amount.plus(cartItem.productPrice.toInt())
            }

            savedAmt = if (cartItem.productMRP.toInt() > cartItem.productPrice.toInt()) {
                cartItem.productMRP.toInt().minus(cartItem.productPrice.toInt()).toLong()
            } else {
                0
            }
        }

        _productTotalAmount.value = amount.toString()
        _savedAmount.value = savedAmt.toString()

//        addDeliveryFeeIfRequire(amount)
    }

    val deliveryFee: LiveData<String> = Transformations.map(productTotalAmount) {
        if (it.toInt() < MAXIMUM_AMOUNT_TO_AVOID_DELIVERY_CHARGE) {
            _totalAmount.value = it.toInt().plus(DELIVERY_CHARGE).toString()
            DELIVERY_CHARGE.toString()
        } else {
            _totalAmount.value = it
            DELIVERY_FREE
        }
    }

//    private fun addDeliveryFeeIfRequire(amount: Long) {
//        if (amount < MAXIMUM_AMOUNT_TO_AVOID_DELIVERY_CHARGE) {
//            _deliveryFee.value = DELIVERY_CHARGE.toString()
//            _totalAmount.value = amount.plus(DELIVERY_CHARGE).toString()
//        } else {
//            _deliveryFee.value = DELIVERY_FREE
//            _totalAmount.value = amount.toString()
//        }
//    }

    fun setTotalAmount(amountPlus: Int? = null, amountMinus: Int? = null) {
        if (amountPlus != null) {
            _productTotalAmount.value =
                productTotalAmount.value?.toInt()?.plus(amountPlus).toString()
        } else {
            _productTotalAmount.value =
                productTotalAmount.value?.toInt()?.minus(amountMinus!!).toString()
            _totalAmount.value = _productTotalAmount.value
        }
//        addDeliveryFeeIfRequire(_productTotalAmount.value?.toLong()!!)
    }

    fun updateProduct(productName: String, quantity: String) {
        viewModelScope.launch {
            cartRepo.update(productName, quantity)
        }
    }


    private var fakeCouponList = ArrayList<CouponModel>()
    private var _couponsList = MutableLiveData<List<CouponModel>>()
    val couponList: LiveData<List<CouponModel>> get() = _couponsList

    private fun getCoupons() {
        dbRef?.getReference(COUPON_REFERENCE)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fakeCouponList.clear()
                    snapshot.children.forEach {
                        it.getValue(CouponModel::class.java)?.let { couponModel ->
                            fakeCouponList.add(couponModel)
                        }
                    }
                    _couponsList.postValue(fakeCouponList)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

}

