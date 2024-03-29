package com.app.kiranachoice.data.network_models

import com.app.kiranachoice.data.database_models.CategoryItem
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class CategoryModel(
    var key: String = "",
    var id: Int = 0,
    var index: Int = 0,
    var name: String = "",
    var image: String = ""
)

fun List<CategoryModel>.asDatabaseModel(): List<CategoryItem> {
    return map {
        CategoryItem(
            key = it.key,
            id = it.id,
            name = it.name,
            image = it.image,
            index = it.index
        )
    }
}
