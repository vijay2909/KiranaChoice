package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.ItemHorizontolProductItemBinding
import com.app.kiranachoice.listeners.ProductClickListener


class HorizontalProductsAdapter(
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

    private lateinit var cartItems : List<CartItem>

    fun submitCartItem(cartItems : List<CartItem>){
        this.cartItems = cartItems
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
        holder.bind(product)

        if (this::cartItems.isInitialized) {
            for (cartItem in cartItems) {
                if (cartItem.productName == product.name) {
                    holder.binding.btnAddToCart.visibility = View.GONE
                    holder.binding.quantityLayout.visibility = View.VISIBLE
                    holder.binding.userQuantity.text = cartItem.quantity
                    break
                }
            }
        }
    }


    inner class HorizontalProductsViewHolder(
        val binding: ItemHorizontolProductItemBinding,
        val listener: ProductClickListener?
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
                            listener?.addItemToCart(
                                product,
                                spinnerPackaging.selectedItemPosition,
                                userQuantity.text.toString(),
                                adapterPosition
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
                            listener?.onQuantityChanged(product/*.key, quantity = qty*/)
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