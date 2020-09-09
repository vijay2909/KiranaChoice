package com.app.kiranachoice.views.products

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.db.Product
import com.app.kiranachoice.db.ProductDao
import com.app.kiranachoice.db.ProductDatabase
import com.app.kiranachoice.models.CartItem
import com.app.kiranachoice.models.SubCategoryModel
import com.app.kiranachoice.repositories.CartRepo
import com.app.kiranachoice.utils.PRODUCT_REFERENCE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductsViewModel(val application: Application) : ViewModel() {
    private var dbRef: FirebaseDatabase? = null

    private val database : ProductDatabase
    private val productDao : ProductDao
    private val cartRepo : CartRepo

    init {
        dbRef = FirebaseDatabase.getInstance()
        database = ProductDatabase.getInstance(application)
        productDao = database.productDao
        cartRepo = CartRepo(productDao)
    }

    private val fakeProductsList = ArrayList<Product>()
    private var _productsList = MutableLiveData<List<Product>>()
    val productsList: LiveData<List<Product>> get() = _productsList

    fun getProductList(subCategoryModel: SubCategoryModel?) {
        subCategoryModel?.let {
            dbRef?.getReference(PRODUCT_REFERENCE)
                ?.orderByChild("sub_category_key")?.equalTo(subCategoryModel.sub_category_Key)
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        fakeProductsList.clear()
                        snapshot.children.forEach {
                            val productModel = it.getValue(Product::class.java)
                            if (productModel != null) {
                                fakeProductsList.add(productModel)
                            }
                        }
                        _productsList.postValue(fakeProductsList)
                    }

                    override fun onCancelled(error: DatabaseError) {}

                })
        }
    }

    fun insert(product: Product) {
        viewModelScope.launch(Dispatchers.IO){
            insert(product)
        }
    }
}