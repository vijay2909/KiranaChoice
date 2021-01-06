package com.app.kiranachoice.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DatabaseDao {

    @Query("SELECT * from cart_item_table")
    fun getAllCartItem(): LiveData<List<CartItem>>

    @Query("SELECT EXISTS(SELECT * FROM cart_item_table WHERE productKey = :productKey AND packagingSize = :packagingSize)")
    suspend fun isAlreadyAdded(productKey: String, packagingSize: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cartItem: CartItem): Long

    @Query("DELETE FROM cart_item_table WHERE productKey = :productKey")
    suspend fun delete(productKey: String)

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

    @Query("SELECT * FROM searchitem  WHERE productName LIKE :query OR categoryName LIKE :query OR tag Like :query")
    fun getSearchWords(query: String): LiveData<List<SearchItem>>

    @Query("select * from bannerimage")
    fun getBanners(): LiveData<List<BannerImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanners(banners: List<BannerImage>)

    @Query("select * from categoryitem WHERE `index` = :index")
    fun getCategories(index : Int): LiveData<List<CategoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryItem>)

    @Query("select * from productitem WHERE makeBestOffer = :value AND isAvailable = :value ORDER BY id ASC")
    fun getBestOfferProducts(value: Boolean = true): LiveData<List<ProductItem>>

    @Query("select * from productitem WHERE makeBestSelling = :value AND isAvailable = :value ORDER BY id ASC")
    fun getBestSellingProducts(value: Boolean = true): LiveData<List<ProductItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductItem>)

    @Query("SELECT * FROM productitem WHERE `key`=:productKey")
    fun getProduct(productKey: String): LiveData<ProductItem>

    @Query("SELECT * FROM productitem WHERE subCategoryName =:subCategoryName AND isAvailable = :value ORDER BY id ASC")
    fun getProductBySubCategoryName(subCategoryName: String, value: Boolean = true): LiveData<List<ProductItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCategories(subCategories: List<SubCategoryItem>)

    @Query("SELECT * FROM subcategoryitem WHERE categoryName =:categoryName ")
    fun getSubCategories(categoryName: String): LiveData<List<SubCategoryItem>>

    @Query("SELECT COUNT(*) FROM cart_item_table")
    fun getTotalCartItems(): LiveData<Int>

    @Query("DELETE FROM productitem")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM subcategoryitem")
    suspend fun deleteAllSubCategories()

    @Query("DELETE FROM categoryitem")
    suspend fun deleteAllCategories()

    @Query("DELETE FROM bannerimage")
    suspend fun deleteAllBanners()
}
