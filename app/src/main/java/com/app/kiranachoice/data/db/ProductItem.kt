package com.app.kiranachoice.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.kiranachoice.data.AboutProductModel
import com.app.kiranachoice.data.PackagingSizeModel
import com.app.kiranachoice.data.domain.Product

@Entity
data class ProductItem constructor(
    @PrimaryKey
    var product_key: String,
    var listSequence: Long,
    var sub_category_key: String,
    var sub_category_name: String,
    var product_sku: String,
    var productImageUrl: String,
    var productTitle: String,
    var productPackagingSize: List<PackagingSizeModel>,
    var aboutTheProduct: List<AboutProductModel>,
    var totalQuantity: Long,
    var minimumOrderQuantity: Long,
    var makeBestOffer: Boolean,
    var makeBestSelling: Boolean,
    var makeRecommendedProduct: Boolean,
    var isAvailable: Boolean
)


/**
 * Map ProductItem to domain entities
 */
fun List<ProductItem>.asDomainModel(): List<Product> {
    return map {
        Product(
            product_key = it.product_key,
            listSequence = it.listSequence,
            sub_category_key = it.sub_category_key,
            sub_category_name = it.sub_category_name,
            product_sku = it.product_sku,
            productImageUrl = it.productImageUrl,
            productTitle = it.productTitle,
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
