package com.app.kiranachoice.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_item_table")
data class CartItem(
    @PrimaryKey
    var productKey: String,
    var productId: Int,
    var productSKU: String,
    var productName: String,
    var productImage: String,
    var productMRP: String,
    var productPrice: String,
    var packagingSize: String,
    var minOrderQuantity: Long,
    var quantity: String
)