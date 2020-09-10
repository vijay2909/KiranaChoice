package com.app.kiranachoice.models

data class CartItem(
    val sequence: Int = 1,
    var productKey: String? = null,
    var itemKey: String? = null,
    var productTitle: String? = null,
    var productImageUrl: String? = null,
    var productMRP : String? =null,
    var productPrice: String? = null,
    var packagingSize: String? = null,
    var quantity: String? = null
)