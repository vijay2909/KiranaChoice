package com.app.kiranachoice.models

import java.io.Serializable

data class SubCategoryModel(
    var sequence: Long = 0,
    var category_key: String? = null,
    var category_name: String? = null,
    var sub_category_Key: String? = null,
    var sub_category_name: String? = null,
    var sub_category_image_url: String? = null
) : Serializable