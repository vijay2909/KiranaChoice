package com.app.kiranachoice.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.app.kiranachoice.R
import com.bumptech.glide.Glide

@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, url: String?) {
    if (url != null) {
        Glide.with(view.context)
            .load(url)
            .placeholder(R.drawable.placeholder_image)
            .into(view)
    }
}