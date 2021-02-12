package com.app.kiranachoice.data.domain

import com.app.kiranachoice.data.database_models.CartItem
import com.app.kiranachoice.data.network_models.AboutProductModel
import com.app.kiranachoice.data.network_models.PackagingSizeModel
import java.io.Serializable

data class Product constructor(
    var key: String,
    var id: Int,
    var subCategoryName: String,
    var image: String,
    var name: String,
    var packagingSize: List<PackagingSizeModel>,
    var aboutTheProduct: List<AboutProductModel>,
    var totalQty: Int,
    var minOrderQty: Int,
    var isAvailable: Boolean,
    var packagingIndex : Int = 0,
    var addedInCart : Boolean = false,
    var userOrderQuantity : Int = 1
): Serializable


fun Product.toCartItem() : CartItem {
    return CartItem(
        productKey = this.key,
        productId = this.id,
        productName = this.name,
        productImage = this.image,
        productMRP = this.packagingSize[this.packagingIndex].mrp!!.toInt(),
        productPrice = this.packagingSize[this.packagingIndex].price!!.toInt(),
        packagingSize = this.packagingSize[this.packagingIndex].size!!,
        minOrderQuantity = this.minOrderQty,
        quantity = this.userOrderQuantity,
        isEnabled = this.userOrderQuantity < this.minOrderQty
    )
}