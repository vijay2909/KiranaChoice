package com.app.kiranachoice.repositories

import com.app.kiranachoice.db.CartDao
import com.app.kiranachoice.db.CartItem

class CartRepo(private val cartDao: CartDao) {

    val allCartItems = cartDao.getAllCartItem()

    suspend fun insert(cartItem: CartItem) = cartDao.insert(cartItem)

    suspend fun isAlreadyAdded(key: String, packagingSize: String) = cartDao.isAlreadyAdded(key, packagingSize)

    suspend fun delete(cartItem: CartItem) = cartDao.delete(cartItem)
}