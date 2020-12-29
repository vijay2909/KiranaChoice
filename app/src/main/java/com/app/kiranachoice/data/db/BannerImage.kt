package com.app.kiranachoice.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.kiranachoice.data.domain.Banner

@Entity
data class BannerImage(
    @PrimaryKey
    var key: String,
    var imageUrl: String
)


/**
 * Map BannerImage to domain entities
 */
fun List<BannerImage>.asDomainModel(): List<Banner> {
    return map {
        Banner(
            key = it.key,
            imageUrl = it.imageUrl
        )
    }
}
