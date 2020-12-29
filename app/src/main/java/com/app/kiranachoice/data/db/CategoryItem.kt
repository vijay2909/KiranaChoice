package com.app.kiranachoice.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.kiranachoice.data.domain.Banner
import com.app.kiranachoice.data.domain.Category

@Entity
data class CategoryItem(
    @PrimaryKey
    var key: String,
    var category_name: String,
    var category_image_url: String
)

/**
 * Map BannerImage to domain entities
 */
fun List<CategoryItem>.asDomainModel(): List<Category> {
    return map {
        Category(
            key = it.key,
            category_name = it.category_name,
            category_image_url = it.category_image_url
        )
    }
}

