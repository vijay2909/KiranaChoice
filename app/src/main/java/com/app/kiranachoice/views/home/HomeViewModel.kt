package com.app.kiranachoice.views.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.data.CategoryModel
import com.app.kiranachoice.data.PackagingSizeModel
import com.app.kiranachoice.data.ProductModel
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.db.ProductItem
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HomeViewModel(private val dataRepository: DataRepository) : ViewModel() {

    private var dbRef: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val cartItems: LiveData<List<CartItem>> = dataRepository.allCartItems

    init {

        refreshDataFromRepository()
        /*getCategory2()*/
    }

    private var _eventNetworkError = MutableLiveData<Boolean>(false)
    val eventNetworkError: LiveData<Boolean> get() = _eventNetworkError

    val banners = dataRepository.banners

    val categories = dataRepository.categories

    val bestOfferProducts = dataRepository.bestOfferProducts

    val bestSellingProducts = dataRepository.bestSellingProducts

    private fun refreshDataFromRepository() {
        viewModelScope.launch {
            dataRepository.refreshBanners()
            dataRepository.refreshCategories()
            dataRepository.refreshBestOfferProduct()
            dataRepository.refreshBestSellingProduct()
        }
    }


    /*private var fakeCategoryList = ArrayList<CategoryModel>()
    private var _categoryList = MutableLiveData<List<CategoryModel>>()
    val categoryList: LiveData<List<CategoryModel>> get() = _categoryList*/

    /*private fun getCategories() {
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
        *//*dbRef.getReference(CATEGORY_REFERENCE).addListenerForSingleValueEvent(object : ValueEventListener {
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

            })*//*
    }*/


    private var _navigateToAuthActivity = MutableLiveData<Boolean>()
    val navigateToAuthActivity: LiveData<Boolean> get() = _navigateToAuthActivity

    private var _productAdded = MutableLiveData<Boolean>()
    val productAdded: LiveData<Boolean> get() = _productAdded

    private var _alreadyAddedMsg = MutableLiveData<String>()
    val alreadyAddedMsg: LiveData<String> get() = _alreadyAddedMsg

    fun addItemToCart(
        product: Product,
        packagingSizeModel: PackagingSizeModel,
        quantity: String
    ): String {
        if (mAuth.currentUser == null) {
            _navigateToAuthActivity.value = true
            return ""
        } else {
            var added = ""
            runBlocking {
                val result =
                    async { addToCart(dataRepository, product, packagingSizeModel, quantity) }
                if (result.await()) {
                    _productAdded.postValue(true)
                    added = when {
                        product.makeBestOffer -> BEST_OFFER_PRODUCT
                        product.makeBestSelling -> BEST_SELLING_PRODUCT
                        product.makeRecommendedProduct -> BEST_RECOMMENDED_PRODUCT
                        else -> ""
                    }
                } else {
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


    fun getProduct(productKey: String): LiveData<List<Product>> {
        val result = MutableLiveData<List<Product>>()
        viewModelScope.launch {
            result.postValue(dataRepository.getProduct(productKey))
        }
        return result
    }

    private var fakeCategory2 = ArrayList<CategoryModel>()
    private var _category2 = MutableLiveData<List<CategoryModel>>()
    val category2: LiveData<List<CategoryModel>> get() = _category2

    private fun getCategory2() {
        dbRef.getReference(SECOND_CATEGORY_REFERENCE)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        it.getValue(CategoryModel::class.java)?.let { categoryModel ->
                            fakeCategory2.add(categoryModel)
                        }
                    }
                    _category2.postValue(fakeCategory2)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }


    fun removeProductFromCart(productKey: String) = viewModelScope.launch {
        dataRepository.delete(productKey)
    }


    /**
     * update product quantity
     * */
    fun updateQuantity(productKey: String, quantity: String) = viewModelScope.launch {
        dataRepository.update(productKey, quantity)
    }


    companion object {
        private const val TAG = "HomeViewModel"
    }

}