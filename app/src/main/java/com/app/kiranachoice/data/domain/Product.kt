package com.app.kiranachoice.data.domain

import com.app.kiranachoice.data.AboutProductModel
import com.app.kiranachoice.data.PackagingSizeModel
import java.io.Serializable

data class Product constructor(
    var listSequence: Long,
    var sub_category_key: String,
    var sub_category_name: String,
    var product_key: String,
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
): Serializable