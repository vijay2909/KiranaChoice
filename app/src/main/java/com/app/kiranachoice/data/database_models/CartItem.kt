package com.app.kiranachoice.data.database_models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.kiranachoice.data.network_models.Product

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

fun CartItem.asNetworkProduct() : Product {
    return Product(
        null,
        productName = this.productName,
        productImage = this.productImage,
        productSize = this.packagingSize,
        productQuantity = this.quantity,
        productMRP = this.productMRP.toString(),
        productPrice = this.productPrice.toString()
    )
}