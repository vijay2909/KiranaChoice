package com.app.kiranachoice.models

import java.io.Serializable

data class PackagingSizeModel(
    var packagingSize: String? = null,
    var mrp : String? = null,
    var price: String? = null,
    var discount: String? = null
) : Serializable