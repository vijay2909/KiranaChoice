package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemCartProductBinding
import com.app.kiranachoice.db.CartItem
import com.google.android.material.snackbar.Snackbar

class CartItemAdapter(private val listener: CartListener) :
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

        holder.binding.btnDecrease.setOnClickListener {
            var quantity = Integer.parseInt(holder.binding.userQuantity.text.toString())
            --quantity
            if (quantity <= 0) {
                holder.binding.btnDecrease.isEnabled = false
            } else {
                listener.onQuantityChange(
                    getItem(position),
                    null,
                    getItem(position).productPrice.toInt(),
                    getItem(position).productMRP.toInt()
                        .minus(getItem(position).productPrice.toInt())
                )
                holder.binding.userQuantity.text = quantity.toString()
            }
        }


        holder.binding.btnIncrease.setOnClickListener {
            var quantity = holder.binding.userQuantity.text.toString().toInt()
            if (!holder.binding.btnDecrease.isEnabled) holder.binding.btnDecrease.isEnabled = true
            if (quantity < 5) {
                ++quantity
                listener.onQuantityChange(
                    getItem(position),
                    getItem(position).productPrice.toInt(),
                    null,
                    getItem(position).productMRP.toInt()
                        .minus(getItem(position).productPrice.toInt())
                )
                holder.binding.userQuantity.text = quantity.toString()
            } else {
                Snackbar.make(
                    holder.binding.root,
                    "You can get maximum 5 quantity.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

    }

    class CartItemViewHolder(val binding: ItemCartProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem, listener: CartListener) {
            binding.listener = listener
            binding.cartItem = cartItem
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): CartItemViewHolder {
                val view =
                    ItemCartProductBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return CartItemViewHolder(view)
            }
        }
    }


    interface CartListener {
        fun removeCartItem(cartItem: CartItem)
        fun onQuantityChange(
            cartItem: CartItem,
            amountPlus: Int? = null,
            amountMinus: Int? = null,
            mrpAndPriceDifference: Int
        )
    }
}