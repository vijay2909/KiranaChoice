package com.app.kiranachoice.utils

import android.graphics.Paint
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.app.kiranachoice.R
import com.app.kiranachoice.models.ProductModel
import com.bumptech.glide.Glide
import java.text.DecimalFormat


@BindingAdapter("profileImage")
fun setProfileImage(view: ImageView, url: String?) {
    if (url != null) {
        Glide.with(view.context).load(url).into(view)
    } else {
        Glide.with(view.context).load(R.drawable.default_user_image).into(view)
    }
}

@BindingAdapter("userName")
fun setUserName(view: TextView, name: String?) {
    if (name != null) {
        view.text = view.context.getString(R.string.hi_userName)
            .plus(" ${name.substringBefore(" ")}")
    } else {
        view.text = view.context.getString(R.string.hi_user)
    }
}

@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, url: String?) {
    if (url != null) {
        Glide.with(view.context)
            .load(url)
            .placeholder(R.drawable.placeholder_image)
            .into(view)
    }
}

@BindingAdapter("productMRP")
fun setProductMRP(view: TextView, text: String?) {
    view.paintFlags = view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    view.text = view.context.getString(R.string.rupee).plus(text)
        .plus(view.context.getString(R.string.dot_zero))
}

@BindingAdapter("sizes")
fun setSizes(spinner: Spinner, productModel: ProductModel) {
    val packagingSize = arrayOfNulls<String>(productModel.productPackagingSize.size)

    for (i in productModel.productPackagingSize.indices) {
        packagingSize[i] = productModel.productPackagingSize[i].packagingSize.toString()
    }

    val arrayAdapter = ArrayAdapter(
        spinner.context,
        R.layout.spinner_item,
        packagingSize
    )

    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinner.adapter = arrayAdapter
}


@BindingAdapter("priceFormatted")
fun setPriceFormatted(view: TextView, value: String?) {
    if (value != DELIVERY_FREE && value != null) {
        val dec = DecimalFormat("##,##,###.00")
        val formattedPrice = dec.format(value.toDouble())
        view.text = view.context.getString(R.string.rupee).plus(formattedPrice)
    } else {
        view.text = value
    }
}