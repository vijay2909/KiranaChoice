package com.app.kiranachoice.listeners

import com.app.kiranachoice.data.domain.Product

interface ProductClickListener {
    fun addItemToCart(product: Product,
                      packagingSize: Int,
                      quantity: String,
                      position: Int)
    fun onItemClick(product: Product)
    fun onRemoveProduct(productKey: String)
    fun onQuantityChanged(product: Product/*productKey: String, quantity: String*/)
}