package com.app.kiranachoice.views.products

import android.app.Application
import androidx.lifecycle.*
import com.app.kiranachoice.db.CartDatabase
import com.app.kiranachoice.models.PackagingSizeModel
import com.app.kiranachoice.models.ProductModel
import com.app.kiranachoice.repositories.CartRepo
import com.app.kiranachoice.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ProductDetailsViewModel(application: Application) : ViewModel() {
    private val database = CartDatabase.getInstance(application)
    private val cartRepo = CartRepo(database.cartDao)

    val cartItems = cartRepo.allCartItems

    private var dbRef: FirebaseDatabase = FirebaseDatabase.getInstance()


    private val fakeProductsList = ArrayList<ProductModel>()
    private var _productsList = MutableLiveData<List<ProductModel>>()
    val productsList: LiveData<List<ProductModel>> get() = _productsList

    fun getProductList(subCategoryName: String) {
        dbRef.getReference(PRODUCT_REFERENCE)
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
                    _productsList.postValue(fakeProductsList)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    fun addItemToCart(
        productModel: ProductModel,
        packagingSizeModel: PackagingSizeModel,
        quantity: String
    ) = runBlocking {
        val result = async { addToCart(cartRepo, productModel, packagingSizeModel, quantity) }
        result.await()
    }

}