package com.app.kiranachoice.views.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.db.CartDao
import com.app.kiranachoice.db.CartDatabase
import com.app.kiranachoice.db.CartItem
import com.app.kiranachoice.models.*
import com.app.kiranachoice.repositories.CartRepo
import com.app.kiranachoice.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class HomeViewModel(application: Application) : ViewModel() {

    private var dbRef: FirebaseDatabase
    private var dbFire: FirebaseFirestore
    private var mAuth: FirebaseAuth

    private val database: CartDatabase
    private val cartDao: CartDao
    private val cartRepo: CartRepo

    val cartItems : LiveData<List<CartItem>>

    init {
        dbRef = FirebaseDatabase.getInstance()
        dbFire = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        database = CartDatabase.getInstance(application)
        cartDao = database.cartDao
        cartRepo = CartRepo(cartDao)

        cartItems = cartRepo.allCartItems

        getBanners()
        getCategories()
        getBestOfferProduct()
        getBestSellingProduct()
        getCategory2()
    }


    private var fakeBannersList = ArrayList<BannerImageModel>()
    private var _bannersList = MutableLiveData<List<BannerImageModel>>()
    val bannersList: LiveData<List<BannerImageModel>> get() = _bannersList

    private fun getBanners() {
        dbRef.getReference(HOME_TOP_BANNER).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fakeBannersList.clear()

                    snapshot.children.forEach { dataSnapshot ->
                        dataSnapshot.getValue(BannerImageModel::class.java)
                            ?.let { bannerImageModel ->
                                fakeBannersList.add(bannerImageModel)
                            }
                    }

                    _bannersList.postValue(fakeBannersList)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private var fakeCategoryList = ArrayList<Category1Model>()
    private var _categoryList = MutableLiveData<List<Category1Model>>()
    val categoryList: LiveData<List<Category1Model>> get() = _categoryList

    private fun getCategories() {
        dbRef.getReference(CATEGORY_REFERENCE).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fakeCategoryList.clear()

                snapshot.children.forEach {
                    val categoryModel = it.getValue(Category1Model::class.java)
                    categoryModel?.let { model -> fakeCategoryList.add(model) }
                }

                _categoryList.postValue(fakeCategoryList)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        /*dbRef.getReference(CATEGORY_REFERENCE).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i(TAG, "getCategories onDataChange: called")

                    fakeCategoryList.clear()

                    snapshot.children.forEach {
                        val categoryModel = it.getValue(Category1Model::class.java)
                        categoryModel?.let { model -> fakeCategoryList.add(model) }
                    }

                    _categoryList.postValue(fakeCategoryList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "getCategories error code: ${error.code}")
                    Log.e(TAG, "getCategories error message: ${error.message}")
                }

            })*/
    }

    private var fakeBestOfferProductList = ArrayList<ProductModel>()
    private var _bestOfferProductList = MutableLiveData<List<ProductModel>>()
    val bestOfferProductList: LiveData<List<ProductModel>> get() = _bestOfferProductList

    private fun getBestOfferProduct() {
        dbRef.getReference(PRODUCT_REFERENCE)
            .orderByChild(BEST_OFFER_PRODUCT).equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fakeBestOfferProductList.clear()
                    snapshot.children.forEach {
                        it.getValue(ProductModel::class.java)
                            ?.let { model -> fakeBestOfferProductList.add(model) }
                    }
                    _bestOfferProductList.postValue(fakeBestOfferProductList)

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private var fakeBestSellingProductList = ArrayList<ProductModel>()
    private var _bestSellingProductList = MutableLiveData<List<ProductModel>>()
    val bestSellingProductList: LiveData<List<ProductModel>> get() = _bestSellingProductList

    private fun getBestSellingProduct() {
        dbRef.getReference(PRODUCT_REFERENCE)
            .orderByChild(BEST_SELLING_PRODUCT).equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fakeBestSellingProductList.clear()
                    snapshot.children.forEach {
                        it.getValue(ProductModel::class.java)
                            ?.let { model -> fakeBestSellingProductList.add(model) }
                    }
                    _bestSellingProductList.postValue(fakeBestSellingProductList)

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }


    private var _navigateToAuthActivity = MutableLiveData<Boolean>()
    val navigateToAuthActivity: LiveData<Boolean> get() = _navigateToAuthActivity

    private var _productAdded = MutableLiveData<Boolean>()
    val productAdded: LiveData<Boolean> get() = _productAdded

    private var _alreadyAddedMsg = MutableLiveData<String>()
    val alreadyAddedMsg: LiveData<String> get() = _alreadyAddedMsg

    fun addItemToCart(
        productModel: ProductModel,
        packagingSizeModel: PackagingSizeModel,
        quantity: String
    ) : String {
        if (mAuth.currentUser == null) {
            _navigateToAuthActivity.value = true
            return ""
        } else {
            var added = ""
            runBlocking {
                val result = async { addToCart(cartRepo, productModel, packagingSizeModel, quantity) }
                if (result.await()){
                    _productAdded.postValue(true)
                    added = when {
                        productModel.makeBestOffer -> BEST_OFFER_PRODUCT
                        productModel.makeBestSelling -> BEST_SELLING_PRODUCT
                        productModel.makeRecommendedProduct -> BEST_RECOMMENDED_PRODUCT
                        else -> ""
                    }
                } else{
                    _alreadyAddedMsg.postValue("Already added in cart.")
                }
            }

            return added
        }
    }


    fun authActivityNavigated() {
        _navigateToAuthActivity.value = false
    }

    fun productAddedSuccessful() {
        _productAdded.value = false
    }

    private var _product = MutableLiveData<ProductModel>()
    val product: LiveData<ProductModel> get() = _product

    fun getProductDetails(productId: String) {
        dbRef.getReference(PRODUCT_REFERENCE)
            .child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _product.postValue(snapshot.getValue(ProductModel::class.java))
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    fun productShouldBeNull(){
        _product.value = null
    }

    private var fakeCategory2 = ArrayList<Category1Model>()
    private var _category2 = MutableLiveData<List<Category1Model>>()
    val category2: LiveData<List<Category1Model>> get() = _category2

    private fun getCategory2() {
        dbRef.getReference(SECOND_CATEGORY_REFERENCE)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        it.getValue(Category1Model::class.java)?.let { categoryModel ->
                            fakeCategory2.add(categoryModel)
                        }
                    }
                    _category2.postValue(fakeCategory2)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }

}