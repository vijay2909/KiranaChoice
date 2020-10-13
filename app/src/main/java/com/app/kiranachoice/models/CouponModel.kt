package com.app.kiranachoice.models


data class CouponModel(
    var key: String? = null,
    var couponCode: String? = null,
    var couponDescription: String? = null,
    var upToPrice: String? = null,
    var discountRate: String? = null,
    var couponExpiryDate: String? = null,
    var termsList: List<String>? = null,
    var isActive : Boolean = true
)