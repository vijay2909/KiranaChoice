package com.app.kiranachoice.listeners

import android.view.View
import com.app.kiranachoice.data.domain.Product

interface ProductClickListener {
    fun addItemToCart(product: Product)
    fun onItemClick(view: View, product: Product)
    fun onRemoveProduct(product: Product)
    fun onQuantityChanged(product: Product/*productKey: String, quantity: String*/)
}