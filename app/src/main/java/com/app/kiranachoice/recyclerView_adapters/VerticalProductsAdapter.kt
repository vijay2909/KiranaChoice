package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemVerticalProductListBinding
import com.app.kiranachoice.models.ProductModel
import com.google.android.material.snackbar.Snackbar


class VerticalProductsAdapter(private val listener: ProductListener) :
    RecyclerView.Adapter<VerticalProductsAdapter.VerticalProductViewHolder>() {

    var addToCartClickedItemPosition = -1

    var list = listOf<ProductModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalProductViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemVerticalProductListBinding.inflate(layoutInflater, parent, false)
        return VerticalProductViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VerticalProductViewHolder, position: Int) {
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
                listener.onItemRemoved(list[position])
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

    inner class VerticalProductViewHolder(
        val binding: ItemVerticalProductListBinding,
        listener: ProductListener
    ) :
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
                            listener.onAddToCartButtonClick(
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

    interface ProductListener {
        fun onAddToCartButtonClick(
            productModel: ProductModel,
            packagingSize: Int,
            quantity: String,
            position: Int
        )
        fun onItemRemoved(productModel: ProductModel)
    }
}