package com.app.kiranachoice.models

import com.app.kiranachoice.db.Product

data class CartItem (
    val product: Product,
    val quantity : Int
)