package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.ItemHorizontolProductItemBinding
import com.app.kiranachoice.listeners.ProductClickListener

class SimilarProductsAdapter(
    private val productKey: String,
    private val listener: ProductClickListener
) : RecyclerView.Adapter<SimilarProductsAdapter.SimilarProductsViewHolder>() {

    var productsList = listOf<Product>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimilarProductsViewHolder {
        val view = ItemHorizontolProductItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        view.clickListener = listener
        return SimilarProductsViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: SimilarProductsViewHolder, position: Int) {
        val product = productsList[position]
        holder.bind(product)
        /*if (product.key != productKey) {
        }*/
    }

    override fun getItemCount(): Int = productsList.size

    inner class SimilarProductsViewHolder(
        val binding: ItemHorizontolProductItemBinding,
        val listener: ProductClickListener
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
                        listener.addItemToCart(product)
                    }
                }


                btnIncrease.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val product = productsList[adapterPosition]
                        if (product.userOrderQuantity < product.minOrderQty) {
                            ++product.userOrderQuantity
                            if (product.userOrderQuantity == product.minOrderQty)
                                notifyItemChanged(adapterPosition)
                            listener.onQuantityChanged(product)
                        }
                    }
                }


                btnDecrease.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val product = productsList[adapterPosition]
                        if (product.userOrderQuantity == 1) {
                            product.addedInCart = !product.addedInCart
                            notifyItemChanged(adapterPosition)
                            listener.onRemoveProduct(product)
                        } else {
                            --product.userOrderQuantity
                            // enable button increase
                            notifyItemChanged(adapterPosition)
                            listener.onQuantityChanged(product)
                        }
                    }
                }
            }
        }
    }

}