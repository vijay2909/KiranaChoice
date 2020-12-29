package com.app.kiranachoice.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DatabaseDao {

    @Query("SELECT * from cart_item_table")
    fun getAllCartItem(): LiveData<List<CartItem>>

    @Query("SELECT EXISTS(SELECT * FROM cart_item_table WHERE productKey = :key AND packagingSize = :packagingSize)")
    suspend fun isAlreadyAdded(key: String, packagingSize: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cartItem: CartItem): Long

    @Query("DELETE FROM cart_item_table WHERE productKey = :key")
    suspend fun delete(key: String)

    @Delete
    suspend fun delete(cartItem: CartItem)

    @Update
    suspend fun update(cartItem: CartItem): Int

    @Query("UPDATE cart_item_table SET quantity= :quantity WHERE productKey = :productKey")
    suspend fun update(productKey: String, quantity: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchItems(searchItems: List<SearchItem>)

    @Query("SELECT * FROM searchitem")
    fun getAllSearchWords(): LiveData<List<SearchItem>>

    @Query("SELECT * FROM searchitem  WHERE productName LIKE :query")
    fun getSearchWords(query: String): LiveData<List<SearchItem>>

    @Query("select * from bannerimage")
    fun getBanners(): LiveData<List<BannerImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanners(banners: List<BannerImage>)

    @Query("select * from categoryitem")
    fun getCategories(): LiveData<List<CategoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryItem>)

    @Query("select * from productitem WHERE makeBestOffer = :value")
    fun getBestOfferProducts(value: Boolean = true): LiveData<List<ProductItem>>

    @Query("select * from productitem WHERE makeBestSelling = :value")
    fun getBestSellingProducts(value: Boolean = true): LiveData<List<ProductItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductItem>)

    @Query("SELECT * FROM productitem WHERE product_key=:productKey ")
    suspend fun getProduct(productKey: String): ProductItem

    @Query("SELECT * FROM productitem WHERE sub_category_name =:subCategoryName ")
    suspend fun getProductBySubCategoryName(subCategoryName: String): List<ProductItem>

    @Query("SELECT * FROM productitem WHERE sub_category_key =:subCategoryKey ")
    suspend fun getProductBySubCategoryKey(subCategoryKey: String): List<ProductItem>
}
