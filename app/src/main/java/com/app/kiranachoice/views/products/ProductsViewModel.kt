package com.app.kiranachoice.views.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.data.PackagingSizeModel
import com.app.kiranachoice.data.ProductModel
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.PRODUCT_REFERENCE
import com.app.kiranachoice.utils.addToCart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ProductsViewModel(private val dataRepository: DataRepository) : ViewModel() {
    private var dbRef: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var mAuth: FirebaseAuth? = null
    private var dbFire: FirebaseFirestore? = null


    val allCartItems = dataRepository.allCartItems

    init {
        mAuth = FirebaseAuth.getInstance()
        dbFire = FirebaseFirestore.getInstance()
    }

    private val fakeProductsList = ArrayList<ProductModel>()
    private var _productsList = MutableLiveData<List<ProductModel>>()
    val productsList: LiveData<List<ProductModel>> get() = _productsList


    fun getProductList(subCategoryKey: String): LiveData<List<Product>> {
        val result = MutableLiveData<List<Product>>()
        viewModelScope.launch {
            result.postValue(dataRepository.getProductsByCategoryKey(subCategoryKey))
        }
        return result

        /*dbRef.getReference(PRODUCT_REFERENCE)
            .orderByChild("sub_category_key").equalTo(subCategoryKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fakeProductsList.clear()
                    snapshot.children.forEach {
                        val productModel = it.getValue(ProductModel::class.java)
                        if (productModel != null) {
                            fakeProductsList.add(productModel)
                        }
                    }
                    _productsList.postValue(fakeProductsList)
                }

                override fun onCancelled(error: DatabaseError) {}

            })*/

    }

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
    ): Boolean {
        if (mAuth?.currentUser == null) {
            _navigateToAuthActivity.value = true
            return false
        } else {
            var added = false
            runBlocking {
                val result =
                    async { addToCart(dataRepository, product, packagingSizeModel, quantity) }
                if (result.await()) {
                    _productAdded.postValue(true)
                    added = true
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

    fun deleteCartItem(product: Product) = viewModelScope.launch {
        dataRepository.delete(product.product_key)
    }
}