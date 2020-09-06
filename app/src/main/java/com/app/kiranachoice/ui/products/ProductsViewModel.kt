package com.app.kiranachoice.ui.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.models.ProductModel
import com.app.kiranachoice.models.SubCategoryModel
import com.app.kiranachoice.utils.CATEGORY_REFERENCE
import com.app.kiranachoice.utils.PRODUCT_REFERENCE
import com.app.kiranachoice.utils.SUB_CATEGORY_REFERENCE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductsViewModel : ViewModel() {
    private var dbRef: FirebaseDatabase? = null

    init {
        dbRef = FirebaseDatabase.getInstance()
    }

    private val fakeProductsList = ArrayList<ProductModel>()
    private var _productsList = MutableLiveData<List<ProductModel>>()
    val productsList: LiveData<List<ProductModel>> get() = _productsList

    fun getProductList(subCategoryModel: SubCategoryModel?) {
        subCategoryModel?.let {
            dbRef?.getReference(CATEGORY_REFERENCE)
                ?.child(subCategoryModel.category_key.toString())
                ?.child(SUB_CATEGORY_REFERENCE)
                ?.child(subCategoryModel.sub_category_Key.toString())
                ?.child(PRODUCT_REFERENCE)
                ?.addListenerForSingleValueEvent(object : ValueEventListener {
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
    }
}