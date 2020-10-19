package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemHorizontolProductItemBinding
import com.app.kiranachoice.listeners.ProductClickListener
import com.app.kiranachoice.models.ProductModel
import com.google.android.material.snackbar.Snackbar


class HorizontalProductsAdapter(private val listener: ProductClickListener?) :
    RecyclerView.Adapter<HorizontalProductsAdapter.HorizontalProductsViewHolder>() {

    var addToCartClickedItemPosition = -1

    var list = listOf<ProductModel>()
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
        holder.bind(list[position])

        holder.binding.btnIncrease.setOnClickListener {
            var quantity = Integer.parseInt(holder.binding.userQuantity.text.toString())
            if (quantity < 5) ++quantity else Snackbar.make(
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
//                listener.onItemRemoved(list[position])
                holder.binding.btnAddToCart.visibility = View.VISIBLE
                holder.binding.quantityLayout.visibility = View.GONE
            } else {
                holder.binding.userQuantity.text = quantity.toString()
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
        private var model: ProductModel? = null
        fun bind(productModel: ProductModel) {
            this.model = productModel
            binding.productModel = productModel
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