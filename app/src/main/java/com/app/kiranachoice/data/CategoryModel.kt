package com.app.kiranachoice.data

import com.app.kiranachoice.data.db.CategoryItem

data class CategoryModel(
    var key: String? = null,
    var category_name: String? = null,
    var category_image_url: String? = null
)


/**
 * Convert Network results to database objects
 */
fun List<CategoryModel>.asDatabaseModel(): List<CategoryItem> {
    return map {
        CategoryItem(
            key = it.key.toString(),
            category_name = it.category_name.toString(),
            category_image_url = it.category_image_url.toString()
        )
    }
}