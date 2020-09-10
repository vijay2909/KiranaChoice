package com.app.kiranachoice.views.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.models.Category1Model
import com.app.kiranachoice.utils.CATEGORY_REFERENCE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel() {

    private var dbRef: FirebaseDatabase? = null

    init {
        dbRef = FirebaseDatabase.getInstance()
        getCategories()
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
                    Log.i(TAG, "categoryList: $fakeCategoryList")
                    _categoryList.postValue(fakeCategoryList)

                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }

}