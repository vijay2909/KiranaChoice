package com.app.kiranachoice.views.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.data.database_models.asDomainModel
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.data.domain.toCartItem
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.addToCart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val dataRepository: DataRepository) : ViewModel() {

    val totalCartItems = dataRepository.totalCartItems

    init {
        refreshDataFromRepository()
    }


    private var _eventNetworkError = MutableLiveData(false)
    val eventNetworkError: LiveData<Boolean> get() = _eventNetworkError

    val banners = dataRepository.banners

    val firstCategories = dataRepository.firstCategories

    val secondCategories = dataRepository.secondCategories

//    val bestOfferProducts = dataRepository.bestOfferProducts

    suspend fun bestSellingProducts() = withContext(Dispatchers.IO) {
        dataRepository.bestSellingProducts().asDomainModel()
    }

    /*val bestSellingProducts = object : MediatorLiveData<Pair<List<CartItem>, List<Product>>>(){
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
    }*/


    suspend fun bestOfferProducts() = withContext(Dispatchers.IO) {
        dataRepository.bestOfferProducts().asDomainModel()
    }

    /*val bestOfferProducts = object : MediatorLiveData<Pair<List<CartItem>, List<Product>>>(){
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
    }*/

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

    fun addItemToCart(product: Product) {
        viewModelScope.launch { addToCart(dataRepository, product) }
    }


    fun authActivityNavigated() {
        _navigateToAuthActivity.value = false
    }

    fun productAddedSuccessful() {
        _productAdded.value = false
    }

    suspend fun getCartItems() = withContext(Dispatchers.IO) {
        dataRepository.getCartItems()
    }

    fun getProduct(productId: String) = dataRepository.getProduct(productId)


    fun removeProductFromCart(product: Product) = viewModelScope.launch {
        dataRepository.removeFromCart(product.toCartItem())
    }


    /**
     * update product quantity
     * */
    fun updateCartItemQuantity(product: Product) = viewModelScope.launch {
        dataRepository.updateCartItemQuantity(product)
    }
}