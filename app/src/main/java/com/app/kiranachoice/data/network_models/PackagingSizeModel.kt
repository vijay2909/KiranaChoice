package com.app.kiranachoice.data.network_models

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class PackagingSizeModel(
    var mrp : String? = null,
    var size : String? = null,
    var price: String? = null,
    var discount: String? = null
) : Serializable