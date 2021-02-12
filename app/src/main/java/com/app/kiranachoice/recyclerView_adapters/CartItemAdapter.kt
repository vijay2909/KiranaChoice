package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.data.database_models.CartItem
import com.app.kiranachoice.databinding.ItemCartProductBinding
import com.app.kiranachoice.listeners.CartListener

class CartItemAdapter(private val listener: CartListener) :
    ListAdapter<CartItem, CartItemAdapter.CartItemViewHolder>(DiffUtilsCallBack()) {

    class DiffUtilsCallBack : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
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

        fun bind(cartItem: CartItem) {
            binding.cartItem = cartItem
            binding.executePendingBindings()
        }

        init {
            with(binding) {
                btnDecrease.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        getItem(adapterPosition)?.let { cartItem ->
                            if (cartItem.quantity == 1) {
                                listener.removeCartItem(cartItem)
                            } else {
                                --cartItem.quantity
                                notifyItemChanged(adapterPosition)
                                listener.onQuantityChange(cartItem)
                            }
                        }
                    }
                }

                btnIncrease.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        getItem(adapterPosition)?.let { cartItem ->
                            if (cartItem.quantity < cartItem.minOrderQuantity) {
                                ++cartItem.quantity
                                notifyItemChanged(adapterPosition)
                                listener.onQuantityChange(cartItem)
                            }
                        }
                    }
                }
            }
        }
    }

}