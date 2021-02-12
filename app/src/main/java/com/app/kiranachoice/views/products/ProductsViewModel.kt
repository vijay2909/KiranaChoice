package com.app.kiranachoice.views.products

import androidx.lifecycle.*
import com.app.kiranachoice.data.network_models.PackagingSizeModel
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.data.domain.toCartItem
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.addToCart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    suspend fun getCartItems() = withContext(Dispatchers.IO) { dataRepository.getCartItems() }

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


    fun addItemToCart(product: Product) =
        viewModelScope.launch { addToCart(dataRepository, product) }


    fun authActivityNavigated() {
        _navigateToAuthActivity.value = false
    }

    fun productAddedSuccessful() {
        _productAdded.value = false
    }

    fun deleteCartItem(product: Product) = viewModelScope.launch {
        dataRepository.removeFromCart(product.toCartItem())
    }

    /**
     * update product quantity
     * */
    fun updateQuantity(product: Product) = viewModelScope.launch {
        dataRepository.updateCartItemQuantity(product)
    }
}