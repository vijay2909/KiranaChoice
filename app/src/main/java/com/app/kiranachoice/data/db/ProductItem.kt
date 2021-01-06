package com.app.kiranachoice.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.kiranachoice.data.AboutProductModel
import com.app.kiranachoice.data.PackagingSizeModel
import com.app.kiranachoice.data.domain.Product

@Entity
data class ProductItem constructor(
    @PrimaryKey
    var key: String,
    var id: Int,
    var subCategoryName: String,
    var product_sku: String,
    var image: String,
    var name: String,
    var packagingSize: List<PackagingSizeModel>,
    var aboutTheProduct: List<AboutProductModel>,
    var totalQty: Long,
    var minOrderQty: Long,
    var makeBestOffer: Boolean,
    var makeBestSelling: Boolean,
    var makeRecommendedProduct: Boolean,
    var isAvailable: Boolean
)

fun ProductItem.asDomainModel(): Product {
    return Product(
        key = this.key,
        id = this.id,
        subCategoryName = this.subCategoryName,
        product_sku = this.product_sku,
        image = this.image,
        name = this.name,
        packagingSize = this.packagingSize,
        aboutTheProduct = this.aboutTheProduct,
        totalQty = this.totalQty,
        minOrderQty = this.minOrderQty,
        makeBestOffer = this.makeBestOffer,
        makeBestSelling = this.makeBestSelling,
        makeRecommendedProduct = this.makeRecommendedProduct,
        isAvailable = this.isAvailable
    )
}

/**
 * Map ProductItem to domain entities
 */
fun List<ProductItem>.asDomainModel(): List<Product> {
    return map {
        Product(
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
            isAvailable = it.isAvailable
        )
    }
}
