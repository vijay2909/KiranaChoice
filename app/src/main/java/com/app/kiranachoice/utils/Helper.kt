package com.app.kiranachoice.utils

import com.app.kiranachoice.db.CartItem
import com.app.kiranachoice.models.PackagingSizeModel
import com.app.kiranachoice.models.ProductModel
import com.app.kiranachoice.repositories.CartRepo
import java.text.DecimalFormat

fun String.toPriceAmount() : String {
    val dec = DecimalFormat("##,##,###.00")
    return dec.format(this.toDouble())
}

suspend fun addToCart(
    cartRepo : CartRepo,
    productModel: ProductModel,
    packagingSizeModel: PackagingSizeModel,
    quantity: String
) : Boolean {
    var addedSuccessfully = false
    val isAlreadyAdded = cartRepo.isAlreadyAdded(
        productModel.product_key.toString(),
        packagingSizeModel.packagingSize.toString()
    )
    if (!isAlreadyAdded) {
        val cartItem = CartItem(
            productModel.product_key.toString(),
            productModel.productTitle.toString(),
            productModel.productImageUrl.toString(),
            packagingSizeModel.mrp.toString(),
            packagingSizeModel.price.toString(),
            packagingSizeModel.packagingSize.toString(),
            quantity
        )
        cartRepo.insert(cartItem)
        addedSuccessfully = true
    }
    return addedSuccessfully
}