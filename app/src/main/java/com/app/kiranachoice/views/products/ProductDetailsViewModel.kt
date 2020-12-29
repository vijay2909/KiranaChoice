package com.app.kiranachoice.views.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.data.PackagingSizeModel
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.addToCart
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ProductDetailsViewModel(private val dataRepository: DataRepository) : ViewModel() {

    val cartItems = dataRepository.allCartItems

    fun getProductList(subCategoryName: String): LiveData<List<Product>> {
        val result = MutableLiveData<List<Product>>()
        viewModelScope.launch {
            result.postValue(dataRepository.getProductsByCategoryName(subCategoryName))
        }
        return result
        /*dbRef.getReference(PRODUCT_REFERENCE)
            .orderByChild("sub_category_name").equalTo(subCategoryName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fakeProductsList.clear()
                    snapshot.children.forEach {
                        val productModel = it.getValue(ProductModel::class.java)
                        if (productModel != null) {
                            fakeProductsList.add(productModel)
                        }
                    }
                    _productsList.postValue(fakeProductsList.asProductDatabaseModel())
                }

                override fun onCancelled(error: DatabaseError) {}

            })*/
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