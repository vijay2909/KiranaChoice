package com.app.kiranachoice.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.app.kiranachoice.models.CartItem

@Dao
interface ProductDao {

    @Query("SELECT * from product_table")
    suspend fun getAllCartItems(): LiveData<List<CartItem>>

    @Insert
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)
}