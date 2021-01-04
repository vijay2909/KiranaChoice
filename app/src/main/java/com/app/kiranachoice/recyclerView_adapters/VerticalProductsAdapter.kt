package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.data.db.CartItem
import com.app.kiranachoice.data.domain.Product
import com.app.kiranachoice.databinding.ItemVerticalProductListBinding
import com.google.android.material.snackbar.Snackbar


class VerticalProductsAdapter(
    private val list : List<Product>,
    private val cartItem: List<CartItem>,
    private val listener: ProductListener
) :
    RecyclerView.Adapter<VerticalProductsAdapter.VerticalProductViewHolder>() {

    var addToCartClickedItemPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalProductViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemVerticalProductListBinding.inflate(layoutInflater, parent, false)
        return VerticalProductViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VerticalProductViewHolder, position: Int) {
        holder.bind(list[position])

        if (!cartItem.isNullOrEmpty() ){
            for (cartItem in cartItem) {
                if (cartItem.productId == list[position].id){
                    holder.binding.btnAddToCart.visibility = View.GONE
                    holder.binding.quantityLayout.visibility = View.VISIBLE
                    holder.binding.userQuantity.text = cartItem.quantity
                    break
                }
            }
        }

        holder.binding.btnIncrease.setOnClickListener {
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

        private var model: Product? = null

        fun bind(product: Product) {
            this.model = product
            binding.clickListener = listener
            binding.product = product
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
            product: Product,
            packagingSize: Int,
            quantity: String,
            position: Int
        )

        fun onItemRemoved(product: Product)
        fun onItemClick(product: Product)
    }
}