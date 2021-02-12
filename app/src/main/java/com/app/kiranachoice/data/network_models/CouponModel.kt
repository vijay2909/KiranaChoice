package com.app.kiranachoice.data.network_models


data class AppliedCouponModel(
    var key: String? = null,
    var couponCode: String? = null,
    var appliedDate: Long? = null,
)


data class CouponModel(
    var key: String? = null,
    var couponCode: String? = null,
    var couponDescription: String? = null,
    var upToPrice: String? = null,
    var discountRate: String? = null,
    var couponExpiryDate: Long? = null,
    var termsList: List<String>? = null,
    var isActive: Boolean = true
)