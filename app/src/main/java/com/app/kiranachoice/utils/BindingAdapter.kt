package com.app.kiranachoice.utils

import android.graphics.Paint
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.app.kiranachoice.R
import com.bumptech.glide.Glide


@BindingAdapter("profileImage")
fun setProfileImage(view: ImageView, url: String?) {
    if (url != null) {
        Glide.with(view.context).load(url).into(view)
    } else {
        Glide.with(view.context).load(R.drawable.default_user_image).into(view)
    }
}

@BindingAdapter("userName")
fun setUserName(view: TextView , name: String?){
    if (name != null) {
        view.text = view.context.getString(R.string.hi_userName)
            .plus(" ${name.substringBefore(" ")}")
    }else {
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