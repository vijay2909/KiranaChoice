package com.app.kiranachoice.listeners

import com.app.kiranachoice.models.PackagingSizeModel
import com.app.kiranachoice.models.ProductModel

interface ProductClickListener {
    fun addItemToCart(productModel: ProductModel, packagingSizeModel: PackagingSizeModel, quantity: String)
    fun onItemClick(productModel: ProductModel)
}