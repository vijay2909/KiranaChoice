package com.app.kiranachoice.views.products

import android.util.Log
import androidx.lifecycle.*
import com.app.kiranachoice.data.PackagingSizeModel
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.addToCart
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProductsViewModel(subCategoryName: String, private val dataRepository: DataRepository) : ViewModel() {

    init {
        Log.d("ProductsViewModel", "subCategoryName: $subCategoryName")
    }
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val products = dataRepository.getProductsByCategoryName(subCategoryName)

    val getProducts= object : MediatorLiveData<Pair<List<CartItem>, List<Product>>>() {
            var productsList: List<Product>? = null
            var cartItems : List<CartItem>? =null
            init {
                addSource(products){ products ->
                    productsList = products
                    cartItems?.let {
                        value = it to products
                    }
                }
                addSource(dataRepository.allCartItems){ cartItems ->
                    this.cartItems = cartItems
                    productsList?.let {
                        value = cartItems to it
                    }
                }
            }
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
    ) {
        if (mAuth.currentUser == null) {
            _navigateToAuthActivity.value = true
        } else {
            viewModelScope.launch {
                addToCart(dataRepository, product, packagingSizeModel, quantity)
            }
        }
    }


    fun authActivityNavigated() {
        _navigateToAuthActivity.value = false
    }

    fun productAddedSuccessful() {
        _productAdded.value = false
    }

    fun deleteCartItem(productKey: String) = viewModelScope.launch {
        dataRepository.delete(productKey)
    }

    /**
     * update product quantity
     * */
    fun updateQuantity(productKey: String, quantity: String) = viewModelScope.launch {
        dataRepository.update(productKey, quantity)
    }
}