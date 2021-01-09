package com.app.kiranachoice.utils

import android.net.ConnectivityManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.app.kiranachoice.data.PackagingSizeModel
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.repositories.DataRepository
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

fun String.toPriceAmount(): String {
    val dec = DecimalFormat("##,##,###.00")
    return dec.format(this.toDouble())
}


suspend fun addToCart(
    dataRepository: DataRepository,
    product: Product,
    packagingSizeModel: PackagingSizeModel,
    quantity: String
) {
    val cartItem = CartItem(
        product.key,
        product.id,
        product.product_sku,
        product.name,
        product.image,
        packagingSizeModel.mrp.toString(),
        packagingSizeModel.price.toString(),
        packagingSizeModel.size.toString(),
        quantity
    )
    dataRepository.insert(cartItem)
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


fun getDateTimeFromUnix(unix: Long?) : String {
    val date = unix?.let { Date(it) }
    val sdf = SimpleDateFormat("dd MMM, yyyy hh:mm a", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("GMT+5:30")
    return sdf.format(date!!)
}


fun getDateFromUnix(unix: Long?) : String?{
    val date = unix?.let { Date(it) }
    val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("GMT+5:30")
    return date?.let { sdf.format(it) }
}

