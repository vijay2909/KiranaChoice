package com.app.kiranachoice.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DatabaseDao {

    /**
     * Get Cart Items
     * */
    @Query("SELECT * FROM productitem WHERE addedInCart = 1")
    fun getCartItems() : LiveData<List<ProductItem>>

    /**
     * Add To Cart
     * */
    @Query("UPDATE productitem SET addedInCart = 1 WHERE `key` = :productKey")
    suspend fun addToCart(productKey: String)

    /**
     * Delete Product From Cart
     * */
    @Query("UPDATE productitem SET addedInCart = 0 WHERE `key` = :productKey")
    suspend fun removeFromCart(productKey: String)


    /**
     * Update Cart Product Quantity
     * */
    @Query("UPDATE productitem SET orderQuantity = :quantity WHERE `key` = :productKey")
    suspend fun updateCartItemQuantity(productKey: String, quantity: Int)

    /**
     * Get Total Cart Items Size
     * */
    @Query("SELECT COUNT(*) FROM productitem WHERE addedInCart = 1")
    fun getTotalCartItems(): LiveData<Int>


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
    fun getCategories(index: Int): LiveData<List<CategoryItem>>

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

    @Query("SELECT * FROM productitem WHERE subCategoryName =:subCategoryName AND isAvailable = 1 ORDER BY id ASC")
    fun getProductBySubCategoryName(subCategoryName: String): LiveData<List<ProductItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCategories(subCategories: List<SubCategoryItem>)

    @Query("SELECT * FROM subcategoryitem WHERE categoryName =:categoryName ")
    fun getSubCategories(categoryName: String): LiveData<List<SubCategoryItem>>

    @Query("DELETE FROM productitem")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM subcategoryitem")
    suspend fun deleteAllSubCategories()

    @Query("DELETE FROM categoryitem")
    suspend fun deleteAllCategories()

    @Query("DELETE FROM bannerimage")
    suspend fun deleteAllBanners()
}
