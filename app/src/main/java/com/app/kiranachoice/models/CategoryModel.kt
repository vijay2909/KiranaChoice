package com.app.kiranachoice.models

import java.io.Serializable

data class CategoryModel (
    var key: String? = null,
    var category_name: String? = null,
    var category_image_url: String? = null
) : Serializable