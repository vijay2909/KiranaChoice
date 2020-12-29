package com.app.kiranachoice.data

import com.app.kiranachoice.data.db.BannerImage


data class BannerImageModel(
    var key: String? = null,
    var imageUrl: String? = null
)


/**
 * Convert Network results to database objects
 */
fun List<BannerImageModel>.asDatabaseModel(): List<BannerImage> {
    return map {
        BannerImage(
            key = it.key.toString(),
            imageUrl = it.imageUrl.toString()
        )
    }
}