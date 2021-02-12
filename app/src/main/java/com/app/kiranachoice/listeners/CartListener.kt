package com.app.kiranachoice.listeners

import com.app.kiranachoice.data.database_models.CartItem
import com.app.kiranachoice.data.domain.Product

interface CartListener {
    fun removeCartItem(cartItem: CartItem)
    fun onQuantityChange(cartItem: CartItem)
}