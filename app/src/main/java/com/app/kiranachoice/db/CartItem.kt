package com.app.kiranachoice.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_item_table")
data class CartItem(
    var productKey: String,
    var productSKU: String,
    var productTitle: String,
    var productImageUrl: String,
    var productMRP: String,
    var productPrice: String,
    var packagingSize: String,
    var quantity: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}