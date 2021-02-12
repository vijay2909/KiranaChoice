package com.app.kiranachoice.data.database_models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.kiranachoice.data.network_models.AboutProductModel
import com.app.kiranachoice.data.network_models.PackagingSizeModel
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
    var totalQty: Int,
    var minOrderQty: Int,
    var makeBestOffer: Boolean,
    var makeBestSelling: Boolean,
    var makeRecommendedProduct: Boolean,
    var isAvailable: Boolean,
    var packagingIndex : Int = 0
)

fun ProductItem.asDomainModel(): Product {
    return Product(
        key = this.key,
        id = this.id,
        subCategoryName = this.subCategoryName,
        image = this.image,
        name = this.name,
        packagingSize = this.packagingSize,
        aboutTheProduct = this.aboutTheProduct,
        totalQty = this.totalQty,
        minOrderQty = this.minOrderQty,
        isAvailable = this.isAvailable,
        packagingIndex = this.packagingIndex
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
            image = it.image,
            name = it.name,
            packagingSize = it.packagingSize,
            aboutTheProduct = it.aboutTheProduct,
            totalQty = it.totalQty,
            minOrderQty = it.minOrderQty,
            isAvailable = it.isAvailable,
            packagingIndex = it.packagingIndex
        )
    }
}
