package com.app.kiranachoice

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.app.kiranachoice.db.CartDao
import com.app.kiranachoice.db.CartDatabase
import com.app.kiranachoice.db.CartItem
import com.app.kiranachoice.models.AppliedCouponModel
import com.app.kiranachoice.models.CouponModel
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
import java.util.*
import kotlin.collections.ArrayList

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
    var email: String? = null

    var fileExtension: String? = null
    private var _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> get() = _imageUrl

    private var amount = 0L
    private var savedAmt = 0L

    private var _productTotalAmount = MutableLiveData<String>()
    val productTotalAmount: LiveData<String> get() = _productTotalAmount


    private var _totalAmount = MutableLiveData<String>()
    val totalAmount: LiveData<String> get() = _totalAmount

    var allCartItems: LiveData<List<CartItem>>

    init {
        mAuth = FirebaseAuth.getInstance()
        dbFire = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        dbRef = FirebaseDatabase.getInstance()

        database = CartDatabase.getInstance(application)
        cartDao = database.cartDao
        cartRepo = CartRepo(cartDao)

        allCartItems = cartRepo.allCartItems

        getCoupons()
    }

    fun getAllCartItems() {
        allCartItems = cartRepo.allCartItems
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
            "email" to email,
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



    /*private var _resultList = MutableLiveData<List<ProductModel>>()
    val resultList: LiveData<List<ProductModel>> get() = _resultList

    fun getResultFromProducts(userInput: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val fakeList = ArrayList<ProductModel>()
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
    }*/

    private var _eventDeleteItem = MutableLiveData<Boolean>()
    val eventDeleteItem : LiveData<Boolean> get() = _eventDeleteItem

    fun removeCartItem(cartItem: CartItem) = viewModelScope.launch(Dispatchers.IO) {
        cartRepo.delete(cartItem.productKey, cartItem.packagingSize)
        _eventDeleteItem.postValue(true)
    }

    fun eventDeleteItemFinished(){
        _eventDeleteItem.value = false
    }


    private var _savedAmount = MutableLiveData("0")
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

            if (cartItem.productMRP.toInt() > cartItem.productPrice.toInt()) {
                savedAmt += cartItem.productMRP.toInt().minus(cartItem.productPrice.toInt())
                    .toLong()
            } else {
                savedAmt += 0
            }
        }

        _productTotalAmount.value = amount.toString()
        _savedAmount.value = savedAmt.toString()

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


    fun setTotalAmount(
        amountPlus: Int? = null,
        amountMinus: Int? = null,
        mrpAndPriceDifference: Int
    ) {
        if (amountPlus != null) {
            _productTotalAmount.value =
                productTotalAmount.value?.toInt()?.plus(amountPlus).toString()
            _savedAmount.value = _savedAmount.value?.toInt()?.plus(mrpAndPriceDifference).toString()
        } else {
            _productTotalAmount.value =
                productTotalAmount.value?.toInt()?.minus(amountMinus!!).toString()
            _totalAmount.value = _productTotalAmount.value

            if (_savedAmount.value?.toInt()!! >= 0) {
                _savedAmount.value =
                    _savedAmount.value?.toInt()?.minus(mrpAndPriceDifference).toString()
            }
        }
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

    private var _snackBarForAlreadyAppliedCoupon = MutableLiveData<Boolean>()
    val snackBarForAlreadyAppliedCoupon : LiveData<Boolean> get() = _snackBarForAlreadyAppliedCoupon

    var couponCode: String? = null

    fun couponApplied(couponModel: CouponModel) {
        mAuth?.currentUser?.let { user ->
            dbFire?.collection(USER_REFERENCE)
                ?.document(user.uid)
                ?.collection(APPLIED_COUPON)
                ?.whereEqualTo("couponCode", couponModel.couponCode)
                ?.get()
                ?.addOnSuccessListener {
                    // check.. if user already used coupon
                    if (it != null && !it.isEmpty) {
                        // user used coupon already
                        _snackBarForAlreadyAppliedCoupon.postValue(true)
                    } else {
                        // user use coupon first time
                        couponApply(couponModel)
                    }
                }
                ?.addOnFailureListener {
                    couponApply(couponModel)
                }
        }
    }

    private var _showCoupon = MutableLiveData<Boolean>()
    val showCoupon : LiveData<Boolean> get() = _showCoupon

    private var _couponDescription = MutableLiveData<String>()
    val couponDescription : LiveData<String> get() = _couponDescription

    private var discount : Double? = null
    private var couponKey: String? = null

    private fun couponApply(couponModel: CouponModel) {
        couponCode = couponModel.couponCode
        // discount rate not null means there is some discount rate... which we have to less from total amount
        if (couponModel.discountRate != null){
            discount = productTotalAmount.value.toString().toDouble().times(couponModel.discountRate.toString().toDouble()).div(100)
            _totalAmount.value = totalAmount.value.toString().toDouble().minus(discount!!).toString()
            _couponDescription.value = "₹$discount"
        } else {
            // discount rate null -> a coupon without discount rate ( e.g. 1 kg. sugar free )
            _couponDescription.value = couponModel.couponDescription?.substringBefore("on", "On")?.trim()?.plus(" Free")
        }

        couponKey = UUID.randomUUID().toString()
        val appliedCouponModel = AppliedCouponModel(
            couponKey,
            couponCode,
            System.currentTimeMillis()
        )

        dbFire?.collection(USER_REFERENCE)?.document(mAuth?.currentUser!!.uid)
            ?.collection(APPLIED_COUPON)?.document(couponKey.toString())
            ?.set(appliedCouponModel)

        _showCoupon.value = true
    }

    fun showCouponFinished(){
        _showCoupon.value = false
    }

    fun snackBarEventFinished(){
        _snackBarForAlreadyAppliedCoupon.value = false
    }

    fun removeCoupon() {
        couponCode = null
        discount?.let {discount ->
            _totalAmount.value = totalAmount.value.toString().toDouble().plus(discount).toString()
        }

        couponKey?.let {
            dbFire?.collection(USER_REFERENCE)?.document(mAuth?.currentUser!!.uid)
                ?.collection(APPLIED_COUPON)?.document(it)
                ?.delete()
        }
    }

}

