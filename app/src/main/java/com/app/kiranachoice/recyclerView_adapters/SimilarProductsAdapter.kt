package com.app.kiranachoice.recyclerView_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.kiranachoice.databinding.ItemHorizontolProductItemBinding
import com.app.kiranachoice.db.CartItem
import com.app.kiranachoice.listeners.ProductClickListener
import com.app.kiranachoice.models.ProductModel
import com.google.android.material.snackbar.Snackbar

class SimilarProductsAdapter(
    private val cartItem: List<CartItem>,
    private val productKey : String,
    private val listener: ProductClickListener
) : RecyclerView.Adapter<SimilarProductsAdapter.SimilarProductsViewHolder>() {

    var addToCartClickedItemPosition = -1

    var list = listOf<ProductModel>()
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
        val productModel = list[position]
        holder.bind(productModel)

        if (!cartItem.isNullOrEmpty()){
            for (cartItem in cartItem) {
                if (cartItem.productKey == productModel.product_key){
                    holder.binding.btnAddToCart.visibility = View.GONE
                    holder.binding.quantityLayout.visibility = View.VISIBLE
                    holder.binding.userQuantity.text = cartItem.quantity
                    break
                }
            }
        }

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
                holder.binding.btnAddToCart.visibility = View.VISIBLE
                holder.binding.quantityLayout.visibility = View.GONE
            } else {
                holder.binding.userQuantity.text = quantity.toString()
            }
        }


        with(holder.binding) {
            btnAddToCart.setOnClickListener {
                if (position != RecyclerView.NO_POSITION) {
                    productModel.let { model ->
                        listener.addItemToCart(
                            model,
                            spinnerPackaging.selectedItemPosition,
                            userQuantity.text.toString(),
                            position
                        )
                    }
                }
            }
        }


        if (addToCartClickedItemPosition == position) {
            holder.binding.btnAddToCart.visibility = View.GONE
            holder.binding.quantityLayout.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = list.size

    class SimilarProductsViewHolder(
        val binding: ItemHorizontolProductItemBinding,
        val listener: ProductClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(productModel: ProductModel) {
            binding.productModel = productModel
            binding.executePendingBindings()
        }
    }

}