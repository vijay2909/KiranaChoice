package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemCartProductBinding
import com.app.kiranachoice.db.CartItem

class CartItemAdapter(private val listener : CartListener) :
    ListAdapter<CartItem, CartItemAdapter.CartItemViewHolder>(DiffUtilsCallBack()) {

    class DiffUtilsCallBack : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        return CartItemViewHolder.from(parent)
    }



    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    class CartItemViewHolder(private val binding: ItemCartProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem, listener: CartListener) {
            binding.listener = listener
            binding.cartItem = cartItem
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): CartItemViewHolder {
                val view =
                    ItemCartProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return CartItemViewHolder(view)
            }
        }
    }

    interface CartListener {
        fun removeCartItem(cartItem: CartItem)
    }
}