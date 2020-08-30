package com.app.kiranachoice.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.models.CategoryModel
import com.app.kiranachoice.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel() {

    private var dbRef : FirebaseDatabase? = null

    init {
        dbRef = FirebaseDatabase.getInstance()
        getCategories()
    }

    private var fakeCategoryList = ArrayList<CategoryModel>()
    private var _categoryList = MutableLiveData<List<CategoryModel>>()
    val categoryList: LiveData<List<CategoryModel>> get() = _categoryList

    private fun getCategories() {
        Log.i(TAG, "getCategories: called")
        dbRef?.getReference(Constants.CATEGORY_REFERENCE)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.i(TAG, "onDataChange: called")
                    fakeCategoryList.clear()
                    snapshot.children.forEach {
                        val categoryModel = it.getValue(CategoryModel::class.java)
                        categoryModel?.let { model -> fakeCategoryList.add(model) }
                    }
                    Log.i(TAG, "categoryList : $fakeCategoryList")
                    _categoryList.postValue(fakeCategoryList)

                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }

}