package com.app.kiranachoice.views.searchPage

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kiranachoice.db.CartDatabase
import com.app.kiranachoice.models.SearchWord
import com.app.kiranachoice.repositories.SearchRepository
import kotlinx.coroutines.launch
import java.io.IOException

class SearchViewModel(application: Application) : ViewModel() {

    private val searchRepo = SearchRepository(CartDatabase.getInstance(application))

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