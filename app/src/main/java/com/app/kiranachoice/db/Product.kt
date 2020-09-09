package com.app.kiranachoice.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.kiranachoice.models.PackagingSizeModel

@Entity(tableName = "Product_table")
data class Product(
    var listSequence: Long = 0,
    var sub_category_key: String? = null,
    var product_key: String? = null,
    var product_sku: String? = null,
    var productImageUrl: String? = null,
    var productTitle: String? = null,
    var searchableText: String? = null,
    var productPackagingSize: List<PackagingSizeModel> = emptyList(),
    var totalQuantity: Long = 0,
    var minimumOrderQuantity: Long = 5,
    var isAvailable: Boolean = true
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}