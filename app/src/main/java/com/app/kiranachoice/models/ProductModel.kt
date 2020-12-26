package com.app.kiranachoice.models

import com.app.kiranachoice.db.SearchItem
import java.io.Serializable
import java.util.*

data class ProductsList(val products : List<ProductModel>)

data class ProductModel constructor(
    var listSequence: Long = 0,
    var sub_category_key: String? = null,
    var sub_category_name: String? = null,
    var product_key: String? = null,
    var product_sku: String? = null,
    var productImageUrl: String? = null,
    var productTitle: String? = null,
    var productPackagingSize: List<PackagingSizeModel> = emptyList(),
    var aboutTheProduct: List<AboutProductModel> = emptyList(),
    var totalQuantity: Long = 0,
    var minimumOrderQuantity: Long = 5,
    var makeBestOffer: Boolean = false,
    var makeBestSelling: Boolean = false,
    var makeRecommendedProduct: Boolean = false,
    var isAvailable: Boolean = true
): Serializable


fun ProductsList.asDatabaseModel() : List<SearchItem> {
    return products.map {
        SearchItem(
            subCategoryKey = it.sub_category_key!!,
            categoryName = it.sub_category_name!!,
            productName = it.productTitle?.toLowerCase(Locale.getDefault())!!
        )
    }
}