package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.ItemCartProductBinding
import com.app.kiranachoice.listeners.CartListener

class CartItemAdapter(private val listener: CartListener) :
    ListAdapter<Product, CartItemAdapter.CartItemViewHolder>(DiffUtilsCallBack()) {

    class DiffUtilsCallBack : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        return CartItemViewHolder(
            ItemCartProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    inner class CartItemViewHolder(val binding: ItemCartProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.product = product
            binding.executePendingBindings()
        }

        init {
            with(binding) {
                btnDecrease.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        getItem(adapterPosition)?.let { product ->
                            if (product.orderQuantity == 1) {
                                listener.removeCartItem(product)
                            } else {
                                --product.orderQuantity
                                notifyItemChanged(adapterPosition)
                                listener.onQuantityChange(product)
                            }
                        }
                    }
                }

                btnIncrease.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        getItem(adapterPosition)?.let { product ->
                            if (product.orderQuantity < product.minOrderQty) {
                                ++product.orderQuantity
                                notifyItemChanged(adapterPosition)
                                listener.onQuantityChange(product)
                            }
                        }
                    }
                }
            }
        }
    }

}