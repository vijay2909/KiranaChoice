package com.app.kiranachoice.models

import java.io.Serializable

data class ProductModel(
    val productId: String? = null,
    val productKey: String? = null,
    val category: String? = null,
    val productImageUrl: String? = null,
    val productTitle: String? = null,
    val productMRP: String? = null,
    val productPrice: String? = null,
    val productTotalQuantity: String? = null,
    val productSellingQuantity : String? = null
)