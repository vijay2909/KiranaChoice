package com.app.kiranachoice.data

import java.io.Serializable

data class OfferModel(
    var key: String? = null,
    var sequence: Long? = null,
    var detail: String? = null,
    var image: String? = null,
    var coverImage: String? = null,
    var discountRate: String? = null,
    var upToPrice: String? = null,
    var terms: List<Terms> = emptyList(),
    var validityDate: Long? = null,
    var couponCode: String? = null
) : Serializable

data class Terms(
    var term: String? = null
): Serializable