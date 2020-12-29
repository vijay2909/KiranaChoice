package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.R
import com.app.kiranachoice.databinding.ItemCartProductBinding
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.listeners.CartListener
import com.google.android.material.snackbar.Snackbar

class CartItemAdapter(private val listener: CartListener) : ListAdapter<CartItem, CartItemAdapter.CartItemViewHolder>(DiffUtilsCallBack()) {

    class DiffUtilsCallBack : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.productKey == newItem.productKey
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        return CartItemViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val cartItem = getItem(position)
        holder.bind(cartItem, listener)

        if (cartItem.quantity == "1"){
            holder.binding.btnDecrease.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.context.resources, R.drawable.ic_delete, null))
        } else{
            holder.binding.btnDecrease.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.context.resources, R.drawable.ic_minus_box, null))
        }

        holder.binding.btnDecrease.setOnClickListener {
            var quantity = Integer.parseInt(holder.binding.userQuantity.text.toString())
            --quantity
            if (quantity <= 0) {
                listener.removeCartItem(cartItem)
            } else {
                listener.onQuantityChange(
                    cartItem = cartItem,
                    quantity = quantity
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
                    cartItem = cartItem,
                    quantity = quantity
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

}