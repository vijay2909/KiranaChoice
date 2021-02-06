package com.app.kiranachoice.listeners

import com.app.kiranachoice.data.domain.Product

interface CartListener {
    fun removeCartItem(product: Product)
    fun onQuantityChange(product: Product)
}