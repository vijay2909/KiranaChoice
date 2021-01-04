package com.app.kiranachoice.views.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.data.SubCategoryModel
import com.app.kiranachoice.data.domain.Category
import com.app.kiranachoice.repositories.DataRepository
import com.app.kiranachoice.utils.CATEGORY_REFERENCE
import com.app.kiranachoice.utils.SUB_CATEGORY_REFERENCE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CategoryViewModel(private val categoryName: String, private val dataRepository: DataRepository) : ViewModel() {

    val subCategories = dataRepository.getSubCategories(categoryName)

    init {
        viewModelScope.launch { dataRepository.refreshSubCategories() }
    }


    private var fakeSubCategoryList = ArrayList<SubCategoryModel>()
    private var _subCategoryList = MutableLiveData<List<SubCategoryModel>>()
    val subCategoryList: LiveData<List<SubCategoryModel>> get() = _subCategoryList

    fun getSubCategories(category: Category) {
        /*dbRef?.getReference(CATEGORY_REFERENCE)
            ?.child(category.key)
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
            })*/
    }

}