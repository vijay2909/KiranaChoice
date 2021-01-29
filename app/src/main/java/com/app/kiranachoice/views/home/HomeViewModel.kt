package com.app.kiranachoice.views.home

import androidx.lifecycle.*
import com.app.kiranachoice.data.PackagingSizeModel
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.addToCart
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeViewModel(private val dataRepository: DataRepository) : ViewModel() {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val totalCartItems = dataRepository.totalCartItems

    init {

        refreshDataFromRepository()
        /*getCategory2()*/
//        dataRepository.getCategories()
    }


    private var _eventNetworkError = MutableLiveData(false)
    val eventNetworkError: LiveData<Boolean> get() = _eventNetworkError

    val banners = dataRepository.banners

    val firstCategories = dataRepository.firstCategories

    val secondCategories = dataRepository.secondCategories

//    val bestOfferProducts = dataRepository.bestOfferProducts

    val bestSellingProducts = object : MediatorLiveData<Pair<List<CartItem>, List<Product>>>(){
        var cartItems : List<CartItem>? = null
        var products : List<Product>? = null
        init {
            addSource(dataRepository.bestSellingProducts){products ->
                this.products = products
                cartItems?.let {
                    value = it to products
                }
            }
            addSource(dataRepository.allCartItems){ cartItems ->
                this.cartItems = cartItems
                products?.let {
                    value = cartItems to it
                }
            }
        }
    }


    val bestOfferProducts = object : MediatorLiveData<Pair<List<CartItem>, List<Product>>>(){
        var cartItems : List<CartItem>? = null
        var products : List<Product>? = null
        init {
            addSource(dataRepository.bestOfferProducts){ products ->
                this.products = products
                cartItems?.let {
                    value = it to products
                }
            }
            addSource(dataRepository.allCartItems){ cartItems ->
                this.cartItems = cartItems
                products?.let {
                    value = cartItems to it
                }
            }
        }
    }

    private fun refreshDataFromRepository() {
        viewModelScope.launch {
            dataRepository.refreshBanners()
            dataRepository.refreshCategories()
            dataRepository.refreshProducts()
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
        packagingSizeModel: PackagingSizeModel
    ) {
        viewModelScope.launch {
            addToCart(dataRepository, product, packagingSizeModel)
        }
    }


    fun authActivityNavigated() {
        _navigateToAuthActivity.value = false
    }

    fun productAddedSuccessful() {
        _productAdded.value = false
    }


    fun getProduct(productId: String)= dataRepository.getProduct(productId)


    fun removeProductFromCart(productKey: String) = viewModelScope.launch {
        dataRepository.delete(productKey)
    }


    /**
     * update product quantity
     * */
    fun updateQuantity(productKey: String, quantity: String) = viewModelScope.launch {
        dataRepository.update(productKey, quantity)
    }
}