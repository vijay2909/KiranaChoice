package com.app.kiranachoice.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.app.kiranachoice.db.CartItem
import com.app.kiranachoice.models.PackagingSizeModel
import com.app.kiranachoice.models.ProductModel
import com.app.kiranachoice.repositories.CartRepo
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.DecimalFormat

fun String.toPriceAmount(): String {
    val dec = DecimalFormat("##,##,###.00")
    return dec.format(this.toDouble())
}

suspend fun addToCart(
    cartRepo: CartRepo,
    productModel: ProductModel,
    packagingSizeModel: PackagingSizeModel,
    quantity: String
): Boolean {
    var addedSuccessfully = false
    val isAlreadyAdded = cartRepo.isAlreadyAdded(
        productModel.product_key.toString(),
        packagingSizeModel.packagingSize.toString()
    )
    if (!isAlreadyAdded) {
        val cartItem = CartItem(
            productModel.product_key.toString(),
            productModel.product_sku.toString(),
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


fun TextInputEditText.isNotNullOrEmpty(errorString: String): Boolean {
    val textInputLayout = this.parent.parent as TextInputLayout
    textInputLayout.errorIconDrawable = null
    this.onChange { textInputLayout.error = null }

    return if (this.text.toString().trim().isEmpty()) {
        textInputLayout.error = errorString
        false
    } else {
        true
    }
}

fun EditText.onChange(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            cb(s.toString())
        }
    })
}