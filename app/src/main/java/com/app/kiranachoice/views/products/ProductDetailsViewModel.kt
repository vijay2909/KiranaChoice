package com.app.kiranachoice.views.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.data.network_models.PackagingSizeModel
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.data.domain.toCartItem
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
        Timber.d("categoryName: ${state.get<Product>("product")?.subCategoryName}")
    }

    val getProducts = dataRepository.getProductsByCategoryName(state.get<Product>("product")?.subCategoryName ?: "")


    fun addItemToCart(
        product: Product
    ) = viewModelScope.launch {
        addToCart(dataRepository, product)
    }

    fun removeProductFromCart(product: Product) = viewModelScope.launch {
        dataRepository.removeFromCart(product.toCartItem())
    }

    /**
     * update product quantity
     * */
    fun updateCartItemQuantity(product : Product) = viewModelScope.launch {
        dataRepository.updateCartItemQuantity(product)
    }

}