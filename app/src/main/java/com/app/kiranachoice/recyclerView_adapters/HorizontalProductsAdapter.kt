package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemHorizontolProductItemBinding
import com.app.kiranachoice.listeners.ProductClickListener
import com.app.kiranachoice.models.ProductModel


class HorizontalProductsAdapter(private val listener: ProductClickListener?) :
    RecyclerView.Adapter<HorizontalProductsAdapter.HorizontalProductsViewHolder>() {

    var list = listOf<ProductModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HorizontalProductsViewHolder {
        val view = ItemHorizontolProductItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        view.clickListener = listener
        return HorizontalProductsViewHolder(view)
    }

    override fun onBindViewHolder(holder: HorizontalProductsViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    class HorizontalProductsViewHolder(private val binding: ItemHorizontolProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(productModel: ProductModel) {
            binding.productModel = productModel
            binding.executePendingBindings()
        }
    }
}