package com.app.kiranachoice.views.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.kiranachoice.repositories.DataRepository

@Suppress("UNCHECKED_CAST")
class ProductViewModelFactory(private val dataRepository: DataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
            return ProductsViewModel(dataRepository) as T
        } else if (modelClass.isAssignableFrom(ProductDetailsViewModel::class.java)) {
            return ProductDetailsViewModel(dataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}