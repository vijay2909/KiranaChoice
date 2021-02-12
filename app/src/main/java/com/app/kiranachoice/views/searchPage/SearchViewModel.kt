package com.app.kiranachoice.views.searchPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.data.network_models.SearchWord
import com.app.kiranachoice.repositories.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(searchRepository: SearchRepository) : ViewModel() {

    private val searchRepo = searchRepository

    val allSearchWords = searchRepo.allSearchWords

    fun getSearchWords(query : String) = searchRepo.getSearchWords(query)

    init {
        refreshDataFromRepository()
    }

    private fun refreshDataFromRepository() = viewModelScope.launch {
        try {
            searchRepo.refreshProducts()
        }catch (networkError : IOException){

        }
    }

    private val _navigateToSelectedProduct = MutableLiveData<SearchWord>()
    val navigateToSelectedProduct: LiveData<SearchWord>
        get() = _navigateToSelectedProduct

    fun showSearchProducts(searchWord: SearchWord) {
        _navigateToSelectedProduct.value = searchWord
    }

    fun showSearchProductsComplete() {
        _navigateToSelectedProduct.value = null
    }
}