package com.app.kiranachoice.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CartDao {

    @Query("SELECT * from cart_item_table ORDER BY id DESC")
    fun getAllCartItem(): LiveData<List<CartItem>>

    @Query("SELECT EXISTS(SELECT * FROM cart_item_table WHERE productKey = :key AND packagingSize = :packagingSize)")
    suspend fun isAlreadyAdded(key: String, packagingSize: String) : Boolean

    @Insert
    suspend fun insert(cartItem: CartItem)

    @Query("UPDATE cart_item_table SET quantity=:quantity WHERE productTitle = :productName")
    suspend fun update(productName: String, quantity: String)

    @Query("DELETE FROM cart_item_table WHERE productKey = :key")
    suspend fun delete(key: String)
}