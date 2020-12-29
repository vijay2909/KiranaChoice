package com.app.kiranachoice.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_item_table")
data class CartItem(
    @PrimaryKey
    var productKey: String,
    var productSKU: String,
    var productTitle: String,
    var productImageUrl: String,
    var productMRP: String,
    var productPrice: String,
    var packagingSize: String,
    var quantity: String
)