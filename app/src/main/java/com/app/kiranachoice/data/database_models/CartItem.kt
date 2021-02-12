package com.app.kiranachoice.data.database_models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CartItem(
    @PrimaryKey(autoGenerate = false)
    var productKey: String,
    var productId: Int,
    var productName: String,
    var productImage: String,
    var productMRP: Int,
    var productPrice: Int,
    var packagingSize: String,
    var minOrderQuantity: Int,
    var quantity: Int,
    var isEnabled: Boolean = true
)