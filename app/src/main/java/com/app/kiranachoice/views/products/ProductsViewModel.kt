package com.app.kiranachoice.views.products

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.db.CartDao
import com.app.kiranachoice.db.CartDatabase
import com.app.kiranachoice.db.CartItem
import com.app.kiranachoice.models.Category1Model
import com.app.kiranachoice.models.PackagingSizeModel
import com.app.kiranachoice.models.ProductModel
import com.app.kiranachoice.models.SubCategoryModel
import com.app.kiranachoice.repositories.CartRepo
import com.app.kiranachoice.utils.PRODUCT_REFERENCE
import com.app.kiranachoice.utils.addToCart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ProductsViewModel(application: Application) : ViewModel() {
    private var dbRef: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null
    private var dbFire: FirebaseFirestore? = null

    private val database: CartDatabase
    private val cartDao: CartDao
    private val cartRepo: CartRepo

    val allCartItems: LiveData<List<CartItem>>

    init {
        dbRef = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        dbFire = FirebaseFirestore.getInstance()

        database = CartDatabase.getInstance(application)
        cartDao = database.cartDao
        cartRepo = CartRepo(cartDao)

        allCartItems = cartRepo.allCartItems
    }

    private val fakeProductsList = ArrayList<ProductModel>()
    private var _productsList = MutableLiveData<List<ProductModel>>()
    val productsList: LiveData<List<ProductModel>> get() = _productsList

    fun getProductList(subCategoryModel: SubCategoryModel?, categoryModel : Category1Model?) {
        subCategoryModel?.let {
            dbRef?.getReference(PRODUCT_REFERENCE)
                ?.orderByChild("sub_category_key")?.equalTo(it.sub_category_Key)
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
        categoryModel?.let {
            dbRef?.getReference(PRODUCT_REFERENCE)
                ?.orderByChild("sub_category_key")?.equalTo(it.key)
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

    private var _alreadyAddedMsg = MutableLiveData<String>()
    val alreadyAddedMsg: LiveData<String> get() = _alreadyAddedMsg

    fun addItemToCart(
        productModel: ProductModel,
        packagingSizeModel: PackagingSizeModel,
        quantity: String
    ) : Boolean {
        if (mAuth?.currentUser == null) {
            _navigateToAuthActivity.value = true
            return false
        } else {
            var added = false
            runBlocking {
                val result = async { addToCart(cartRepo, productModel, packagingSizeModel, quantity) }
                if (result.await()){
                    _productAdded.postValue(true)
                    added = true
                } else{
                    _alreadyAddedMsg.postValue("Already added in cart.")
                }
            }

            return added
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

    fun deleteCartItem(productModel: ProductModel) = viewModelScope.launch(Dispatchers.IO) {
        cartRepo.delete(productModel.product_key.toString(), null)
        cartRepo.allCartItems
    }
}