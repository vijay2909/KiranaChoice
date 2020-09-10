package com.app.kiranachoice.views.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.models.CartItem
import com.app.kiranachoice.models.PackagingSizeModel
import com.app.kiranachoice.models.ProductModel
import com.app.kiranachoice.models.SubCategoryModel
import com.app.kiranachoice.utils.CART_PRODUCTS
import com.app.kiranachoice.utils.PRODUCT_REFERENCE
import com.app.kiranachoice.utils.USER_REFERENCE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class ProductsViewModel : ViewModel() {
    private var dbRef: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null
    private var dbFire: FirebaseFirestore? = null

    init {
        dbRef = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        dbFire = FirebaseFirestore.getInstance()
    }

    private val fakeProductsList = ArrayList<ProductModel>()
    private var _productsList = MutableLiveData<List<ProductModel>>()
    val productsList: LiveData<List<ProductModel>> get() = _productsList

    fun getProductList(subCategoryModel: SubCategoryModel?) {
        subCategoryModel?.let {
            dbRef?.getReference(PRODUCT_REFERENCE)
                ?.orderByChild("sub_category_key")?.equalTo(subCategoryModel.sub_category_Key)
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

    private var _navigateToAuthActivity = MutableLiveData<Boolean>()
    val navigateToAuthActivity: LiveData<Boolean> get() = _navigateToAuthActivity

    private var _productAdded = MutableLiveData<Boolean>()
    val productAdded: LiveData<Boolean> get() = _productAdded

    fun addItemToCart(productModel: ProductModel, packagingSizeModel: PackagingSizeModel, quantity: String) {
        if (mAuth?.currentUser == null) {
            _navigateToAuthActivity.value = true
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                val key = UUID.randomUUID().toString()
                dbFire?.collection(USER_REFERENCE)?.document(mAuth?.currentUser!!.uid)
                    ?.collection(CART_PRODUCTS)?.get()?.addOnSuccessListener {
                        var sequence = it.documents.size

                        val cartItem = CartItem(
                            ++sequence,
                            productModel.product_key,
                            key,
                            productModel.productTitle,
                            productModel.productImageUrl,
                            packagingSizeModel.mrp,
                            packagingSizeModel.price,
                            packagingSizeModel.packagingSize,
                            quantity
                        )

                        dbFire?.collection(USER_REFERENCE)?.document(mAuth?.currentUser!!.uid)
                            ?.collection(CART_PRODUCTS)
                            ?.document(key)?.set(cartItem)
                            ?.addOnSuccessListener {
                                _productAdded.postValue(true)
                            }

                    }
            }
        }
    }

    companion object {
        private const val TAG = "ProductsViewModel"
    }

    fun authActivityNavigated() {
        _navigateToAuthActivity.value = false
    }

    fun productAddedSuccessful() {
        _productAdded.value = false
    }
}