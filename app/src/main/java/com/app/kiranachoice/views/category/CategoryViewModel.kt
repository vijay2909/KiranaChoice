package com.app.kiranachoice.views.category

import androidx.lifecycle.*
import com.app.kiranachoice.data.network_models.SubCategoryModel
import com.app.kiranachoice.data.domain.Category
import com.app.kiranachoice.repositories.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    val subCategories = dataRepository.getSubCategories(state.get<String>("categoryName") ?: "")

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