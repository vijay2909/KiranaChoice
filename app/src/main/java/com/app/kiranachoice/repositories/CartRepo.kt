package com.app.kiranachoice.repositories

import com.app.kiranachoice.db.Product
import com.app.kiranachoice.db.ProductDao

class CartRepo(private val productDao: ProductDao) {

    suspend fun insert(product: Product) {
        productDao.insert(product)
    }
}