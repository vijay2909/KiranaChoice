package com.app.kiranachoice.views.products

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.data.PackagingSizeModel
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.addToCart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class ProductDetailsViewModel(private val subCategoryName: String, private val dataRepository: DataRepository) : ViewModel() {

    val getProducts= object : MediatorLiveData<Pair<List<CartItem>, List<Product>>>() {
        var productsList: List<Product>? = null
        var cartItems : List<CartItem>? =null
        init {
            addSource(dataRepository.getProductsByCategoryName(subCategoryName)){ products ->
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

    fun addItemToCart(
        product: Product,
        packagingSizeModel: PackagingSizeModel,
        quantity: String
    ) = runBlocking {
        val result = async { addToCart(dataRepository, product, packagingSizeModel, quantity) }
        result.await()
    }

}