package com.app.kiranachoice.data.network_models

import com.app.kiranachoice.data.database_models.SubCategoryItem
import java.io.Serializable


data class SubCategoryModel(
    var key: String = "",
    var categoryName: String = "",
    var image: String = "",
    var name: String = ""
) : Serializable


fun List<SubCategoryModel>.asDatabaseModel(): List<SubCategoryItem> {
    return map {
        SubCategoryItem(
            key = it.key,
            name = it.name,
            image = it.image,
            categoryName = it.categoryName
        )
    }
}