package com.app.kiranachoice.models

import com.app.kiranachoice.utils.ORDER_PLACED
import java.io.Serializable

data class Product(
    val productSKU : String? = null,
    val productName : String? = null,
    val productImage: String? = null,
    val productSize : String? = null,
    val productQuantity : String? = null,
    val productMRP: String? = null,
    val productPrice: String? = null,
    val status : String = ORDER_PLACED
)

data class BookItemOrderModel(
    val key: String? = null,
    val productList: List<Product>? = null,
    // total amount without delivery charge
    val invoiceAmount: String? = null,
    val deliveryCharge: String? = null,
    val deliveryAddress: String? = null,
    val orderId: String? = null,
    val orderPlacedDate: String? = null,
    val couponCode: String? = null,
    val isCouponApplied: Boolean? = null,
    val status: String? = ORDER_PLACED
) : Serializable