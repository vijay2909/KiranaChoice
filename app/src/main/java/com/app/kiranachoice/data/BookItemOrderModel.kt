package com.app.kiranachoice.data

import com.app.kiranachoice.utils.AWAITING_PICKUP
import java.io.Serializable

data class Product(
    val productSKU : String? = null,
    val productName : String? = null,
    val productImage: String? = null,
    val productSize : String? = null,
    val productQuantity : Int? = null,
    val productMRP: String? = null,
    val productPrice: String? = null,
    val status : String = AWAITING_PICKUP
)

data class BookItemOrderModel(
    val key: String? = null,
    val userUid: String? = null,
    val productList: List<Product>? = null,
    // total amount without delivery charge
    val invoiceAmount: String? = null,
    val deliveryCharge: String? = null,
    val deliveryAddress: String? = null,
    val orderId: String? = null,
    val orderPlacedDate: Long? = null,
    val couponCode: String? = null,
    val couponApplied: Boolean? = null,
    val status: String? = AWAITING_PICKUP
) : Serializable