package com.app.kiranachoice.models

import java.io.Serializable

data class ProductModel constructor(
    var listSequence: Long? = null,
    var sub_category_key: String? = null,
    var product_key: String? = null,
    var product_sku: String? = null,
    var productImageUrl: String? = null,
    var productTitle: String? = null,
    var searchableText: String? = null,
    var productPackagingSize: List<PackagingSizeModel> = emptyList(),
    var aboutTheProduct: List<AboutProductModel> = emptyList(),
    var totalQuantity: Long = 0,
    var minimumOrderQuantity: Long = 5,
    var isAvailable: Boolean = true
) : Serializable