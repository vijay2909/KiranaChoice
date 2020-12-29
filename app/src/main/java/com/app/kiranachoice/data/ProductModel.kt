package com.app.kiranachoice.data

import com.app.kiranachoice.data.db.BannerImage
import com.app.kiranachoice.data.db.ProductItem
import com.app.kiranachoice.data.db.SearchItem
import java.io.Serializable
import java.util.*

data class ProductsList(val products: List<ProductModel>)

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
)


fun ProductsList.asDatabaseModel(): List<SearchItem> {
    return products.map {
        SearchItem(
            subCategoryKey = it.sub_category_key!!,
            categoryName = it.sub_category_name!!,
            productName = it.productTitle?.toLowerCase(Locale.getDefault())!!
        )
    }
}

/**
 * Convert Network results to database objects
 */
fun List<ProductModel>.asProductDatabaseModel(): List<ProductItem> {
    return map {
        ProductItem(
            product_key = it.product_key.toString(),
            listSequence = it.listSequence,
            sub_category_key = it.sub_category_key.toString(),
            sub_category_name = it.sub_category_name.toString(),
            product_sku = it.product_sku.toString(),
            productImageUrl = it.productImageUrl.toString(),
            productTitle = it.productTitle.toString(),
            productPackagingSize = it.productPackagingSize,
            aboutTheProduct = it.aboutTheProduct,
            totalQuantity = it.totalQuantity,
            minimumOrderQuantity = it.minimumOrderQuantity,
            makeBestOffer = it.makeBestOffer,
            makeBestSelling = it.makeBestSelling,
            makeRecommendedProduct = it.makeRecommendedProduct,
            isAvailable = it.isAvailable
        )
    }
}
