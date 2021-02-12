package com.app.kiranachoice.data.database_models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.kiranachoice.data.domain.SubCategory

/*
data class SubCategoryModel(
    var sequence: Long = 0,
    var category_key: String? = null,
    var category_name: String? = null,
    var sub_category_Key: String? = null,
    var sub_category_name: String? = null,
    var sub_category_image_url: String? = null
) : Serializable*/

@Entity
data class SubCategoryItem(
    @PrimaryKey
    val key: String,
    val name: String,
    val categoryName: String,
    val image: String
)


fun List<SubCategoryItem>.asDomainModel(): List<SubCategory> {
    return map {
        SubCategory(
            key = it.key,
            name = it.name,
            categoryName = it.categoryName,
            image = it.image
        )
    }
}
