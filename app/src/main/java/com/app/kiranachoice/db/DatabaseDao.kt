package com.app.kiranachoice.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.app.kiranachoice.data.database_models.*

@Dao
interface DatabaseDao {

    /**
     * Get Cart Items
     * */
    @Query("SELECT * FROM cartitem")
    suspend fun getCartItems() : List<CartItem>

    /**
     * Add To Cart
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(cartItem: CartItem)

    /**
     * Delete Product From Cart
     * */
    @Delete
    suspend fun removeFromCart(cartItem: CartItem)

    /**
     * Get Total Cart Items Size
     * */
    @Query("SELECT COUNT(*) FROM cartitem")
    fun getTotalCartItems(): LiveData<Int>

    /**
     * Get Sub Total Of Cart Items
     * */
    @Query("SELECT SUM(productPrice * quantity) FROM cartitem")
    fun getSubTotal() : LiveData<String>

    /**
     * Get Saved Amount of Cart Items
     * */
    @Query("SELECT SUM((productMRP - productPrice) * quantity) FROM cartitem WHERE productMRP > productPrice")
    fun getSavedPrice() : LiveData<Int>

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

    @Query("select * from productitem WHERE makeBestOffer = 1 AND isAvailable = 1 ORDER BY id ASC")
    suspend fun getBestOfferProducts(): List<ProductItem>

    @Query("select * from productitem WHERE makeBestSelling = 1 AND isAvailable = 1 ORDER BY id ASC")
    suspend fun getBestSellingProducts(): List<ProductItem>

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
