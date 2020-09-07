package com.app.kiranachoice.views.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.kiranachoice.models.Category1Model
import com.app.kiranachoice.models.SubCategoryModel
import com.app.kiranachoice.utils.CATEGORY_REFERENCE
import com.app.kiranachoice.utils.SUB_CATEGORY_REFERENCE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoryViewModel : ViewModel() {
    private var dbRef: FirebaseDatabase? = null

    init {
        dbRef = FirebaseDatabase.getInstance()
    }


    private var fakeSubCategoryList = ArrayList<SubCategoryModel>()
    private var _subCategoryList = MutableLiveData<List<SubCategoryModel>>()
    val subCategoryList: LiveData<List<SubCategoryModel>> get() = _subCategoryList

    fun getSubCategories(categoryModel: Category1Model) {
        dbRef?.getReference(CATEGORY_REFERENCE)
            ?.child(categoryModel.key.toString())
            ?.child(SUB_CATEGORY_REFERENCE)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    fakeSubCategoryList.clear()
                    snapshot.children.forEach { snap ->
                        val data = snap.getValue(SubCategoryModel::class.java)
                        data?.let { d -> fakeSubCategoryList.add(d) }
                    }
                    _subCategoryList.postValue(fakeSubCategoryList)
                }
            })
    }

}