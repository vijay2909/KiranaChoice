package com.app.kiranachoice.models

data class ProductModel(
    var listSequence: Long = 0,
    var category_key: String? = null,
    var sub_category_key: String? = null,
    var product_key: String? = null,
    var product_sku: String? = null,
    var productImageUrl: String? = null,
    var productTitle: String? = null,
    var productMRP: Long = 0,
    var productPrice: Long = 0,
    var productPackagingSize: List<PackagingSizeModel> = emptyList(),
    var discount: Long = 0,
    var totalQuantity: Long = 0,
    var userOrderQuantity: Long = 0,
    var minimumOrderQuantity: Long = 5,
    var isAvailable: Boolean = true
)