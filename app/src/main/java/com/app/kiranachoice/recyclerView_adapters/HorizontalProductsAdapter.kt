package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.ItemHorizontolProductItemBinding
import com.app.kiranachoice.listeners.ProductClickListener


class HorizontalProductsAdapter(
    private val listener: ProductClickListener,
) :
    RecyclerView.Adapter<HorizontalProductsAdapter.HorizontalProductsViewHolder>() {

    var productsList = listOf<Product>()
        set(value) {
            field = value
            notifyDataSetChanged()
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
        val product = productsList[position]
        holder.bind(product)

    }

    override fun getItemCount(): Int = productsList.size

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
                        val product = productsList[adapterPosition]

                        product.addedInCart = !product.addedInCart
                        notifyItemChanged(adapterPosition)
                        product.packagingIndex = spinnerPackaging.selectedItemPosition
                        listener?.addItemToCart(product)
                    }
                }


                btnIncrease.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val product = productsList[adapterPosition]
                        if (product.userOrderQuantity < product.minOrderQty) {
                            ++product.userOrderQuantity
                            notifyItemChanged(adapterPosition)
                            listener?.onQuantityChanged(product)
                        }
                    }
                }


                btnDecrease.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val product = productsList[adapterPosition]
                        if (product.userOrderQuantity == 1) {
                            product.addedInCart = !product.addedInCart
                            notifyItemChanged(adapterPosition)
                            listener?.onRemoveProduct(product)
                        } else {
                            --product.userOrderQuantity
                            // enable button increase
                            notifyItemChanged(adapterPosition)
                            listener?.onQuantityChanged(product)
                        }
                    }
                }
            }
        }
    }
}