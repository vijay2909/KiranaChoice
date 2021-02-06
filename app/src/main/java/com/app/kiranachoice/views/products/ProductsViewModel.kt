package com.app.kiranachoice.views.products

import android.util.Log
import androidx.lifecycle.*
import com.app.kiranachoice.data.PackagingSizeModel
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.addToCart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    init {
        Timber.d("subCategoryName: ${state.get<String>("title")}")
    }

//    private val products = dataRepository.getProductsByCategoryName(subCategoryName)

    fun getCartItems() = dataRepository.getCartItems()

    val products = dataRepository.getProductsByCategoryName(state.get<String>("title") ?: "")

    /*val getProducts= object : MediatorLiveData<Pair<List<CartItem>, List<Product>>>() {
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
*/

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

    fun deleteCartItem(productKey: String) = viewModelScope.launch {
        dataRepository.removeFromCart(productKey)
    }

    /**
     * update product quantity
     * */
    fun updateQuantity(productKey: String, quantity: Int) = viewModelScope.launch {
        dataRepository.updateCartItemQuantity(productKey, quantity)
    }
}