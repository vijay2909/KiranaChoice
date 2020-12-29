package com.app.kiranachoice.repositories

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

    val allCartItems = databaseDao.getAllCartItem()


    suspend fun insert(cartItem: CartItem) = withContext(Dispatchers.IO) {
        databaseDao.insert(cartItem)
    }


    suspend fun isAlreadyAdded(key: String, packagingSize: String) = withContext(Dispatchers.IO) {
        databaseDao.isAlreadyAdded(key, packagingSize)
    }


    suspend fun delete(key: String) = withContext(Dispatchers.IO) {
        databaseDao.delete(key)
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


    suspend fun removeCoupon(){
        withContext(Dispatchers.IO){
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
                        withContext(Dispatchers.IO){
                            databaseDao.insertBanners(bannersList.asDatabaseModel())
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    val categories : LiveData<List<Category>> = Transformations.map(databaseDao.getCategories()){
        it.asDomainModel()
    }

    fun refreshCategories() {
        dbRef.getReference(CATEGORY_REFERENCE).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryList = ArrayList<CategoryModel>()

                snapshot.children.forEach {
                    val categoryModel = it.getValue(CategoryModel::class.java)
                    categoryModel?.let { model -> categoryList.add(model) }
                }
                runBlocking {
                    withContext(Dispatchers.IO){
                        databaseDao.insertCategories(categoryList.asDatabaseModel())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


    val bestOfferProducts : LiveData<List<Product>> = Transformations.map(databaseDao.getBestOfferProducts()){
        it.asDomainModel()
    }

    fun refreshBestOfferProduct() {
        dbRef.getReference(PRODUCT_REFERENCE)
            .orderByChild(BEST_OFFER_PRODUCT).equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bestOfferProductList = ArrayList<ProductModel>()
                    snapshot.children.forEach {
                        it.getValue(ProductModel::class.java)
                            ?.let { model -> bestOfferProductList.add(model) }
                    }
                    runBlocking {
                        withContext(Dispatchers.IO){
                            databaseDao.insertProducts(bestOfferProductList.asProductDatabaseModel())
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    val bestSellingProducts : LiveData<List<Product>> = Transformations.map(databaseDao.getBestSellingProducts()){
        it.asDomainModel()
    }

    fun refreshBestSellingProduct() {
        dbRef.getReference(PRODUCT_REFERENCE)
            .orderByChild(BEST_SELLING_PRODUCT).equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bestSellingProductList = ArrayList<ProductModel>()
                    snapshot.children.forEach {
                        it.getValue(ProductModel::class.java)
                            ?.let { model -> bestSellingProductList.add(model) }
                    }
                    runBlocking {
                        withContext(Dispatchers.IO){
                            databaseDao.insertProducts(bestSellingProductList.asProductDatabaseModel())
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }


    suspend fun getProduct(productKey: String) = withContext(Dispatchers.IO){
        listOf(databaseDao.getProduct(productKey)).asDomainModel()
    }

    suspend fun getProductsByCategoryName(subCategoryName: String) = withContext(Dispatchers.IO){
        databaseDao.getProductBySubCategoryName(subCategoryName).asDomainModel()
    }

    suspend fun getProductsByCategoryKey(subCategoryKey: String) = withContext(Dispatchers.IO){
        databaseDao.getProductBySubCategoryKey(subCategoryKey).asDomainModel()
    }

}