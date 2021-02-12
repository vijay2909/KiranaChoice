package com.app.kiranachoice.data.network_models

import com.app.kiranachoice.data.database_models.ProductItem
import com.app.kiranachoice.data.database_models.SearchItem
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

data class ProductsList(val products: List<ProductModel>)

@IgnoreExtraProperties
data class ProductModel constructor(
    var key: String = "",
    var id: Int = 0,
    var subCategoryName: String = "",
    var product_sku: String = "",
    var image: String = "",
    var name: String = "",
    var packagingSize: List<PackagingSizeModel> = emptyList(),
    var aboutTheProduct: List<AboutProductModel> = emptyList(),
    var tag: List<String> = emptyList(),
    var totalQty: Int = 0,
    var minOrderQty: Int = 5,
    var makeBestOffer: Boolean = false,
    var makeBestSelling: Boolean = false,
    var makeRecommendedProduct: Boolean = false,
    var isAvailable: Boolean = true,
    var packagingIndex : Int = 0
)


fun ProductsList.asDatabaseModel(): List<SearchItem> {
    return products.map {
        SearchItem(
            categoryName = it.subCategoryName,
            productName = it.name.toLowerCase(Locale.getDefault()),
            tag = it.tag
        )
    }
}

/**
 * Convert Network results to database objects
 */
fun List<ProductModel>.asProductDatabaseModel(): List<ProductItem> {
    return map {
        ProductItem(
            key = it.key,
            id = it.id,
            subCategoryName = it.subCategoryName,
            product_sku = it.product_sku,
            image = it.image,
            name = it.name,
            packagingSize = it.packagingSize,
            aboutTheProduct = it.aboutTheProduct,
            totalQty = it.totalQty,
            minOrderQty = it.minOrderQty,
            makeBestOffer = it.makeBestOffer,
            makeBestSelling = it.makeBestSelling,
            makeRecommendedProduct = it.makeRecommendedProduct,
            isAvailable = it.isAvailable,
            packagingIndex = it.packagingIndex
        )
    }
}
