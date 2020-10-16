package com.app.kiranachoice.listeners

import com.app.kiranachoice.models.PackagingSizeModel
import com.app.kiranachoice.models.ProductModel

interface ProductClickListener {
    fun addItemToCart(productModel: ProductModel,
                      packagingSize: Int,
                      quantity: String,
                      position: Int)
    fun onItemClick(productModel: ProductModel)
}