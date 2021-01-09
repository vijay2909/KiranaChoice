package com.app.kiranachoice.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.app.kiranachoice.data.*
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.db.DatabaseDao
import com.app.kiranachoice.data.db.asDomainModel
import com.app.kiranachoice.data.domain.Banner
import com.app.kiranachoice.data.domain.Category
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class DataRepository(private val databaseDao: DatabaseDao) {


    private val dbRef = FirebaseDatabase.getInstance()
    private val mAuth = FirebaseAuth.getInstance()
    private val dbFire = FirebaseFirestore.getInstance().collection(USER_REFERENCE)

    private var _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    suspend fun getUserDetails() {
        withContext(Dispatchers.IO) {
            mAuth.currentUser?.let { user ->
                dbFire.document(user.uid).get()
                    .addOnSuccessListener { snapShot ->
                        if (snapShot.exists()) {
                            _user.postValue(snapShot.toObject(User::class.java))
                        }
                    }
            }
        }
    }


    val totalCartItems = databaseDao.getTotalCartItems()
    val allCartItems = databaseDao.getAllCartItem()


    suspend fun insert(cartItem: CartItem) = withContext(Dispatchers.IO) {
        databaseDao.insert(cartItem)
    }


    suspend fun isAlreadyAdded(productKey: String, packagingSize: String) =
        withContext(Dispatchers.IO) {
            databaseDao.isAlreadyAdded(productKey, packagingSize)
        }


    suspend fun delete(productKey: String) = withContext(Dispatchers.IO) {
        databaseDao.delete(productKey)
    }


    suspend fun delete(cartItem: CartItem) = withContext(Dispatchers.IO) {
        databaseDao.delete(cartItem)
    }


    suspend fun update(productKey: String, quantity: String) = withContext(Dispatchers.IO) {
        databaseDao.update(productKey, quantity)
    }


    private var fakeCouponList = ArrayList<CouponModel>()
    private var _couponsList = MutableLiveData<List<CouponModel>>()
    val couponList: LiveData<List<CouponModel>> get() = _couponsList

    fun getCoupons() {
        dbRef.getReference(COUPON_REFERENCE)
            .addListenerForSingleValueEvent(object : ValueEventListener {
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


    private var _toastForAlreadyAppliedCoupon = MutableLiveData<Boolean>()
    val toastForAlreadyAppliedCoupon: LiveData<Boolean> get() = _toastForAlreadyAppliedCoupon


    private var _couponDiscount = MutableLiveData<CouponModel>()
    val couponDiscount: LiveData<CouponModel> get() = _couponDiscount


    fun couponApplied(couponModel: CouponModel) {
        mAuth.currentUser?.let { user ->
            dbFire.document(user.uid)
                .collection(APPLIED_COUPON)
                .whereEqualTo("couponCode", couponModel.couponCode)
                .get()
                .addOnSuccessListener {
                    // check.. if user already used coupon
                    if (it != null && !it.isEmpty) {
                        // user used coupon already
                        _toastForAlreadyAppliedCoupon.postValue(true)
                    } else {
                        // user use coupon first time
                        _couponDiscount.postValue(couponModel)

                        // save coupon in database because user can use coupon once in a month
                        val couponKey = UUID.randomUUID().toString()
                        val appliedCouponModel = AppliedCouponModel(
                            couponKey,
                            couponModel.couponCode,
                            System.currentTimeMillis()
                        )

                        dbFire.document(mAuth.currentUser!!.uid)
                            .collection(APPLIED_COUPON).document(couponKey)
                            .set(appliedCouponModel)
                    }
                }
        }
    }


    suspend fun removeCoupon() {
        withContext(Dispatchers.IO) {
            couponDiscount.value?.let {
                dbFire.document(mAuth.currentUser!!.uid)
                    .collection(APPLIED_COUPON).document(it.key.toString())
                    .delete()
            }
        }
    }


    val banners: LiveData<List<Banner>> = Transformations.map(databaseDao.getBanners()) {
        it.asDomainModel()
    }

    suspend fun refreshBanners() {
        dbRef.getReference(HOME_TOP_BANNER)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bannersList = ArrayList<BannerImageModel>()

                    snapshot.children.forEach { dataSnapshot ->
                        dataSnapshot.getValue(BannerImageModel::class.java)
                            ?.let { bannerImageModel ->
                                bannersList.add(bannerImageModel)
                            }
                    }
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            databaseDao.deleteAllBanners()
                            databaseDao.insertBanners(bannersList.asDatabaseModel())
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }


    val firstCategories: LiveData<List<Category>> =
        Transformations.map(databaseDao.getCategories(1)) {
            it.asDomainModel()
        }

    val secondCategories: LiveData<List<Category>> =
        Transformations.map(databaseDao.getCategories(2)) {
            it.asDomainModel()
        }

    suspend fun refreshCategories() {
        withContext(Dispatchers.IO) {
            dbRef.getReference(CATEGORY_REFERENCE)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val categoryList = ArrayList<CategoryModel>()
                        snapshot.children.forEach {
                            val categoryModel = it.getValue(CategoryModel::class.java)
                            categoryModel?.let { model -> categoryList.add(model) }
                        }
                        Log.d(TAG, "categoryList: $categoryList")
                        runBlocking {
                            withContext(Dispatchers.IO) {
                                databaseDao.deleteAllCategories()
                                databaseDao.insertCategories(categoryList.asDatabaseModel())
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

    val bestOfferProducts: LiveData<List<Product>> =
        Transformations.map(databaseDao.getBestOfferProducts()) {
            it.asDomainModel()
        }


    val bestSellingProducts: LiveData<List<Product>> =
        Transformations.map(databaseDao.getBestSellingProducts()) {
            it.asDomainModel()
        }

    fun getProduct(productId: String) = Transformations.map(databaseDao.getProduct(productId)){
        it.asDomainModel()
    }


    fun getProductsByCategoryName(subCategoryName: String) =
        Transformations.map(databaseDao.getProductBySubCategoryName(subCategoryName)) {
            it.asDomainModel()
        }

    var orderId: String? = null
    var sequence: Int? = null

    suspend fun generateOrderId() {
        withContext(Dispatchers.IO) {
            dbRef.getReference(SEQUENCE)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d(TAG, "generateOrderId sequence: ${snapshot.value} ")
                        if (snapshot.exists()){
                            sequence = snapshot.children.elementAt(0).value.toString().toInt().plus(1)
                        }else{
                            sequence = 1
                        }
                        orderId = "KC" + (1000 + sequence!!)
                    }

                    override fun onCancelled(error: DatabaseError) {}

                })
        }
    }


    fun getSubCategories(categoryName: String) =
        Transformations.map(databaseDao.getSubCategories(categoryName)) {
            it.asDomainModel()
        }

    suspend fun refreshSubCategories() {
        dbRef.getReference(SUB_CATEGORY_REFERENCE)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                val subCategoriesList = ArrayList<SubCategoryModel>()
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        it.getValue(SubCategoryModel::class.java)?.let { subCategoryModel ->
                            subCategoriesList.add(subCategoryModel)
                        }
                    }
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            databaseDao.deleteAllSubCategories()
                            databaseDao.insertSubCategories(subCategoriesList.asDatabaseModel())
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }


    suspend fun refreshProducts() {
        dbRef.getReference(PRODUCT_REFERENCE).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = ArrayList<ProductModel>()
                Log.d(TAG, "outer product: ${snapshot.value}")
                snapshot.children.forEach {
                    it.getValue(ProductModel::class.java)?.let { productModel ->
                        Log.d(TAG, "inner product: $productModel")
                        productList.add(productModel)
                    }
                }
                runBlocking {
                    withContext(Dispatchers.IO) {
                        databaseDao.deleteAllProducts()
                        databaseDao.insertProducts(productList.asProductDatabaseModel())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    companion object {
        private const val TAG = "DataRepository"
    }

}