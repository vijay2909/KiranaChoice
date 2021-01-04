package com.app.kiranachoice.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.kiranachoice.data.domain.Category

/*@Entity
data class CategoryItem(
    @PrimaryKey
    val key: String,
    val category_name: String,
    val category_image_url: String,
    val index: Int
)*/
@Entity
data class CategoryItem(
    @PrimaryKey
    var key: String,
    var id : Int,
    var index: Int,
    var name: String,
    var image: String
)

/**
 * Map BannerImage to domain entities
 */
/*
fun List<CategoryItem>.asDomainModel(): List<Category> {
    return map {
        Category(
            key = it.key,
            category_name = it.category_name,
            category_image_url = it.category_image_url
        )
    }
}
*/

fun List<CategoryItem>.asDomainModel(): List<Category> {
    return map {
        Category(
            key = it.key,
            id = it.id,
            index = it.index,
            name = it.name,
            image = it.image
        )
    }
}

