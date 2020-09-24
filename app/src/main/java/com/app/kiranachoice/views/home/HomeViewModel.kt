package com.app.kiranachoice.views.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.models.BannerImageModel
import com.app.kiranachoice.models.Category1Model
import com.app.kiranachoice.utils.CATEGORY_REFERENCE
import com.app.kiranachoice.utils.HOME_TOP_BANNER
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
    }

    private var fakeBannersList = ArrayList<BannerImageModel>()
    private var _bannersList = MutableLiveData<List<BannerImageModel>>()
    val bannersList: LiveData<List<BannerImageModel>> get() = _bannersList

    private fun getBanners() {
        dbRef?.getReference(HOME_TOP_BANNER)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    fakeBannersList.clear()

                    snapshot.children.forEach{ dataSnapshot ->
                        dataSnapshot.getValue(BannerImageModel::class.java)?.let {bannerImageModel ->
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

    companion object {
        private const val TAG = "HomeViewModel"
    }

}