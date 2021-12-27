package com.app.kiranachoice.utils

import com.app.kiranachoice.data.network_models.CouponModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SingletonData @Inject constructor() {

    var couponModel : CouponModel? = null
}