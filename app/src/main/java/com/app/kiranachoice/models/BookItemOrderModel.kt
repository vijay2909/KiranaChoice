package com.app.kiranachoice.models

data class Product(
    val productSKU : String,
    val productName : String,
    val productImage: String,
    val productSize : String,
    val productQuantity : String,
    val productMRP: String,
    val productPrice: String
)

data class BookItemOrderModel (
    val key : String,
    val productList : List<Product>,
    // total amount without delivery charge
    val totalAmount : String,
    val deliveryCharge: String,
    val deliveryAddress: String,
    val orderPlacedDate: String,
    val couponCode : String?,
    val isCouponApplied : Boolean
)