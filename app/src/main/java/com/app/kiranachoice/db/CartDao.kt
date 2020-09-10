package com.app.kiranachoice.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CartDao {

    @Query("SELECT * from cart_item_table ORDER BY id DESC")
    fun getAllCartItem(): LiveData<List<CartItem>>

    @Query("SELECT 1 FROM cart_item_table WHERE productKey = :key and packagingSize = :packagingSize")
    suspend fun isAlreadyAdded(key: String, packagingSize: String) : Boolean

    @Insert
    suspend fun insert(cartItem: CartItem)

    @Update
    suspend fun update(cartItem: CartItem)

    @Delete
    suspend fun delete(cartItem: CartItem)
}