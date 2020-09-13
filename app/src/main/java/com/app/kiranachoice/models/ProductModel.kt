package com.app.kiranachoice.models

import java.io.Serializable

data class ProductModel constructor(
    var listSequence: Long = 0,
    var sub_category_key: String? = null,
    var sub_category_name: String? = null,
    var product_key: String? = null,
    var product_sku: String? = null,
    var productImageUrl: String? = null,
    var productTitle: String? = null,
    var searchableText: List<SearchableTextModel> = emptyList(),
    var productPackagingSize: List<PackagingSizeModel> = emptyList(),
    var aboutTheProduct: List<AboutProductModel> = emptyList(),
    var totalQuantity: Long = 0,
    var minimumOrderQuantity: Long = 5,
    var isAvailable: Boolean = true
) : Serializable