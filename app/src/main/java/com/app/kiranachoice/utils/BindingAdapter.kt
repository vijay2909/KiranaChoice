package com.app.kiranachoice.utils

import android.graphics.Paint
import android.media.Image
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.R
import com.app.kiranachoice.models.AddressModel
import com.app.kiranachoice.models.ProductModel
import com.app.kiranachoice.models.SearchWord
import com.app.kiranachoice.recyclerView_adapters.SearchResultsAdapter
import com.app.kiranachoice.recyclerView_adapters.SimilarProductsAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.storage.FirebaseStorage
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


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
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_placeholder)
            )
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

@BindingAdapter("address")
fun setAddress(view: MaterialTextView, addressModel: AddressModel) {
    val address =
        addressModel.flat_num_or_building_name + " " + addressModel.area + " " + addressModel.city +
                " " + addressModel.state + " " + addressModel.pincode
    view.text = address
}

@BindingAdapter("totalProducts")
fun setTotalProducts(view: TextView, size: String) {
    view.text = view.context.resources.getQuantityString(
        R.plurals.total_products,
        size.toInt(),
        size.toInt()
    )
}

@BindingAdapter("unixToDateTime")
fun setUnixToDateTime(view: TextView, unix: Long) {
    view.text = getDateTimeFromUnix(unix)
}

@BindingAdapter("unixToDate")
fun setUnixToDate(view: TextView, unix: Long) {
    view.text = getDateFromUnix(unix)
}

@BindingAdapter("title")
fun setTitle(view: TextView, title: String) {
    view.text = title.split(' ').joinToString(" ") { it.capitalize() }
}

@BindingAdapter("searchWords")
fun setWords(recyclerView: RecyclerView, words: List<SearchWord>?) {
    val adapter = recyclerView.adapter as SearchResultsAdapter
    recyclerView.addItemDecoration(
        DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
    )
    adapter.submitList(words)
}

@BindingAdapter("time")
fun setTime(view: TextView, time: Long) {
    val simpleDateFormat = SimpleDateFormat("hh:mm aaa", Locale.getDefault())
    view.text = simpleDateFormat.format(Date(time))

}

@BindingAdapter("productListImage")
fun setListImage(view: ImageView, url: String?) {
    url?.let {
        if (it.startsWith("gs://")) {
            val storageReference =
                FirebaseStorage.getInstance().getReferenceFromUrl(it)
            storageReference.downloadUrl.addOnSuccessListener { u ->
                Glide.with(view.context).load(u).into(view)
            }
        } else {
            Glide.with(view.context).load(it).into(view)
        }
    }
}