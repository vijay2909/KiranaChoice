package com.app.kiranachoice.listeners

import com.app.kiranachoice.data.db.CartItem

interface CartListener {
    fun removeCartItem(cartItem: CartItem)
    fun onQuantityChange(
        cartItem: CartItem,
        quantity : Int
    )
}