package com.app.kiranachoice.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.kiranachoice.data.SearchWord

@Entity
data class SearchItem(
    @PrimaryKey
    val productName: String,
    val categoryName: String,
    val tag : List<String>
)

fun List<SearchItem>.asDomainModel() : List<SearchWord> {
    return map {
        SearchWord(
            productName = it.productName,
            categoryName = it.categoryName
        )
    }
}