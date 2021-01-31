package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.BR
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.ItemHorizontolProductItemBinding
import com.app.kiranachoice.listeners.ProductClickListener


class HorizontalProductsAdapter(
    private val cartItems: List<CartItem>?,
    private val listener: ProductClickListener,
) :
    ListAdapter<Product, HorizontalProductsAdapter.HorizontalProductsViewHolder>(DiffUtilCallback()) {

    class DiffUtilCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }
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
        return HorizontalProductsViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: HorizontalProductsViewHolder, position: Int) {
        val product = getItem(position)

        if (cartItems != null && cartItems.isNotEmpty()) {
            // here find lambda return cartItem or return null
            cartItems.singleOrNull { cartItem ->
                cartItem.productName.equals(product.name, true)
            }?.let { cartItem ->
                product.added = true
                product.userQuantity = cartItem.quantity.toInt()
            }
        }

        holder.bind(product)

    }


    inner class HorizontalProductsViewHolder(
        val binding: ItemHorizontolProductItemBinding,
        val listener: ProductClickListener?
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.setVariable(BR.product, product)
            binding.executePendingBindings()
        }

        init {
            with(binding) {
                btnAddToCart.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        getItem(adapterPosition)?.let { product ->
                            product.added = !product.added
                            notifyItemChanged(adapterPosition)
                            listener?.addItemToCart(
                                product,
                                spinnerPackaging.selectedItemPosition
                            )
                        }
                    }
                }


                btnIncrease.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        getItem(adapterPosition)?.let { product ->
                            if (product.userQuantity < product.minOrderQty) {
                                ++product.userQuantity
                                if (product.userQuantity.toLong() == product.minOrderQty)
                                    product.isEnable = !product.isEnable
                                notifyItemChanged(adapterPosition)
                                listener?.onQuantityChanged(product)
                            }
                        }
                    }
                }


                btnDecrease.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        getItem(adapterPosition)?.let { product ->
                            if (product.userQuantity == 1) {
                                product.added = !product.added
                                notifyItemChanged(adapterPosition)
                                listener?.onRemoveProduct(product.key)
                            } else {
                                --product.userQuantity
                                // enable button increase
                                product.isEnable = true
                                notifyItemChanged(adapterPosition)
                                listener?.onQuantityChanged(product)
                            }
                        }
                    }
                }
            }
        }
    }
}