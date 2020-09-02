package com.app.kiranachoice.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemHorizontolProductItemBinding
import com.app.kiranachoice.models.ProductModel


class HorizontalProductsAdapter(private val list: List<ProductModel>?) :
    RecyclerView.Adapter<HorizontalProductsAdapter.HorizontalProductsViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HorizontalProductsViewHolder {
        return HorizontalProductsViewHolder(
            ItemHorizontolProductItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HorizontalProductsViewHolder, position: Int) {
//        holder.bind(list[position])
    }

    override fun getItemCount(): Int = 8

    class HorizontalProductsViewHolder(private val binding: ItemHorizontolProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ProductModel) {

        }
    }
}