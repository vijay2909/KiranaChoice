package com.app.kiranachoice.data.domain

import com.app.kiranachoice.data.AboutProductModel
import com.app.kiranachoice.data.PackagingSizeModel
import java.io.Serializable

data class Product constructor(
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
): Serializable