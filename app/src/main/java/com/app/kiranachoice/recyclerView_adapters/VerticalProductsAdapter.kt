package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemVerticalProductListBinding
import com.app.kiranachoice.models.PackagingSizeModel
import com.app.kiranachoice.models.ProductModel
import com.google.android.material.snackbar.Snackbar


class VerticalProductsAdapter(private val listener: ProductListener) :
    RecyclerView.Adapter<VerticalProductsAdapter.VerticalProductViewHolder>() {

    var data = listOf<ProductModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalProductViewHolder {
        return VerticalProductViewHolder.from(parent)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: VerticalProductViewHolder, position: Int) {
        holder.bind(data[position], listener)

        holder.binding.btnIncrease.setOnClickListener {
            var quantity = Integer.parseInt(holder.binding.userQuantity.text.toString())
            if (quantity < 5) ++quantity else Snackbar.make(
                holder.binding.root,
                "You can't get maximum 5 quantity.",
                Snackbar.LENGTH_SHORT
            ).show()
            holder.binding.quantity = quantity
        }

        holder.binding.btnDecrease.setOnClickListener {
            var quantity = Integer.parseInt(holder.binding.userQuantity.text.toString())
            --quantity
            if (quantity == 0) {
                holder.binding.btnAddToCart.visibility = View.VISIBLE
                holder.binding.quantityLayout.visibility = View.GONE
            }else {
                holder.binding.userQuantity.text = quantity.toString()
            }
        }
        
        holder.binding.btnAddToCart.setOnClickListener {
            holder.binding.btnAddToCart.visibility = View.GONE
            holder.binding.quantityLayout.visibility = View.VISIBLE
            val packagingModel = if (data[position].productPackagingSize.size > 1) {
                val packagingSize = holder.binding.spinnerPackaging.selectedItemPosition
                data[position].productPackagingSize[packagingSize]
            } else {
                data[position].productPackagingSize[0]
            }
            listener.addItemToCart(data[position], packagingModel, holder.binding.userQuantity.text.toString())
        }
    }

    class VerticalProductViewHolder private constructor(val binding: ItemVerticalProductListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(productModel: ProductModel, listener: ProductListener) {
            binding.productListener = listener
            binding.productModel = productModel
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): VerticalProductViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemVerticalProductListBinding.inflate(layoutInflater, parent, false)
                return VerticalProductViewHolder(binding)
            }
        }
    }

    interface ProductListener {
        fun addItemToCart(productModel: ProductModel, packagingSizeModel: PackagingSizeModel,  quantity: String)
        fun onItemClick(productModel: ProductModel)
    }
}