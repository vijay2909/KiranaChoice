package com.app.kiranachoice.data.domain

import java.io.Serializable

data class Category(
    val key: String,
    val category_name: String,
    val category_image_url: String
) : Serializable