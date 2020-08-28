package com.app.kiranachoice.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemHorizontolProductItemBinding
import com.app.kiranachoice.models.ProductModel


class ProductsAdapter(private val list: List<ProductModel>?) :
    RecyclerView.Adapter<ProductsAdapter.ProductsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        return ProductsViewHolder(
            ItemHorizontolProductItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
//        holder.bind(list[position])
    }

    override fun getItemCount(): Int = 8

    class ProductsViewHolder(private val binding: ItemHorizontolProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ProductModel) {

        }
    }
}