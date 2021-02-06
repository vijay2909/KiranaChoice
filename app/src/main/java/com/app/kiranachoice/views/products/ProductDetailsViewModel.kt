package com.app.kiranachoice.views.products

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.data.PackagingSizeModel
import com.app.kiranachoice.data.db.ProductItem
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.addToCart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    init {
        Timber.d("categoryName: ${state.get<String>("title")}")
    }

    val getProducts = dataRepository.getProductsByCategoryName(state.get<String>("title") ?: "")

    /*val getProducts = object : MediatorLiveData<Pair<List<ProductItem>, List<Product>>>() {
        var productsList: List<Product>? = null
        var cartItems: List<ProductItem>? = null

        init {
            addSource(
                dataRepository.getProductsByCategoryName(
                    state.get<String>("title") ?: ""
                )
            ) { products ->
                productsList = products
                cartItems?.let {
                    value = it to products
                }
            }
            addSource(dataRepository.allCartItems) { cartItems ->
                this.cartItems = cartItems
                productsList?.let {
                    value = cartItems to it
                }
            }
        }
    }*/

    fun addItemToCart(
        product: Product,
        packagingSizeModel: PackagingSizeModel
    ) = viewModelScope.launch {
        addToCart(dataRepository, product, packagingSizeModel)
    }

}