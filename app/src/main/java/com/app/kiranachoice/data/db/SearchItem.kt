package com.app.kiranachoice.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.kiranachoice.data.SearchWord

@Entity
data class SearchItem(
    @PrimaryKey
    val productName: String,
    val subCategoryKey: String,
    val categoryName: String
)

fun List<SearchItem>.asDomainModel() : List<SearchWord> {
    return map {
        SearchWord(
            productName = it.productName,
            key = it.subCategoryKey,
            categoryName = it.categoryName
        )
    }
}