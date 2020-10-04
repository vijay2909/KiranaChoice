package com.app.kiranachoice.views.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.models.BannerImageModel
import com.app.kiranachoice.models.Category1Model
import com.app.kiranachoice.models.ProductModel
import com.app.kiranachoice.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel() {

    private var dbRef: FirebaseDatabase? = null

    init {
        dbRef = FirebaseDatabase.getInstance()
        getBanners()
        getCategories()
        getBestOfferProduct()
        getBestSellingProduct()
    }

    private var fakeBannersList = ArrayList<BannerImageModel>()
    private var _bannersList = MutableLiveData<List<BannerImageModel>>()
    val bannersList: LiveData<List<BannerImageModel>> get() = _bannersList

    private fun getBanners() {
        dbRef?.getReference(HOME_TOP_BANNER)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    fakeBannersList.clear()

                    snapshot.children.forEach { dataSnapshot ->
                        dataSnapshot.getValue(BannerImageModel::class.java)
                            ?.let { bannerImageModel ->
                                fakeBannersList.add(bannerImageModel)
                            }
                    }

                    _bannersList.postValue(fakeBannersList)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    private var fakeCategoryList = ArrayList<Category1Model>()
    private var _categoryList = MutableLiveData<List<Category1Model>>()
    val categoryList: LiveData<List<Category1Model>> get() = _categoryList

    private fun getCategories() {
        dbRef?.getReference(CATEGORY_REFERENCE)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    fakeCategoryList.clear()

                    snapshot.children.forEach {
                        val categoryModel = it.getValue(Category1Model::class.java)
                        categoryModel?.let { model -> fakeCategoryList.add(model) }
                    }

                    _categoryList.postValue(fakeCategoryList)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    private var fakeBestOfferProductList = ArrayList<ProductModel>()
    private var _bestOfferProductList = MutableLiveData<List<ProductModel>>()
    val bestOfferProductList: LiveData<List<ProductModel>> get() = _bestOfferProductList

    private fun getBestOfferProduct() {
        dbRef?.getReference(PRODUCT_REFERENCE)
            ?.orderByChild(BEST_OFFER_PRODUCT)?.equalTo(true)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fakeBestOfferProductList.clear()
                    snapshot.children.forEach {
                        it.getValue(ProductModel::class.java)
                            ?.let { model -> fakeBestOfferProductList.add(model) }
                    }
                    _bestOfferProductList.postValue(fakeBestOfferProductList)

                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    private var fakeBestSellingProductList = ArrayList<ProductModel>()
    private var _bestSellingProductList = MutableLiveData<List<ProductModel>>()
    val bestSellingProductList: LiveData<List<ProductModel>> get() = _bestSellingProductList

    private fun getBestSellingProduct() {
        dbRef?.getReference(PRODUCT_REFERENCE)
            ?.orderByChild(BEST_SELLING_PRODUCT)?.equalTo(true)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fakeBestSellingProductList.clear()
                    snapshot.children.forEach {
                        it.getValue(ProductModel::class.java)
                            ?.let { model -> fakeBestSellingProductList.add(model) }
                    }
                    _bestSellingProductList.postValue(fakeBestSellingProductList)

                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }

}