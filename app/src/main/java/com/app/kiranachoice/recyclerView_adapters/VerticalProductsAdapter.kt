package com.app.kiranachoice.recyclerView_adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.ItemVerticalProductListBinding
import com.app.kiranachoice.listeners.ProductClickListener


class VerticalProductsAdapter(
    private val listener: ProductClickListener
) :
    ListAdapter<Product, VerticalProductsAdapter.VerticalProductViewHolder>(DiffUtilCallback()) {

    class DiffUtilCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    private lateinit var cartItems: List<CartItem>

    fun submitCartItem(cartItems: List<CartItem>) {
        this.cartItems = cartItems
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalProductViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemVerticalProductListBinding.inflate(layoutInflater, parent, false)
        binding.clickListener = listener
        return VerticalProductViewHolder(binding)
    }


    override fun onBindViewHolder(holder: VerticalProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)

        if (this::cartItems.isInitialized) {
            for (cartItem in cartItems) {
                if (cartItem.productKey == product.key) {
                    holder.binding.btnAddToCart.visibility = View.GONE
                    holder.binding.quantityLayout.visibility = View.VISIBLE
                    holder.binding.userQuantity.text = cartItem.quantity
                    break
                }
            }
        }

        /*holder.binding.btnIncrease.setOnClickListener {
            var quantity = Integer.parseInt(holder.binding.userQuantity.text.toString())
            if (quantity < list[holder.adapterPosition].minOrderQty) ++quantity else Snackbar.make(
                holder.binding.root,
                "You can get maximum 5 quantity.",
                Snackbar.LENGTH_SHORT
            ).show()
            holder.binding.userQuantity.text = quantity.toString()
        }

        holder.binding.btnDecrease.setOnClickListener {
            var quantity = Integer.parseInt(holder.binding.userQuantity.text.toString())
            --quantity
            if (quantity == 0) {
                listener.onItemRemoved(list[position])
                holder.binding.btnAddToCart.visibility = View.VISIBLE
                holder.binding.quantityLayout.visibility = View.GONE
            } else {
                holder.binding.userQuantity.text = quantity.toString()
            }
        }*/

    }

    inner class VerticalProductViewHolder(
        val binding: ItemVerticalProductListBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.product = product
            binding.executePendingBindings()
        }

        init {
            with(binding) {
                btnAddToCart.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        getItem(adapterPosition)?.let { product ->
                            product.added = !product.added
                            notifyItemChanged(adapterPosition)
                            Log.d("VerticalProductsAdapter", "btnAddToCart called ")
                            listener.addItemToCart(
                                product,
                                spinnerPackaging.selectedItemPosition,
                            )
                        }
                    }
                }


                btnIncrease.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        getItem(adapterPosition)?.let { product ->
                            if (product.userQuantity < product.minOrderQty) {
                                product.userQuantity += 1
                                notifyItemChanged(adapterPosition)
                            }
                            listener.onQuantityChanged(product/*.key, quantity = qty*/)
                        }
                    }
                }


                btnDecrease.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        getItem(adapterPosition)?.let { product ->
                            if (product.userQuantity == 1) {
                                product.added = !product.added
                                notifyItemChanged(adapterPosition)
                                listener.onRemoveProduct(product.key)
                            } else {
                                --product.userQuantity
                                notifyItemChanged(adapterPosition)
                                listener.onQuantityChanged(product)
                            }
                        }
                    }
                }
            }
        }

    }
}