package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.ItemHorizontolProductItemBinding
import com.app.kiranachoice.listeners.ProductClickListener
import com.google.android.material.snackbar.Snackbar


class HorizontalProductsAdapter(
    private val list : List<Product>,
    private val cartItem : List<CartItem>,
    private val listener: ProductClickListener,
) :
    RecyclerView.Adapter<HorizontalProductsAdapter.HorizontalProductsViewHolder>() {

    var addToCartClickedItemPosition = -1


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
        val product = list[position]
        holder.bind(product)

        if (!cartItem.isNullOrEmpty() ){
            for (cartItem in cartItem) {
                if (cartItem.productName == product.name){
                    holder.binding.btnAddToCart.visibility = View.GONE
                    holder.binding.quantityLayout.visibility = View.VISIBLE
                    holder.binding.userQuantity.text = cartItem.quantity
                    break
                }
            }
        }


        holder.binding.btnIncrease.setOnClickListener {
            var quantity = Integer.parseInt(holder.binding.userQuantity.text.toString())
            if (quantity < list[holder.adapterPosition].minOrderQty) {
                ++quantity
                val qty = quantity.toString()
                holder.binding.userQuantity.text = qty
                listener.onQuantityChanged(product.key, quantity = qty)
            } else Snackbar.make(
                holder.binding.root,
                "You can get maximum 5 quantity.",
                Snackbar.LENGTH_SHORT
            ).show()
        }


        holder.binding.btnDecrease.setOnClickListener {
            var quantity = Integer.parseInt(holder.binding.userQuantity.text.toString())
            --quantity
            if (quantity == 0) {
                holder.binding.btnAddToCart.visibility = View.VISIBLE
                holder.binding.quantityLayout.visibility = View.GONE
                listener.onRemoveProduct(product.key)
            } else {
                val qty = quantity.toString()
                holder.binding.userQuantity.text = qty
                listener.onQuantityChanged(product.key, qty)
            }
        }

        if (addToCartClickedItemPosition == position) {
            holder.binding.btnAddToCart.visibility = View.GONE
            holder.binding.quantityLayout.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = list.size


    inner class HorizontalProductsViewHolder(val binding: ItemHorizontolProductItemBinding, val listener: ProductClickListener?) :
        RecyclerView.ViewHolder(binding.root) {
        private var model: Product? = null
        fun bind(product: Product) {
            this.model = product
            binding.product = product
            binding.executePendingBindings()
        }

        init {
            with(binding) {
                btnAddToCart.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        model?.let { model ->
                            listener?.addItemToCart(
                                model,
                                spinnerPackaging.selectedItemPosition,
                                userQuantity.text.toString(),
                                adapterPosition
                            )
                        }
                    }
                }
            }
        }
    }
}